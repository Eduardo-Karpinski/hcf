package com.hcf;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import jakarta.persistence.criteria.Subquery;

public final class HCFConnection<T, E> {

    private final Class<T> persistentClass;
    private final Session session;
    private Transaction transaction;
    private final List<Predicate> predicates = new ArrayList<>();

    public HCFConnection(Class<T> persistentClass) {
        this.persistentClass = Optional.ofNullable(persistentClass).orElseThrow(() -> new NullPointerException("PersistentClass is null"));
        session = HCFFactory.getInstance().getFactory().openSession();
    }

    public HCFConnection(Class<T> persistentClass, Connection connection) {
        this.persistentClass = Optional.ofNullable(persistentClass).orElseThrow(() -> new NullPointerException("PersistentClass is null"));
        session = HCFFactory.getInstance().getFactory().withOptions().connection(Optional.ofNullable(connection).orElseThrow(() -> new NullPointerException("Connection is null"))).openSession();
    }

    public HCFConnection(Class<T> persistentClass, SessionFactory sessionFactory) {
        this.persistentClass = Optional.ofNullable(persistentClass).orElseThrow(() -> new NullPointerException("PersistentClass is null"));
        session = Optional.ofNullable(sessionFactory).orElseThrow(() -> new NullPointerException("SessionFactory is null")).openSession();
    }

    public HCFConnection(Class<T> persistentClass, Session session) {
        this.persistentClass = Optional.ofNullable(persistentClass).orElseThrow(() -> new NullPointerException("PersistentClass is null"));
        this.session = Optional.ofNullable(session).orElseThrow(() -> new NullPointerException("Session is null"));
    }

    public void save(T entity) {
        save(Collections.singletonList(entity), false);
    }

    public void save(List<T> entities, Boolean commitInError) {
        persist(entities, commitInError, true);
    }

    public void delete(T entity) {
        delete(Collections.singletonList(entity), false);
    }

    public void delete(List<T> entities, Boolean commitInError) {
        persist(entities, commitInError, false);
    }

