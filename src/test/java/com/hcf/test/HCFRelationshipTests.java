package com.hcf.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hcf.test.entities.TestEntitiyChildren;
import com.hcf.test.entities.TestEntitiyParent;

import br.com.hcf.HCFConnection;

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
		
		System.out.println(parent.getId());
		System.out.println(parent.getName());
		/**
		 * use @HCFRelationship on the parent entity to avoid: LazyInitializationException
		 * or
		 * hibernate.enable_lazy_load_no_trans=true (not recommended)
		 */
		System.out.println(parent.getChildren());
		
		new HCFConnection<>(TestEntitiyParent.class).delete(parent);

	}
	
}