package br.com.hcf;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

import br.com.hcf.annotations.HCFRelationship;
import br.com.hcf.enums.HCFOperator;
import br.com.hcf.enums.HCFParameter;
import br.com.hcf.utils.HCFUtil;

public final class HCFConnection<T, E> {
	
	private Class<T> classe = null;
	private Session session = null;
	private List<Predicate> predicates = new ArrayList<>();

	public HCFConnection(Class<T> persistentClass) {
		classe = Optional.ofNullable(persistentClass).orElseThrow(() -> new NullPointerException("PersistentClass is null"));
		session = HCFactory.getFactory().openSession();
	}
	
	public HCFConnection(Class<T> persistentClass, Connection connection) {
		classe = Optional.ofNullable(persistentClass).orElseThrow(() -> new NullPointerException("PersistentClass is null"));
		session = HCFactory.getFactory().withOptions().connection(Optional.ofNullable(connection).orElseThrow(() -> new NullPointerException("Connection is null"))).openSession();
	}
	
	public HCFConnection(Class<T> persistentClass, SessionFactory sessionFactory) {
		classe = Optional.ofNullable(persistentClass).orElseThrow(() -> new NullPointerException("PersistentClass is null"));
		session = Optional.ofNullable(sessionFactory).orElseThrow(() -> new NullPointerException("SessionFactory is null")).openSession();
	}
	
	public void save(T entidade) {
		save(Collections.singletonList(entidade), false);
	}
	
