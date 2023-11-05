package com.hcf.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import org.reflections.Reflections;

public final class HCFUtil {
	
	private static final Logger logger = Logger.getLogger("HCF");
	
	private HCFUtil() {

	}
	
	public static <T> String getId(Class<T> classe) {
		Field[] fields = classe.getDeclaredFields();
		
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].isAnnotationPresent(Id.class)) {
				return fields[i].getName();
			}
		}
			
		if (classe.getSuperclass().equals(Object.class)) {
			throw new RuntimeException(Id.class + " not found in fields of " + classe);
		}
			
		return getId(classe.getSuperclass());
	}
		
	public static Set<Class<?>> getAnnotatedClasses() {
		Set<String> packages = Arrays.asList(Thread.currentThread().getContextClassLoader().getDefinedPackages()).stream()
				.map(Package::getName)
				.collect(Collectors.toSet());
		HCFUtil.getLogger().info("Packages To Read - " + packages);
		Set<Class<?>> typesAnnotatedWith = getEntities(packages);
		HCFUtil.getLogger().info("Annotated Classes - " + typesAnnotatedWith);
		return typesAnnotatedWith;
	}

	public static Set<Class<?>> getEntities(Set<String> packages) {
		Reflections reflections = new Reflections(packages);
		return reflections.getTypesAnnotatedWith(Entity.class);
	}
	
	public static Logger getLogger() {
		return logger;
	}
}