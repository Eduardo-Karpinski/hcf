package com.hcf.test.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String description;

	@Column(precision = 18, scale = 2, nullable = false)
	private BigDecimal amount;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	public Order() {}

	public Order(String description, BigDecimal amount, LocalDateTime createdAt) {
		this.description = description;
		this.amount = amount;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public Order setDescription(String description) {
		this.description = description;
		return this;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Order setAmount(BigDecimal amount) {
		this.amount = amount;
		return this;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Order setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public Customer getCustomer() {
		return customer;
	}

	public Order setCustomer(Customer customer) {
		this.customer = customer;
		return this;
	}
	
}