package com.hcf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hcf.HCFConnection;
import com.hcf.HCFOrder;
import com.hcf.HCFSearch;
import com.hcf.enums.HCFOperator;
import com.hcf.enums.HCFParameter;
import com.hcf.test.entities.TestEntity;

@DisplayName("HCFSearchTest")
class HCFSearchTest {
	
	private static List<TestEntity> entities;
	
	@AfterAll
	static void removeEntities() {
		new HCFConnection<>(TestEntity.class).delete(entities, false);
	}
	
	@BeforeAll
	static void insertEntities() {
		TestEntity entity1 = new TestEntity(null, "Test 1", 21, new BigDecimal(1234.56), LocalDateTime.now(), true);
		TestEntity entity2 = new TestEntity(null, "Test 2", 22, new BigDecimal(1345.67), LocalDateTime.now().plusDays(1), false);
		TestEntity entity3 = new TestEntity(null, "Test 3", 23, new BigDecimal(1456.78), LocalDateTime.now().plusDays(2), true);
		TestEntity entity4 = new TestEntity(null, "Test 4", 24, new BigDecimal(1567.89), LocalDateTime.now().plusDays(3), false);
		TestEntity entity5 = new TestEntity(null, "Test 5", 25, new BigDecimal(1678.90), LocalDateTime.now().plusDays(4), false);
		TestEntity entity6 = new TestEntity(null, "Test 6", 26, new BigDecimal(1789.01), LocalDateTime.now().plusDays(5), false);
		TestEntity entity7 = new TestEntity(null, "Test 7", 27, new BigDecimal(1890.12), LocalDateTime.now().plusDays(6), false);
		TestEntity entity8 = new TestEntity(null, "Test 8", 28, new BigDecimal(1901.23), LocalDateTime.now().plusDays(7), false);
		TestEntity entity9 = new TestEntity(null, "Test 9", 29, new BigDecimal(2012.34), LocalDateTime.now().plusDays(8), false);
		TestEntity entity10 = new TestEntity(null, "Test 10", 30, new BigDecimal(2123.45), LocalDateTime.now().plusDays(9), false);
		TestEntity entity11 = new TestEntity(null, "Test 11", 31, new BigDecimal(2234.56), LocalDateTime.now().plusDays(10), false);
		TestEntity entity12 = new TestEntity(null, "Test 12", 32, new BigDecimal(2345.67), LocalDateTime.now().plusDays(11), true);
		TestEntity entity13 = new TestEntity(null, "Test 13", 33, new BigDecimal(2456.78), LocalDateTime.now().plusDays(12), false);
		TestEntity entity14 = new TestEntity(null, "Test 14", 34, new BigDecimal(2567.89), LocalDateTime.now().plusDays(13), false);
		TestEntity entity15 = new TestEntity(null, "Test 15", 35, new BigDecimal(2678.90), LocalDateTime.now().plusDays(14), false);
		TestEntity entity16 = new TestEntity(null, "Test 16", 36, new BigDecimal(2789.01), LocalDateTime.now().plusDays(15), false);
		TestEntity entity17 = new TestEntity(null, "Test 17", 37, new BigDecimal(2890.12), LocalDateTime.now().plusDays(16), false);
		TestEntity entity18 = new TestEntity(null, "Test 18", 38, new BigDecimal(2901.23), LocalDateTime.now().plusDays(17), true);
		TestEntity entity19 = new TestEntity(null, "Test 19", 39, new BigDecimal(3012.34), LocalDateTime.now().plusDays(18), false);
		TestEntity entity20 = new TestEntity(null, "Test 20", 40, new BigDecimal(3123.45), LocalDateTime.now().plusDays(19), false);
		TestEntity entity21 = new TestEntity(null, "", null, new BigDecimal(3123.56), LocalDateTime.now().plusDays(20), false);

		entities = Arrays.asList(entity3, entity2, entity1, entity4, entity5,
				entity6, entity7, entity8, entity9, entity10, entity11,
				entity12, entity13, entity14, entity15, entity16, entity17,
				entity18, entity19, entity20, entity21);
		
		new HCFConnection<>(TestEntity.class).save(entities, false);
	}
	
	@Test
	void testSearchNotTrue() {
		HCFSearch search = new HCFSearch("isAdmin", null, HCFParameter.FALSE, HCFOperator.NONE);
		
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, Arrays.asList(search));
	
