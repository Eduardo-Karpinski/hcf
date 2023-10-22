package com.hcf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hcf.test.entities.TestEntity;

import br.com.hcf.HCFConnection;
import br.com.hcf.HCFOrder;
import br.com.hcf.HCFSearch;
import br.com.hcf.enums.HCFOperator;
import br.com.hcf.enums.HCFParameter;

@DisplayName("HCFUtilitiesTest")
class HCFUtilitiesTest {
	
	@Test
	void testFirstAndLast() {
		
		TestEntity entity1 = new TestEntity(null, "Test 1", 21, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity2 = new TestEntity(null, "Test 2", 25, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity3 = new TestEntity(null, "Test 3", 27, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity4 = new TestEntity(null, "Test 4", 27, new BigDecimal(1234.56), LocalDateTime.now());
		
		List<TestEntity> entities = Arrays.asList(entity1, entity2, entity3, entity4);
		
		new HCFConnection<>(TestEntity.class).save(entities, false);
		
		TestEntity first = new HCFConnection<>(TestEntity.class)
				.getFirstOrLast(new HCFOrder(true, "id", null, 0));
		
		TestEntity last = new HCFConnection<>(TestEntity.class)
				.getFirstOrLast(new HCFOrder(false, "id", null, 0));
		
		assertEquals(entity1.getId(), first.getId());
		assertEquals(entity4.getId(), last.getId());
		
		new HCFConnection<>(TestEntity.class).delete(entities, false);
	}
	
	@Test
	void testCount() {
		
		TestEntity entity1 = new TestEntity(null, "Test 1", 21, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity2 = new TestEntity(null, "Test 2", 25, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity3 = new TestEntity(null, "Test 3", 27, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity4 = new TestEntity(null, "Test 4", 27, new BigDecimal(1234.56), LocalDateTime.now());
		
		List<TestEntity> entities = Arrays.asList(entity1, entity2, entity3, entity4);
		new HCFConnection<>(TestEntity.class).save(entities, false);
		
		long count = new HCFConnection<>(TestEntity.class).count();
		assertEquals(4, count);
		
		new HCFConnection<>(TestEntity.class).delete(entities, false);
		
		count = new HCFConnection<>(TestEntity.class).count();
		assertEquals(0, count);
	}
	
	@Test
	void testSendSQL() {
		
		var insert = "INSERT INTO testentity (id, age, birthDate, name, salary) VALUES(9999, 23, '2000-01-01 00:00:00', 'Test', 1234.10);";
		HCFConnection.sendSQL(insert);
	
		var count = new HCFConnection<>(TestEntity.class).count();
		
		assertEquals(1, count);
		
		var delete = "DELETE FROM testentity WHERE id=9999;";
		HCFConnection.sendSQL(delete);
		
		count = new HCFConnection<>(TestEntity.class).count();
		
		assertEquals(0, count);
		
	}
	
	@Test
	void testGetElementsBySQL() {
		
		var insert1 = "INSERT INTO testentity (id, age, birthDate, name, salary) VALUES(9998, 23, '2000-01-01 00:00:00', 'Test', 1234.10);";
		var insert2 = "INSERT INTO testentity (id, age, birthDate, name, salary) VALUES(9999, 23, '2000-01-01 00:00:00', 'Test', 1234.10);";
		HCFConnection.sendSQL(insert1);
		HCFConnection.sendSQL(insert2);
		
		List<?> result1 = HCFConnection.getElementsBySQL("SELECT * FROM testentity;");
		
		for (Object entity : result1) {
			Object[] columns = (Object[]) entity;
			assertNotNull(columns[0]);
			assertNotNull(columns[1]);
			assertNotNull(columns[2]);
			assertNotNull(columns[3]);
			assertNotNull(columns[4]);
		}
		
		var delete1 = "DELETE FROM testentity WHERE id=9998;";
		var delete2 = "DELETE FROM testentity WHERE id=9999;";
		HCFConnection.sendSQL(delete1);
		HCFConnection.sendSQL(delete2);
	}
	
	@Test
	void testGetObjectBySQL() {
		
		TestEntity entity1 = new TestEntity(null, "Test 1", 21, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity2 = new TestEntity(null, "Test 2", 25, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity3 = new TestEntity(null, "Test 3", 27, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity4 = new TestEntity(null, "Test 4", 27, new BigDecimal(1234.56), LocalDateTime.now());
		
		List<TestEntity> entities = Arrays.asList(entity1, entity2, entity3, entity4);
		new HCFConnection<>(TestEntity.class).save(entities, false);
		
		List<TestEntity> entitiesBySQL = new HCFConnection<>(TestEntity.class).getObjectBySQL("SELECT * FROM testentity;");
		
		assertEquals(4, entitiesBySQL.size());
		
		new HCFConnection<>(TestEntity.class).delete(entities, false);
		
	}
	
	@Test
	void testGetDistinctField() {
		
		TestEntity entity1 = new TestEntity(null, "Test 1", 21, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity2 = new TestEntity(null, "Test 2", 25, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity3 = new TestEntity(null, "Test 3", 27, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity4 = new TestEntity(null, "Test 4", 27, new BigDecimal(1234.56), LocalDateTime.now());
		
		List<TestEntity> entities = Arrays.asList(entity1, entity2, entity3, entity4);
		new HCFConnection<>(TestEntity.class).save(entities, false);
		
		List<Object> distinct1 = new HCFConnection<>(TestEntity.class).getDistinctField("age");
		List<Object> distinct2 = new HCFConnection<>(TestEntity.class).getDistinctField("age",
				"age", "25", HCFParameter.NOTEQUAL, HCFOperator.NONE);
		List<Object> distinct3 = new HCFConnection<>(TestEntity.class).getDistinctField("age",
				Arrays.asList(new HCFSearch("age", "25", HCFParameter.NOTEQUAL, HCFOperator.NONE)));
		
		assertEquals(3, distinct1.size());
		assertEquals(2, distinct2.size());
		assertEquals(2, distinct3.size());
		
		new HCFConnection<>(TestEntity.class).delete(entities, false);
	}
	
}