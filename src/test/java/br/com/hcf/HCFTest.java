package br.com.hcf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@DisplayName("HCF tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HCFTest {
	
	@BeforeAll
    	static void initAll() {
		HCFConnection.getElementsBySQL("select 0;");
		HCFConnection.sendSQL("DELETE FROM EntityTestHCF_EntityTestHCFSon ");
		HCFConnection.sendSQL("DELETE FROM EntityTestHCFSon");
		HCFConnection.sendSQL("DELETE FROM EntityTestHCF");
	}

	@Test
	@Order(1)
	@DisplayName("save entity test")
	void saveEntity() {
		EntityTestHCF test = new EntityTestHCF();
		test.setId(1L);
		test.setName("HCF");
		test.setDateTime(LocalDateTime.now());
		new HCFConnection<>(EntityTestHCF.class).save(test);
		assertTrue(true);
	}
	
	@Test
	@Order(2)
	@DisplayName("update entity test")
	void updateEntity() {
		EntityTestHCF test = new HCFConnection<>(EntityTestHCF.class).getById(1);
		test.setName("new HCF");
		new HCFConnection<>(EntityTestHCF.class).save(test);
		
		test = new HCFConnection<>(EntityTestHCF.class).getById(1);
		assertEquals("new HCF", test.getName());
	}
	
	@Test
	@Order(3)
	@DisplayName("delete entity test")
	void deleteEntity() {
		EntityTestHCF test = new HCFConnection<>(EntityTestHCF.class).getById(1);
		new HCFConnection<>(EntityTestHCF.class).delete(test);
		assertEquals(0, new HCFConnection<>(EntityTestHCF.class).count());
	}
	
	@Test
	@Order(4)
	@DisplayName("get all entities")
	void listAll() {
		EntityTestHCF test1 = new EntityTestHCF();
		test1.setId(1L);
		EntityTestHCF test2 = new EntityTestHCF();
		test2.setId(2L);
		EntityTestHCF test3 = new EntityTestHCF();
		test3.setId(3L);
		EntityTestHCF test4 = new EntityTestHCF();
		test4.setId(4L);
		EntityTestHCF test5 = new EntityTestHCF();
		test5.setId(5L);
		
		new HCFConnection<>(EntityTestHCF.class).save(Arrays.asList(test1, test2, test3, test4, test5), true);
		
		List<EntityTestHCF> tests = new HCFConnection<>(EntityTestHCF.class).all();
		
		assertEquals(5, tests.size());
	}
	
	@Test
	@Order(5)
	@DisplayName("get by id")
	void getById() {
		EntityTestHCF test = new EntityTestHCF();
		test.setId(9999L);
		new HCFConnection<>(EntityTestHCF.class).save(test);
		
		test = new HCFConnection<>(EntityTestHCF.class).getById(9999);
		
		assertEquals(9999L, test.getId());
	}
	
	@Test
	@Order(6)
	@DisplayName("delete by id")
	void deleteById() {
		new HCFConnection<>(EntityTestHCF.class).deleteById(9999L);
		EntityTestHCF test = new HCFConnection<>(EntityTestHCF.class).getById(9999L);
		assertEquals(null, test);
	}
	
	@Test
	@Order(7)
	@DisplayName("Get first and last")
	void getFirstAndLast() {
		EntityTestHCF first = new HCFConnection<>(EntityTestHCF.class).getFirstOrLast(new HCFOrder(true, "id", null, null));
		EntityTestHCF last = new HCFConnection<>(EntityTestHCF.class).getFirstOrLast(new HCFOrder(false, "id", null, null));
		assertEquals(first.getId(), 1L);
		assertEquals(last.getId(), 5L);
	}
	
	@Test
	@Order(8)
	@DisplayName("Total records")
	void totalRecords() {
		long total = new HCFConnection<>(EntityTestHCF.class).count();
		assertEquals(total, 5L);
	}
	
	@Test
	@Order(9)
	@DisplayName("Get relations")
	void getRelations() {
		EntityTestHCF test = new HCFConnection<>(EntityTestHCF.class).getById(1);
		
		EntityTestHCFSon son1 = new EntityTestHCFSon();
		EntityTestHCFSon son2 = new EntityTestHCFSon();
		EntityTestHCFSon son3 = new EntityTestHCFSon();
		
		son1.setName("son1");
		son2.setName("son2");
		son3.setName("son3");
		
		test.getChildren().addAll(Arrays.asList(son1, son2, son3));
		
		new HCFConnection<>(EntityTestHCF.class).save(test);
		test = new HCFConnection<>(EntityTestHCF.class).getById(1);
		
		assertEquals(test.getChildren().size(), 3);
		
		List<EntityTestHCFSon> children = new HCFConnection<>(EntityTestHCFSon.class).getRelations(EntityTestHCF.class, "children", test.getId());
		assertEquals(children.size(), 3);
	}
	
	@Test
	@Order(10)
	@DisplayName("Get inverted relations")
	void getByInvertedRelation() {
		EntityTestHCFSon son = new HCFConnection<>(EntityTestHCFSon.class).getFirstOrLast(new HCFOrder(true, "id", null, null));
		EntityTestHCF father = new HCFConnection<>(EntityTestHCF.class).getByInvertedRelation(EntityTestHCFSon.class, "children", son.getId()).get(0);
		assertFalse(father.equals(null));
	}
	
}