		assertEquals(17, entities.size());
	}
	
	@Test
	void testSearchTrue() {
		HCFSearch search = new HCFSearch("isAdmin", null, HCFParameter.TRUE, HCFOperator.NONE);
		
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, Arrays.asList(search));
	
		assertEquals(4, entities.size());
	}
	
	@Test
	void testSearchWithOneResult() {
		
		TestEntity entity1 = new HCFConnection<>(TestEntity.class).searchWithOneResult(null, 
				"id", entities.get(0).getId(), HCFParameter.EQUAL, HCFOperator.NONE);
		assertNotNull(entity1);
		
		TestEntity entity2 = new HCFConnection<>(TestEntity.class).searchWithOneResult(null, 
				Arrays.asList(new HCFSearch("id", entities.get(1).getId(), HCFParameter.EQUAL, HCFOperator.NONE)));
		
		assertNotNull(entity2);
	}
	
	@Test
	void testSearchLike() {
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"name", "%3%", HCFParameter.LIKE, HCFOperator.NONE);
		
		assertEquals(2, entities.size());
	}
	
	@Test
	void testSearchNotLike() {
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"name", "%3%", HCFParameter.NOTLIKE, HCFOperator.NONE);
		
		assertEquals(19, entities.size());
	}
	
	@Test
	void testSearchEqual() {
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"name", "Test 1", HCFParameter.EQUAL, HCFOperator.NONE);
		
		assertEquals(1, entities.size());
	}
	
	@Test
	void testSearchNotEqual() {
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"name", "Test 1", HCFParameter.NOTEQUAL, HCFOperator.NONE);
		
		assertEquals(20, entities.size());
	}
	
	@Test
	void testSearchEmpty() {
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"name", null, HCFParameter.EMPTY, HCFOperator.NONE);
		
		assertEquals(1, entities.size());
	}
	
	@Test
	void testSearchNotEmpty() {
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"name", null, HCFParameter.NOTEMPTY, HCFOperator.NONE);
		
		assertEquals(20, entities.size());
	}
	
	@Test
	void testSearchIsNull() {
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"age", null, HCFParameter.ISNULL, HCFOperator.NONE);
		
		assertEquals(1, entities.size());
	}
	
	@Test
	void testSearchIsNotNull() {
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"age", null, HCFParameter.ISNOTNULL, HCFOperator.NONE);
		
		assertEquals(20, entities.size());
	}
	
	@Test
	void testSearchLessThan() {
		
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"age", 30, HCFParameter.LESSTHAN, HCFOperator.NONE);
		
		assertEquals(9, entities.size());
	}
	
	@Test
	void testSearchLessThanOrEqualTo() {
		
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"age", 30, HCFParameter.LESSTHANOREQUALTO, HCFOperator.NONE);
		
		assertEquals(10, entities.size());
	}
	
	@Test
	void testSearchGreaterThan() {
		
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"age", 30, HCFParameter.GREATERTHAN, HCFOperator.NONE);
		
		assertEquals(10, entities.size());
	}
	
	@Test
	void testSearchGreaterThanOrEqualTo() {
		
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"age", 30, HCFParameter.GREATERTHANOREQUALTO, HCFOperator.NONE);
		
		assertEquals(11, entities.size());
	}
	
	@Test
	void testSearchWithOperator1() {
		
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"age", 30, HCFParameter.EQUAL, HCFOperator.NONE,
				"age", 32, HCFParameter.EQUAL, HCFOperator.OR,
				"age", 23, HCFParameter.LESSTHANOREQUALTO, HCFOperator.OR);
		
		assertEquals(5, entities.size());
		
	}
	
	@Test
	void testSearchWithOperator2() {
		
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"name", "%1%", HCFParameter.LIKE, HCFOperator.AND,
				"age", 25, HCFParameter.GREATERTHANOREQUALTO, HCFOperator.AND);
		
		assertEquals(10, entities.size());
	}
	
	@Test
	void testSearchWithOrder() {
		
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(
				Arrays.asList(new HCFOrder(false, "id", null, 0)),
				"age", null, HCFParameter.ISNOTNULL, HCFOperator.NONE);
		
		assertEquals(20, entities.size());
		assertTrue(entities.get(0).getId() > entities.get(entities.size() - 1).getAge());
		
	}
	
}