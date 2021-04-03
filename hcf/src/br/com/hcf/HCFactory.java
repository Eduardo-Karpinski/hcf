package br.com.hcf;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import br.com.hcf.utils.HCFUtil;

public final class HCFactory {

	private static SessionFactory sessionFactory = null;
	private static Set<Class<?>> classes = new HashSet<>();
	private static String propertiesPath = "hibernate.properties";
	private static boolean internal = true;

	private HCFactory() {

	}

	public static SessionFactory getFactory() {
		if (sessionFactory == null) {
			HCFUtil.insertAnnotatedClasses();

			StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
			if (internal) {
				registryBuilder.loadProperties(propertiesPath);
			} else {
				registryBuilder.loadProperties(new File(propertiesPath));
			}
			StandardServiceRegistry registry = registryBuilder.build();
			try {
				MetadataSources metadataSources = new MetadataSources(registry);
				classes = classes.stream().distinct().collect(Collectors.toSet());
				classes.forEach(c -> {
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
		HCFUtil.insertAnnotatedClasses();
		classes.stream().distinct().forEach(c -> {
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

	public static void setAnnotatedClasses(Set<Class<?>> annotedClasses) {
		classes.addAll(annotedClasses);
	}

	public static void setAnnotatedClasses(Class<?> c) {
		classes.add(c);
	}

	public static void getAnnotatedClasses() {
		classes.stream().distinct().forEach(System.out::println);
	}

	public static void clearAnnotatedClasses() {
		classes = new HashSet<>();
	}

	public static void shutdown() {
		try {
			if (sessionFactory != null) {
				sessionFactory.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clearAnnotatedClasses();
		}
	}

}