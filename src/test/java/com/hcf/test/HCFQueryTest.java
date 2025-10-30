package com.hcf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.hcf.core.HCFFactory;
import com.hcf.core.HCFRepository;
import com.hcf.query.HCFQuery;
import com.hcf.query.HCFQueryExecutor;
import com.hcf.query.enums.HCFParameter;
import com.hcf.test.entities.Customer;
import com.hcf.test.entities.Order;
import com.hcf.utils.HCFUtil;

import jakarta.persistence.criteria.JoinType;

class HCFQueryTest {
	
	@BeforeEach
	void seed(TestInfo testInfo) {
		Customer ana = new Customer("Ana", 28, true, LocalDateTime.of(2025, 1, 10, 9, 0));
		ana.addOrder(new Order("Pedido A1", new BigDecimal("100.00"), LocalDateTime.of(2025, 1, 11, 10, 0)));

		Customer bia = new Customer("Bia", 35, true, LocalDateTime.of(2025, 3, 20, 14, 0));
		bia.addOrder(new Order("Pedido B1", new BigDecimal("150.00"), LocalDateTime.of(2025, 3, 21, 9, 15)));

		Customer carla = new Customer("Carla", 41, false, LocalDateTime.of(2024, 12, 31, 23, 59));

		HCFRepository<Customer> hcfRepository = new HCFRepository<>(Customer.class).manageSessionExternally(true);
		hcfRepository.saveAll(Arrays.asList(ana, bia));
		hcfRepository.save(carla);
		hcfRepository.closeIfExternallyManaged();
	}

	@AfterEach
	void cleanup() {
		new HCFRepository<>(Order.class).bulkDelete();
		new HCFRepository<>(Customer.class).bulkDelete();
	}
	
	@BeforeAll
	static void startStatistics() {
		SessionFactory factory = HCFFactory.INSTANCE.getFactory();
		factory.getStatistics().setStatisticsEnabled(true);
	}
	
	@AfterAll
	static void showStatistics() {
		SessionFactory factory = HCFFactory.INSTANCE.getFactory();
		long sessionOpenCount = factory.getStatistics().getSessionOpenCount();
		long sessionCloseCount = factory.getStatistics().getSessionCloseCount();
		HCFUtil.getLogger().info("[HCF-INFO] Total number of sessions: " + (sessionOpenCount - sessionCloseCount));
	}
	
	@Test
	void listAllCustomersWithOrders() {
	    var all = new HCFQuery<>(Customer.class)
	    		.fetch("orders", JoinType.LEFT)
	            .list();

	    assertEquals(3, all.size());
	    all.forEach(c -> c.getOrders().size());
	}

	@Test
	void listAllCustomers() {
		var all = new HCFQuery<>(Customer.class).list();
		assertEquals(3, all.size());
	}

	@Test
	void findByExactName() {
		var onlyAna = new HCFQuery<>(Customer.class)
				.where("name", HCFParameter.EQUAL, "Ana")
				.list();

		assertEquals(1, onlyAna.size());
		assertEquals("Ana", onlyAna.getFirst().getName());
	}

	@Test
	void findByIdEquals() {
		var any = new HCFQuery<>(Customer.class).orderBy("id").list();
		assertFalse(any.isEmpty());
		Long id = any.get(1).getId();

		var byId = new HCFQuery<>(Customer.class).where("id", HCFParameter.EQUAL, id).list();

		assertEquals(1, byId.size());
		assertEquals(id, byId.getFirst().getId());
	}

	@Test
	void findByNameLike() {
		var startsWithA = new HCFQuery<>(Customer.class).where("name", HCFParameter.LIKE, "A%").list();

		assertEquals(1, startsWithA.size());
		assertEquals("Ana", startsWithA.getFirst().getName());
	}

	@Test
	void orConditionOnName() {
		var anaOrBia = new HCFQuery<>(Customer.class).where("name", HCFParameter.EQUAL, "Ana")
				.or("name", HCFParameter.EQUAL, "Bia").orderBy("name").list();

		List<String> names = anaOrBia.stream().map(Customer::getName).toList();
		assertEquals(2, anaOrBia.size());
		assertEquals(List.of("Ana", "Bia"), names);
	}

