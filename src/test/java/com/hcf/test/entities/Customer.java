package com.hcf.test.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private Integer age;
	private Boolean active;

	@Column(nullable = false)
	private LocalDateTime registeredAt;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Order> orders = new ArrayList<>();

	public Customer() {}

	public Customer(String name, Integer age, Boolean active, LocalDateTime registeredAt) {
		this.name = name;
		this.age = age;
		this.active = active;
		this.registeredAt = registeredAt;
	}

	public void addOrder(Order order) {
		order.setCustomer(this);
		orders.add(order);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Customer setName(String name) {
		this.name = name;
		return this;
	}

	public Integer getAge() {
		return age;
	}

	public Customer setAge(Integer age) {
		this.age = age;
		return this;
	}

	public Boolean getActive() {
		return active;
	}

	public Customer setActive(Boolean active) {
		this.active = active;
		return this;
	}

	public LocalDateTime getRegisteredAt() {
		return registeredAt;
	}

	public Customer setRegisteredAt(LocalDateTime registeredAt) {
		this.registeredAt = registeredAt;
		return this;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public Customer setOrders(List<Order> orders) {
		this.orders = orders;
		return this;
	}
			
}