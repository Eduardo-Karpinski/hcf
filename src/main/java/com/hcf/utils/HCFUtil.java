package com.hcf.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.hcf.core.HCFEntityProvider;
import com.hcf.query.HCFSearch;
import com.hcf.query.enums.HCFOperator;
import com.hcf.query.enums.HCFParameter;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import jakarta.persistence.Entity;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.Metamodel;

public final class HCFUtil {

	private static final Logger LOGGER = Logger.getLogger("HCF");

	private static final String[] DEFAULT_PACKAGE_EXCLUDES = { "java.", "javax.", "jakarta.", "org.hibernate.",
			"org.jboss.", "org.junit.", "org.testng.", "org.slf4j.", "ch.qos.logback.", "com.fasterxml.",
			"io.github.classgraph.", "com.mysql.cj.", "org.eclipse", "org.mariadb.jdbc.", "org.postgresql.",
			"com.microsoft.sqlserver.", "oracle.jdbc.", "oracle.ucp.", "org.h2.", "org.sqlite.", "org.apache",
			"com.ibm.db2.", "org.firebirdsql.", "nonapi.", "org.antlr.", "org.glassfish.", "net.bytebuddy.",
			"org.apiguardian.", "org.opentest4j.", "sun.", "com.sun.", "org.springframework.", "org.eclipse." };

	private HCFUtil() {

	}

	public static Set<Class<?>> getHCFEntityProviderImpl() {
		ServiceLoader<HCFEntityProvider> serviceLoader = ServiceLoader.load(HCFEntityProvider.class, Thread.currentThread().getContextClassLoader());
		return serviceLoader.stream()
				.map(ServiceLoader.Provider::get)
				.flatMap(provider -> provider.getEntities().stream())
				.collect(Collectors.toSet());
	}

	public static List<HCFSearch> varargsToSearch(Object... parameters) {
		if (parameters.length % 4 != 0) {
			throw new IllegalArgumentException("Parameters is not a multiple of 4.");
		}

		List<HCFSearch> hcfSearches = new ArrayList<>();

		for (int i = 0; i < parameters.length; i += 4) {
			hcfSearches.add(new HCFSearch(parameters[i].toString(), parameters[i + 1], (HCFParameter) parameters[i + 2], (HCFOperator) parameters[i + 3]));
		}

		return hcfSearches;
	}

	public static <T> String getIdFieldName(Session session, Class<T> clazz) {
		Metamodel metamodel = session.getEntityManagerFactory().getMetamodel();
		IdentifiableType<T> entity = (IdentifiableType<T>) metamodel.managedType(clazz);
		return entity.getId(entity.getIdType().getJavaType()).getName();
	}

	public static Set<Class<?>> getAnnotatedClasses() {
		Set<Class<?>> byServiceLoader = getHCFEntityProviderImpl();

		if (!byServiceLoader.isEmpty()) {
			return byServiceLoader;
		}

		Set<Class<?>> typesAnnotatedWith = getEntities();

		return typesAnnotatedWith;
	}

	public static Set<Class<?>> getEntities() {
		try (ScanResult scan = new ClassGraph()
				.enableAnnotationInfo()
				.ignoreClassVisibility()
				.rejectPackages(DEFAULT_PACKAGE_EXCLUDES)
				.scan()) {

			List<String> roots = scan.getPackageInfo().getAsStringsWithSimpleNames().stream().limit(100).toList();
			LOGGER.info("[HCF-INFO] Scanned packages (" + roots.size() + "): " + roots);

			Set<Class<?>> entities = new HashSet<>(scan.getClassesWithAnnotation(Entity.class.getName()).loadClasses());

			LOGGER.info("[HCF-INFO] Annotated Classes - " + entities);
			return entities;
		}
	}

	public static Logger getLogger() {
		return LOGGER;
	}

}