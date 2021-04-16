package br.com.hcf;

import java.io.File;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import br.com.hcf.utils.HCFUtil;

public final class HCFactory {

	private static SessionFactory sessionFactory = null;
	private static String propertiesPath = "hibernate.properties";
	private static boolean internal = true;
	
	private HCFactory() {

	}

	public static SessionFactory getFactory() {
		if (sessionFactory == null) {
			StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
			if (internal) {
				registryBuilder.loadProperties(propertiesPath);
			} else {
				registryBuilder.loadProperties(new File(propertiesPath));
			}
			StandardServiceRegistry registry = registryBuilder.build();
			try {
				MetadataSources metadataSources = new MetadataSources(registry);
				
				HCFUtil.getAnnotatedClasses().forEach(c -> {
					metadataSources.addAnnotatedClass(c);
				});
				
				sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
				StandardServiceRegistryBuilder.destroy(registry);
			}
		}
		return sessionFactory;
	}

	public static void setDirectoryProperties(String pathname, boolean isInternal) {
		propertiesPath = pathname;
		internal = isInternal;
	}

	public static SessionFactory getNewFactory(Map<String, String> properties, boolean replaceCurrent) {
		Configuration conf = new Configuration();
		
		properties.forEach((k, v) -> {
			conf.setProperty(k, v);
		});
		
		HCFUtil.getAnnotatedClasses().forEach(c -> {
			conf.addAnnotatedClass(c);
		});
		
		SessionFactory newFactory = conf.buildSessionFactory();
		
		if (replaceCurrent) {
			shutdown();
			sessionFactory = newFactory;
			return sessionFactory;
		}
		
		return newFactory;
	}

	public static void getAnnotatedClasses() {
		EntityManager em = null;
		try {
			em = sessionFactory.createEntityManager();
			em.getMetamodel().getEntities().forEach(System.out::println);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public static void shutdown() {
		try {
			if (sessionFactory != null) {
				sessionFactory.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