	public void save(List<T> entidade, Boolean commitInError) {
		Transaction tx = null;
		AtomicInteger cont = new AtomicInteger(0);
		try {
			tx = session.beginTransaction();
			entidade.stream().forEach(e -> {
				session.saveOrUpdate(e);
				if (cont.incrementAndGet() % 20 == 0) {
					session.flush();
			        session.clear();
			    }
			});
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				if (commitInError) {
					try {
						tx.commit();
					} catch (Exception e2) {
						e.printStackTrace();
					}
				} else {
					tx.rollback();
				}
			}
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	public void delete(T entidade) {
		delete(Collections.singletonList(entidade), false);
	}
	
	public void delete(List<T> entidade, Boolean commitInError) {
		Transaction tx = null;
		AtomicInteger cont = new AtomicInteger(0);
		try {
			tx = session.beginTransaction();
			entidade.stream().forEach(e -> {
				session.delete(e);
				if (cont.incrementAndGet() % 20 == 0) {
					session.flush();
			        session.clear();
			        cont.set(0);
			    }
			});
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				if (commitInError) {
					try {
						tx.commit();
					} catch (Exception e2) {
						e.printStackTrace();
					}
				} else {
					tx.rollback();
				}
			}
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	public List<T> all() {
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteria = builder.createQuery(classe);
			criteria.from(classe);
			List<T> resultList = session.createQuery(criteria).getResultList();
			resultList.forEach(t -> getRelationshipByHCF(session, t));
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	public List<T> getRelations(Class<?> father, String column, Object id) {
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteria = builder.createQuery(classe);
			Root<?> r = criteria.from(father);
			Join<?, T> join = r.join(column);
			criteria.select(join).where(builder.equal(r.get(HCFUtil.getId(father)), id));
			TypedQuery<T> query = session.createQuery(criteria);
			List<T> resultList = query.getResultList();
			resultList.forEach(t -> getRelationshipByHCF(session, t));
			return resultList;
		} catch (NoResultException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	public List<T> getByInvertedRelation(String column, String field, Object id) {
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteria = builder.createQuery(classe);
			Root<T> r = criteria.from(classe);
			criteria.where(builder.equal(r.join(column).get(field), id));
			TypedQuery<T> query = session.createQuery(criteria);
			List<T> resultList = query.getResultList();
			resultList.forEach(t -> getRelationshipByHCF(session, t));
			return resultList;
		} catch (NoResultException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	public T getById(Object id) {
		Optional.ofNullable(id).orElseThrow(() -> new NullPointerException("Id is null"));
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteria = builder.createQuery(classe);
			Root<T> r = criteria.from(classe);
			criteria.select(r).where(builder.equal(r.get(HCFUtil.getId(classe)), id));
			TypedQuery<T> query = session.createQuery(criteria);
			T singleResult = query.getSingleResult();
			getRelationshipByHCF(session, singleResult);
			return singleResult;
		} catch (NoResultException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public Object bringAddition(List<String> Fields, E... parameters) {
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteria = builder.createQuery(Object[].class);
			Root<T> r = criteria.from(classe);
			Selection<?>[] espressoes = new Selection<?>[Fields.size()];
			applyPredicate(builder, r, parameters);
			for (int i = 0; i < Fields.size(); i++) {
				espressoes[i] = builder.sum(r.get(Fields.get(i)));
			}
			criteria.multiselect(espressoes).where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Object[]> query = session.createQuery(criteria);
			return query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> bringAdditionByGroup(String group, List<String> Fields, E... parameters) {
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Object[]> criteria = builder.createQuery(Object[].class);
			Root<T> r = criteria.from(classe);
			Selection<?>[] espressoes = new Selection<?>[Fields.size()];
			applyPredicate(builder, r, parameters);
			for (int i = 0; i < Fields.size(); i++) {
				espressoes[i] = builder.sum(r.get(Fields.get(i)));
			}
			criteria.groupBy(r.get(group));
			criteria.multiselect(espressoes).where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Object[]> query = session.createQuery(criteria);
			return query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	public T getFirstOrLast(HCFOrder ordenador) {
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteria = builder.createQuery(classe);
			Subquery<Long> subquery = criteria.subquery(Long.class);
			Root<T> r = subquery.from(classe);
			Path<Long> subPath = r.get(ordenador.getField());
			r = criteria.from(classe);
			Path<T> path = r.get(ordenador.getField());
			if (ordenador.getAsc()) {
				subquery.select(builder.min(subPath));
			} else {
				subquery.select(builder.max(subPath));
			}
			criteria.select(r).where(builder.equal(path, subquery));
			TypedQuery<T> query = session.createQuery(criteria);
			T singleResult = query.getSingleResult();
			getRelationshipByHCF(session, singleResult);
			return singleResult;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	public List<T> getByOrders(List<HCFOrder> orders) {
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteria = builder.createQuery(classe);
			Root<T> r = criteria.from(classe);
			order(orders, builder, criteria, r);
			TypedQuery<T> query = limitResults(orders, criteria);
			List<T> resultList = query.getResultList();
			resultList.forEach(t -> getRelationshipByHCF(session, t));
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
		
	
	public static void sendSQL(String sql) {
		Transaction tx = null;
		Session session = null;
		try {
			session = HCFactory.getFactory().openSession();
			tx = session.beginTransaction();
			session.createNativeQuery(sql).executeUpdate();
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	public List<T> getObjectBySQL(String sql) {
		Session session = null;
		try {
			session = HCFactory.getFactory().openSession();
			NativeQuery<T> nativeQuery = session.createNativeQuery(sql,classe);
			return nativeQuery.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	public static List<?> getElementsBySQL(String sql) {
		Session session = null;
		try {
			session = HCFactory.getFactory().openSession();
			return session.createNativeQuery(sql).getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	public Long count() {
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
			Root<T> r = criteria.from(classe);
			criteria.select(builder.count(r));
			TypedQuery<Long> query = session.createQuery(criteria);
			return query.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	public Long countDistinct() {
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
			Root<T> r = criteria.from(classe);
			criteria.select(builder.countDistinct(r));
			TypedQuery<Long> query = session.createQuery(criteria);
			return query.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public T searchWithOneResult(List<HCFOrder> orders, E... parameters) {
		
		if (parameters.length % 4 != 0) throw new IllegalArgumentException("Parameters is not a multiple of 4.");
		
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteria = builder.createQuery(classe);
			Root<T> r = criteria.from(classe);
			order(orders, builder, criteria, r);
			applyPredicate(builder, r, parameters);
			criteria.select(r).where(predicates.toArray(new Predicate[] {}));
			TypedQuery<T> query = limitResults(orders, criteria);
			T singleResult = query.getSingleResult();
			getRelationshipByHCF(session, singleResult);
			return singleResult;
		} catch (NoResultException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> search(List<HCFOrder> orders, E... parameters) {
		
		if (parameters.length % 4 != 0) throw new IllegalArgumentException("Parameters is not a multiple of 4.");
		
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteria = builder.createQuery(classe);
			Root<T> r = criteria.from(classe);
			order(orders, builder, criteria, r);
			applyPredicate(builder, r, parameters);
			criteria.select(r).where(predicates.toArray(new Predicate[] {}));
			TypedQuery<T> query = limitResults(orders, criteria);
			List<T> resultList = query.getResultList();
			resultList.forEach(t -> getRelationshipByHCF(session, t));
			return resultList;
		} catch (NoResultException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	public List<T> search(List<HCFOrder> orders, List<HCFSearch> parameters) {
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<T> criteria = builder.createQuery(classe);
			Root<T> r = criteria.from(classe);
			order(orders, builder, criteria, r);
			applyPredicate(builder, r, parameters);
			criteria.select(r).where(predicates.toArray(new Predicate[] {}));
			TypedQuery<T> query = limitResults(orders, criteria);
			List<T> resultList = query.getResultList();
			resultList.forEach(t -> getRelationshipByHCF(session, t));
			return resultList;
		} catch (NoResultException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	private void getRelationshipByHCF(Session session, T father) {
		try {
			Class<?> fatherClass = father.getClass();
			if(fatherClass.getAnnotation(HCFRelationship.class) != null) {
				Arrays.asList(fatherClass.getDeclaredFields()).stream()
				.filter(f -> Arrays.asList(f.getAnnotations()).stream().anyMatch(a -> a instanceof OneToMany
						|| a instanceof ManyToMany || a instanceof ManyToOne))
				.map(Field::getName)
				.forEach(table -> {
					try {
						String nameField = HCFUtil.getId(fatherClass);
						Field idField = fatherClass.getDeclaredField(nameField);
						
						idField.setAccessible(true);
						Object id = idField.get(father);
						idField.setAccessible(false);
						
						Field lista = fatherClass.getDeclaredField(table);
			            lista.setAccessible(true);
			            
			            CriteriaBuilder builder = session.getCriteriaBuilder();
						CriteriaQuery<T> criteria = builder.createQuery(classe);
						Root<?> r = criteria.from(fatherClass);
						Join<?, T> join = r.join(table);
						criteria.select(join).where(builder.equal(r.get(nameField), id));
						TypedQuery<T> query = session.createQuery(criteria);
						
			            lista.set(father, query.getResultList());
			            lista.setAccessible(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void applyPredicate(CriteriaBuilder builder, Root<T> r, List<HCFSearch> parameters) {
		
		parameters.forEach(i ->{
			
			Path path = r.get(i.getField());
			
			switch (i.getParameter()) {  
		    case EQUAL:
		    	predicates.add(builder.equal(path, (Comparable) i.getValue()));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		        break;
		    case NOTEQUAL:
		    	predicates.add(builder.notEqual(path, (Comparable) i.getValue()));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		        break;
		    case LIKE:
		    	predicates.add(builder.like(path, i.getValue() + "%"));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		        break;
		    case NOTLIKE:
		    	predicates.add(builder.notLike(path, i.getValue() + "%"));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		        break;
		    case LESSTHAN:
		    	predicates.add(builder.lessThan(path, (Comparable) i.getValue()));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		    	break;
		    case GREATERTHAN:
		    	predicates.add(builder.greaterThan(path, (Comparable) i.getValue()));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		        break;
		    case LESSTHANOREQUALTO: 
		    	predicates.add(builder.lessThanOrEqualTo(path, (Comparable) i.getValue()));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		        break;
		    case GREATERTHANOREQUALTO:
		    	predicates.add(builder.greaterThanOrEqualTo(path, (Comparable) i.getValue()));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		        break;
		    case ISNULL:
		    	predicates.add(builder.isNull(path));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		        break;
		    case ISNOTNULL:
		    	predicates.add(builder.isNotNull(path));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		    	break;
		    case TRUE:
		    	predicates.add(builder.isTrue(path));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		    	break;
		    case FALSE:
		    	predicates.add(builder.isFalse(path));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		    	break;
		    default:
		    	predicates.add(builder.equal(path, (Comparable) i.getValue()));
		    	applyOperator(builder, (HCFOperator) i.getOperator());
		        break;
			}
		});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void applyPredicate(CriteriaBuilder builder, Root<T> r, E... parameters) {
		IntStream.iterate(3, i -> i + 4).limit(parameters.length / 4).forEach(i ->{
			
			Path path = r.get(parameters[i - 3].toString());
			
			switch ((HCFParameter) parameters[i-1]) {  
		    case EQUAL:
		    	predicates.add(builder.equal(path, (Comparable) parameters[i - 2]));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		        break;
		    case NOTEQUAL:
		    	predicates.add(builder.notEqual(path, (Comparable) parameters[i - 2]));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		        break;
		    case LIKE:
		    	predicates.add(builder.like(path, parameters[i - 2] + "%"));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		        break;
		    case NOTLIKE:
		    	predicates.add(builder.notLike(path, parameters[i - 2] + "%"));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		        break;
		    case LESSTHAN:
		    	predicates.add(builder.lessThan(path, (Comparable) parameters[i - 2]));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		    	break;
		    case GREATERTHAN:
		    	predicates.add(builder.greaterThan(path, (Comparable) parameters[i - 2]));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		        break;
		    case LESSTHANOREQUALTO: 
		    	predicates.add(builder.lessThanOrEqualTo(path, (Comparable) parameters[i - 2]));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		        break;
		    case GREATERTHANOREQUALTO:
		    	predicates.add(builder.greaterThanOrEqualTo(path, (Comparable) parameters[i - 2]));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		        break;
		    case ISNULL:
		    	predicates.add(builder.isNull(path));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		        break;
		    case ISNOTNULL:
		    	predicates.add(builder.isNotNull(path));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		    	break;
		    case TRUE:
		    	predicates.add(builder.isTrue(path));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		    	break;
		    case FALSE:
		    	predicates.add(builder.isFalse(path));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		    	break;
		    default:
		    	predicates.add(builder.equal(path, (Comparable) parameters[i - 2]));
		    	applyOperator(builder, (HCFOperator) parameters[i]);
		        break;
			}
		});
	}
	
	private void applyOperator(CriteriaBuilder builder, HCFOperator conjuncao) {
		switch (conjuncao) {
		case OR:
			predicates.add(builder.or(predicates.get(predicates.size() -2), predicates.get(predicates.size() - 1)));
			predicates.remove(predicates.size() - 3);
			predicates.remove(predicates.size() - 2);
			break;
		case AND:
			predicates.add(builder.and(predicates.get(predicates.size() -2), predicates.get(predicates.size() - 1)));
			predicates.remove(predicates.size() - 3);
			predicates.remove(predicates.size() - 2);
			break;
		case DEFAULT:
			break;
		}
	}

	private void order(List<HCFOrder> orders, CriteriaBuilder builder, CriteriaQuery<T> criteria, Root<T> r) {
		if (orders == null) return;
		List<Order> persistenceOrders = new ArrayList<>();
		orders.stream().filter(o -> o.getAsc() != null && o.getField() != null)
		.forEach(o -> {
			if (o.getAsc()) {
				persistenceOrders.add(builder.asc(r.get(o.getField())));
			} else {
				persistenceOrders.add(builder.desc(r.get(o.getField())));
			}
		});
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
	
}
