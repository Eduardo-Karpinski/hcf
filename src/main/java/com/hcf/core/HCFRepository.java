package com.hcf.core;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import com.hcf.query.HCFSearch;
import com.hcf.utils.HCFLog;
import com.hcf.utils.HCFPredicateUtil;
import com.hcf.utils.HCFUtil;

import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public final class HCFRepository<T> {

	private Transaction transaction;
	private boolean manageSessionExternally = false;
	private boolean manageTransactionExternally = false;

	private final Session session;
	private final Class<T> entityClass;

	public HCFRepository(Class<T> entityClass) {
		this.entityClass = Objects.requireNonNull(entityClass, "EntityClass is null");
		this.session = HCFFactory.INSTANCE.getFactory().openSession();
	}

	public HCFRepository(Class<T> entityClass, Connection connection) {
		this.entityClass = Objects.requireNonNull(entityClass, "EntityClass is null");
		this.session = HCFFactory.INSTANCE.getFactory()
				.withOptions()
				.connection(Objects.requireNonNull(connection, "Connection is null"))
				.openSession();
	}

	public HCFRepository(Class<T> entityClass, SessionFactory sessionFactory) {
		this.entityClass = Objects.requireNonNull(entityClass, "EntityClass is null");
		this.session = Objects.requireNonNull(sessionFactory, "SessionFactory is null").openSession();
	}

	public HCFRepository(Class<T> entityClass, Session session) {
		this.entityClass = Objects.requireNonNull(entityClass, "EntityClass is null");
		this.session = Objects.requireNonNull(session, "Session is null");
		this.manageSessionExternally = true;
	}

	public HCFRepository<T> manageSessionExternally(boolean value) {
		this.manageSessionExternally = value;
		return this;
	}

	public HCFRepository<T> manageTransactionExternally(boolean value) {
		this.manageTransactionExternally = value;
		return this;
	}

	public void save(T entity) {
		saveAll(Collections.singletonList(entity));
	}

	public void saveAll(List<T> entities) {
		persist(entities, true);
	}

	public void delete(T entity) {
		deleteAll(Collections.singletonList(entity));
	}

	public void deleteAll(List<T> entities) {
		persist(entities, false);
	}

	public int bulkDelete(Object... parameters) {
		return bulkDelete(HCFUtil.varargsToSearch(parameters));
	}

	public int bulkUpdate(Map<String, Object> values, Object... parameters) {
		return bulkUpdate(values, HCFUtil.varargsToSearch(parameters));
	}

	public int bulkDelete(List<HCFSearch> hcfSearches) {
		try {
			beginIfNeeded();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaDelete<T> cd = cb.createCriteriaDelete(entityClass);
			Root<T> root = cd.from(entityClass);

			Predicate where = HCFPredicateUtil.buildForRoot(cb, root, hcfSearches);
			if (where != null) {
				cd.where(where);
			}

			int rows = session.createMutationQuery(cd).executeUpdate();

			commitIfNeeded();

			return rows;
		} catch (Exception e) {
			rollbackIfNeeded();
			HCFLog.showError(e);
			return -1;
		} finally {
			close();
		}
	}

	public int bulkUpdate(Map<String, Object> values, List<HCFSearch> hcfSearches) {
		try {
			beginIfNeeded();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaUpdate<T> cu = cb.createCriteriaUpdate(entityClass);
			Root<T> root = cu.from(entityClass);

			values.forEach(cu::set);

			Predicate where = HCFPredicateUtil.buildForRoot(cb, root, hcfSearches);
			if (where != null) {
				cu.where(where);
			}

			int rows = session.createMutationQuery(cu).executeUpdate();

			commitIfNeeded();

			return rows;
		} catch (Exception e) {
			rollbackIfNeeded();
			HCFLog.showError(e);
			return -1;
		} finally {
			close();
		}
	}

	private void persist(List<T> entities, boolean isSaveOrUpdate) {
		try {
			beginIfNeeded();
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
					session.remove(session.contains(entity) ? entity : session.merge(entity));
				}
			});
			commitIfNeeded();
		} catch (Exception e) {
			rollbackIfNeeded();
			HCFLog.showError(e);
			throw e;
		} finally {
			close();
		}
	}
	
	public void closeIfExternallyManaged() {
        if (!manageSessionExternally) return;
        try {
            if (!manageTransactionExternally && session.getTransaction() != null && session.getTransaction().isActive()) {
                try {
                    session.getTransaction().commit();
                } catch (Exception e) {
                    HCFLog.showError(e);
                    try {
                        session.getTransaction().rollback();
                    } catch (Exception ex) {
                        HCFLog.showError(ex);
                    }
                }
            }
        } catch (Exception e) {
            HCFLog.showError(e);
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

	private void close() {
		if (!manageTransactionExternally) {
			if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
				try {
					transaction.commit();
				} catch (Exception e) {
					HCFLog.showError(e);
				} finally {
					transaction = null;
				}
			}
		}

		if (!manageSessionExternally) {
			if (session != null && session.isOpen()) {
				try {
					session.close();
				} catch (Exception e) {
					HCFLog.showError(e);
				}
			}
		}
	}

	private void beginIfNeeded() {
		if (manageTransactionExternally)
			return;
		if (session.getTransaction() == null || !session.getTransaction().isActive()) {
			transaction = session.beginTransaction();
		}
	}

	private void commitIfNeeded() {
		if (manageTransactionExternally)
			return;
		if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
			try {
				transaction.commit();
			} catch (Exception e) {
				HCFLog.showError(e);
			} finally {
				transaction = null;
			}
		}
	}

	private void rollbackIfNeeded() {
		if (manageTransactionExternally)
			return;
		if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
			try {
				transaction.rollback();
			} catch (Exception e) {
				HCFLog.showError(e);
			} finally {
				transaction = null;
			}
		}
	}

}