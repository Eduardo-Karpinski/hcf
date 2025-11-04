package com.hcf.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.hcf.core.HCFFactory;
import com.hcf.query.enums.HCFOperator;
import com.hcf.query.enums.HCFParameter;
import com.hcf.utils.HCFLog;
import com.hcf.utils.HCFPredicateUtil;
import com.hcf.utils.HCFUtil;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;

public final class HCFQuery<T> {

	private Integer limit, offset;
	private final Class<T> type;
	private final Session session;
	private final List<HCFSort> sorts = new ArrayList<>();
	private final List<HCFSearch> searches = new ArrayList<>();
	private final LinkedHashMap<String, JoinType> joinAssociations = new LinkedHashMap<>();
	private final LinkedHashMap<String, JoinType> fetchAssociations = new LinkedHashMap<>();
	private final LinkedHashMap<String, JoinType> fetchJoinAssociations = new LinkedHashMap<>();
	
	public HCFQuery(Class<T> type) {
		this(HCFFactory.INSTANCE.getFactory(), type);
	}

	public HCFQuery(SessionFactory sessionFactory, Class<T> type) {
		this(Objects.requireNonNull(sessionFactory, "SessionFactory is null").openSession(), type);
	}

	private HCFQuery(Session session, Class<T> type) {
		this.session = Objects.requireNonNull(session, "Session is null");
		this.type = Objects.requireNonNull(type, "Entity type is null");
	}

	public HCFQuery<T> where(String fieldPath, HCFParameter param, Object value) {
		addCondition(fieldPath, param, value, HCFOperator.NONE);
		return this;
	}

	public HCFQuery<T> and(String fieldPath, HCFParameter param, Object value) {
		addCondition(fieldPath, param, value, HCFOperator.AND);
		return this;
	}

	public HCFQuery<T> or(String fieldPath, HCFParameter param, Object value) {
		addCondition(fieldPath, param, value, HCFOperator.OR);
		return this;
	}

	public HCFQuery<T> join(String association) {
		return join(association, JoinType.INNER);
	}

	public HCFQuery<T> join(String association, JoinType type) {
		Objects.requireNonNull(association, "association is null");
		Objects.requireNonNull(type, "join type is null");
		joinAssociations.put(association, type);
		return this;
	}

	public HCFQuery<T> fetchJoin(String association) {
		return fetchJoin(association, JoinType.INNER);
	}

	public HCFQuery<T> fetchJoin(String association, JoinType type) {
		Objects.requireNonNull(association, "association is null");
		Objects.requireNonNull(type, "join type is null");
		fetchJoinAssociations.put(association, type);
		return this;
	}

	public HCFQuery<T> orderBy(String fieldPath) {
		return orderBy(fieldPath, true);
	}

	public HCFQuery<T> orderBy(String fieldPath, boolean asc) {
		Objects.requireNonNull(fieldPath, "orderBy fieldPath is null");
		sorts.add(new HCFSort(fieldPath, asc));
		return this;
	}

	public HCFQuery<T> limit(int n) {
		this.limit = n;
		return this;
	}

	public HCFQuery<T> offset(int n) {
		this.offset = n;
		return this;
	}

