package br.com.hcf.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.reflections.Reflections;

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
		return Set.of(Package.getPackages()).stream()
				.map(Package::getName)
				.filter(checkPackageName())
				.map(Reflections::new)
				.map(HCFValidator.classValid(reflections -> reflections.getTypesAnnotatedWith(Entity.class)))
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}
	
	/**
	 * org.jboss.security.acl.ACLImpl and
	 * org.jboss.security.acl.ACLEntryImpl are an entity,
	 * the rest is unnecessary for reading.
	 */
	private static Predicate<? super String> checkPackageName() {
		return p -> !p.startsWith("br.com.hcf") &&
					!p.startsWith("ch.qos") &&
					!p.startsWith("com.fasterxml") &&
					!p.startsWith("com.sun") &&
					!p.startsWith("com.mysql") &&
					!p.startsWith("io.jaegertracing") &&
					!p.startsWith("io.smallrye") &&
					!p.startsWith("io.undertow") &&
					!p.startsWith("java") &&
					!p.startsWith("jakarta") &&
					!p.startsWith("jdk") &&
					!p.startsWith("net.bytebuddy") &&
					!p.startsWith("org.apache") &&
					!p.startsWith("org.eclipse") &&
					!p.startsWith("org.glassfish") &&
					!p.startsWith("org.graalvm") &&
					!p.startsWith("org.hibernate") &&
					!p.startsWith("org.ietf") &&
					!p.startsWith("org.jberet") &&
					!p.startsWith("org.jcp") &&
					!p.startsWith("org.jboss") &&
					!p.startsWith("org.reflections") &&
					!p.startsWith("org.picketbox") &&
					!p.startsWith("org.slf4j") &&
					!p.startsWith("org.springframework") &&
					!p.startsWith("org.wildfly") &&
					!p.startsWith("org.w3c.dom") &&
					!p.startsWith("org.xml") &&
					!p.startsWith("org.yaml") &&
					!p.startsWith("sun");
	}

}

@FunctionalInterface 
interface HCFValidator<I, O, T extends Throwable> {

	O apply(I t) throws T;

	static <I, O, T extends Throwable> Function<I, Set<Class<?>>> classValid(HCFValidator<I, Set<Class<?>>, T> validator) {
		return c -> {
			try {
				return validator.apply(c);
			} catch (Throwable e) {
				return new HashSet<Class<?>>();
			}
		};
	}

}