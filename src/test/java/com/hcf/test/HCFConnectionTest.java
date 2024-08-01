package com.hcf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcf.HCFConnection;
import com.hcf.HCFFactory;
import com.hcf.HCFJoinSearch;
import com.hcf.HCFOrder;
import com.hcf.HCFSearch;
import com.hcf.enums.HCFOperator;
import com.hcf.enums.HCFParameter;
import com.hcf.test.entities.Data;
import com.hcf.test.entities.DataChild;
import com.hcf.test.entities.DataWithChildren;
import com.hcf.test.entities.DataWithOutId;
import com.hcf.test.entities.Permission;
import com.hcf.utils.HCFUtil;

import jakarta.persistence.Id;

class HCFConnectionTest {
	
	@BeforeEach
	void createData() {
	    List<Data> datas = new ArrayList<>();

	    datas.add(new Data("User 1", 21, new BigDecimal("5500.00"), LocalDateTime.of(2023, 5, 15, 10, 0), true));
	    datas.add(new Data("User 2", 22, new BigDecimal("5600.00"), LocalDateTime.of(2023, 6, 20, 11, 30), false));
	    datas.add(new Data("User 3", 23, new BigDecimal("5700.00"), LocalDateTime.of(2023, 7, 25, 12, 45), true));
	    datas.add(new Data("User 4", 24, new BigDecimal("5800.00"), LocalDateTime.of(2023, 8, 30, 14, 15), false));
	    datas.add(new Data("User 5", 25, new BigDecimal("5900.00"), LocalDateTime.of(2023, 9, 5, 15, 30), true));
	    datas.add(new Data("User 6", 26, new BigDecimal("6000.00"), LocalDateTime.of(2023, 10, 10, 9, 0), false));
	    datas.add(new Data("User 7", 27, new BigDecimal("6100.00"), LocalDateTime.of(2023, 11, 15, 10, 30), true));
	    datas.add(new Data("User 8", 28, new BigDecimal("6200.00"), LocalDateTime.of(2023, 12, 20, 11, 45), false));
	    datas.add(new Data("User 9", 29, new BigDecimal("6300.00"), LocalDateTime.of(2024, 1, 25, 13, 0), true));
	    datas.add(new Data("User 10", 30, new BigDecimal("6400.00"), LocalDateTime.of(2024, 2, 28, 14, 15), false));
	    datas.add(new Data("User 11", 31, new BigDecimal("6500.00"), LocalDateTime.of(2024, 3, 5, 15, 30), true));
	    datas.add(new Data("User 12", 32, new BigDecimal("6600.00"), LocalDateTime.of(2024, 4, 10, 9, 0), false));
	    datas.add(new Data("User 13", 33, new BigDecimal("6700.00"), LocalDateTime.of(2024, 5, 15, 10, 30), true));
	    datas.add(new Data("User 14", 34, new BigDecimal("6800.00"), LocalDateTime.of(2024, 6, 20, 11, 45), false));
	    datas.add(new Data("User 15", 35, new BigDecimal("6900.00"), LocalDateTime.of(2024, 7, 25, 12, 0), true));
	    datas.add(new Data("User 16", 36, new BigDecimal("7000.00"), LocalDateTime.of(2024, 8, 30, 14, 15), false));
	    datas.add(new Data("User 17", 37, new BigDecimal("7100.00"), LocalDateTime.of(2024, 9, 5, 15, 30), true));
	    datas.add(new Data("User 18", 38, new BigDecimal("7200.00"), LocalDateTime.of(2024, 10, 10, 9, 0), false));
	    datas.add(new Data("User 19", 39, new BigDecimal("7300.00"), LocalDateTime.of(2024, 11, 15, 10, 30), true));
	    datas.add(new Data("User 20", 40, new BigDecimal("7400.00"), LocalDateTime.of(2024, 12, 20, 11, 45), false));
	    datas.add(new Data("User 21", 41, new BigDecimal("7500.00"), LocalDateTime.of(2025, 1, 25, 13, 0), true));
	    datas.add(new Data("User 22", 42, new BigDecimal("7600.00"), LocalDateTime.of(2025, 2, 28, 14, 15), false));
	    datas.add(new Data("User 23", 43, new BigDecimal("7700.00"), LocalDateTime.of(2025, 3, 5, 15, 30), true));
	    datas.add(new Data("User 24", 44, new BigDecimal("7800.00"), LocalDateTime.of(2025, 4, 10, 9, 0), false));
	    datas.add(new Data("User 25", 45, new BigDecimal("7900.00"), LocalDateTime.of(2025, 5, 15, 10, 30), true));
	    datas.add(new Data("User 26", 46, new BigDecimal("8000.00"), LocalDateTime.of(2025, 6, 20, 11, 45), false));
	    datas.add(new Data("User 27", 47, new BigDecimal("8100.00"), LocalDateTime.of(2025, 7, 25, 12, 0), true));
	    datas.add(new Data("User 28", 48, new BigDecimal("8200.00"), LocalDateTime.of(2025, 8, 30, 14, 15), false));
	    datas.add(new Data("User 29", 49, new BigDecimal("8300.00"), LocalDateTime.of(2025, 9, 5, 15, 30), true));
	    datas.add(new Data("User 30", 50, new BigDecimal("8400.00"), LocalDateTime.of(2025, 10, 10, 9, 0), false));
	    datas.add(new Data("User 31", 51, new BigDecimal("8500.00"), LocalDateTime.of(2025, 11, 15, 10, 30), true));
	    datas.add(new Data("User 32", 52, new BigDecimal("8600.00"), LocalDateTime.of(2025, 12, 20, 11, 45), false));
	    datas.add(new Data("User 33", 53, new BigDecimal("8700.00"), LocalDateTime.of(2026, 1, 25, 13, 0), true));
	    datas.add(new Data("User 34", 54, new BigDecimal("8800.00"), LocalDateTime.of(2026, 2, 28, 14, 15), false));
	    datas.add(new Data("User 35", 55, new BigDecimal("8900.00"), LocalDateTime.of(2026, 3, 5, 15, 30), true));
	    datas.add(new Data("User 36", 56, new BigDecimal("9000.00"), LocalDateTime.of(2026, 4, 10, 9, 0), false));
	    datas.add(new Data("User 37", 57, new BigDecimal("9100.00"), LocalDateTime.of(2026, 5, 15, 10, 30), true));
	    datas.add(new Data("User 38", 58, new BigDecimal("9200.00"), LocalDateTime.of(2026, 6, 20, 11, 45), false));
	    datas.add(new Data("User 39", 59, new BigDecimal("9300.00"), LocalDateTime.of(2026, 7, 25, 12, 0), true));
	    datas.add(new Data("User 40", 60, new BigDecimal("9400.00"), LocalDateTime.of(2026, 8, 30, 14, 15), false));
	    datas.add(new Data("User 41", 61, new BigDecimal("9500.00"), LocalDateTime.of(2026, 9, 5, 15, 30), true));
	    datas.add(new Data("User 42", 62, new BigDecimal("9600.00"), LocalDateTime.of(2026, 10, 10, 9, 0), false));
	    datas.add(new Data("User 43", 63, new BigDecimal("9700.00"), LocalDateTime.of(2026, 11, 15, 10, 30), true));
	    datas.add(new Data("User 44", 64, new BigDecimal("9800.00"), LocalDateTime.of(2026, 12, 20, 11, 45), false));
	    datas.add(new Data("User 45", 65, new BigDecimal("9900.00"), LocalDateTime.of(2027, 1, 25, 13, 0), true));
	    datas.add(new Data("User 46", 66, new BigDecimal("10000.00"), LocalDateTime.of(2027, 2, 28, 14, 15), false));
	    datas.add(new Data("User 47", 67, new BigDecimal("10100.00"), LocalDateTime.of(2027, 3, 5, 15, 30), true));
	    datas.add(new Data("User 48", 68, new BigDecimal("10200.00"), LocalDateTime.of(2027, 4, 10, 9, 0), false));
	    datas.add(new Data("User 49", 69, new BigDecimal("10300.00"), LocalDateTime.of(2027, 5, 15, 10, 30), true));
	    datas.add(new Data("User 50", 70, new BigDecimal("10400.00"), LocalDateTime.of(2027, 6, 20, 11, 45), false));

	    new HCFConnection<>(Data.class).save(datas);
	    
	    DataWithChildren father = new DataWithChildren("father 1");
	    
	    DataChild child1 = new DataChild("child 1");
	    DataChild child2 = new DataChild("child 2");
	    
	    father.getChildren().add(child1);
	    father.getChildren().add(child2);
	    
	    new HCFConnection<>(DataWithChildren.class).save(father);
	}

