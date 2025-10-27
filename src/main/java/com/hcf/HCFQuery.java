package com.hcf;

import java.util.*;
import java.util.function.Consumer;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.hcf.enums.HCFOperator;
import com.hcf.enums.HCFParameter;
import com.hcf.utils.HCFLog;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

public final class HCFQuery<T> {

    private final Class<T> type;
    private final Session session;
    private final boolean ownsSession;
    private final Consumer<T> relationLoader;

    @FunctionalInterface
    private interface Condition {
        Predicate build(CriteriaBuilder cb, Root<?> root, Map<String, From<?, ?>> joins);
    }

    private final List<Condition> conditions = new ArrayList<>();
    private final List<HCFOperator> operators = new ArrayList<>();

    private static final class OrderSpec {
        final String fieldPath;
        final boolean asc;
        OrderSpec(String fieldPath, boolean asc) { this.fieldPath = fieldPath; this.asc = asc; }
    }
    private final List<OrderSpec> orderSpecs = new ArrayList<>();

    private Integer limit, offset;

    private final LinkedHashSet<String> joinAssociations = new LinkedHashSet<>();

    public HCFQuery(Class<T> type) {
        this(HCFFactory.INSTANCE.getFactory(), type);
    }

    public HCFQuery(SessionFactory sessionFactory, Class<T> type) {
        this(Objects.requireNonNull(sessionFactory, "SessionFactory is null").openSession(),
             Objects.requireNonNull(type, "Entity type is null"),
             true,
             null);
    }

    public HCFQuery(Session session, Class<T> type) {
        this(session, type, null);
    }

    public HCFQuery(Session session, Class<T> type, Consumer<T> relationLoader) {
        this(session, type, false, relationLoader);
    }

    private HCFQuery(Session session, Class<T> type, boolean ownsSession, Consumer<T> relationLoader) {
        this.session = Objects.requireNonNull(session, "Session is null");
        this.type = Objects.requireNonNull(type, "Entity type is null");
        this.ownsSession = ownsSession;
        this.relationLoader = relationLoader;
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
        Objects.requireNonNull(association, "association is null");
        joinAssociations.add(association);
        return this;
    }

    public HCFQuery<T> orderBy(String fieldPath) { return orderBy(fieldPath, true); }

    public HCFQuery<T> orderBy(String fieldPath, boolean asc) {
        Objects.requireNonNull(fieldPath, "orderBy fieldPath is null");
        orderSpecs.add(new OrderSpec(fieldPath, asc));
        return this;
    }

    public HCFQuery<T> limit(int n) { this.limit = n; return this; }
    public HCFQuery<T> offset(int n) { this.offset = n; return this; }

    public List<T> list() {
        try {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(type);
            Root<T> root = cq.from(type);

            Map<String, From<?, ?>> joins = createJoins(root);
            cq.select(root);

            Predicate combined = buildCombinedPredicate(cb, root, joins);
            if (combined != null) cq.where(combined);

            if (!orderSpecs.isEmpty()) cq.orderBy(buildOrders(cb, root, joins));

            TypedQuery<T> q = session.createQuery(cq);
            if (offset != null) q.setFirstResult(offset);
            if (limit  != null) q.setMaxResults(limit);

            List<T> result = q.getResultList();
            if (relationLoader != null) result.forEach(relationLoader);
            return result;
        } catch (Exception e) {
            HCFLog.showError(e, "HCFQuery.list");
            throw e;
        } finally {
            closeIfOwned();
        }
    }

    public List<Object[]> listJoined() {
        try {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
            Root<T> root = cq.from(type);

            Map<String, From<?, ?>> joins = createJoins(root);

            List<Selection<?>> selections = new ArrayList<>();
            selections.add(root);
            for (String assoc : joinAssociations) {
                From<?, ?> j = joins.get(assoc);
                if (j instanceof Join<?,?> join) {
                    selections.add(join);
                } else {
                    selections.add(resolvePath(root, joins, assoc));
                }
            }
            cq.select(cb.array(selections.toArray(new Selection<?>[0])));

            Predicate combined = buildCombinedPredicate(cb, root, joins);
            if (combined != null) cq.where(combined);

            if (!orderSpecs.isEmpty()) cq.orderBy(buildOrders(cb, root, joins));

            TypedQuery<Object[]> q = session.createQuery(cq);
            if (offset != null) q.setFirstResult(offset);
            if (limit  != null) q.setMaxResults(limit);

            return q.getResultList();
        } catch (Exception e) {
            HCFLog.showError(e, "HCFQuery.listJoined");
            throw e;
        } finally {
            closeIfOwned();
        }
    }

