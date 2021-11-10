package br.com.hcf;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import br.com.hcf.utils.HCFUtil;

public final class HCFFactory {

	private static boolean internal = true;
	private static SessionFactory sessionFactory = null;
	private static final HCFFactory instance = new HCFFactory();
	private static String propertiesPath = "hibernate.properties";
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> HCFFactory.getInstance().shutdown()));
		
		HCFUtil.getLogger().info("################################################");
		HCFUtil.getLogger().info("Hibernate Connection facilitator - Version 3.4.4");
		HCFUtil.getLogger().info("Eduardo William - karpinskipriester@gmail.com");
		HCFUtil.getLogger().info("################################################");
	}
	
	private HCFFactory() {
		
	}
	
	public SessionFactory getFactory() {
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
				sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
				StandardServiceRegistryBuilder.destroy(registry);
			}
		}
		return sessionFactory;
	}

	public void setDirectoryProperties(String pathname, boolean isInternal) {
		propertiesPath = pathname;
		internal = isInternal;
	}
	
	public SessionFactory getNewFactory(Map<String, String> propertiesInMap,
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
				Set<String> names = Arrays.asList(packages).stream()
						.map(Package::getName)
						.collect(Collectors.toSet());
				HCFUtil.getLogger().info("Packages To Read - " + names);
				HCFUtil.getEntities(names).forEach(e -> conf.addAnnotatedClass(e));
			}
			Optional.ofNullable(classes).ifPresent(classesOptional -> classesOptional.forEach(c -> conf.addAnnotatedClass(c)));
		}
		
		SessionFactory newFactory = conf.buildSessionFactory();
		
		if (replaceCurrent) {
			shutdown();
			sessionFactory = newFactory;
			instance.getAnnotatedClasses();
			return sessionFactory;
		}
		
		return newFactory;
	}

	public void getAnnotatedClasses() {
		EntityManager em = null;
		try {
			em = sessionFactory.createEntityManager();
			HCFUtil.getLogger().info("Annotated Classes - " + em.getMetamodel().getEntities());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void shutdown() {
		try {
			if (sessionFactory != null && sessionFactory.isOpen()) {
				sessionFactory.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static HCFFactory getInstance() {
		return instance;
	}
	
}