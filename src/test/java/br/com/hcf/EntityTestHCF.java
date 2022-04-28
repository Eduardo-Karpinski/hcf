package br.com.hcf;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import br.com.hcf.annotations.HCFRelationship;

@Entity
@HCFRelationship
public class EntityTestHCF {

	@Id
	private Long id;
	private String name;
	private LocalDateTime dateTime;
	@OneToMany(cascade = CascadeType.ALL)
	private List<EntityTestHCFSon> children = new ArrayList<>();

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

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public List<EntityTestHCFSon> getChildren() {
		return children;
	}

	public void setChildren(List<EntityTestHCFSon> children) {
		this.children = children;
	}

}