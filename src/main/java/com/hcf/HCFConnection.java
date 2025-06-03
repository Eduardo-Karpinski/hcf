package com.hcf;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import com.hcf.annotations.HCFRelationship;
import com.hcf.enums.HCFOperator;
import com.hcf.enums.HCFParameter;
import com.hcf.utils.HCFUtil;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

public final class HCFConnection<T> {

    private final Class<T> persistentClass;
    private final Session session;
    private Transaction transaction;
    private final List<Predicate> predicates = new ArrayList<>();

    public HCFConnection(Class<T> persistentClass) {
        this.persistentClass = Objects.requireNonNull(persistentClass, "PersistentClass is null");
        session = HCFFactory.INSTANCE.getFactory().openSession();
    }

    public HCFConnection(Class<T> persistentClass, Connection connection) {
        this.persistentClass = Objects.requireNonNull(persistentClass, "PersistentClass is null");
        session = HCFFactory.INSTANCE.getFactory().withOptions().connection(Objects.requireNonNull(connection, "Connection is null")).openSession();
    }

    public HCFConnection(Class<T> persistentClass, SessionFactory sessionFactory) {
        this.persistentClass = Objects.requireNonNull(persistentClass, "PersistentClass is null");
        session = Objects.requireNonNull(sessionFactory, "SessionFactory is null").openSession();
    }

    public HCFConnection(Class<T> persistentClass, Session session) {
        this.persistentClass = Objects.requireNonNull(persistentClass, "PersistentClass is null");
        this.session = Objects.requireNonNull(session, "Session is null");
    }

    public void save(T entity) {
        save(Collections.singletonList(entity));
    }

    public void save(List<T> entities) {
        persist(entities, true);
    }

    public void delete(T entity) {
        delete(Collections.singletonList(entity));
    }

    public void delete(List<T> entities) {
        persist(entities, false);
    }

    public int massiveDelete(Object... parameters) {
        return massiveDelete(HCFUtil.varargsToSearch(parameters));
    }

    public int massiveDelete(List<HCFSearch> hcfSearches) {
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<T> criteria = builder.createCriteriaDelete(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            applyPredicate(builder, root, hcfSearches);
            criteria.where(predicates.toArray(Predicate[]::new));
            return session.createMutationQuery(criteria).executeUpdate();
        } catch (Exception e) {
            HCFUtil.showError(e);
            if (transaction != null) transaction.rollback();
            return -1;
        } finally {
            close();
        }
    }

    public int massiveUpdate(Map<String, Object> values, Object... parameters) {
        return massiveUpdate(values, HCFUtil.varargsToSearch(parameters));
    }

    public int massiveUpdate(Map<String, Object> values, List<HCFSearch> hcfSearches) {
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaUpdate<T> criteria = builder.createCriteriaUpdate(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            applyPredicate(builder, root, hcfSearches);
            values.forEach(criteria::set);
            criteria.where(predicates.toArray(Predicate[]::new));
            return session.createMutationQuery(criteria).executeUpdate();
        } catch (Exception e) {
            HCFUtil.showError(e);
            if (transaction != null) transaction.rollback();
            return -1;
        } finally {
            close();
        }
    }

    public List<T> all(List<HCFOrder> orders) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            
            List<T> resultList;
            if (orders != null) {
            	order(orders, builder, criteria, root);
            	TypedQuery<T> query = limitResults(orders, criteria);
            	resultList = query.getResultList();
			} else {
				resultList = session.createQuery(criteria).getResultList();
			}
            
