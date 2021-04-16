package br.com.hcf.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;

public final class HCFUtil {
	
	private HCFUtil() {

	}

	public static <T> String getId(Class<T> classe) {
		return Arrays.asList(classe.getDeclaredFields()).stream()
				.filter(f -> f.getAnnotation(Id.class) != null)
				.map(Field::getName)
				.findFirst().orElseThrow(() -> new NullPointerException("Undeclared id"));
	}

	public static Set<Class<?>> getAnnotatedClasses() {
		ClassLoader[] classLoaders = new ClassLoader[2];
		classLoaders[0] = ClasspathHelper.contextClassLoader();
		classLoaders[1] = ClasspathHelper.staticClassLoader();
		return Set.of(Package.getPackages()).stream()
				.map(Package::getName)
				.filter(checkPackageName())
				.map(name -> new Reflections(name, ClasspathHelper.forClassLoader(classLoaders)))
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
					!p.startsWith("org.hibernate") &&
					!p.startsWith("org.ietf") &&
					!p.startsWith("org.jberet") &&
					!p.startsWith("org.jcp") &&
					!p.startsWith("org.jboss") &&
					!p.startsWith("org.reflections") &&
					!p.startsWith("org.picketbox") &&
					!p.startsWith("org.slf4j") &&
					!p.startsWith("org.wildfly") &&
					!p.startsWith("org.w3c.dom") &&
					!p.startsWith("org.xml") &&
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