    public T one() {
        try {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(type);
            Root<T> root = cq.from(type);
            Map<String, From<?, ?>> joins = createJoins(root);

            cq.select(root);

            Predicate combined = buildCombinedPredicate(cb, root, joins);
            if (combined != null) cq.where(combined);

            if (!orderSpecs.isEmpty()) cq.orderBy(buildOrders(cb, root, joins));

            try {
                T entity = session.createQuery(cq).setMaxResults(1).getSingleResult();
                if (relationLoader != null && entity != null) relationLoader.accept(entity);
                return entity;
            } catch (NoResultException nre) {
                return null;
            }
        } catch (Exception e) {
            HCFLog.showError(e, "HCFQuery.one");
            throw e;
        } finally {
            closeIfOwned();
        }
    }

    public long count() {
        try {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> root = cq.from(type);
            Map<String, From<?, ?>> joins = createJoins(root);

            Predicate combined = buildCombinedPredicate(cb, root, joins);
            cq.select(cb.count(root));
            if (combined != null) cq.where(combined);

            return session.createQuery(cq).getSingleResult();
        } catch (Exception e) {
            HCFLog.showError(e, "HCFQuery.count");
            throw e;
        } finally {
            closeIfOwned();
        }
    }

    public List<Object> distinct(String fieldPath) {
        Objects.requireNonNull(fieldPath, "fieldPath is null");
        try {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Object> cq = cb.createQuery(Object.class);
            Root<T> root = cq.from(type);
            Map<String, From<?, ?>> joins = createJoins(root);

            Path<?> path = resolvePath(root, joins, fieldPath);
            cq.select(path).distinct(true);

            Predicate combined = buildCombinedPredicate(cb, root, joins);
            if (combined != null) cq.where(combined);

            if (!orderSpecs.isEmpty()) cq.orderBy(buildOrders(cb, root, joins));

            return session.createQuery(cq).getResultList();
        } catch (Exception e) {
            HCFLog.showError(e, "HCFQuery.distinct");
            throw e;
        } finally {
            closeIfOwned();
        }
    }

    public List<Number> sum(String... fields) {
        Objects.requireNonNull(fields, "fields is null");
        try {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            var cq = cb.createTupleQuery();
            Root<T> root = cq.from(type);
            Map<String, From<?, ?>> joins = createJoins(root);

            var selections = new ArrayList<Selection<?>>();
            for (String f : fields) {
                Path<?> p = resolvePath(root, joins, f);
                selections.add(cb.sum(p.as(Number.class)).alias(f));
            }

            Predicate combined = buildCombinedPredicate(cb, root, joins);
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
            closeIfOwned();
        }
    }

