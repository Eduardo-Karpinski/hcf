package com.hcf.test.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Data {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private Integer age;
	private BigDecimal salary;
	private LocalDateTime registrationDate;
	private Boolean active;

	public Data() {

	}

	public Data(Data data) {
		this.name = data.getName();
		this.age = data.getAge();
		this.salary = data.getSalary();
		this.registrationDate = data.getRegistrationDate();
		this.active = data.getActive();
	}
	
	public Data(String name, Integer age, BigDecimal salary, LocalDateTime registrationDate, Boolean active) {
		this.name = name;
		this.age = age;
		this.salary = salary;
		this.registrationDate = registrationDate;
		this.active = active;
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

	public LocalDateTime getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(LocalDateTime registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		return Objects.hash(active, age, id, name, registrationDate, salary);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Data other = (Data) obj;
		return Objects.equals(active, other.active) && Objects.equals(age, other.age) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name) && Objects.equals(registrationDate, other.registrationDate)
				&& Objects.equals(salary, other.salary);
	}

	@Override
	public String toString() {
		return "Data [id=" + id + ", name=" + name + ", age=" + age + ", salary=" + salary + ", registrationDate="
				+ registrationDate + ", active=" + active + "]";
	}

}