	@AfterEach
	void deleteData() {
		new HCFConnection<>(Data.class).delete(new HCFConnection<>(Data.class).all());
		new HCFConnection<>(DataWithChildren.class).delete(new HCFConnection<>(DataWithChildren.class).all());
	}
	
	@Test
	void testSave() {
	    // Step 1: Verify initial number of data entries
	    List<Data> datas = new HCFConnection<>(Data.class).all();
	    assertEquals(50, datas.size(), "There should be 50 data entries initially.");
	    
	    // Step 2: Create a new data entry and save it
	    Data data = new Data("new data", 0, BigDecimal.ZERO, LocalDateTime.now(), true);
	    new HCFConnection<>(Data.class).save(data);
	    
	    // Step 3: Verify the number of data entries after saving the new data
	    datas = new HCFConnection<>(Data.class).all();
	    assertEquals(51, datas.size(), "There should be 51 data entries after saving the new data.");
	    
	    // Step 4: Verify the new data entry is correctly saved
	    Data savedData = datas.stream().filter(d -> "new data".equals(d.getName())).findFirst().orElse(null);
	    assertNotNull(savedData, "The new data entry should be found in the data list.");
	    assertEquals("new data", savedData.getName(), "The name of the new data entry should be 'new data'.");
	    assertEquals(0, savedData.getAge(), "The age of the new data entry should be 0.");
        assertEquals(0, savedData.getSalary().compareTo(BigDecimal.ZERO), "The salary of the new data entry should be 0.");
	    assertTrue(savedData.getActive(), "The new data entry should be active.");
	}

