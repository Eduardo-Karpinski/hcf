package com.hcf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.hcf.HCFConnection;
import br.com.hcf.HCFSearch;
import br.com.hcf.enums.HCFOperator;
import br.com.hcf.enums.HCFParameter;

@DisplayName("HCFSearchTest")
class HCFSearchTest {
	
//	TRUE(),
//	LIKE(),
//	FALSE(),
//	EQUAL(), ok
//	EMPTY(),
//	ISNULL(),
//	NOTLIKE(),
//	GROUPBY(),
//	NOTEQUAL(),
//	NOTEMPTY(),
//	LESSTHAN(),
//	ISNOTNULL(),
//	GREATERTHAN(),
//	LESSTHANOREQUALTO(),
//	GREATERTHANOREQUALTO();
	
	private static List<TestEntity> entities;
	
	@AfterAll
	static void removeEntities() {
		new HCFConnection<>(TestEntity.class).delete(entities, false);
	}
	
	@BeforeAll
	static void insertEntities() {
		TestEntity entity1 = new TestEntity(null, "Test 1", 21, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity2 = new TestEntity(null, "Test 2", 22, new BigDecimal(1345.67), LocalDateTime.now().plusDays(1));
		TestEntity entity3 = new TestEntity(null, "Test 3", 23, new BigDecimal(1456.78), LocalDateTime.now().plusDays(2));
		TestEntity entity4 = new TestEntity(null, "Test 4", 24, new BigDecimal(1567.89), LocalDateTime.now().plusDays(3));
		TestEntity entity5 = new TestEntity(null, "Test 5", 25, new BigDecimal(1678.90), LocalDateTime.now().plusDays(4));
		TestEntity entity6 = new TestEntity(null, "Test 6", 26, new BigDecimal(1789.01), LocalDateTime.now().plusDays(5));
		TestEntity entity7 = new TestEntity(null, "Test 7", 27, new BigDecimal(1890.12), LocalDateTime.now().plusDays(6));
		TestEntity entity8 = new TestEntity(null, "Test 8", 28, new BigDecimal(1901.23), LocalDateTime.now().plusDays(7));
		TestEntity entity9 = new TestEntity(null, "Test 9", 29, new BigDecimal(2012.34), LocalDateTime.now().plusDays(8));
		TestEntity entity10 = new TestEntity(null, "Test 10", 30, new BigDecimal(2123.45), LocalDateTime.now().plusDays(9));
		TestEntity entity11 = new TestEntity(null, "Test 11", 31, new BigDecimal(2234.56), LocalDateTime.now().plusDays(10));
		TestEntity entity12 = new TestEntity(null, "Test 12", 32, new BigDecimal(2345.67), LocalDateTime.now().plusDays(11));
		TestEntity entity13 = new TestEntity(null, "Test 13", 33, new BigDecimal(2456.78), LocalDateTime.now().plusDays(12));
		TestEntity entity14 = new TestEntity(null, "Test 14", 34, new BigDecimal(2567.89), LocalDateTime.now().plusDays(13));
		TestEntity entity15 = new TestEntity(null, "Test 15", 35, new BigDecimal(2678.90), LocalDateTime.now().plusDays(14));
		TestEntity entity16 = new TestEntity(null, "Test 16", 36, new BigDecimal(2789.01), LocalDateTime.now().plusDays(15));
		TestEntity entity17 = new TestEntity(null, "Test 17", 37, new BigDecimal(2890.12), LocalDateTime.now().plusDays(16));
		TestEntity entity18 = new TestEntity(null, "Test 18", 38, new BigDecimal(2901.23), LocalDateTime.now().plusDays(17));
		TestEntity entity19 = new TestEntity(null, "Test 19", 39, new BigDecimal(3012.34), LocalDateTime.now().plusDays(18));
		TestEntity entity20 = new TestEntity(null, "Test 20", 40, new BigDecimal(3123.45), LocalDateTime.now().plusDays(19));
		TestEntity entity21 = new TestEntity(null, "Test 21", 41, new BigDecimal(3123.56), LocalDateTime.now().plusDays(20));

		entities = Arrays.asList(entity1, entity2, entity3, entity4, entity5,
				entity6, entity7, entity8, entity9, entity10, entity11,
				entity12, entity13, entity14, entity15, entity16, entity17,
				entity18, entity19, entity20, entity21);
		
		new HCFConnection<>(TestEntity.class).save(entities, false);
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
	void testSearch1() {
		
		List<TestEntity> entities = new HCFConnection<>(TestEntity.class).search(null, 
				"age", 30, HCFParameter.GREATERTHANOREQUALTO, HCFOperator.NONE);
		
		assertEquals(12, entities.size());
		
	}
	
}