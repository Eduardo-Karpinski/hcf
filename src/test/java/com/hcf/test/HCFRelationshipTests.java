package com.hcf.test;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hcf.HCFConnection;
import com.hcf.test.entities.TestEntitiyChildren;
import com.hcf.test.entities.TestEntitiyParent;

/**
 * use @HCFRelationship on the parent entity to avoid: LazyInitializationException
 * or
 * hibernate.enable_lazy_load_no_trans=true (not recommended)
 */
@DisplayName("HCFRelationshipTests")
class HCFRelationshipTests {
	
	@Test
	void getParentWithChildren() {
		
		TestEntitiyParent parent = new TestEntitiyParent(null, "Parent 1");
		
		TestEntitiyChildren children1 = new TestEntitiyChildren(null, "Children 1");
		TestEntitiyChildren children2 = new TestEntitiyChildren(null, "Children 2");
		TestEntitiyChildren children3 = new TestEntitiyChildren(null, "Children 3");
		
		parent.getChildren().add(children1);
		parent.getChildren().add(children2);
		parent.getChildren().add(children3);
		
		new HCFConnection<>(TestEntitiyParent.class).save(parent);
		
		parent = new HCFConnection<>(TestEntitiyParent.class).getById(parent.getId());
		
		assertTrue(parent.getChildren().size() == 3);
		
		new HCFConnection<>(TestEntitiyParent.class).delete(parent);

	}
	
	@Test
	void getChildrenByParent() {
		
		TestEntitiyParent parent = new TestEntitiyParent(null, "Parent 1");
		
		TestEntitiyChildren children1 = new TestEntitiyChildren(null, "Children 1");
		TestEntitiyChildren children2 = new TestEntitiyChildren(null, "Children 2");
		TestEntitiyChildren children3 = new TestEntitiyChildren(null, "Children 3");
		
		parent.getChildren().add(children1);
		parent.getChildren().add(children2);
		parent.getChildren().add(children3);
		
		new HCFConnection<>(TestEntitiyParent.class).save(parent);
		
		List<TestEntitiyChildren> ChildrenByParent = new HCFConnection<>(TestEntitiyChildren.class).getRelations(TestEntitiyParent.class, "children", parent.getId());
		
		assertIterableEquals(parent.getChildren(), ChildrenByParent);
		
		new HCFConnection<>(TestEntitiyParent.class).delete(parent);
	}
	
	@Test
	void getParentByChildren() {
		
		TestEntitiyParent parent = new TestEntitiyParent(null, "Parent 1");
		
		TestEntitiyChildren children1 = new TestEntitiyChildren(null, "Children 1");
		TestEntitiyChildren children2 = new TestEntitiyChildren(null, "Children 2");
		TestEntitiyChildren children3 = new TestEntitiyChildren(null, "Children 3");
		
		parent.getChildren().add(children1);
		parent.getChildren().add(children2);
		parent.getChildren().add(children3);
		
		new HCFConnection<>(TestEntitiyParent.class).save(parent);
		
		TestEntitiyParent Parent1 = new HCFConnection<>(TestEntitiyParent.class).getByInvertedRelation(TestEntitiyChildren.class, "children", children1.getId()).get(0);
		TestEntitiyParent Parent2 = new HCFConnection<>(TestEntitiyParent.class).getByInvertedRelation(TestEntitiyChildren.class, "children", children2.getId()).get(0);
		TestEntitiyParent Parent3 = new HCFConnection<>(TestEntitiyParent.class).getByInvertedRelation(TestEntitiyChildren.class, "children", children3.getId()).get(0);
		
		assertTrue(Parent1.equals(Parent2) && Parent2.equals(Parent3));
		
		new HCFConnection<>(TestEntitiyParent.class).delete(parent);
	}
	
}