	@Test
	void testDelete() {
	    // Step 1: Verify initial number of data entries
	    List<Data> datas = new HCFConnection<>(Data.class).all();
	    assertEquals(50, datas.size(), "There should be 50 data entries initially.");
	    
	    // Step 2: Select a data entry to delete
	    Data dataToDelete = new HCFConnection<>(Data.class).getById(datas.get(datas.size() / 2).getId());
	    assertNotNull(dataToDelete, "The data entry to delete should not be null.");
	    
	    // Step 3: Delete the selected data entry
	    new HCFConnection<>(Data.class).delete(dataToDelete);
	    
	    // Step 4: Verify the number of data entries after deletion
	    datas = new HCFConnection<>(Data.class).all();
	    assertEquals(49, datas.size(), "There should be 49 data entries after deletion.");
	    
	    // Step 5: Verify the data entry was actually deleted
	    Data deletedData = new HCFConnection<>(Data.class).getById(dataToDelete.getId());
	    assertNull(deletedData, "The data entry should be deleted and no longer retrievable by its ID.");
	}
	
	@Test
	void testMassiveDelete() {
	    // Step 1: Count the initial number of data entries
	    long totalData = new HCFConnection<>(Data.class).count();
	    assertEquals(50, totalData, "There should be 50 data entries initially.");

	    // Step 2: Delete data entries where the ID is even
	    int deletedData = new HCFConnection<>(Data.class).massiveDelete("id", null, HCFParameter.IS_EVEN, HCFOperator.NONE);
	    assertEquals(25, deletedData, "25 data entries with even IDs should be deleted.");
	    
	    // Step 3: Verify the remaining data count after the first deletion
	    long remainingData = new HCFConnection<>(Data.class).count();
	    assertEquals(25, remainingData, "There should be 25 data entries remaining after deleting even IDs.");

	    // Step 4: Delete the remaining data entries where the ID is odd
	    deletedData = new HCFConnection<>(Data.class).massiveDelete(List.of(new HCFSearch("id", null, HCFParameter.IS_ODD, HCFOperator.NONE)));
	    assertEquals(25, deletedData, "25 data entries with odd IDs should be deleted.");
	    
	    // Step 5: Verify the data count after the second deletion
	    remainingData = new HCFConnection<>(Data.class).count();
	    assertEquals(0, remainingData, "There should be no data entries remaining after deleting odd IDs.");
	}

	@Test
	void testMassiveUpdate() {
	    // Step 1: Verify that not all data entries have age 100 initially
	    List<Data> datas = new HCFConnection<>(Data.class).all();
	    boolean sameAge = datas.stream().allMatch(data -> data.getAge() == 100);
	    assertFalse(sameAge, "Initially, not all data entries should have age 100.");
	    
	    // Step 2: Update all data entries' ages to 100 where id > 0
	    Map<String, Object> newAges = new HashMap<>();
	    newAges.put("age", 100);
	    new HCFConnection<>(Data.class).massiveUpdate(newAges, "id", 0, HCFParameter.GREATER_THAN, HCFOperator.NONE);
	    
	    // Step 3: Verify that all data entries' ages are now 100
	    datas = new HCFConnection<>(Data.class).all();
	    sameAge = datas.stream().allMatch(data -> data.getAge() == 100);
	    assertTrue(sameAge, "All data entries should have age 100 after the first update.");
	    
	    // Step 4: Update all data entries' ages to 50 where id > 0 using HCFSearch
	    newAges.clear();
	    newAges.put("age", 50);
	    new HCFConnection<>(Data.class).massiveUpdate(newAges, List.of(new HCFSearch("id", 0, HCFParameter.GREATER_THAN, HCFOperator.NONE)));
	    
	    // Step 5: Verify that all data entries' ages are now 50
	    datas = new HCFConnection<>(Data.class).all();
	    sameAge = datas.stream().allMatch(data -> data.getAge() == 50);
	    assertTrue(sameAge, "All data entries should have age 50 after the second update.");
	}
	
	@Test
	void testAll() {
		List<Data> datas = new HCFConnection<>(Data.class).all();
		assertEquals(50, datas.size(), "There should be 50 data entries initially.");
	}

	@Test
	void testHCFRelationship() {
	    DataWithChildren father = new HCFConnection<>(DataWithChildren.class).searchWithOneResult(
	            null, "name", "father 1", HCFParameter.EQUAL, HCFOperator.NONE);

	    assertNotNull(father, "Father with name 'father 1' should be found.");
	    
	    List<DataChild> children = father.getChildren();
	    
	    // Verify that the father has the expected number of children
	    assertEquals(2, children.size(), "Father should have 2 children.");

	    // Verify the children's details
	    children.forEach(child -> {
	    	assertNotNull(child, "Child should not be null.");
	    	assertNotNull(child.getId(), "Child id should not be null.");
	    	assertNotNull(child.getName(), "Child name should not be null.");
	    	assertTrue(child.getName().startsWith("child"), "Child name should start with 'child'.");
	    });
	}
	
