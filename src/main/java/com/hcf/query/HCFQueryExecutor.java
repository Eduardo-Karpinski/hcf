package com.hcf.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.CommonQueryContract;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import com.hcf.core.HCFFactory;
import com.hcf.utils.HCFLog;

public final class HCFQueryExecutor {

    private HCFQueryExecutor() {}

    public static int executeNative(String sql) {
        return executeNative(sql, Map.of());
    }

    public static int executeNative(String sql, Map<String, ?> params) {
        Objects.requireNonNull(sql, "sql is null");
        Objects.requireNonNull(params, "params is null");

        Transaction tx = null;
        Session session = null;
        try {
            session = HCFFactory.INSTANCE.getFactory().openSession();
            tx = session.beginTransaction();

            MutationQuery q = session.createNativeMutationQuery(sql);
            bindParams(q, params);

            int updated = q.executeUpdate();
            tx.commit();
            return updated;
        } catch (Throwable e) {
            rollback(tx);
            HCFLog.showError(e, "HCFSql.executeNative(params)");
            return -1;
        } finally {
            close(session);
        }
    }

    public static List<Object[]> listNative(String sql) {
        return listNative(sql, Map.of());
    }

    public static List<Object[]> listNative(String sql, Map<String, ?> params) {
        Objects.requireNonNull(sql, "sql is null");
        Objects.requireNonNull(params, "params is null");

        Session session = null;
        try {
            session = HCFFactory.INSTANCE.getFactory().openSession();
            NativeQuery<Object[]> q = session.createNativeQuery(sql, Object[].class);
            bindParams(q, params);
            return q.getResultList();
        } catch (Exception e) {
            HCFLog.showError(e, "HCFSql.listNative(Object[], params)");
            return null;
        } finally {
            close(session);
        }
    }

    public static <R> List<R> listNative(String sql, Class<R> resultClass) {
        return listNative(sql, resultClass, Map.of());
    }

    public static <R> List<R> listNative(String sql, Class<R> resultClass, Map<String, ?> params) {
        Objects.requireNonNull(sql, "sql is null");
        Objects.requireNonNull(resultClass, "resultClass is null");
        Objects.requireNonNull(params, "params is null");

        Session session = null;
        try {
            session = HCFFactory.INSTANCE.getFactory().openSession();
            NativeQuery<R> q = session.createNativeQuery(sql, resultClass);
            bindParams(q, params);
            return q.getResultList();
        } catch (Exception e) {
            HCFLog.showError(e, "HCFSql.listNative(Class, params)");
            return null;
        } finally {
            close(session);
        }
    }

    public static int executeHql(String hql) {
        return executeHql(hql, Map.of());
    }

    public static int executeHql(String hql, Map<String, ?> params) {
        Objects.requireNonNull(hql, "hql is null");
        Objects.requireNonNull(params, "params is null");

        Transaction tx = null;
        Session session = null;
        try {
            session = HCFFactory.INSTANCE.getFactory().openSession();
            tx = session.beginTransaction();

            MutationQuery q = session.createMutationQuery(hql); // HQL/JPQL DML
            bindParams(q, params);

            int updated = q.executeUpdate();
            tx.commit();
            return updated;
        } catch (Throwable e) {
            rollback(tx);
            HCFLog.showError(e, "HCFSql.executeHql(params)");
            return -1;
        } finally {
            close(session);
        }
    }

    public static <R> List<R> listHql(String hql, Class<R> resultClass) {
        return listHql(hql, resultClass, Map.of());
    }

    public static <R> List<R> listHql(String hql, Class<R> resultClass, Map<String, ?> params) {
        Objects.requireNonNull(hql, "hql is null");
        Objects.requireNonNull(resultClass, "resultClass is null");
        Objects.requireNonNull(params, "params is null");

        Session session = null;
        try {
            session = HCFFactory.INSTANCE.getFactory().openSession();
            Query<R> q = session.createQuery(hql, resultClass);
            bindParams(q, params);
            return q.getResultList();
        } catch (Exception e) {
            HCFLog.showError(e, "HCFSql.listHql(Class, params)");
            return null;
        } finally {
            close(session);
        }
    }

    public static List<Object[]> listHql(String hql) {
        return listHql(hql, Map.of());
    }

    public static List<Object[]> listHql(String hql, Map<String, ?> params) {
        Objects.requireNonNull(hql, "hql is null");
        Objects.requireNonNull(params, "params is null");

        Session session = null;
        try {
            session = HCFFactory.INSTANCE.getFactory().openSession();
            Query<Object[]> q = session.createQuery(hql, Object[].class);
            bindParams(q, params);
            return q.getResultList();
        } catch (Exception e) {
            HCFLog.showError(e, "HCFSql.listHql(Object[], params)");
            return null;
        } finally {
            close(session);
        }
    }

    private static void bindParams(CommonQueryContract query, Map<String, ?> params) {
        if (params == null || params.isEmpty()) return;

        for (Map.Entry<String, ?> e : params.entrySet()) {
            String name = e.getKey();
            Object value = e.getValue();

            if (value instanceof Collection<?> col) {
                query.setParameterList(name, col);
            } else if (value != null && value.getClass().isArray()) {
                query.setParameterList(name, Arrays.asList((Object[]) value));
            } else {
                query.setParameter(name, value);
            }
        }
    }

    private static void rollback(Transaction tx) {
        if (tx != null && tx.getStatus() == TransactionStatus.ACTIVE) {
            try { tx.rollback(); } catch (Exception ignore) {}
        }
    }

    private static void close(Session session) {
        if (session != null && session.isOpen()) {
            try { session.close(); } catch (Exception ignore) {}
        }
    }
    
}