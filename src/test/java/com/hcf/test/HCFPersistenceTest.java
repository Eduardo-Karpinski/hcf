package com.hcf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hcf.test.entities.TestEntity;

import br.com.hcf.HCFConnection;
import br.com.hcf.HCFSearch;
import br.com.hcf.enums.HCFOperator;
import br.com.hcf.enums.HCFParameter;

/**
 * @author Eduardo
 * Here are all the tests related to saving, updating, deleting and simple readings
 */
@DisplayName("HCFPersistenceTest")
class HCFPersistenceTest {

	private TestEntity getObject() {
		return new TestEntity(null, "Test 1", 20, new BigDecimal(1234.56), LocalDateTime.now());
	}

	@Test
	void testCrud() {
		TestEntity entity = getObject();
		new HCFConnection<>(TestEntity.class).save(entity);
		assertNotNull(entity.getId());

		entity.setName("Test 2");
		new HCFConnection<>(TestEntity.class).save(entity);

		entity = new HCFConnection<>(TestEntity.class).getById(entity.getId());
		assertEquals("Test 2", entity.getName());

		new HCFConnection<>(TestEntity.class).delete(entity);
		entity = new HCFConnection<>(TestEntity.class).getById(entity.getId());
		assertNull(entity);
	}

	@Test
	void testAll() {
		TestEntity entity1 = new TestEntity(null, "Test 1", 20, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity2 = new TestEntity(null, "Test 2", 20, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity3 = new TestEntity(null, "Test 3", 20, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity4 = new TestEntity(null, "Test 4", 20, new BigDecimal(1234.56), LocalDateTime.now());

		new HCFConnection<>(TestEntity.class).save(Arrays.asList(entity1, entity2, entity3, entity4), false);

		List<TestEntity> all = new HCFConnection<>(TestEntity.class).all();

		assertEquals(4, all.size());

		new HCFConnection<>(TestEntity.class).delete(all, false);
	}

	@Test
	void testDeleteById() {
		TestEntity entity = getObject();
		new HCFConnection<>(TestEntity.class).save(entity);
		assertNotNull(entity.getId());

		new HCFConnection<>(TestEntity.class).deleteById(entity.getId());

		entity = new HCFConnection<>(TestEntity.class).getById(entity.getId());
		assertNull(entity);
	}

	@Test
	void testMassiveUpdate() {
		TestEntity entity1 = new TestEntity(null, "Test 1", 21, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity2 = new TestEntity(null, "Test 2", 25, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity3 = new TestEntity(null, "Test 3", 27, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity4 = new TestEntity(null, "Test 4", 27, new BigDecimal(1234.56), LocalDateTime.now());
		
		List<TestEntity> entities = Arrays.asList(entity1, entity2, entity3, entity4);
		
		new HCFConnection<>(TestEntity.class).save(entities, false);

		Map<String, Object> newValues = Map.of("age", 99);

		long total = new HCFConnection<>(TestEntity.class).massiveUpdate(newValues, 
				"age", 25, HCFParameter.GREATERTHAN, HCFOperator.NONE);

		assertEquals(2, total);
		
		newValues = Map.of("age", 27);
		
		total = new HCFConnection<>(TestEntity.class).massiveUpdate(newValues,
				Arrays.asList(new HCFSearch("age", 99, HCFParameter.EQUAL, HCFOperator.NONE)));

		assertEquals(2, total);
		
		new HCFConnection<>(TestEntity.class).delete(entities, false);
	}
	
	@Test
	void testMassiveDelete() {
		
		TestEntity entity1 = new TestEntity(null, "Test 1", 21, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity2 = new TestEntity(null, "Test 2", 25, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity3 = new TestEntity(null, "Test 3", 27, new BigDecimal(1234.56), LocalDateTime.now());
		TestEntity entity4 = new TestEntity(null, "Test 4", 27, new BigDecimal(1234.56), LocalDateTime.now());
		
		List<TestEntity> entities = Arrays.asList(entity1, entity2, entity3, entity4);
		
		new HCFConnection<>(TestEntity.class).save(entities, false);
		
		long total = new HCFConnection<>(TestEntity.class).massiveDelete( 
				"age", 25, HCFParameter.GREATERTHAN, HCFOperator.NONE);
		
		assertEquals(2, total);
		
		total = new HCFConnection<>(TestEntity.class).massiveDelete( 
				Arrays.asList(new HCFSearch("age", 25, HCFParameter.LESSTHANOREQUALTO, HCFOperator.NONE)));
		
		assertEquals(2, total);
	}

}