	@Test
	void testGetRelations() {
		DataWithChildren father = new HCFConnection<>(DataWithChildren.class).searchWithOneResult(
				null, "name", "father 1", HCFParameter.EQUAL, HCFOperator.NONE);
		
		assertNotNull(father, "Father with name 'father 1' should be found.");
		
		List<DataChild> children = new HCFConnection<>(DataChild.class).getRelations(DataWithChildren.class, "children", father.getId());
		
		// Verify that the father has the expected number of children
		assertEquals(2, children.size(), "Father should have 2 children.");
		
		// Verify the children's details
		children.forEach(child -> {
			assertNotNull(child, "Child should not be null.");
			assertNotNull(child.getId(), "Child id should not be null.");
			assertNotNull(child.getName(), "Child name should not be null.");
			assertTrue(child.getName().startsWith("child"), "Child name should start with 'child'.");
		});
	}

	@Test
	void getByInvertedRelation() {
		 // Fetch the father by name
	    DataWithChildren father = new HCFConnection<>(DataWithChildren.class).searchWithOneResult(
	            null, "name", "father 1", HCFParameter.EQUAL, HCFOperator.NONE);

	    // Ensure the father was found
	    assertNotNull(father, "Father with name 'father 1' should be found.");
	    
	    // Get the first child of the father
	    DataChild child = father.getChildren().getFirst();

	    // Ensure the child was found
	    assertNotNull(child, "Child should not be null.");

	    // Fetch the father by the inverted relation using the child's ID
	    List<DataWithChildren> fatherByChildList = new HCFConnection<>(DataWithChildren.class).getByInvertedRelation(
	            DataChild.class, "children", child.getId());

	    // Ensure the list of fathers found is not empty
	    assertFalse(fatherByChildList.isEmpty(), "Father by child list should not be empty.");

	    // Ensure only one father was found (since the relationship is @OneToMany)
	    assertEquals(1, fatherByChildList.size(), "There should be exactly one father related to the child.");

	    // Get the father found by the inverted relation
	    DataWithChildren fatherByChild = fatherByChildList.get(0);

	    // Ensure the father found by the inverted relation matches the original father
	    assertEquals(father.getId(), fatherByChild.getId(), "The father found by the inverted relation should match the original father.");
	    assertEquals(father.getName(), fatherByChild.getName(), "The father name should match.");
	}

	@Test
	void testDeleteById() {
		// Step 1: Retrieve all data entries
		List<Data> allData = new HCFConnection<>(Data.class).all();
		assertEquals(50, allData.size(), "There should be 50 data entries initially.");

		// Step 2: Select a data entry to delete
		Data dataToDelete = new HCFConnection<>(Data.class).getById(allData.get(allData.size() / 2).getId());
		assertNotNull(dataToDelete, "The selected data entry should not be null.");

		// Step 3: Delete the selected data entry
		int deletedId = new HCFConnection<>(Data.class).deleteById(dataToDelete.getId());

		// Step 4: Verify the data entry is deleted
		Data deletedData = new HCFConnection<>(Data.class).getById(deletedId);
		assertNull(deletedData, "The data entry should be null after deletion.");

		// Step 5: Verify the total count of data entries
		allData = new HCFConnection<>(Data.class).all();
		assertEquals(49, allData.size(), "There should be 49 data entries after deletion.");
	}

	@Test
	void testGetById() {
		// Step 1: Retrieve all data entries
		List<Data> allData = new HCFConnection<>(Data.class).all();

		// Ensure there is data to test with
		assertFalse(allData.isEmpty(), "The data list should not be empty.");

		// Step 2: Get a valid ID from the data
		long validId = allData.get(allData.size() / 2).getId();

		// Step 3: Test for existing data
		Data data = new HCFConnection<>(Data.class).getById(validId);
		assertNotNull(data, "Data with the valid ID should exist.");
		assertEquals(validId, data.getId(), "The ID of the retrieved data should match the valid ID.");

		// Step 4: Test for non-existing data
		Data nonExistentData = new HCFConnection<>(Data.class).getById(-1); // Assuming -1 is a non-existing ID
		assertNull(nonExistentData, "Data with ID -1 should not exist.");
	}

	@Test
	void testSum() {
		// Calculate total salaries of all data
		List<Data> allData = new HCFConnection<>(Data.class).all();
		BigDecimal allSalaries = allData.stream().map(Data::getSalary).reduce(BigDecimal.ZERO, BigDecimal::add);

		// Test sum for all salaries
		List<Object> results = new HCFConnection<>(Data.class).sum(List.of("salary"));
		assertNotNull(results, "Results should not be null.");
		assertEquals(1, results.size(), "Exactly one result should be returned.");
		assertEquals(allSalaries, results.get(0), "Sum of all salaries should match.");

		// Calculate total salaries of data with even IDs
		BigDecimal evenIdSalaries = allData.stream().filter(data -> data.getId() % 2 == 0).map(Data::getSalary)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		// Test sum for salaries with even IDs
		results = new HCFConnection<>(Data.class).sum(List.of("salary"), "id", null, HCFParameter.IS_EVEN,
				HCFOperator.NONE);
		assertNotNull(results, "Results should not be null.");
		assertEquals(1, results.size(), "Exactly one result should be returned.");
		assertEquals(evenIdSalaries, results.get(0), "Sum of salaries with even IDs should match.");
	}