	@Test
	void booleanTrueFalseFilters() {
		var actives = new HCFQuery<>(Customer.class).where("active", HCFParameter.TRUE, null).orderBy("name").list();

		assertEquals(2, actives.size());
		assertEquals(List.of("Ana", "Bia"), actives.stream().map(Customer::getName).toList());

		var inactives = new HCFQuery<>(Customer.class).where("active", HCFParameter.FALSE, null).list();

		assertEquals(1, inactives.size());
		assertEquals("Carla", inactives.getFirst().getName());
	}

	@Test
	void countAll() {
		long total = new HCFQuery<>(Customer.class).count();
		assertEquals(3L, total);
	}

	@Test
	void oneWithOrderDesc() {
		var lastByRegisteredAt = new HCFQuery<>(Customer.class).orderBy("registeredAt", false).one();

		assertNotNull(lastByRegisteredAt);
		assertEquals("Bia", lastByRegisteredAt.getName());
	}

	@Test
	void paginationOrderByIdDesc() {
		var page = new HCFQuery<>(Customer.class).orderBy("id", false).limit(2).offset(0).list();

		assertEquals(2, page.size());
		assertTrue(page.get(0).getId() > page.get(1).getId());
	}

	@Test
	void distinctNames() {
		var names = new HCFQuery<>(Customer.class).distinct("name");
		assertEquals(3, names.size());
		assertTrue(names.containsAll(List.of("Ana", "Bia", "Carla")));
	}

	@Test
	void numericComparisonsOnAge() {
		var greaterOrEqual35 = new HCFQuery<>(Customer.class).where("age", HCFParameter.GREATER_THAN_OR_EQUAL_TO, 35)
				.orderBy("age").list();

		assertEquals(2, greaterOrEqual35.size());
		assertEquals(List.of("Bia", "Carla"), greaterOrEqual35.stream().map(Customer::getName).toList());

		var lessThan35 = new HCFQuery<>(Customer.class).where("age", HCFParameter.LESS_THAN, 35).list();
		assertEquals(1, lessThan35.size());
		assertEquals("Ana", lessThan35.getFirst().getName());
	}

	@Test
	void combineAndOr() {
		var list = new HCFQuery<>(Customer.class)
				.where("name", HCFParameter.LIKE, "A%")
				.or("name", HCFParameter.LIKE, "B%")
				.and("active", HCFParameter.TRUE, null)
				.orderBy("name")
				.list();

		assertEquals(2, list.size());
		assertEquals(List.of("Ana", "Bia"), list.stream().map(Customer::getName).toList());
	}

	@Test
	void join_basic_order_with_customer() {
		var rows = new HCFQuery<>(Order.class).join("customer", JoinType.INNER).orderBy("id").listJoined();

		assertNotNull(rows);
		assertFalse(rows.isEmpty());

		for (Object[] cols : rows) {
			assertEquals(2, cols.length);
			assertInstanceOf(Order.class, cols[0]);
			assertInstanceOf(Customer.class, cols[1]);

			Order o = (Order) cols[0];
			Customer c = (Customer) cols[1];

			assertNotNull(o.getId());
			assertNotNull(c.getId());
			assertEquals(c.getId(), o.getCustomer().getId());
		}
	}

	@Test
	void join_filter_by_joined_attribute() {
		List<Order> anaOrders = new HCFQuery<>(Order.class)
				.join("customer")
				.where("customer.name", HCFParameter.EQUAL, "Ana")
				.orderBy("createdAt")
				.list();

		assertNotNull(anaOrders);
		assertFalse(anaOrders.isEmpty());

		for (Order o : anaOrders) {
			assertNotNull(o.getCustomer());
			assertEquals("Ana", o.getCustomer().getName());
		}

		boolean anyBia = anaOrders.stream()
				.anyMatch(o -> o.getCustomer() != null && "Bia".equals(o.getCustomer().getName()));
		assertFalse(anyBia);
	}

	@Test
	void join_order_by_joined_attribute() {
		List<Order> orders = new HCFQuery<>(Order.class).join("customer").orderBy("customer.name", true)
				.orderBy("id", true).list();

		assertNotNull(orders);
		assertTrue(orders.size() >= 2);

		String previous = null;
		for (Order o : orders) {
			assertNotNull(o.getCustomer());
			String current = o.getCustomer().getName();
			if (previous != null) {
				assertTrue(previous.compareTo(current) <= 0);
			}
			previous = current;
		}
	}