    @SuppressWarnings("unchecked")
    public int massiveDelete(E... parameters) {
        if (parameters.length % 4 != 0) throw new IllegalArgumentException("Parameters is not a multiple of 4.");
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<T> criteria = builder.createCriteriaDelete(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            applyPredicate(builder, null, root, parameters); // criteria is not necessary for this action
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

    public int massiveDelete(List<HCFSearch> hcfSearches) {
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<T> criteria = builder.createCriteriaDelete(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            applyPredicate(builder, null, root, hcfSearches); // criteria is not necessary for this action
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

    @SuppressWarnings("unchecked")
    public int massiveUpdate(Map<String, Object> values, E... parameters) {
        if (parameters.length % 4 != 0) throw new IllegalArgumentException("Parameters is not a multiple of 4.");
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaUpdate<T> criteria = builder.createCriteriaUpdate(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            applyPredicate(builder, null, root, parameters); // criteria is not necessary for this action
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

    public int massiveUpdate(Map<String, Object> values, List<HCFSearch> hcfSearches) {
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaUpdate<T> criteria = builder.createCriteriaUpdate(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            applyPredicate(builder, null, root, hcfSearches); // criteria is not necessary for this action
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

    public List<T> all() {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            criteria.from(persistentClass);
            List<T> resultList = session.createQuery(criteria).getResultList();
            resultList.forEach(this::getRelationshipByHCF);
            return resultList;
        } catch (Exception e) {
            HCFUtil.showError(e);
            return null;
        } finally {
            close();
        }
    }

    public List<T> getRelations(Class<?> father, String column, Object id) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<?> root = criteria.from(father);
            Join<?, T> join = root.join(column);
            criteria.select(join).where(builder.equal(root.get(HCFUtil.getId(father)), id));
            TypedQuery<T> query = session.createQuery(criteria);
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

    public List<T> getByInvertedRelation(Class<?> child, String column, Object id) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            criteria.where(builder.equal(root.join(column).get(HCFUtil.getId(child)), id));
            TypedQuery<T> query = session.createQuery(criteria);
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

    public int deleteById(Object id) {
        Optional.ofNullable(id).orElseThrow(() -> new NullPointerException("Id is null"));
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<T> criteria = builder.createCriteriaDelete(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            criteria.where(builder.equal(root.get(HCFUtil.getId(persistentClass)), id));
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
        Optional.ofNullable(id).orElseThrow(() -> new NullPointerException("Id is null"));
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            criteria.select(root).where(builder.equal(root.get(HCFUtil.getId(persistentClass)), id));
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

    @SuppressWarnings("unchecked")
    public List<Object> sum(List<HCFOrder> orders, List<String> fields, E... parameters) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Tuple> criteria = builder.createTupleQuery();
            Root<T> root = criteria.from(persistentClass);

            List<Expression<? extends Number>> expressions = new ArrayList<>();

            for (String column : fields) {
                Expression<? extends Number> expression = builder.sum(root.get(column));
                expression.alias(column);
                expressions.add(expression);
            }

            order(orders, builder, criteria, root);
            applyPredicate(builder, criteria, root, parameters);

            criteria.multiselect(expressions.toArray(new Expression[0])).where(predicates.toArray(Predicate[]::new));

            // limitResults method doesn't accept TypedQuery<Tuple>, maybe in the near future I'll remove the type of TypedQuery
            TypedQuery<Tuple> query;
            Integer limit = null;
            int offset = 0;
            try {
                limit = orders.stream().map(HCFOrder::getLimit).filter(Objects::nonNull).findFirst().orElse(null);
                offset = orders.stream().map(HCFOrder::getOffset).filter(Objects::nonNull).findFirst().orElse(0);
                query = session.createQuery(criteria).setFirstResult(offset).setMaxResults(limit);
            } catch (Exception ignore) {
                query = session.createQuery(criteria);
            }

            Tuple tuple = query.getSingleResult();

            return expressions.stream()
                    .map(expression -> tuple.get(expression.getAlias(), Number.class))
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
            Subquery<Long> subquery = criteria.subquery(Long.class);
            Root<T> root = subquery.from(persistentClass);
            Path<Long> subPath = root.get(order.getField());
            root = criteria.from(persistentClass);
            Path<T> path = root.get(order.getField());
            subquery.select(order.getAsc() ? builder.min(subPath) : builder.max(subPath));
            criteria.select(root).where(builder.equal(path, subquery));
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

    public static void sendSQL(String sql) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HCFFactory.getInstance().getFactory().openSession();
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
            session = HCFFactory.getInstance().getFactory().openSession();
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
            TypedQuery<Long> query = session.createQuery(criteria);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            close();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Object> getDistinctField(String field, E... parameters) {
        if (parameters.length % 4 != 0) throw new IllegalArgumentException("Parameters is not a multiple of 4.");
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Object> criteria = builder.createQuery(Object.class);
            Root<T> root = criteria.from(persistentClass);
            applyPredicate(builder, criteria, root, parameters);
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

    public List<Object> getDistinctField(String field, List<HCFSearch> parameters) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Object> criteria = builder.createQuery(Object.class);
            Root<T> root = criteria.from(persistentClass);
            applyPredicate(builder, criteria, root, parameters);
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

    @SuppressWarnings("unchecked")
    public T searchWithOneResult(List<HCFOrder> orders, E... parameters) {
        if (parameters.length % 4 != 0) throw new IllegalArgumentException("Parameters is not a multiple of 4.");
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            order(orders, builder, criteria, root);
            applyPredicate(builder, criteria, root, parameters);
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

    public T searchWithOneResult(List<HCFOrder> orders, List<HCFSearch> parameters) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            order(orders, builder, criteria, root);
            applyPredicate(builder, criteria, root, parameters);
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

    @SuppressWarnings("unchecked")
    public List<T> search(List<HCFOrder> orders, E... parameters) {
        if (parameters.length % 4 != 0) throw new IllegalArgumentException("Parameters is not a multiple of 4.");
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            order(orders, builder, criteria, root);
            applyPredicate(builder, criteria, root, parameters);
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

    public List<T> search(List<HCFOrder> orders, List<HCFSearch> parameters) {
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteria = builder.createQuery(persistentClass);
            Root<T> root = criteria.from(persistentClass);
            order(orders, builder, criteria, root);
            applyPredicate(builder, criteria, root, parameters);
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

    private void persist(List<T> entities, Boolean commitInError, boolean isSaveOrUpdate) {
        try {
            transaction = session.beginTransaction();
            entities.forEach(e -> {
                if (isSaveOrUpdate) {
                    session.merge(e);
                } else {
                    session.remove(e);
                }
            });
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                if (commitInError) {
                    try {
                        transaction.commit();
                    } catch (Exception e2) {
                        throw e;
                    }
                } else {
                    transaction.rollback();
                }
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
            if (parentClass.getAnnotation(HCFRelationship.class) != null) {

                String idFieldName = HCFUtil.getId(parentClass);
                Field idField = parentClass.getDeclaredField(idFieldName);

                idField.setAccessible(true);
                Object id = idField.get(parentObject);
                idField.setAccessible(false);

                for (Field field : parentClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {

                        field.setAccessible(true);

                        CriteriaBuilder builder = session.getCriteriaBuilder();
                        CriteriaQuery<Object> criteria = builder.createQuery();
                        Root<?> root = criteria.from(parentClass);
                        Join<?, T> join = root.join(field.getName());
                        criteria.select(join).where(builder.equal(root.get(idFieldName), id));
                        TypedQuery<Object> query = session.createQuery(criteria);
                        List<Object> resultList = query.getResultList();

                        resultList.forEach(t -> getRelationshipByHCF((T) t));

                        field.set(parentObject, resultList);
                        field.setAccessible(false);
                    }
                }

            }
        } catch (Exception e) {
            HCFUtil.showError(e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void applyPredicate(CriteriaBuilder builder, CriteriaQuery<?> criteria, Root<T> root, List<HCFSearch> parameters) {
        parameters.forEach(i -> {
            Path field = root.get(i.getField());
            Comparable value = (Comparable) i.getValue();
            addPredicate(builder, criteria, field, value, i.getParameter(), i.getOperator());
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void applyPredicate(CriteriaBuilder builder, CriteriaQuery criteria, Root<T> root, E... parameters) {
        IntStream.iterate(0, i -> i + 4).limit(parameters.length / 4).forEach(i -> {
            Path field = root.get(parameters[i].toString());
            Comparable value = (Comparable) parameters[i + 1];
            HCFParameter parameter = (HCFParameter) parameters[i + 2];
            HCFOperator operator = (HCFOperator) parameters[i + 3];
            addPredicate(builder, criteria, field, value, parameter, operator);
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addPredicate(CriteriaBuilder builder, CriteriaQuery<?> criteria, Path field, Comparable value, HCFParameter parameter, HCFOperator operator) {
        switch (parameter) {
            case TRUE:
                predicates.add(builder.isTrue(field));
                applyOperator(builder, operator);
                break;
            case LIKE:
                predicates.add(builder.like(field, value.toString()));
                applyOperator(builder, operator);
                break;
            case FALSE:
                predicates.add(builder.isFalse(field));
                applyOperator(builder, operator);
                break;
            case EQUAL:
                predicates.add(builder.equal(field, value));
                applyOperator(builder, operator);
                break;
            case EMPTY:
                predicates.add(builder.length(builder.trim(field)).in(0));
                applyOperator(builder, operator);
                break;
            case ISNULL:
                predicates.add(builder.isNull(field));
                applyOperator(builder, operator);
                break;
            case NOTLIKE:
                predicates.add(builder.notLike(field, value.toString()));
                applyOperator(builder, operator);
                break;
            case GROUPBY:
                List<Expression<?>> groupList = new ArrayList<>(criteria.getGroupList());
                groupList.add(field);
                criteria.groupBy(groupList);
                break;
            case NOTEQUAL:
                predicates.add(builder.notEqual(field, value));
                applyOperator(builder, operator);
                break;
            case NOTEMPTY:
                predicates.add(builder.length(builder.trim(field)).in(0).not());
                applyOperator(builder, operator);
                break;
            case LESSTHAN:
                predicates.add(builder.lessThan(field, value));
                applyOperator(builder, operator);
                break;
            case ISNOTNULL:
                predicates.add(builder.isNotNull(field));
                applyOperator(builder, operator);
                break;
            case GREATERTHAN:
                predicates.add(builder.greaterThan(field, value));
                applyOperator(builder, operator);
                break;
            case LESSTHANOREQUALTO:
                predicates.add(builder.lessThanOrEqualTo(field, value));
                applyOperator(builder, operator);
                break;
            case GREATERTHANOREQUALTO:
                predicates.add(builder.greaterThanOrEqualTo(field, value));
                applyOperator(builder, operator);
                break;
            default:
                throw new IllegalArgumentException("HCFParameter not valid");
        }
    }

    private void applyOperator(CriteriaBuilder builder, HCFOperator operator) {
        try {
            switch (operator) {
                case OR:
                    predicates.add(builder.or(predicates.get(predicates.size() - 2), predicates.get(predicates.size() - 1)));
                    predicates.remove(predicates.size() - 3);
                    predicates.remove(predicates.size() - 2);
                    break;
                case AND:
                    predicates.add(builder.and(predicates.get(predicates.size() - 2), predicates.get(predicates.size() - 1)));
                    predicates.remove(predicates.size() - 3);
                    predicates.remove(predicates.size() - 2);
                    break;
                default:
                    break;
            }
        } catch (IndexOutOfBoundsException ignore) {
            // Are probably iterating a collection, and it was not possible to use HCFOperator.NONE
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void order(List<HCFOrder> orders, CriteriaBuilder builder, CriteriaQuery criteria, Root<T> root) {
        if (orders == null) return;
        List<Order> persistenceOrders = orders.stream()
                .filter(o -> o.getAsc() != null && o.getField() != null)
                .map(o -> o.getAsc() ? builder.asc(root.get(o.getField())) : builder.desc(root.get(o.getField())))
                .collect(Collectors.toList());
        criteria.orderBy(persistenceOrders);
    }

    private TypedQuery<T> limitResults(List<HCFOrder> orders, CriteriaQuery<T> criteria) {
        try {
            Integer limit = orders.stream().map(HCFOrder::getLimit).filter(Objects::nonNull).findFirst().orElse(null);
            Integer offset = orders.stream().map(HCFOrder::getOffset).filter(Objects::nonNull).findFirst().orElse(0);
            return session.createQuery(criteria).setFirstResult(offset).setMaxResults(limit);
        } catch (Exception e) {
            return session.createQuery(criteria);
        }
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