	@Test
	void testGetFirstOrLast() {
		// Step 1: Retrieve all data entries
		List<Data> allData = new HCFConnection<>(Data.class).all();
		assertFalse(allData.isEmpty(), "The data list should not be empty.");

		// Step 2: Get the first and last data using getFirstOrLast method
		Data first = new HCFConnection<>(Data.class).getFirstOrLast(new HCFOrder(true, "id", null, 0));
		Data last = new HCFConnection<>(Data.class).getFirstOrLast(new HCFOrder(false, "id", null, 0));

		// Step 3: Assert that the retrieved first and last data match the expected IDs
		assertNotNull(first, "The first data should not be null.");
		assertNotNull(last, "The last data should not be null.");

		assertEquals(allData.getFirst().getId(), first.getId(), "The ID of the first data should match.");
		assertEquals(allData.getLast().getId(), last.getId(), "The ID of the last data should match.");
	}

	@Test
	void testSendSQL() {
	    // Step 1: Verify the initial count of data entries
	    long count = new HCFConnection<>(Data.class).count();
	    assertEquals(count, 50, "There should be 50 data entries initially.");
	    
	    // Step 2: Insert new data using raw SQL
	    String sql = "INSERT INTO Data (name, age, salary, registrationDate, active) VALUES ('test sql', 30, 75000.00, '2024-07-17T12:30:00', true);";
	    HCFConnection.sendSQL(sql);
	    
	    // Step 3: Verify the count of data entries after insertion
	    count = new HCFConnection<>(Data.class).count();
	    assertEquals(count, 51, "There should be 51 data entries after the SQL insertion.");
	    
	    // Step 4: Retrieve the newly inserted data entry
	    List<Data> datas = new HCFConnection<>(Data.class).all();
	    Data savedData = datas.stream().filter(d -> "test sql".equals(d.getName())).findFirst().orElse(null);
	    assertNotNull(savedData, "The new data entry should be found in the data list.");
	    
	    // Step 5: Validate all values of the new data entry
	    assertEquals("test sql", savedData.getName(), "The name should be 'test sql'.");
	    assertEquals(30, savedData.getAge(), "The age should be 30.");
	    assertEquals(new BigDecimal("75000.00"), savedData.getSalary(), "The salary should be 75000.00.");
	    assertEquals(LocalDateTime.of(2024, 7, 17, 12, 30), savedData.getRegistrationDate(), "The registration date should be '2024-07-17T12:30:00'.");
	    assertTrue(savedData.getActive(), "The active status should be true.");
	}

	@Test
	void testGetObjectBySQL() {
	    // Step 1: Retrieve all data entries using the `all` method
	    List<Data> datas = new HCFConnection<>(Data.class).all();
	    assertNotNull(datas, "The list of data should not be null.");
	    assertFalse(datas.isEmpty(), "The list of data should not be empty.");

	    // Step 2: Retrieve all data entries using a raw SQL query
	    List<Data> datasFromSql = new HCFConnection<>(Data.class).getObjectBySQL("SELECT * FROM data");
	    assertNotNull(datasFromSql, "The list of data from SQL should not be null.");
	    assertFalse(datasFromSql.isEmpty(), "The list of data from SQL should not be empty.");

	    // Step 3: Compare the two lists
	    assertEquals(datas.size(), datasFromSql.size(), "The size of the lists should be the same.");
	    assertTrue(datas.containsAll(datasFromSql), "The data list should contain all elements from the SQL query result.");
	    assertTrue(datasFromSql.containsAll(datas), "The SQL query result should contain all elements from the data list.");
	}

	@Test
	void testGetElementsBySQL() {
	    // Step 1: Retrieve the first data entry using the `all` method
	    Data data = new HCFConnection<>(Data.class).all().getFirst();
	    assertNotNull(data, "The first data entry should not be null.");

	    // Step 2: Construct the SQL query to select specific fields for the first data entry
	    String sql = "SELECT name, age, salary, registrationDate, active FROM data WHERE id = " + data.getId();
	    
	    // Step 3: Retrieve the elements using the SQL query
	    List<?> elementsBySQL = HCFConnection.getElementsBySQL(sql);
	    assertNotNull(elementsBySQL, "The elements list should not be null.");
	    assertFalse(elementsBySQL.isEmpty(), "The elements list should not be empty.");
	    assertEquals(1, elementsBySQL.size(), "The elements list should contain one entry.");

	    // Step 4: Verify the retrieved data matches the expected values
	    Object[] columns = (Object[]) elementsBySQL.getFirst();
	    assertEquals(data.getName(), columns[0], "The name should match.");
	    assertEquals(data.getAge(), columns[1], "The age should match.");
	    assertEquals(data.getSalary(), columns[2], "The salary should match.");
	    assertEquals(data.getRegistrationDate(), LocalDateTime.parse(columns[3].toString().replace(" ", "T")), "The registration date should match.");
	    assertEquals(data.getActive(), columns[4], "The active status should match.");
	}

	@Test
	void testCount() {
	    // Step 1: Retrieve all data entries
	    List<Data> allData = new HCFConnection<>(Data.class).all();
	    assertNotNull(allData, "The data list should not be null.");
	    
	    // Step 2: Get count of all data entries
	    long count = new HCFConnection<>(Data.class).count();
	    
	    // Step 3: Assert that the count matches the size of allData
	    assertEquals(allData.size(), count, "The count should match the size of all data entries.");
	}
	