    public int update(Map<String, Object> values) {
        Objects.requireNonNull(values, "values is null");
        if (!joinAssociations.isEmpty()) {
            throw new IllegalStateException("update() não suporta join(). Remova joins ou execute via SQL/Query HQL.");
        }

        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaUpdate<T> cu = cb.createCriteriaUpdate(type);
            Root<T> root = cu.from(type);

            values.forEach(cu::set);

            Predicate combined = buildCombinedPredicate(cb, root, Map.of());
            if (combined != null) cu.where(combined);

            int updated = session.createMutationQuery(cu).executeUpdate();
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            HCFLog.showError(e, "HCFQuery.update");
            throw e;
        } finally {
            closeIfOwned();
        }
    }

    public int delete() {
        if (!joinAssociations.isEmpty()) {
            throw new IllegalStateException("delete() não suporta join(). Remova joins ou execute via SQL/Query HQL.");
        }

        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaDelete<T> cd = cb.createCriteriaDelete(type);
            Root<T> root = cd.from(type);

            Predicate combined = buildCombinedPredicate(cb, root, Map.of());
            if (combined != null) cd.where(combined);

            int deleted = session.createMutationQuery(cd).executeUpdate();
            tx.commit();
            return deleted;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            HCFLog.showError(e, "HCFQuery.delete");
            throw e;
        } finally {
            closeIfOwned();
        }
    }

    private void addCondition(String fieldPath, HCFParameter param, Object value, HCFOperator op) {
        Objects.requireNonNull(fieldPath, "fieldPath is null");
        Objects.requireNonNull(param, "parameter is null");
        Objects.requireNonNull(op, "operator is null");

        conditions.add((cb, root, joins) -> buildPredicate(cb, resolvePath(root, joins, fieldPath), param, value));
        operators.add(op);
    }

    private Map<String, From<?, ?>> createJoins(Root<T> root) {
        Map<String, From<?, ?>> map = new LinkedHashMap<>();
        for (String assoc : joinAssociations) {
            From<?, ?> current = root;
            for (String seg : assoc.split("\\.")) {
                current = current.join(seg); // inner join
            }
            map.put(assoc, current);
        }
        return map;
    }

    private Path<?> resolvePath(Root<?> root, Map<String, From<?, ?>> joins, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        From<?, ?> base = null;
        int start = 0;

        if (joins.containsKey(fieldPath)) {
            base = joins.get(fieldPath);
            start = parts.length;
        } else {
            String prefix = "";
            for (int i = 0; i < parts.length; i++) {
                prefix = (i == 0) ? parts[0] : prefix + "." + parts[i];
                if (joins.containsKey(prefix)) {
                    base = joins.get(prefix);
                    start = i + 1;
                }
            }
        }

        Path<?> path;
        if (base == null) {
            path = root.get(parts[0]);
            start = 1;
        } else {
            path = base;
        }

        for (int i = start; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return path;
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Predicate buildPredicate(CriteriaBuilder cb, Path<?> field, HCFParameter p, Object v) {
		return switch (p) {
		case TRUE -> cb.isTrue(field.as(Boolean.class));
		case FALSE -> cb.isFalse(field.as(Boolean.class));
		case IS_NULL -> cb.isNull(field);
		case IS_NOT_NULL -> cb.isNotNull(field);
		case EMPTY -> cb.equal(cb.length(cb.trim(field.as(String.class))), 0);
		case NOT_EMPTY -> cb.notEqual(cb.length(cb.trim(field.as(String.class))), 0);
		case IS_ODD -> cb.equal(cb.mod(field.as(Integer.class), 2), 1);
		case IS_EVEN -> cb.equal(cb.mod(field.as(Integer.class), 2), 0);
		case LIKE -> cb.like(field.as(String.class), Objects.toString(v, ""));
		case NOT_LIKE -> cb.notLike(field.as(String.class), Objects.toString(v, ""));
		case EQUAL -> cb.equal(field, v);
		case NOT_EQUAL -> cb.notEqual(field, v);
		case LESS_THAN -> cb.lessThan((Expression) field, (Comparable) v);
		case GREATER_THAN -> cb.greaterThan((Expression) field, (Comparable) v);
		case LESS_THAN_OR_EQUAL_TO -> cb.lessThanOrEqualTo((Expression) field, (Comparable) v);
		case GREATER_THAN_OR_EQUAL_TO -> cb.greaterThanOrEqualTo((Expression) field, (Comparable) v);
		};
	}

    private Predicate buildCombinedPredicate(CriteriaBuilder cb, Root<?> root, Map<String, From<?, ?>> joins) {
        if (conditions.isEmpty()) return null;

        Deque<Predicate> stack = new ArrayDeque<>();
        for (int i = 0; i < conditions.size(); i++) {
            Predicate p = conditions.get(i).build(cb, root, joins);
            HCFOperator op = operators.get(i);

            if (op == HCFOperator.NONE) {
                stack.addLast(p);
            } else {
                if (stack.isEmpty()) {
                    stack.addLast(p);
                } else {
                    Predicate left = stack.removeLast();
                    Predicate combined = (op == HCFOperator.AND) ? cb.and(left, p) : cb.or(left, p);
                    stack.addLast(combined);
                }
            }
        }
        return stack.getLast();
    }

    private List<Order> buildOrders(CriteriaBuilder cb, Root<T> root, Map<String, From<?, ?>> joins) {
        List<Order> orders = new ArrayList<>(orderSpecs.size());
        for (OrderSpec spec : orderSpecs) {
            Path<?> p = resolvePath(root, joins, spec.fieldPath);
            orders.add(spec.asc ? cb.asc(p) : cb.desc(p));
        }
        return orders;
    }

    private void closeIfOwned() {
        if (!ownsSession) return;
        try {
            if (session != null && session.isOpen()) session.close();
        } catch (Exception e) {
            HCFLog.showError(e, "HCFQuery.closeIfOwned");
        }
    }
}