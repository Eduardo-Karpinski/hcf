package com.hcf.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.hcf.utils.HCFLog;
import com.hcf.utils.HCFUtil;

public enum HCFFactory {
	INSTANCE;

	private boolean internal = true;
	private String propertiesPath = "hibernate.properties";
	private volatile SessionFactory sessionFactory = null;
	private volatile StandardServiceRegistry standardServiceRegistry;

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(HCFFactory.INSTANCE::shutdown));
	}

	public SessionFactory getFactory() {
		if (sessionFactory == null || sessionFactory.isClosed()) {
			synchronized (this) {
				if (sessionFactory == null || sessionFactory.isClosed()) {
					StandardServiceRegistryBuilder standardServiceRegistryBuilder = new StandardServiceRegistryBuilder();
					if (internal) {
						standardServiceRegistryBuilder.loadProperties(propertiesPath);
					} else {
						standardServiceRegistryBuilder.loadProperties(new File(propertiesPath));
					}
					standardServiceRegistry = standardServiceRegistryBuilder.build();
					try {
						MetadataSources metadataSources = new MetadataSources(standardServiceRegistry);
						HCFUtil.getAnnotatedClasses().forEach(metadataSources::addAnnotatedClass);
						sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
					} catch (Exception e) {
						HCFLog.showError(e);
						StandardServiceRegistryBuilder.destroy(standardServiceRegistry);
						standardServiceRegistry = null;
						throw new IllegalStateException("Failed to construct SessionFactory", e);
					}
				}
			}
		}
		return sessionFactory;
	}

	public synchronized void setDirectoryProperties(String pathname, boolean isInternal) {
		if (sessionFactory != null && !sessionFactory.isClosed()) {
			throw new IllegalStateException("Already initialized. Use getNewFactory(..., replaceCurrent=true).");
		}
		propertiesPath = pathname;
		internal = isInternal;
	}

	public SessionFactory getNewFactory(
			Map<String, String> propertiesInMap,
			String path,
			boolean replaceCurrent,
			boolean useHCFClassCollector,
			Set<Class<?>> classes) {
		
		Properties properties = new Properties();
		
		if (path != null && !path.isBlank()) {
		    properties.putAll(loadPath(path));
		}
		
		if (propertiesInMap != null && !propertiesInMap.isEmpty()) {
		    propertiesInMap.forEach(properties::setProperty);
		}

		if (properties.isEmpty()) {
			throw new IllegalStateException("No property was provided");
		}
		
		StandardServiceRegistryBuilder standardServiceRegistryBuilder = new StandardServiceRegistryBuilder().applySettings(properties);
		StandardServiceRegistry standardServiceRegistry = standardServiceRegistryBuilder.build();
		
		try {
			MetadataSources metadataSources = new MetadataSources(standardServiceRegistry);
			
			if (useHCFClassCollector) {
	            HCFUtil.getAnnotatedClasses().forEach(metadataSources::addAnnotatedClass);
	        }
			
			if (classes != null && !classes.isEmpty()) {
				classes.forEach(metadataSources::addAnnotatedClass);
			}
			
			SessionFactory sessionFactory = metadataSources.buildMetadata().buildSessionFactory();

			if (replaceCurrent) {
				synchronized (this) {
					shutdown();
					this.sessionFactory = sessionFactory;
					this.standardServiceRegistry = standardServiceRegistry;
				}
				return this.sessionFactory;
			}
			
			return sessionFactory;
		} catch (Exception e) {
			HCFLog.showError(e);
			StandardServiceRegistryBuilder.destroy(standardServiceRegistry);
			throw new IllegalStateException("Failed to construct SessionFactory", e);
		}
	}
	
	private Properties loadPath(String path) {
	    try {
	        Properties properties = new Properties();
	        File file = new File(path);
	        if (file.isFile()) {
	            try (InputStream inputStream = new FileInputStream(file)) {
	            	properties.load(inputStream);
	            }
	        } else {
	            try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
	                if (inputStream == null) {
	                	throw new FileNotFoundException("Classpath resource not found: " + path);
	                }
	                properties.load(inputStream);
	            }
	        }
	        return properties;
	    } catch (IOException e) {
	        throw new IllegalStateException("Failed to load properties from: " + path, e);
	    }
	}

	public void shutdown() {
		try {
			if (sessionFactory == null) {
				return;
			}

			if (sessionFactory.isOpen()) {
				sessionFactory.close();
			}

			if (sessionFactory.isClosed()) {
				StandardServiceRegistryBuilder.destroy(standardServiceRegistry);
				sessionFactory = null;
				standardServiceRegistry = null;
			}
		} catch (Exception e) {
			HCFLog.showError(e);
		}
	}

}