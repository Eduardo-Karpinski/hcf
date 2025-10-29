package com.hcf;

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
import org.hibernate.resource.transaction.spi.TransactionStatus;

import com.hcf.utils.HCFLog;

public final class HCFSql {

	private HCFSql() {}

	public static int execute(String sql) {
		return execute(sql, Map.of());
	}

	public static List<Object[]> list(String sql) {
		return list(sql, Map.of());
	}

	public static <R> List<R> list(String sql, Class<R> resultClass) {
		return list(sql, resultClass, Map.of());
	}

	public static int execute(String sql, Map<String, ?> params) {
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
			if (tx != null && tx.getStatus() == TransactionStatus.ACTIVE) {
				try {
					tx.rollback();
				} catch (Exception ex) {
					HCFLog.showError(ex);
				}
			}
			HCFLog.showError(e, "HCFSql.execute(params)");
			return -1;
		} finally {
			if (session != null && session.isOpen()) {
				try {
					session.close();
				} catch (Exception e) {
					HCFLog.showError(e);
				}
			}
		}
	}

	public static List<Object[]> list(String sql, Map<String, ?> params) {
		Objects.requireNonNull(sql, "sql is null");
		Objects.requireNonNull(params, "params is null");

		Session session = null;
		try {
			session = HCFFactory.INSTANCE.getFactory().openSession();
			NativeQuery<Object[]> q = session.createNativeQuery(sql, Object[].class);
			bindParams(q, params);
			return q.getResultList();
		} catch (Exception e) {
			HCFLog.showError(e, "HCFSql.list(Object[], params)");
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				try {
					session.close();
				} catch (Exception e) {
					HCFLog.showError(e);
				}
			}
		}
	}

	public static <R> List<R> list(String sql, Class<R> resultClass, Map<String, ?> params) {
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
			HCFLog.showError(e, "HCFSql.list(Class, params)");
			return null;
		} finally {
			if (session != null && session.isOpen()) {
				try {
					session.close();
				} catch (Exception e) {
					HCFLog.showError(e);
				}
			}
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
	
}