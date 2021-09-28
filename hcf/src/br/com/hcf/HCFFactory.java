package br.com.hcf;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import br.com.hcf.utils.HCFUtil;

public final class HCFFactory {

	private static SessionFactory sessionFactory = null;
	private static String propertiesPath = "hibernate.properties";
	private static boolean internal = true;
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
		
		System.err.println("################################################");
		System.err.println("Hibernate Connection facilitator - Version 3.4.1");
		System.err.println("Eduardo William - karpinskipriester@gmail.com");
		System.err.println("################################################");
	}
	
	private HCFFactory() {

	}

	public static SessionFactory getFactory() {
		if (sessionFactory == null || sessionFactory.isClosed()) {
			StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
			if (internal) {
				registryBuilder.loadProperties(propertiesPath);
			} else {
				registryBuilder.loadProperties(new File(propertiesPath));
			}
			StandardServiceRegistry registry = registryBuilder.build();
			try {
				MetadataSources metadataSources = new MetadataSources(registry);
				HCFUtil.getAnnotatedClasses().forEach(c -> metadataSources.addAnnotatedClass(c));
				System.err.println("HCF Annotated Classes - " + metadataSources.getAnnotatedClasses());
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
	
	public static SessionFactory getNewFactory(Map<String, String> propertiesInMap,
			String propertiesPath,
			boolean isFile,
			boolean replaceCurrent,
			boolean useHCFClassCollector,
			Package[] packages,
			Set<Class<?>> classes) {

		Configuration conf = new Configuration();
		
		if (propertiesInMap == null) {
			StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
			Properties properties;
			if (isFile) {
				properties = registryBuilder.getConfigLoader().loadProperties(new File(propertiesPath));
			} else {
				properties = registryBuilder.getConfigLoader().loadProperties(propertiesPath);
			}
			properties.forEach((k, v) -> conf.setProperty(k.toString(), v.toString()));
		} else {
			propertiesInMap.forEach((k, v) -> conf.setProperty(k, v));
		}

		if (useHCFClassCollector) {
			HCFUtil.getAnnotatedClasses().forEach(c -> conf.addAnnotatedClass(c));
		} else {
			if (packages != null) {
				for (Package p : packages) {
					conf.addPackage(p.getName());
				}
			}
			Optional.ofNullable(classes).ifPresent(classesOptional -> classesOptional.forEach(c -> conf.addAnnotatedClass(c)));
		}
		
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
			if (sessionFactory != null && sessionFactory.isOpen()) {
				sessionFactory.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sessionFactory = null;
		}
	}

}