	@Test
	void sum_amount_filtered_by_join_attribute() {
		var sums = new HCFQuery<>(Order.class).join("customer").where("customer.name", HCFParameter.EQUAL, "Ana")
				.sum("amount");

		assertNotNull(sums);
		assertEquals(1, sums.size());
		assertEquals(new BigDecimal("100.00"), new BigDecimal(sums.getFirst().toString()));
	}

	@Test
	void distinct_customer_names_from_orders() {
		var names = new HCFQuery<>(Order.class).join("customer").distinct("customer.name");

		assertNotNull(names);
		assertEquals(2, names.size());
		assertTrue(names.containsAll(List.of("Ana", "Bia")));
	}

	@Test
	void left_join_keeps_orders_without_customer() {
		var orphan = new Order("Pedido ORFAO", new BigDecimal("77.77"), LocalDateTime.of(2025, 2, 1, 12, 0));
		new HCFRepository<>(Order.class).save(orphan);

		var rows = new HCFQuery<>(Order.class).join("customer", JoinType.LEFT).orderBy("id").listJoined();

		assertNotNull(rows);
		assertTrue(rows.size() >= 3);

		boolean foundOrphan = false;
		for (Object[] cols : rows) {
			assertEquals(2, cols.length);
			Order o = (Order) cols[0];
			Customer c = (Customer) cols[1];
			if ("Pedido ORFAO".equals(o.getDescription())) {
				assertNotNull(o.getId());
				assertEquals(null, c);
				foundOrphan = true;
			}
		}
		assertTrue(foundOrphan);
	}

	@Test
	void odd_even_predicates_on_order_id() {
		var all = new HCFQuery<>(Order.class).orderBy("id").list();
		assertTrue(all.size() >= 2);

		var evens = new HCFQuery<>(Order.class).where("id", HCFParameter.IS_EVEN, null).list();
		var odds = new HCFQuery<>(Order.class).where("id", HCFParameter.IS_ODD, null).list();

		assertTrue(!evens.isEmpty() || !odds.isEmpty());
		if (!evens.isEmpty()) {
			assertTrue(evens.stream().allMatch(o -> o.getId() % 2 == 0));
		}
		if (!odds.isEmpty()) {
			assertTrue(odds.stream().allMatch(o -> o.getId() % 2 == 1));
		}
	}

	@Test
	void date_range_on_orders() {
		var from = LocalDateTime.of(2025, 1, 1, 0, 0);
		var to = LocalDateTime.of(2025, 3, 1, 23, 59);

		var inRange = new HCFQuery<>(Order.class).where("createdAt", HCFParameter.GREATER_THAN_OR_EQUAL_TO, from)
				.and("createdAt", HCFParameter.LESS_THAN_OR_EQUAL_TO, to).orderBy("createdAt").list();

		assertNotNull(inRange);
		assertTrue(inRange.stream().allMatch(o -> !o.getCreatedAt().isBefore(from) && !o.getCreatedAt().isAfter(to)));

		var descs = inRange.stream().map(Order::getDescription).toList();
		assertTrue(descs.contains("Pedido A1"));
		assertFalse(descs.contains("Pedido B1"));
	}

	@Test
	void update_single_row_by_id() {
		var all = new HCFQuery<>(Customer.class).orderBy("id").list();
		assertEquals(3, all.size());
		Long targetId = all.get(0).getId();

		int updated = new HCFQuery<>(Customer.class).where("id", HCFParameter.EQUAL, targetId)
				.update(Map.of("name", "Ana-Updated"));

		assertEquals(1, updated);

		var reloaded = new HCFQuery<>(Customer.class).where("id", HCFParameter.EQUAL, targetId).one();

		assertNotNull(reloaded);
		assertEquals("Ana-Updated", reloaded.getName());
	}

	@Test
	void update_multiple_fields_with_and_filter() {
		int updated = new HCFQuery<>(Customer.class).where("name", HCFParameter.EQUAL, "Bia")
				.and("age", HCFParameter.GREATER_THAN_OR_EQUAL_TO, 30).update(Map.of("active", false, "age", 36));

		assertEquals(1, updated);

		var bia = new HCFQuery<>(Customer.class).where("name", HCFParameter.EQUAL, "Bia").one();

		assertNotNull(bia);
		assertEquals(36, bia.getAge());
		assertFalse(bia.getActive());
	}

