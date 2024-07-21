package com.hcf.test.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class DataWithOutId {

	private Long id;
	private String name;
	private Integer age;
	private BigDecimal salary;
	private LocalDateTime registrationDate;
	private Boolean active;

	public DataWithOutId() {

	}

	public DataWithOutId(DataWithOutId data) {
		this.name = data.getName();
		this.age = data.getAge();
		this.salary = data.getSalary();
		this.registrationDate = data.getRegistrationDate();
		this.active = data.getActive();
	}
	
	public DataWithOutId(String name, Integer age, BigDecimal salary, LocalDateTime registrationDate, Boolean active) {
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
		DataWithOutId other = (DataWithOutId) obj;
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