	@Test
	void testGetDistinctField() {
	    // Step 1: Test distinct ages without additional conditions
	    List<Object> distinct = new HCFConnection<>(Data.class).getDistinctField("age");
	    assertEquals(50, distinct.size(), "Expected 50 distinct ages.");
	    
	    // Step 2: Test distinct ages greater than or equal to 40
	    distinct = new HCFConnection<>(Data.class).getDistinctField("age",
	            "age", 40, HCFParameter.GREATER_THAN_OR_EQUAL_TO, HCFOperator.NONE);
	    
	    assertEquals(31, distinct.size(), "Expected 31 distinct ages greater than or equal to 40.");
	    
	    // Step 3: Test distinct ages greater than or equal to 40 using HCFSearch
	    distinct = new HCFConnection<>(Data.class).getDistinctField("age",
	            Arrays.asList(new HCFSearch("age", 40, HCFParameter.GREATER_THAN_OR_EQUAL_TO, HCFOperator.NONE)));
	    
	    assertEquals(31, distinct.size(), "Expected 31 distinct ages greater than or equal to 40.");
	}
	
	@Test
	void testSearchWithOneResult() {
		// Step 1: Perform the search with one result
	    Data data = new HCFConnection<>(Data.class).searchWithOneResult(null, "age", 21, HCFParameter.EQUAL, HCFOperator.NONE);
	    
	    // Step 2: Assert that the result is not null
	    assertNotNull(data, "Expected to find a data entry with age 21.");
	    
	    // Step 3: Validate that the retrieved data matches the search criteria
	    assertEquals(21, data.getAge(), "The age of the retrieved data should be 21.");
	    
	    // Step 4: Perform the search with one result with HCFSearch
	    data = new HCFConnection<>(Data.class).searchWithOneResult(null, List.of(new HCFSearch("age", 22, HCFParameter.EQUAL, HCFOperator.NONE)));
	    
	    // Step 5: Assert that the result is not null
	    assertNotNull(data, "Expected to find a data entry with age 22.");
	    
	    // Step 6: Validate that the retrieved data matches the search criteria
	    assertEquals(22, data.getAge(), "The age of the retrieved data should be 22.");
	}
	
	@Test
	void testSearch() {
		BigDecimal lowestValue = BigDecimal.valueOf(7000.00);
		BigDecimal highestValue = BigDecimal.valueOf(8000.00);
		
	    LocalDateTime from = LocalDateTime.of(2024, 12, 20, 11, 45);
	    LocalDateTime to = LocalDateTime.of(2025, 3, 5, 15, 30);
		
		List<Data> data = new HCFConnection<>(Data.class).search(null,
				"salary", lowestValue, HCFParameter.GREATER_THAN_OR_EQUAL_TO, HCFOperator.NONE,
				"salary", highestValue, HCFParameter.LESS_THAN_OR_EQUAL_TO, HCFOperator.AND,
				"registrationDate", from, HCFParameter.GREATER_THAN_OR_EQUAL_TO, HCFOperator.AND,
				"registrationDate", to, HCFParameter.LESS_THAN_OR_EQUAL_TO, HCFOperator.AND,
				"active", null, HCFParameter.TRUE, HCFOperator.AND);

		assertEquals(data.size(), 2); 
		
		data.forEach(d -> {
			assertTrue(d.getSalary().compareTo(lowestValue) >= 0 && d.getSalary().compareTo(highestValue) <= 0,
	                "Data entry salary should be between " + lowestValue + " and " + highestValue);
	        assertTrue(!d.getRegistrationDate().isBefore(from) && !d.getRegistrationDate().isAfter(to),
	                "Data entry registration date should be between " + from + " and " + to);
	        assertTrue(d.getActive(), "Data entry should be active.");
		});
		
		HCFSearch search1 = new HCFSearch().setField("salary").setValue(lowestValue).setParameter(HCFParameter.GREATER_THAN_OR_EQUAL_TO).setOperator(HCFOperator.NONE);
		HCFSearch search2 = new HCFSearch().setField("salary").setValue(highestValue).setParameter(HCFParameter.LESS_THAN_OR_EQUAL_TO).setOperator(HCFOperator.AND);
		HCFSearch search3 = new HCFSearch().setField("registrationDate").setValue(from).setParameter(HCFParameter.GREATER_THAN_OR_EQUAL_TO).setOperator(HCFOperator.AND);
		HCFSearch search4 = new HCFSearch().setField("registrationDate").setValue(to).setParameter(HCFParameter.LESS_THAN_OR_EQUAL_TO).setOperator(HCFOperator.AND);
		HCFSearch search5 = new HCFSearch().setField("active").setParameter(HCFParameter.TRUE).setOperator(HCFOperator.AND);
		
		data = new HCFConnection<>(Data.class).search(null, Arrays.asList(search1, search2, search3, search4, search5));

		assertEquals(data.size(), 2); 
		
		data.forEach(d -> {
			assertTrue(d.getSalary().compareTo(lowestValue) >= 0 && d.getSalary().compareTo(highestValue) <= 0,
	                "Data entry salary should be between " + lowestValue + " and " + highestValue);
	        assertTrue(!d.getRegistrationDate().isBefore(from) && !d.getRegistrationDate().isAfter(to),
	                "Data entry registration date should be between " + from + " and " + to);
	        assertTrue(d.getActive(), "Data entry should be active.");
		});
	}
	
