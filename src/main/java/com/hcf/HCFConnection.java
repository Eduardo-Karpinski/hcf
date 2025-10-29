package com.hcf;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import com.hcf.utils.HCFLog;
import com.hcf.utils.HCFPredicateUtil;
import com.hcf.utils.HCFUtil;

import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public final class HCFConnection<T> {

	private Transaction transaction;
    private final Session session;
    private final Class<T> persistentClass;

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

    public int massiveUpdate(Map<String, Object> values, Object... parameters) {
    	return massiveUpdate(values, HCFUtil.varargsToSearch(parameters));
    }
    
    public int massiveDelete(List<HCFSearch> hcfSearches) {
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaDelete<T> cd = cb.createCriteriaDelete(persistentClass);
            Root<T> root = cd.from(persistentClass);

            Predicate where = HCFPredicateUtil.buildForRoot(cb, root, hcfSearches);
            if (where != null) cd.where(where);

            return session.createMutationQuery(cd).executeUpdate();
        } catch (Exception e) {
            HCFLog.showError(e);
            if (transaction != null) transaction.rollback();
            return -1;
        } finally {
            close();
        }
    }

    public int massiveUpdate(Map<String, Object> values, List<HCFSearch> hcfSearches) {
        try {
            transaction = session.beginTransaction();
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaUpdate<T> cu = cb.createCriteriaUpdate(persistentClass);
            Root<T> root = cu.from(persistentClass);

            values.forEach(cu::set);

            Predicate where = HCFPredicateUtil.buildForRoot(cb, root, hcfSearches);
            if (where != null) cu.where(where);

            return session.createMutationQuery(cu).executeUpdate();
        } catch (Exception e) {
            HCFLog.showError(e);
            if (transaction != null) transaction.rollback();
            return -1;
        } finally {
            close();
        }
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

    private void close() {
        if (transaction != null && transaction.getStatus().equals(TransactionStatus.ACTIVE)) {
            try {
                transaction.commit();
            } catch (Exception e) {
                HCFLog.showError(e);
            }
        }
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                HCFLog.showError(e);
            }
        }
    }

}