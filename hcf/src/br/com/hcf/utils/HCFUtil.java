package br.com.hcf.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.reflections8.Reflections;
import org.reflections8.scanners.FieldAnnotationsScanner;

import br.com.hcf.HCFFactory;

public final class HCFUtil {
	
	private HCFUtil() {

	}

	public static <T> String getId(Class<T> classe) {
		Reflections reflections = new Reflections(classe, new FieldAnnotationsScanner());
		Set<Field> ids = reflections.getFieldsAnnotatedWith(Id.class);
		return ids.isEmpty() ? getId(classe.getSuperclass()) : ids.iterator().next().getName();
	}

	public static Set<Class<?>> getAnnotatedClasses() {
		Set<String> packages = Arrays.asList(Thread.currentThread().getContextClassLoader().getDefinedPackages()).stream()
				.map(Package::getName)
				.collect(Collectors.toSet());
		HCFFactory.getInstance().getLogger().info("Packages To Read - " + packages);
		Set<Class<?>> typesAnnotatedWith = getEntities(packages);
		HCFFactory.getInstance().getLogger().info("Annotated Classes - " + typesAnnotatedWith);
		return typesAnnotatedWith;
	}

	public static Set<Class<?>> getEntities(Set<String> packages) {
		Reflections reflections = new Reflections(packages);
		return reflections.getTypesAnnotatedWith(Entity.class);
	}
}