	@Test
	void testExplicitOr() {
	    // Perform a search with explicit OR logic
	    List<Data> datas = new HCFConnection<>(Data.class).search(null,
	            "name", "User 25", HCFParameter.EQUAL, HCFOperator.NONE,
	            "salary", 8000, HCFParameter.EQUAL, HCFOperator.AND,
	            "name", "User 26", HCFParameter.EQUAL, HCFOperator.OR);

	    // Assert that exactly one result is returned
	    assertEquals(1, datas.size(), "There should be exactly one result.");

	    // Get the single result
	    Data data = datas.getFirst();

	    // Assert that the result is not null
	    assertNotNull(data, "The result should not be null.");

	    // Assert that the name of the result is "User 26"
	    assertEquals("User 26", data.getName(), "The name should be 'User 26'.");
	}
	
	@Test
	void testHCFOrder() {
	    // Set up the order parameters
	    Boolean asc = false;
	    String field = "id";
	    Integer limit = 5;
	    Integer offset = 10;
	    
	    // Create an HCFOrder object with the given parameters
	    HCFOrder hcfOrder = new HCFOrder(asc, field, limit, offset);
	    
	    // Add the HCFOrder to a list of orders
	    List<HCFOrder> hcfOrders = new ArrayList<>();
	    hcfOrders.add(hcfOrder);
	    
	    // Perform the search with the specified order and pagination
	    List<Data> datas = new HCFConnection<>(Data.class).search(hcfOrders);
	    
	    // Assert that the result list contains the expected number of entries
	    assertEquals(5, datas.size(), "The result list should contain 5 entries.");
	    
	    // Assert that the first entry in the result list has the expected name
	    assertEquals("User 40", datas.getFirst().getName(), "The first entry should be 'User 40'.");
	    
	    // Assert that the last entry in the result list has the expected name
	    assertEquals("User 36", datas.getLast().getName(), "The last entry should be 'User 36'.");
	}
	
	@Test
	void testBigSearch() {
	    String sql = "SELECT\r\n"
	            + "    d.id,\r\n"
	            + "    d.active,\r\n"
	            + "    d.age,\r\n"
	            + "    d.name,\r\n"
	            + "    d.registrationDate,\r\n"
	            + "    d.salary \r\n"
	            + "FROM\r\n"
	            + "    Data d \r\n"
	            + "WHERE\r\n"
	            + "    (\r\n"
	            + "        LENGTH(TRIM(BOTH FROM d.name)) = 0 \r\n"
	            + "        AND d.age IS NULL \r\n"
	            + "        OR LENGTH(TRIM(BOTH FROM d.name)) != 0\r\n"
	            + "    ) \r\n"
	            + "    AND d.name LIKE 'User 2%' \r\n"
	            + "    AND d.name NOT LIKE 'User 1%' \r\n"
	            + "    AND d.name NOT LIKE 'User 3%' \r\n"
	            + "    AND d.name NOT LIKE 'User 4%' \r\n"
	            + "    AND d.name NOT LIKE 'User 5%' \r\n"
	            + "    AND d.name <> 'User 2' \r\n"
	            + "    AND d.active IS NOT NULL \r\n"
	            + "    AND d.registrationDate < '2025-08-30 14:15:00' \r\n"
	            + "    AND NOT d.active;";

	    List<Data> datasBySQL = new HCFConnection<>(Data.class).getObjectBySQL(sql);

	    List<Data> datas = new HCFConnection<>(Data.class).search(null,
	            "name", null, HCFParameter.EMPTY, HCFOperator.NONE,
	            "age", null, HCFParameter.IS_NULL, HCFOperator.AND,
	            "name", null, HCFParameter.NOT_EMPTY, HCFOperator.OR,
	            "name", "User 2%", HCFParameter.LIKE, HCFOperator.AND,
	            "name", "User 1%", HCFParameter.NOT_LIKE, HCFOperator.AND,
	            "name", "User 3%", HCFParameter.NOT_LIKE, HCFOperator.AND,
	            "name", "User 4%", HCFParameter.NOT_LIKE, HCFOperator.AND,
	            "name", "User 5%", HCFParameter.NOT_LIKE, HCFOperator.AND,
	            "name", "User 2", HCFParameter.NOT_EQUAL, HCFOperator.AND,
	            "active", null, HCFParameter.IS_NOT_NULL, HCFOperator.AND,
	            "registrationDate", LocalDateTime.of(2025, 8, 30, 14, 15), HCFParameter.LESS_THAN, HCFOperator.AND,
	            "active", null, HCFParameter.FALSE, HCFOperator.AND);

	    assertEquals(datasBySQL.size(), datas.size(), "The size of the lists should be the same.");
	    assertTrue(datasBySQL.containsAll(datas), "The data list should contain all elements from the SQL query result.");
	    assertTrue(datas.containsAll(datasBySQL), "The SQL query result should contain all elements from the data list.");
	}
	