            resultList.forEach(this::getRelationshipByHCF);
            return resultList;
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }
    
    public List<T> all() {
    	return all(null);
    }

    public List<T> getRelations(Class<?> father, String column, Object id) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<?> root = criteria.from(father);
            Join<?, T> join = root.join(column);
            criteria.select(join).where(builder.equal(root.get(HCFUtil.getIdFieldName(session, father)), id));
            TypedQuery<T> query = session.createQuery(criteria);
            List<T> resultList = query.getResultList();
            resultList.forEach(this::getRelationshipByHCF);
            return resultList;
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }

    public List<T> getByInvertedRelation(Class<?> child, String column, Object id) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            criteria.where(builder.equal(root.join(column).get(HCFUtil.getIdFieldName(session, child)), id));
            TypedQuery<T> query = session.createQuery(criteria);
            List<T> resultList = query.getResultList();
            resultList.forEach(this::getRelationshipByHCF);
            return resultList;
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }

    public int deleteById(Object id) {
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<T> criteria = builder.createCriteriaDelete(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            criteria.where(builder.equal(root.get(HCFUtil.getIdFieldName(session, persistentClass)), Objects.requireNonNull(id, "Id is null")));
            return session.createMutationQuery(criteria).executeUpdate();
        } catch (Exception e) {
            HCFUtil.showError(e);
            if (transaction != null) transaction.rollback();
            return -1;
        } finally {
            close();
        }
    }

    public T getById(Object id) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            criteria.select(root).where(builder.equal(root.get(HCFUtil.getIdFieldName(session, persistentClass)), Objects.requireNonNull(id, "Id is null")));
            TypedQuery<T> query = session.createQuery(criteria);
            T singleResult = query.getSingleResult();
            getRelationshipByHCF(singleResult);
            return singleResult;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }

    public List<Object> sum(List<String> fields, Object... parameters) {
        return sum(fields, HCFUtil.varargsToSearch(parameters));
    }

    public List<Object> sum(List<String> fields, List<HCFSearch> parameters) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Tuple> criteria = builder.createTupleQuery();
            Root<T> root = criteria.from(persistentClass);

            List<Selection<?>> selections = new ArrayList<>();
            fields.forEach(column -> {
                Expression<? extends Number> expression = builder.sum(root.get(column));
                selections.add(expression.alias(column));
            });

            applyPredicate(builder, root, parameters);

            criteria.select(builder.tuple(selections.toArray(new Selection<?>[0])))
                    .where(predicates.toArray(Predicate[]::new));

            TypedQuery<Tuple> query = session.createQuery(criteria);
            Tuple tuple = query.getSingleResult();

            return selections.stream()
                    .map(selection -> tuple.get(selection.getAlias(), Number.class))
                    .collect(Collectors.toList());

        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }

    public T getFirstOrLast(HCFOrder order) {
        try {
        	 CriteriaBuilder builder = session.getCriteriaBuilder();
             CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
             Root<T> root = criteria.from(persistentClass);
             criteria.select(root);
             Order hibernateOrder = order.getAsc() ? builder.asc(root.get(order.getField())) : builder.desc(root.get(order.getField()));
             criteria.orderBy(hibernateOrder);
             T singleResult = session.createQuery(criteria).setMaxResults(1).getSingleResult();
             getRelationshipByHCF(singleResult);
             return singleResult;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }

    public static void sendSQL(String sql) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HCFFactory.INSTANCE.getFactory().openSession();
            transaction = session.beginTransaction();
            session.createNativeQuery(sql, Object.class).executeUpdate();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            HCFUtil.showError(e);
        } finally {
            if (transaction != null && transaction.getStatus().equals(TransactionStatus.ACTIVE)) {
                try {
                    transaction.commit();
                } catch (Exception e) {
                    HCFUtil.showError(e);
                }
            }
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (Exception e) {
                    HCFUtil.showError(e);
                }
            }
        }
    }

    public List<T> getObjectBySQL(String sql) {
        try {
            NativeQuery<T> nativeQuery = session.createNativeQuery(sql, persistentClass);
            return nativeQuery.getResultList();
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }

    public static List<?> getElementsBySQL(String sql) {
        Session session = null;
        try {
            session = HCFFactory.INSTANCE.getFactory().openSession();
            return session.createNativeQuery(sql, Object.class).getResultList();
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public Long count() {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
            Root<T> root = criteria.from(persistentClass);
            criteria.select(builder.count(root));
            return session.createQuery(criteria).getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            close();
        }
    }

    public List<Object> getDistinctField(String field, Object... parameters) {
        return getDistinctField(field, HCFUtil.varargsToSearch(parameters));
    }

    public List<Object> getDistinctField(String field, List<HCFSearch> parameters) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Object> criteria = builder.createQuery(Object.class);
            Root<T> root = criteria.from(persistentClass);
            applyPredicate(builder, root, parameters);
            criteria.select(root.get(field)).distinct(true).where(predicates.toArray(Predicate[]::new)).orderBy(builder.asc(root.get(field)));
            TypedQuery<Object> query = session.createQuery(criteria);
            return query.getResultList();
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }

    public T searchWithOneResult(List<HCFOrder> orders, Object... parameters) {
        return searchWithOneResult(orders, HCFUtil.varargsToSearch(parameters));
    }

    public T searchWithOneResult(List<HCFOrder> orders, List<HCFSearch> parameters) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            order(orders, builder, criteria, root);
            applyPredicate(builder, root, parameters);
            criteria.select(root).where(predicates.toArray(Predicate[]::new));
            TypedQuery<T> query = limitResults(orders, criteria);
            T singleResult = query.getSingleResult();
            getRelationshipByHCF(singleResult);
            return singleResult;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }

    public List<T> search(List<HCFOrder> orders, Object... parameters) {
        return search(orders, HCFUtil.varargsToSearch(parameters));
    }

    public List<T> search(List<HCFOrder> orders, List<HCFSearch> parameters) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            order(orders, builder, criteria, root);
            applyPredicate(builder, root, parameters);
            criteria.select(root).where(predicates.toArray(Predicate[]::new));
            TypedQuery<T> query = limitResults(orders, criteria);
            List<T> resultList = query.getResultList();
            resultList.forEach(this::getRelationshipByHCF);
            return resultList;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }

    public List<Object[]> searchWithJoin(List<HCFOrder> orders, List<HCFSearch> parameters, List<HCFJoinSearch> hcfJoinSearchs) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> criteria = builder.createQuery(Object[].class);
        Root<T> root = criteria.from(persistentClass);
        order(orders, builder, criteria, root);
        applyPredicate(builder, root, parameters);

        List<Selection<?>> selections = new ArrayList<>();
        selections.add(root);

        hcfJoinSearchs.forEach(hcfJoinSearch -> {
            Root<?> joinRoot = criteria.from(hcfJoinSearch.getJoinClass());
            predicates.add(builder.equal(
                root.get(hcfJoinSearch.getPrimaryField()),
                joinRoot.get(hcfJoinSearch.getForeignField())
            ));
            selections.add(joinRoot);
        });

        criteria.select(builder.array(selections.toArray(new Selection<?>[0])))
                .where(predicates.toArray(Predicate[]::new));

        return session.createQuery(criteria).getResultList();
    }

    private void persist(List<T> entities, boolean isSaveOrUpdate) {
        try {
            transaction = session.beginTransaction();
            PersistenceUnitUtil persistenceUnitUtil = session.getEntityManagerFactory().getPersistenceUnitUtil();
            entities.forEach(entity -> {
            	if (isSaveOrUpdate) {
                	Object id = persistenceUnitUtil.getIdentifier(entity);
                	if (id == null) {
                        session.persist(entity);
                    } else {
                        session.merge(entity);
                    }
                } else {
                    session.remove(entity);
                }
            });
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            close();
        }
    }

    @SuppressWarnings("unchecked")
    private void getRelationshipByHCF(T parentObject) {
        try {
            Class<?> parentClass = parentObject.getClass();
            
            if (!parentClass.isAnnotationPresent(HCFRelationship.class)) return;
            
            Object id = session.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(parentObject);
            String idFieldName = HCFUtil.getIdFieldName(session, parentClass);
            
            CriteriaBuilder builder = session.getCriteriaBuilder();
            
            for (PropertyDescriptor pd : Introspector.getBeanInfo(parentClass).getPropertyDescriptors()) {
            	Field field;
            	try {
            		field = parentClass.getDeclaredField(pd.getName());
				} catch (Exception e) {
					continue;
				}
            	if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
            		Method writeMethod = pd.getWriteMethod();
                    CriteriaQuery<Object> criteria = builder.createQuery();
                    Root<?> root = criteria.from(parentClass);
                    Join<?, T> join = root.join(field.getName());
                    criteria.select(join).where(builder.equal(root.get(idFieldName), id));
                    List<Object> resultList = session.createQuery(criteria).getResultList();
                    resultList.forEach(item -> getRelationshipByHCF((T) item));
                    writeMethod.invoke(parentObject, resultList);
                }
            }
        } catch (Exception e) {
            HCFUtil.showError(e);
        }
    }

    private void applyPredicate(CriteriaBuilder builder, Root<T> root, List<HCFSearch> parameters) {
        parameters.forEach(search -> {
            Path<?> field = root.get(search.getField());
            Comparable<?> value = (Comparable<?>) search.getValue();
            addPredicate(builder, field, value, search.getParameter(), search.getOperator());
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addPredicate(CriteriaBuilder builder, Path field, Comparable value, HCFParameter parameter, HCFOperator operator) {
        Predicate predicate = switch (parameter) {
            case TRUE -> builder.isTrue(field);
            case FALSE -> builder.isFalse(field);
            case IS_NULL -> builder.isNull(field);
            case IS_NOT_NULL -> builder.isNotNull(field);
            case EMPTY -> builder.length(builder.trim(field)).in(0);
            case NOT_EMPTY -> builder.length(builder.trim(field)).in(0).not();
            case IS_ODD -> builder.equal(builder.mod(field, 2), 1);
            case IS_EVEN -> builder.equal(builder.mod(field, 2), 0);
            case LIKE -> builder.like(field, value.toString());
            case NOT_LIKE -> builder.notLike(field, value.toString());
            case EQUAL -> builder.equal(field, value);
            case NOT_EQUAL -> builder.notEqual(field, value);
            case LESS_THAN -> builder.lessThan(field, value);
            case GREATER_THAN -> builder.greaterThan(field, value);
            case LESS_THAN_OR_EQUAL_TO -> builder.lessThanOrEqualTo(field, value);
            case GREATER_THAN_OR_EQUAL_TO -> builder.greaterThanOrEqualTo(field, value);
        };

        if (predicate != null) {
            predicates.add(predicate);
            applyOperator(builder, operator);
        }
    }

    /**
     * <p>
     * This method applies logical operators (AND, OR) to the list of predicates.
     * It processes the predicates list from bottom to top due to the order in which the criteria are evaluated.
     * </p>
     * <p>
     * Important Note:
     * The first HCFOperator should always be HCFOperator.NONE to prevent an IndexOutOfBoundsException.
     * This is because the method attempts to combine the last two predicates in the list when applying AND/OR.
     * </p>
     * <p>
     * Example of correct usage:
     * List<Data> datas = new HCFConnection<>(Data.class).search(null,
     *     "name", "User 25", HCFParameter.EQUAL, HCFOperator.NONE,
     *     "salary", 8000, HCFParameter.EQUAL, HCFOperator.AND,
     *     "name", "User 26", HCFParameter.EQUAL, HCFOperator.OR);
     * </p>
     * <p>
     * Example of incorrect usage:
     * List<Data> datas = new HCFConnection<>(Data.class).search(null,
     *     "name", "User 25", HCFParameter.EQUAL, HCFOperator.AND,
     *     "salary", 8000, HCFParameter.EQUAL, HCFOperator.AND,
     *     "name", "User 26", HCFParameter.EQUAL, HCFOperator.OR);
     * </p>
     * <p>
     * In the incorrect example, the first parameter being HCFOperator.AND causes an IndexOutOfBoundsException
     * because there are not enough predicates to combine at the start of the evaluation.
     * </p>
     */
	private void applyOperator(CriteriaBuilder builder, HCFOperator hcfOperator) {
		if ((hcfOperator == HCFOperator.AND || hcfOperator == HCFOperator.OR) && predicates.size() >= 2) {
			Predicate right = predicates.removeLast();
			Predicate left = predicates.removeLast();
			Predicate combined = hcfOperator == HCFOperator.AND ? builder.and(left, right) : builder.or(left, right);
			predicates.add(combined);
		} else if (hcfOperator != HCFOperator.NONE) {
			HCFUtil.getLogger().warning("[HCF-WARNING] Cannot apply operator '" + hcfOperator + "'. Ensure the first operator is HCFOperator.NONE and that there are at least two predicates to combine.");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void order(List<HCFOrder> orders, CriteriaBuilder builder, CriteriaQuery criteria, Root<T> root) {
		Optional.ofNullable(orders)
				.map(ords -> ords.stream()
						.filter(order -> order != null && order.getField() != null && order.getAsc() != null)
						.map(order -> order.getAsc() ? builder.asc(root.get(order.getField())) : builder.desc(root.get(order.getField())))
						.collect(Collectors.toList()))
				.filter(list -> !list.isEmpty()).ifPresent(criteria::orderBy);
	}

	private TypedQuery<T> limitResults(List<HCFOrder> orders, CriteriaQuery<T> criteria) {
		TypedQuery<T> query = session.createQuery(criteria);
		
		Optional.ofNullable(orders).ifPresent(ords -> {
			ords.stream().map(HCFOrder::getOffset).filter(Objects::nonNull).findFirst().ifPresent(query::setFirstResult);
			ords.stream().map(HCFOrder::getLimit).filter(Objects::nonNull).findFirst().ifPresent(query::setMaxResults);
		});

		return query;
	}

    private void close() {
        if (transaction != null && transaction.getStatus().equals(TransactionStatus.ACTIVE)) {
            try {
                transaction.commit();
            } catch (Exception e) {
                HCFUtil.showError(e);
            }
        }
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                HCFUtil.showError(e);
            }
        }
    }

}