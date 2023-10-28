package com.hcf.test.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.hcf.annotations.HCFRelationship;

@Entity
@HCFRelationship
public class TestEntitiyParent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@OneToMany(cascade = CascadeType.ALL)
	private List<TestEntitiyChildren> children = new ArrayList<>();

	public TestEntitiyParent() {

	}

	public TestEntitiyParent(Long id, String name) {
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

	public List<TestEntitiyChildren> getChildren() {
		return children;
	}

	public void setChildren(List<TestEntitiyChildren> children) {
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
		TestEntitiyParent other = (TestEntitiyParent) obj;
		return Objects.equals(children, other.children) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "TestEntitiyParent [id=" + id + ", name=" + name + ", children=" + children + "]";
	}

}