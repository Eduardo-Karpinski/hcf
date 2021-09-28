package br.com.hcf.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.reflections8.Reflections;

public final class HCFUtil {
	
	private HCFUtil() {

	}

	public static <T> String getId(Class<T> classe) {
		return Arrays.asList(classe.getDeclaredFields()).stream()
				.filter(f -> f.getAnnotation(Id.class) != null)
				.map(Field::getName)
				.findFirst().orElseGet(getFieldByFather(classe.getSuperclass()));
	}
	
	private static <T> Supplier<? extends String> getFieldByFather(Class<? super T> superclass) {
		return () -> Arrays.asList(superclass.getDeclaredFields()).stream()
				.filter(f -> f.getAnnotation(Id.class) != null)
				.map(Field::getName)
				.findFirst().orElseThrow(() -> new NullPointerException("Undeclared id"));
	}

	public static Set<Class<?>> getAnnotatedClasses() {
		
		Arrays.asList(Package.getPackages()).stream()
		.map(Package::getName)
		.filter(HCFUtil::checkPackageName)
		.collect(Collectors.toSet()).forEach(System.out::println);
		
		Reflections reflections = new Reflections(Arrays.asList(Package.getPackages()).stream()
				.map(Package::getName)
				.filter(HCFUtil::checkPackageName)
				.collect(Collectors.toSet()));
		return reflections.getTypesAnnotatedWith(Entity.class);
	}
	
	/**
	 * org.jboss.security.acl.ACLImpl and
	 * org.jboss.security.acl.ACLEntryImpl are an entity,
	 * the rest is unnecessary for reading.
	 */
	private static boolean checkPackageName(String name) {
		return !name.startsWith("br.com.hcf") &&
				!name.startsWith("ch.qos") &&
				!name.startsWith("com.fasterxml") &&
				!name.startsWith("com.sun") &&
				!name.startsWith("com.mysql") &&
				!name.startsWith("io.jaegertracing") &&
				!name.startsWith("io.smallrye") &&
				!name.startsWith("io.undertow") &&
				!name.startsWith("java") &&
				!name.startsWith("jakarta") &&
				!name.startsWith("jdk") &&
				!name.startsWith("net.bytebuddy") &&
				!name.startsWith("org.apache") &&
				!name.startsWith("org.eclipse") &&
				!name.startsWith("org.glassfish") &&
				!name.startsWith("org.graalvm") &&
				!name.startsWith("org.hibernate") &&
				!name.startsWith("org.ietf") &&
				!name.startsWith("org.jberet") &&
				!name.startsWith("org.jcp") &&
				!name.startsWith("org.jboss") &&
				!name.startsWith("org.reflections") &&
				!name.startsWith("org.picketbox") &&
				!name.startsWith("org.slf4j") &&
				!name.startsWith("org.springframework") &&
				!name.startsWith("org.wildfly") &&
				!name.startsWith("org.w3c.dom") &&
				!name.startsWith("org.xml") &&
				!name.startsWith("org.yaml") &&
				!name.startsWith("sun");
	}

}