	public List<T> list() {
		try {
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);
			Root<T> root = criteriaQuery.from(type);

			Map<String, From<?, ?>> joins = createJoins(root, true);
			applyFetchJoins(root);
			applyFetches(root);
			criteriaQuery.select(root);
			
			if (requiresDistinctForFetches(root)) {
	            criteriaQuery.distinct(true);
	        }

			Predicate combined = HCFPredicateUtil.buildForJoins(criteriaBuilder, root, joins, searches);
			if (combined != null) {
				criteriaQuery.where(combined);
			}

			if (!sorts.isEmpty()) {
				criteriaQuery.orderBy(buildOrders(criteriaBuilder, root, joins));
			}

			TypedQuery<T> typedQuery = session.createQuery(criteriaQuery);
			if (offset != null) {
				typedQuery.setFirstResult(offset);
			}
			if (limit != null) {
				typedQuery.setMaxResults(limit);
			}

			List<T> result = typedQuery.getResultList();
			
			return result;
		} catch (Exception e) {
			HCFLog.showError(e, "HCFQuery.list");
			throw e;
		} finally {
			close();
		}
	}

	public List<Object[]> listJoined() {
		try {
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
			Root<T> root = criteriaQuery.from(type);

			Map<String, From<?, ?>> joins = createJoins(root, false);
			applyFetchJoins(root);

			List<Selection<?>> selections = new ArrayList<>();
			selections.add(root);
			for (String assoc : joinAssociations.keySet()) {
				From<?, ?> j = joins.get(assoc);
				if (j instanceof Join<?, ?> join) {
					selections.add(join);
				} else {
					selections.add(HCFPredicateUtil.resolvePath(root, joins, assoc));
				}
			}
			criteriaQuery.select(criteriaBuilder.array(selections.toArray(new Selection<?>[0])));

			Predicate predicate = HCFPredicateUtil.buildForJoins(criteriaBuilder, root, joins, searches);
			if (predicate != null) {
				criteriaQuery.where(predicate);
			}

			if (!sorts.isEmpty()) {
				criteriaQuery.orderBy(buildOrders(criteriaBuilder, root, joins));
			}

			TypedQuery<Object[]> typedQuery = session.createQuery(criteriaQuery);
			
			if (offset != null) {
				typedQuery.setFirstResult(offset);
			}
			
			if (limit != null) {
				typedQuery.setMaxResults(limit);
			}

			return typedQuery.getResultList();
		} catch (Exception e) {
			HCFLog.showError(e, "HCFQuery.listJoined");
			throw e;
		} finally {
			close();
		}
	}

	public T one() {
		try {
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);
			Root<T> root = criteriaQuery.from(type);
			Map<String, From<?, ?>> joins = createJoins(root, true);
			applyFetchJoins(root);
			applyFetches(root);
			
			if (requiresDistinctForFetches(root)) {
	            criteriaQuery.distinct(true);
	        }

			criteriaQuery.select(root);

			Predicate predicate = HCFPredicateUtil.buildForJoins(criteriaBuilder, root, joins, searches);
			
			if (predicate != null) {
				criteriaQuery.where(predicate);
			}

			if (!sorts.isEmpty()) {
				criteriaQuery.orderBy(buildOrders(criteriaBuilder, root, joins));
			}

			try {
				T entity = session.createQuery(criteriaQuery).setMaxResults(1).getSingleResult();
				return entity;
			} catch (NoResultException exception) {
				return null;
			}
		} catch (Exception e) {
			HCFLog.showError(e, "HCFQuery.one");
			throw e;
		} finally {
			close();
		}
	}

	public long count() {
		try {
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
			Root<T> root = criteriaQuery.from(type);
			Map<String, From<?, ?>> joins = createJoins(root, false);

			Predicate predicate = HCFPredicateUtil.buildForJoins(criteriaBuilder, root, joins, searches);
			criteriaQuery.select(criteriaBuilder.count(root));
			
			if (predicate != null) {
				criteriaQuery.where(predicate);
			}

			return session.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			HCFLog.showError(e, "HCFQuery.count");
			throw e;
		} finally {
			close();
		}
	}

	public List<Object> distinct(String fieldPath) {
		Objects.requireNonNull(fieldPath, "fieldPath is null");
		try {
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
			Root<T> root = criteriaQuery.from(type);
			Map<String, From<?, ?>> joins = createJoins(root, false);
			applyFetchJoins(root);

			Path<?> path = HCFPredicateUtil.resolvePath(root, joins, fieldPath);;
			criteriaQuery.select(path).distinct(true);

			Predicate predicate = HCFPredicateUtil.buildForJoins(criteriaBuilder, root, joins, searches);
			if (predicate != null) {
				criteriaQuery.where(predicate);
			}

			if (!sorts.isEmpty()) {
				criteriaQuery.orderBy(buildOrders(criteriaBuilder, root, joins));
			}

			return session.createQuery(criteriaQuery).getResultList();
		} catch (Exception e) {
			HCFLog.showError(e, "HCFQuery.distinct");
			throw e;
		} finally {
			close();
		}
	}

	public List<Number> sum(String... fields) {
	    Objects.requireNonNull(fields, "fields is null");
	    try {
	        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
	        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
	        Root<T> root = criteriaQuery.from(type);

	        Map<String, From<?, ?>> joins = createJoins(root, false);

	        List<Selection<?>> selections = new ArrayList<>();
	        for (String field : fields) {
	            Path<?> path = HCFPredicateUtil.resolvePath(root, joins, field);
	            Class<?> javaType = path.getJavaType();

	            if (!Number.class.isAssignableFrom(javaType)) {
	                throw new IllegalArgumentException("Field '" + field + "' is not numerical. (javaType=" + javaType.getSimpleName() + ")");
	            }

	            @SuppressWarnings("unchecked")
	            Expression<? extends Number> numberExpression = (Expression<? extends Number>) path;

	            selections.add(criteriaBuilder.sum(numberExpression).alias(field));
	        }

	        Predicate predicate = HCFPredicateUtil.buildForJoins(criteriaBuilder, root, joins, searches);
	        criteriaQuery.select(criteriaBuilder.tuple(selections.toArray(new Selection<?>[0])));
	        
	        if (predicate != null) {
	        	criteriaQuery.where(predicate);
	        }

	        Tuple tuple = session.createQuery(criteriaQuery).getSingleResult();
	        List<Number> sums = new ArrayList<>(fields.length);
	        
	        for (String field : fields) {
	        	sums.add(tuple.get(field, Number.class));
	        }
	        
	        return sums;
	    } catch (Exception e) {
	        HCFLog.showError(e, "HCFQuery.sum");
	        throw e;
	    } finally {
	        close();
	    }
	}
	
	public List<T> getByInvertedRelation(Class<?> child, String column, Object id) {
	    String childIdField = HCFUtil.getIdFieldName(session, child);
	    return new HCFQuery<>(session, type)
	            .join(column)
	            .where(column + "." + childIdField, HCFParameter.EQUAL, id)
	            .list();
	}
	
	public HCFQuery<T> fetch(String association) { 
	    return fetch(association, JoinType.LEFT); 
	}
	
	public HCFQuery<T> fetch(String association, JoinType type) {
	    Objects.requireNonNull(association, "association is null");
	    fetchAssociations.put(association, type);
	    return this;
	}
	
	public HCFQuery<T> fetchAll() {
		Arrays.stream(type.getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(OneToMany.class)
						|| f.isAnnotationPresent(ManyToOne.class)
						|| f.isAnnotationPresent(ManyToMany.class)
						|| f.isAnnotationPresent(OneToOne.class))
				.forEach(f -> fetch(f.getName(), JoinType.LEFT));
		return this;
	}

	private void applyFetches(Root<T> root) {
		for (Entry<String, JoinType> e : fetchAssociations.entrySet()) {
			String path = e.getKey();
			JoinType joinType = e.getValue();
			String[] attributeNames = path.split("\\.");
			FetchParent<?, ?> fetchParent = root;
			for (String attributeName : attributeNames) {
				fetchParent = fetchParent.fetch(attributeName, joinType);
			}
		}
	}

	public int update(Map<String, Object> values) {
		Transaction transaction = null;
		try {
			Objects.requireNonNull(values, "values is null");
			if (!joinAssociations.isEmpty()) {
				throw new UnsupportedOperationException("JOIN usage is not allowed for update() — please use SQL/HQL instead.");
			}
			
			transaction = session.beginTransaction();

			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaUpdate<T> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(type);
			Root<T> root = criteriaUpdate.from(type);

			values.forEach(criteriaUpdate::set);

			Predicate predicate = HCFPredicateUtil.buildForJoins(criteriaBuilder, root, Map.of(), searches);
			
			if (predicate != null) {
				criteriaUpdate.where(predicate);
			}

			int updated = session.createMutationQuery(criteriaUpdate).executeUpdate();
			transaction.commit();
			return updated;
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			HCFLog.showError(e, "HCFQuery.update");
			throw e;
		} finally {
			close();
		}
	}

	public int delete() {
		Transaction transaction = null;
		try {
			if (!joinAssociations.isEmpty()) {
				throw new UnsupportedOperationException("JOIN usage is not allowed for delete() — please use SQL/HQL instead.");
			}
			
			transaction = session.beginTransaction();

			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaDelete<T> criteriaDelete = criteriaBuilder.createCriteriaDelete(type);
			Root<T> root = criteriaDelete.from(type);

			Predicate predicate = HCFPredicateUtil.buildForJoins(criteriaBuilder, root, Map.of(), searches);
			if (predicate != null) {
				criteriaDelete.where(predicate);
			}

			int deleted = session.createMutationQuery(criteriaDelete).executeUpdate();
			transaction.commit();
			return deleted;
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			HCFLog.showError(e, "HCFQuery.delete");
			throw e;
		} finally {
			close();
		}
	}
	
	private boolean requiresDistinctForFetches(Root<T> root) {
	    for (String path : fetchAssociations.keySet()) {
	        if (isPluralPath(root, path)) return true;
	    }
	    for (String path : fetchJoinAssociations.keySet()) {
	        if (isPluralPath(root, path)) return true;
	    }
	    return false;
	}
	
	private boolean isPluralPath(Root<T> root, String dotPath) {
	    ManagedType<?> managedType = (ManagedType<?>) root.getModel();
	    String[] names = dotPath.split("\\.");
	    for (String name : names) {
	        Attribute<?,?> attribute = managedType.getAttribute(name);
	        if (attribute instanceof PluralAttribute) {
	            return true;
	        }
	        managedType = (ManagedType<?>) ((Attribute<?,?>) attribute).getDeclaringType();
	        Class<?> javaType = attribute.getJavaType();
	        managedType = (ManagedType<?>) session.getMetamodel().managedType(javaType);
	    }
	    return false;
	}

	private void addCondition(String fieldPath, HCFParameter param, Object value, HCFOperator op) {
		Objects.requireNonNull(fieldPath, "fieldPath is null");
		Objects.requireNonNull(param, "parameter is null");
		Objects.requireNonNull(op, "operator is null");

		searches.add(new HCFSearch(fieldPath, value, param, op));
	}

	private Map<String, From<?, ?>> createJoins(Root<T> root, boolean fetchAllowed) {
	    Map<String, From<?, ?>> map = new LinkedHashMap<>();
	    for (Map.Entry<String, JoinType> entry : joinAssociations.entrySet()) {
	        String assoc = entry.getKey();
	        JoinType joinType = entry.getValue();

	        From<?, ?> from = root;
	        String[] parts = assoc.split("\\.");
	        StringBuilder stringBuilder = new StringBuilder();

	        for (int i = 0; i < parts.length; i++) {
	            String seg = parts[i];

	            from = from.join(seg, joinType);

	            if (fetchAllowed) {
	                ((FetchParent<?, ?>) (i == 0 ? root : map.get(stringBuilder.toString()))).fetch(seg, joinType);
	            }

	            if (stringBuilder.length() == 0) {
	            	stringBuilder.append(seg);
	            } else {
	            	stringBuilder.append('.').append(seg);
	            }

	            map.put(stringBuilder.toString(), from);
	        }
	    }
	    return map;
	}

	private void applyFetchJoins(Root<T> root) {
		for (Entry<String, JoinType> e : fetchJoinAssociations.entrySet()) {
			String assoc = e.getKey();
			JoinType joinType = e.getValue();
			String[] parts = assoc.split("\\.");
			Fetch<Object, Object> currentFetch = root.fetch(parts[0], joinType);
			for (int i = 1; i < parts.length; i++) {
				currentFetch = currentFetch.fetch(parts[i], joinType);
			}
		}
	}

	private List<Order> buildOrders(CriteriaBuilder cb, Root<T> root, Map<String, From<?, ?>> joins) {
		List<Order> criteriaOrders = new ArrayList<>(sorts.size());
		for (HCFSort sort : sorts) {
			Path<?> path = HCFPredicateUtil.resolvePath(root, joins, sort.field());;
			criteriaOrders.add(sort.asc() ? cb.asc(path) : cb.desc(path));
		}
		return criteriaOrders;
	}

	private void close() {
		try {
			if (session != null && session.isOpen()) {
				session.close();
			}
		} catch (Exception e) {
			HCFLog.showError(e, "HCFQuery.close");
		}
	}
	
}