	@Test
	void update_all_rows_when_no_where_clause() {
		int updated = new HCFQuery<>(Customer.class).update(Map.of("active", false));
		assertEquals(3, updated);

		var inactives = new HCFQuery<>(Customer.class).where("active", HCFParameter.FALSE, null).list();

		assertEquals(3, inactives.size());
	}

	@Test
	void delete_by_condition() {
		int deleted = new HCFQuery<>(Customer.class).where("active", HCFParameter.FALSE, null).delete();

		assertEquals(1, deleted);

		long remaining = new HCFQuery<>(Customer.class).count();
		assertEquals(2L, remaining);

		var names = new HCFQuery<>(Customer.class).orderBy("name").list().stream().map(Customer::getName).toList();
		assertEquals(List.of("Ana", "Bia"), names);
	}

	@Test
	void delete_all_when_no_where_clause() {
		int deletedOrders = new HCFQuery<>(Order.class).delete();
		int deletedCustomers = new HCFQuery<>(Customer.class).delete();

		assertEquals(2, deletedOrders);
		assertEquals(3, deletedCustomers);

		long remainingCustomers = new HCFQuery<>(Customer.class).count();
		long remainingOrders = new HCFQuery<>(Order.class).count();

		assertEquals(0L, remainingCustomers);
		assertEquals(0L, remainingOrders);
	}

	@Test
	void first_customer_by_id() {
		Customer first = new HCFQuery<>(Customer.class).orderBy("id", true).one();

		assertNotNull(first);
		var page = new HCFQuery<>(Customer.class).orderBy("id", true).limit(1).list();
		assertEquals(first.getId(), page.getFirst().getId());
	}

	@Test
	void last_customer_by_id() {
		Customer last = new HCFQuery<>(Customer.class).orderBy("id", false).one();

		assertNotNull(last);
		var page = new HCFQuery<>(Customer.class).orderBy("id", false).limit(1).list();
		assertEquals(last.getId(), page.getFirst().getId());
	}

	@Test
	void newest_order_by_createdAt() {
		Order newest = new HCFQuery<>(Order.class).orderBy("createdAt", false).one();

		assertNotNull(newest);
	}

	@Test
	void oldest_order_by_createdAt() {
		Order oldest = new HCFQuery<>(Order.class).orderBy("createdAt", true).one();

		assertNotNull(oldest);
	}

	@Test
	void newest_order_with_tiebreaker() {
		Order newest = new HCFQuery<>(Order.class).orderBy("createdAt", false).orderBy("id", false).one();

		assertNotNull(newest);
	}

	@Test
	void native_tuple_select_with_HCFSql_list() {
		var rows = HCFQueryExecutor.listNative("""
				    select c.name, count(o.id)
				    from Customer c
				    left join Orders o on o.customer_id = c.id
				    group by c.name
				    order by c.name
				""");
		assertNotNull(rows);
		assertFalse(rows.isEmpty());

		assertEquals(3, rows.size());

		Object[] r0 = rows.get(0);
		assertEquals("Ana", r0[0]);
		assertEquals(1L, ((Number) r0[1]).longValue());

		Object[] r1 = rows.get(1);
		assertEquals("Bia", r1[0]);
		assertEquals(1L, ((Number) r1[1]).longValue());

		Object[] r2 = rows.get(2);
		assertEquals("Carla", r2[0]);
		assertEquals(0L, ((Number) r2[1]).longValue());
	}

	@Test
	void native_typed_select_with_HCFSql_list_entity() {
		var list = HCFQueryExecutor.listNative("""
				    select *
				    from customer
				    where name = 'Ana'
				""", Customer.class);

		assertNotNull(list);
		assertEquals(1, list.size());
		Customer c = list.getFirst();
		assertEquals("Ana", c.getName());
		assertTrue(c.getActive());
	}

	@Test
	void native_execute_update_sets_active_false() {
		int updated = HCFQueryExecutor.executeNative("""
				    update customer
				    set active = false
				    where name = 'Bia'
				""");
		assertTrue(updated >= 1);

		var bia = new HCFQuery<>(Customer.class).where("name", HCFParameter.EQUAL, "Bia").one();

		assertNotNull(bia);
		assertFalse(bia.getActive(), "Bia should be inactive after native update");
	}