	@Test
	void testConstructors() throws Exception {
	    // Test using direct connection with DriverManager
	    long count = new HCFConnection<>(Data.class, DriverManager.getConnection("jdbc:h2:mem:hcf", "sa", null)).count();
	    // Assert that count is 50, if not, there may be issues with the connection or entity mapping
	    assertEquals(50, count, "Expected count to be 50 using DriverManager connection, but got " + count);
	    
	    // Test using the HCFFactory connection factory
	    count = new HCFConnection<>(Data.class, HCFFactory.INSTANCE.getFactory()).count();
	    // Assert that count is 50, if not, there may be issues with the factory configuration
	    assertEquals(50, count, "Expected count to be 50 using HCFFactory connection factory, but got " + count);
	    
	    // Test using a session opened by the factory
	    count = new HCFConnection<>(Data.class, HCFFactory.INSTANCE.getFactory().openSession()).count();
	    // Assert that count is 50, if not, there may be issues with opening the session or transaction
	    assertEquals(50, count, "Expected count to be 50 using session opened by HCFFactory, but got " + count);
	}
	
	@Test
    void testGetNewFactory() {
        Map<String, String> propertiesInMap = new HashMap<>();
        propertiesInMap.put("hibernate.connection.url", "jdbc:h2:mem:hcf");
        propertiesInMap.put("hibernate.connection.username", "sa");
        propertiesInMap.put("hibernate.connection.password", "");
        propertiesInMap.put("hibernate.connection.driver_class", "org.h2.Driver");

        // Test using properties map
        SessionFactory newFactory = HCFFactory.INSTANCE.getNewFactory(propertiesInMap, null, false, true, true, null, null);
        long count = new HCFConnection<>(Data.class, newFactory).count();
        assertEquals(50, count, "Expected count to be 50 using HCFFactory connection factory, but got " + count);

        // Test using hibernate.properties file
        newFactory = HCFFactory.INSTANCE.getNewFactory(null, "hibernate.properties", false, false, true, null, null);
        count = new HCFConnection<>(Data.class, newFactory).count();
        assertEquals(50, count, "Expected count to be 50 using HCFFactory connection factory, but got " + count);

        // Test with package scanning
        newFactory = HCFFactory.INSTANCE.getNewFactory(null, "hibernate.properties", false, false, false, Package.getPackages(), null);
        count = new HCFConnection<>(Data.class, newFactory).count();
        assertEquals(50, count, "Expected count to be 50 using HCFFactory connection factory, but got " + count);

        // Test with specific classes
        Set<Class<?>> classes = new HashSet<>();
        classes.add(Data.class);

        newFactory = HCFFactory.INSTANCE.getNewFactory(null, "hibernate.properties", false, false, false, null, classes);
        count = new HCFConnection<>(Data.class, newFactory).count();
        assertEquals(50, count, "Expected count to be 50 using HCFFactory connection factory, but got " + count);
    }

	@Test
	void testUtilGetId() {
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> HCFUtil.getId(DataWithOutId.class),
				"Expected an error because the class does not have " + Id.class);
		assertTrue(thrown.getMessage().contains("not found in fields of"));
	}
	
	@Test
	void testJoin() {

		List<Data> datas = new HCFConnection<>(Data.class).all();
		Data data1 = datas.get(0);
		Data data2 = datas.get(1);
		Data data3 = datas.get(2);

		Permission permission1 = new Permission(data1.getId(), "admin");
		Permission permission2 = new Permission(data1.getId(), "reviser");
		Permission permission3 = new Permission(data1.getId(), "user");
		Permission permission4 = new Permission(data2.getId(), "reviser");
		Permission permission5 = new Permission(data2.getId(), "user");
		Permission permission6 = new Permission(data3.getId(), "user");

		new HCFConnection<>(Permission.class).save(Arrays.asList(permission1, permission2, permission3, permission4, permission5, permission6));

		HCFOrder hcfOrder = new HCFOrder().setAsc(false).setField("id");

		// Configure the search parameters
		HCFSearch search = new HCFSearch()
				.setField("id")
				.setValue(0)
				.setParameter(HCFParameter.NOT_EQUAL)
				.setOperator(HCFOperator.NONE);

		// Configure the join parameters
		HCFJoinSearch joins = new HCFJoinSearch()
				.setPrimaryField("id")
				.setForeignField("idUser")
				.setValue(1)
				.setJoinClass(Permission.class);

		// Execute the search with join
		List<Object[]> result = new HCFConnection<>(Data.class).searchWithJoin(List.of(hcfOrder), List.of(search), List.of(joins));

		// Verify the results
		assertNotNull(result, "The result of the join search should not be null.");
		assertFalse(result.isEmpty(), "The result of the join search should not be empty.");
		assertEquals(6, result.size(), "The join search result should have 6 results.");

		for (Object[] columns : result) {
			boolean hasData = false;
			boolean hasPermission = false;

			for (Object column : columns) {
				if (column instanceof Data) {
					hasData = true;
				} else if (column instanceof Permission) {
					hasPermission = true;
				}
			}

			assertTrue(hasData, "The result should contain a Data object.");
			assertTrue(hasPermission, "The result should contain a Permission object.");
		}
	}
	
}