package com.hcf.test;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hcf.HCFConnection;
import com.hcf.test.entities.TestEntityChildren;
import com.hcf.test.entities.TestEntityParent;

/**
 * use @HCFRelationship on the parent entity to avoid: LazyInitializationException
 * or
 * hibernate.enable_lazy_load_no_trans=true (not recommended)
 */
@DisplayName("HCFRelationshipTests")
class HCFRelationshipTests {

    @Test
    void getParentWithChildren() {

        TestEntityParent parent = new TestEntityParent(null, "Parent 1");

        TestEntityChildren children1 = new TestEntityChildren(null, "Children 1");
        TestEntityChildren children2 = new TestEntityChildren(null, "Children 2");
        TestEntityChildren children3 = new TestEntityChildren(null, "Children 3");

        parent.getChildren().add(children1);
        parent.getChildren().add(children2);
        parent.getChildren().add(children3);

        new HCFConnection<>(TestEntityParent.class).save(parent);

        parent = new HCFConnection<>(TestEntityParent.class).getById(parent.getId());

        assertTrue(parent.getChildren().size() == 3);

        new HCFConnection<>(TestEntityParent.class).delete(parent);

    }

    @Test
    void getChildrenByParent() {

        TestEntityParent parent = new TestEntityParent(null, "Parent 1");

        TestEntityChildren children1 = new TestEntityChildren(null, "Children 1");
        TestEntityChildren children2 = new TestEntityChildren(null, "Children 2");
        TestEntityChildren children3 = new TestEntityChildren(null, "Children 3");

        parent.getChildren().add(children1);
        parent.getChildren().add(children2);
        parent.getChildren().add(children3);

        new HCFConnection<>(TestEntityParent.class).save(parent);

        List<TestEntityChildren> ChildrenByParent = new HCFConnection<>(TestEntityChildren.class).getRelations(TestEntityParent.class, "children", parent.getId());

        assertIterableEquals(parent.getChildren(), ChildrenByParent);

        new HCFConnection<>(TestEntityParent.class).delete(parent);
    }

    @Test
    void getParentByChildren() {

        TestEntityParent parent = new TestEntityParent(null, "Parent 1");

        TestEntityChildren children1 = new TestEntityChildren(null, "Children 1");
        TestEntityChildren children2 = new TestEntityChildren(null, "Children 2");
        TestEntityChildren children3 = new TestEntityChildren(null, "Children 3");

        parent.getChildren().add(children1);
        parent.getChildren().add(children2);
        parent.getChildren().add(children3);

        new HCFConnection<>(TestEntityParent.class).save(parent);

        TestEntityParent Parent1 = new HCFConnection<>(TestEntityParent.class).getByInvertedRelation(TestEntityChildren.class, "children", children1.getId()).get(0);
        TestEntityParent Parent2 = new HCFConnection<>(TestEntityParent.class).getByInvertedRelation(TestEntityChildren.class, "children", children2.getId()).get(0);
        TestEntityParent Parent3 = new HCFConnection<>(TestEntityParent.class).getByInvertedRelation(TestEntityChildren.class, "children", children3.getId()).get(0);

        assertTrue(Parent1.equals(Parent2) && Parent2.equals(Parent3));

        new HCFConnection<>(TestEntityParent.class).delete(parent);
    }

}