	@Test
	void native_execute_insert_and_delete_customer() {
		int inserted = HCFQueryExecutor.executeNative("""
				    insert into Customer (name, age, active, registeredAt)
				    values ('Dora', 30, true, TIMESTAMP '2025-04-01 10:00:00')
				""");
		assertEquals(1, inserted);

		var dora = new HCFQuery<>(Customer.class).where("name", HCFParameter.EQUAL, "Dora").one();
		assertNotNull(dora);
		assertEquals(30, dora.getAge());

		int deleted = HCFQueryExecutor.executeNative("""
				    delete from Customer
				    where name = 'Dora'
				""");
		assertEquals(1, deleted);

		var none = new HCFQuery<>(Customer.class).where("name", HCFParameter.EQUAL, "Dora").list();
		assertTrue(none.isEmpty());
	}

	@Test
	void hcfquery_nativeList_typed_entity() {
		var list = new HCFQuery<>(Customer.class).nativeList("""
				    select *
				    from customer
				    where active = true
				    order by name
				""");

		assertNotNull(list);
		assertFalse(list.isEmpty());
		var names = list.stream().map(Customer::getName).toList();
		assertTrue(names.contains("Ana"));
	}
	
	@Test
	void native_execute_insert_and_delete_customer_named_params() {
	    int inserted = HCFQueryExecutor.executeNative("""
	        insert into customer (name, age, active, registeredAt)
	        values (:name, :age, :active, :registeredAt)
	    """, Map.of(
	        "name", "Dora",
	        "age", 30,
	        "active", true,
	        "registeredAt", LocalDateTime.of(2025, 4, 1, 10, 0)
	    ));
	    assertEquals(1, inserted);

	    var dora = new HCFQuery<>(Customer.class)
	            .where("name", HCFParameter.EQUAL, "Dora")
	            .one();
	    assertNotNull(dora);
	    assertEquals(30, dora.getAge());
	    assertTrue(dora.getActive());

	    int deleted = HCFQueryExecutor.executeNative("""
	        delete from customer
	        where name = :name
	    """, Map.of("name", "Dora"));
	    assertEquals(1, deleted);

	    var none = new HCFQuery<>(Customer.class)
	            .where("name", HCFParameter.EQUAL, "Dora")
	            .list();
	    assertTrue(none.isEmpty());
	}

	@Test
	void native_tuple_select_with_HCFSql_list_named_params() {
	    var rows = HCFQueryExecutor.listNative("""
	        select c.name, count(o.id)
	        from customer c
	        left join orders o on o.customer_id = c.id
	        where c.name in (:names)
	        group by c.name
	        order by c.name
	    """, Map.of("names", List.of("Ana", "Bia", "Carla")));

	    assertNotNull(rows);
	    assertFalse(rows.isEmpty());
	    assertEquals(3, rows.size());

	    Object[] r0 = rows.get(0);
	    assertEquals("Ana", r0[0]);
	    assertEquals(1L, ((Number) r0[1]).longValue());

	    Object[] r1 = rows.get(1);
	    assertEquals("Bia", r1[0]);
	    assertEquals(1L, ((Number) r1[1]).longValue());

	    Object[] r2 = rows.get(2);
	    assertEquals("Carla", r2[0]);
	    assertEquals(0L, ((Number) r2[1]).longValue());
	}

	@Test
	void native_execute_update_named_params() {
	    var carla = new HCFQuery<>(Customer.class)
	            .where("name", HCFParameter.EQUAL, "Carla")
	            .one();
	    assertNotNull(carla);
	    assertFalse(carla.getActive());

	    int updated = HCFQueryExecutor.executeNative("""
	        update customer
	        set active = :active
	        where name = :name
	    """, Map.of(
	        "active", true,
	        "name", "Carla"
	    ));
	    assertEquals(1, updated);

	    var carlaAfter = new HCFQuery<>(Customer.class)
	            .where("name", HCFParameter.EQUAL, "Carla")
	            .one();
	    assertNotNull(carlaAfter);
	    assertTrue(carlaAfter.getActive());
	}

}