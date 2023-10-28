package com.hcf.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hcf.HCFConnection;
import com.hcf.HCFFactory;
import com.hcf.test.entities.TestEntity;

@DisplayName("HCFConnectionTest")
class HCFConnectionTest {

	@Test
	void testConstructorWithPersistentClass() {
		long count = new HCFConnection<>(TestEntity.class).count();
		assertEquals(0, count);
	}

	@Test
	void testConstructorWithPersistentClassAndConnection() {
		long count = new HCFConnection<>(TestEntity.class, createConnection()).count();
		assertEquals(0, count);
	}

	@Test
	void testConstructorWithPersistentClassAndSessionFactory() {
		long count = new HCFConnection<>(TestEntity.class, createSessionFactory()).count();
		assertEquals(0, count);
	}

	@Test
	void testConstructorWithPersistentClassAndSession() {
		long count = new HCFConnection<>(TestEntity.class, createSession()).count();
		assertEquals(0, count);
	}

	private Connection createConnection() {
		Connection connection = null;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");

			String jdbcUrl = "jdbc:mysql://localhost:3306/hcf";
			String username = "root";
			String password = "root";

			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return connection;
	}

	private SessionFactory createSessionFactory() {
		return HCFFactory.getInstance().getFactory();
	}

	private Session createSession() {
		return HCFFactory.getInstance().getFactory().openSession();
	}

}