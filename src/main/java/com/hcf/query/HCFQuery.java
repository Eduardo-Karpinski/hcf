package com.hcf.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

public final class HCFQuery<T> {

	private Integer limit, offset;
	
	private final Class<T> type;
	private final Session session;
	private final List<HCFSort> orders = new ArrayList<>();
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

	public HCFQuery<T> innerJoin(String association) {
		return join(association, JoinType.INNER);
	}

	public HCFQuery<T> leftJoin(String association) {
		return join(association, JoinType.LEFT);
	}

	public HCFQuery<T> rightJoin(String association) {
		return join(association, JoinType.RIGHT);
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
		orders.add(new HCFSort(fieldPath, asc));
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
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<T> cq = cb.createQuery(type);
			Root<T> root = cq.from(type);

			Map<String, From<?, ?>> joins = createJoins(root, true);
			applyFetchJoins(root);
			cq.select(root);
			
			if (!fetchAssociations.isEmpty()) {
				applyFetches(root);
				cq.distinct(true);
			}

			Predicate combined = HCFPredicateUtil.buildForJoins(cb, root, joins, searches);
			if (combined != null)
				cq.where(combined);

			if (!orders.isEmpty())
				cq.orderBy(buildOrders(cb, root, joins));

			TypedQuery<T> q = session.createQuery(cq);
			if (offset != null)
				q.setFirstResult(offset);
			if (limit != null)
				q.setMaxResults(limit);

			List<T> result = q.getResultList();
			
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
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
			Root<T> root = cq.from(type);

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
			cq.select(cb.array(selections.toArray(new Selection<?>[0])));

			Predicate combined = HCFPredicateUtil.buildForJoins(cb, root, joins, searches);
			if (combined != null)
				cq.where(combined);

			if (!orders.isEmpty())
				cq.orderBy(buildOrders(cb, root, joins));

			TypedQuery<Object[]> q = session.createQuery(cq);
			if (offset != null)
				q.setFirstResult(offset);
			if (limit != null)
				q.setMaxResults(limit);

			return q.getResultList();
		} catch (Exception e) {
			HCFLog.showError(e, "HCFQuery.listJoined");
			throw e;
		} finally {
			close();
		}
	}

	public T one() {
		try {
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<T> cq = cb.createQuery(type);
			Root<T> root = cq.from(type);
			Map<String, From<?, ?>> joins = createJoins(root, true);
			applyFetchJoins(root);
			
			if (!fetchAssociations.isEmpty()) {
				applyFetches(root);
				cq.distinct(true);
			}

			cq.select(root);

			Predicate combined = HCFPredicateUtil.buildForJoins(cb, root, joins, searches);
			if (combined != null)
				cq.where(combined);

			if (!orders.isEmpty())
				cq.orderBy(buildOrders(cb, root, joins));

			try {
				T entity = session.createQuery(cq).setMaxResults(1).getSingleResult();
				return entity;
			} catch (NoResultException nre) {
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
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<T> root = cq.from(type);
			Map<String, From<?, ?>> joins = createJoins(root, false);

			Predicate combined = HCFPredicateUtil.buildForJoins(cb, root, joins, searches);
			cq.select(cb.count(root));
			if (combined != null)
				cq.where(combined);

			return session.createQuery(cq).getSingleResult();
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
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Object> cq = cb.createQuery(Object.class);
			Root<T> root = cq.from(type);
			Map<String, From<?, ?>> joins = createJoins(root, false);
			applyFetchJoins(root);

			Path<?> path = HCFPredicateUtil.resolvePath(root, joins, fieldPath);;
			cq.select(path).distinct(true);

			Predicate combined = HCFPredicateUtil.buildForJoins(cb, root, joins, searches);
			if (combined != null)
				cq.where(combined);

			if (!orders.isEmpty())
				cq.orderBy(buildOrders(cb, root, joins));

			return session.createQuery(cq).getResultList();
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
	        CriteriaBuilder cb = session.getCriteriaBuilder();
	        var cq = cb.createTupleQuery();
	        Root<T> root = cq.from(type);

	        Map<String, From<?, ?>> joins = createJoins(root, false);

	        var selections = new ArrayList<Selection<?>>();
	        for (String f : fields) {
	            Path<?> p = HCFPredicateUtil.resolvePath(root, joins, f);
	            Class<?> jt = p.getJavaType();

	            if (!Number.class.isAssignableFrom(jt)) {
	                throw new IllegalArgumentException("Field '" + f + "' não é numérico (javaType=" + jt.getSimpleName() + ")");
	            }

	            @SuppressWarnings("unchecked")
	            Expression<? extends Number> numExpr = (Expression<? extends Number>) p;

	            selections.add(cb.sum(numExpr).alias(f));
	        }

	        Predicate combined = HCFPredicateUtil.buildForJoins(cb, root, joins, searches);
	        cq.select(cb.tuple(selections.toArray(new Selection<?>[0])));
	        if (combined != null) cq.where(combined);

	        var t = session.createQuery(cq).getSingleResult();
	        var out = new ArrayList<Number>(fields.length);
	        for (String f : fields) out.add(t.get(f, Number.class));
	        return out;
	    } catch (Exception e) {
	        HCFLog.showError(e, "HCFQuery.sum");
	        throw e;
	    } finally {
	        close();
	    }
	}
	
	public List<T> nativeList(String sql) {
	    Objects.requireNonNull(sql, "sql is null");
	    try {
	        var q = session.createNativeQuery(sql, type);
	        var result = q.getResultList();
	        return result;
	    } catch (Exception e) {
	        HCFLog.showError(e, "HCFQuery.nativeList");
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
		for (var e : fetchAssociations.entrySet()) {
			String path = e.getKey();
			JoinType jt = e.getValue();
			String[] parts = path.split("\\.");
			FetchParent<?, ?> parent = root;
			for (String seg : parts) {
				parent = parent.fetch(seg, jt);
			}
		}
	}

	public int update(Map<String, Object> values) {
		Transaction tx = null;
		try {
			Objects.requireNonNull(values, "values is null");
			if (!joinAssociations.isEmpty()) {
				throw new UnsupportedOperationException("JOIN usage is not allowed for update() — please use SQL/HQL instead.");
			}
			
			tx = session.beginTransaction();

			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaUpdate<T> cu = cb.createCriteriaUpdate(type);
			Root<T> root = cu.from(type);

			values.forEach(cu::set);

			Predicate combined = HCFPredicateUtil.buildForJoins(cb, root, Map.of(), searches);
			if (combined != null)
				cu.where(combined);

			int updated = session.createMutationQuery(cu).executeUpdate();
			tx.commit();
			return updated;
		} catch (Exception e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			HCFLog.showError(e, "HCFQuery.update");
			throw e;
		} finally {
			close();
		}
	}

	public int delete() {
		Transaction tx = null;
		try {
			
			if (!joinAssociations.isEmpty()) {
				throw new UnsupportedOperationException("JOIN usage is not allowed for delete() — please use SQL/HQL instead.");
			}
			
			tx = session.beginTransaction();

			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaDelete<T> cd = cb.createCriteriaDelete(type);
			Root<T> root = cd.from(type);

			Predicate combined = HCFPredicateUtil.buildForJoins(cb, root, Map.of(), searches);
			if (combined != null)
				cd.where(combined);

			int deleted = session.createMutationQuery(cd).executeUpdate();
			tx.commit();
			return deleted;
		} catch (Exception e) {
			if (tx != null && tx.isActive())
				tx.rollback();
			HCFLog.showError(e, "HCFQuery.delete");
			throw e;
		} finally {
			close();
		}
	}

	private void addCondition(String fieldPath, HCFParameter param, Object value, HCFOperator op) {
		Objects.requireNonNull(fieldPath, "fieldPath is null");
		Objects.requireNonNull(param, "parameter is null");
		Objects.requireNonNull(op, "operator is null");

		searches.add(new HCFSearch(fieldPath, value, param, op));
	}

	private Map<String, From<?, ?>> createJoins(Root<T> root, boolean fetchAllowed) {
	    Map<String, From<?, ?>> map = new LinkedHashMap<>();
	    for (Map.Entry<String, jakarta.persistence.criteria.JoinType> entry : joinAssociations.entrySet()) {
	        String assoc = entry.getKey();
	        JoinType type = entry.getValue();

	        From<?, ?> current = root;
	        String[] parts = assoc.split("\\.");
	        StringBuilder built = new StringBuilder();

	        for (int i = 0; i < parts.length; i++) {
	            String seg = parts[i];

	            current = current.join(seg, type);

	            if (fetchAllowed) {
	                ((FetchParent<?, ?>) (i == 0 ? root : map.get(built.toString()))).fetch(seg, type);
	            }

	            if (built.length() == 0) built.append(seg);
	            else built.append('.').append(seg);

	            map.put(built.toString(), current);
	        }
	    }
	    return map;
	}

	private void applyFetchJoins(Root<T> root) {
		for (Map.Entry<String, JoinType> e : fetchJoinAssociations.entrySet()) {
			String assoc = e.getKey();
			JoinType jt = e.getValue();
			String[] parts = assoc.split("\\.");
			var currentFetch = root.fetch(parts[0], jt);
			for (int i = 1; i < parts.length; i++) {
				currentFetch = currentFetch.fetch(parts[i], jt);
			}
		}
	}

	private List<Order> buildOrders(CriteriaBuilder cb, Root<T> root, Map<String, From<?, ?>> joins) {
		List<Order> criteriaOrders = new ArrayList<>(orders.size());
		for (HCFSort order : orders) {
			Path<?> p = HCFPredicateUtil.resolvePath(root, joins, order.field());;
			criteriaOrders.add(order.asc() ? cb.asc(p) : cb.desc(p));
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