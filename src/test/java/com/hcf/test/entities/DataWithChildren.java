package com.hcf.test.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hcf.annotations.HCFRelationship;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
@HCFRelationship
public class DataWithChildren {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@OneToMany(cascade = CascadeType.ALL)
	private List<DataChild> children;
	
	public DataWithChildren() {
		this.children = new ArrayList<>();
	}

	public DataWithChildren(String name) {
		this.name = name;
		this.children = new ArrayList<>();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DataChild> getChildren() {
		return children;
	}

	public void setChildren(List<DataChild> children) {
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
		DataWithChildren other = (DataWithChildren) obj;
		return Objects.equals(children, other.children) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "DataWithChildren [id=" + id + ", name=" + name + ", children=" + children + "]";
	}

}