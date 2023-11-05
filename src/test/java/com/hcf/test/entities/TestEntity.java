package com.hcf.test.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TestEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private Integer age;
	private BigDecimal salary;
	private LocalDateTime birthDate;
	private Boolean isAdmin;

	public TestEntity() {
	}

	public TestEntity(Long id, String name, Integer age, BigDecimal salary, LocalDateTime birthDate, Boolean isAdmin) {
		this.id = id;
		this.name = name;
		this.age = age;
		this.salary = salary;
		this.birthDate = birthDate;
		this.isAdmin = isAdmin;
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

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public BigDecimal getSalary() {
		return salary;
	}

	public void setSalary(BigDecimal salary) {
		this.salary = salary;
	}

	public LocalDateTime getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDateTime birthDate) {
		this.birthDate = birthDate;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	@Override
	public int hashCode() {
		return Objects.hash(age, birthDate, id, isAdmin, name, salary);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestEntity other = (TestEntity) obj;
		return Objects.equals(age, other.age) && Objects.equals(birthDate, other.birthDate)
				&& Objects.equals(id, other.id) && Objects.equals(isAdmin, other.isAdmin)
				&& Objects.equals(name, other.name) && Objects.equals(salary, other.salary);
	}

	@Override
	public String toString() {
		return "TestEntity [id=" + id + ", name=" + name + ", age=" + age + ", salary=" + salary + ", birthDate="
				+ birthDate + ", isAdmin=" + isAdmin + "]";
	}

}