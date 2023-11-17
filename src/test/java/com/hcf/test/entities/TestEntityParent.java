package com.hcf.test.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import com.hcf.annotations.HCFRelationship;

@Entity
@HCFRelationship
public class TestEntityParent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    private List<TestEntityChildren> children = new ArrayList<>();

    public TestEntityParent() {

    }

    public TestEntityParent(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TestEntityChildren> getChildren() {
        return children;
    }

    public void setChildren(List<TestEntityChildren> children) {
        this.children = children;
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestEntityParent other = (TestEntityParent) obj;
        return Objects.equals(children, other.children) && Objects.equals(id, other.id)
                && Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return "TestEntityParent [id=" + id + ", name=" + name + ", children=" + children + "]";
    }

}