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
		Set<String> packages = Arrays.asList(Package.getPackages()).stream()
				.map(Package::getName)
				.filter(HCFUtil::checkPackageName)
				.collect(Collectors.toSet());
		
		HCFFactory.getLogger().info("Packages To Read - " + packages);
		Reflections reflections = new Reflections(packages);
		Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Entity.class);
		HCFFactory.getLogger().info("Annotated Classes - " + typesAnnotatedWith);
		return typesAnnotatedWith;
	}
	
	// i need to make it automatic
	private static boolean checkPackageName(String name) {
		return !name.startsWith("br.com.hcf") 
				&& !name.startsWith("ch.qos") 
				&& !name.startsWith("com.fasterxml")
				&& !name.startsWith("com.sun") 
				&& !name.startsWith("com.mysql") 
				&& !name.startsWith("io.jaegertracing")
				&& !name.startsWith("io.smallrye") 
				&& !name.startsWith("io.undertow") 
				&& !name.startsWith("java")
				&& !name.startsWith("jakarta") 
				&& !name.startsWith("jdk")
				&& !name.startsWith("net.bytebuddy")
				&& !name.startsWith("org.apache") 
				&& !name.startsWith("org.eclipse")
				&& !name.startsWith("org.glassfish") 
				&& !name.startsWith("org.graalvm")
				&& !name.startsWith("org.hibernate") 
				&& !name.startsWith("org.ietf") 
				&& !name.startsWith("org.jberet")
				&& !name.startsWith("org.jcp") 
				&& !name.startsWith("org.jboss") 
				&& !name.startsWith("org.reflections")
				&& !name.startsWith("org.picketbox") 
				&& !name.startsWith("org.slf4j")
				&& !name.startsWith("org.springframework") 
				&& !name.startsWith("org.wildfly")
				&& !name.startsWith("org.w3c.dom") 
				&& !name.startsWith("org.xml") 
				&& !name.startsWith("org.yaml")
				&& !name.startsWith("org.junit")
				&& !name.startsWith("org.apiguardian")
				&& !name.startsWith("org.opentest4j")
				&& !name.startsWith("junit.runner")
				&& !name.startsWith("org.hamcrest")
				&& !name.startsWith("org.junit")
				&& !name.startsWith("sun");
	}

}