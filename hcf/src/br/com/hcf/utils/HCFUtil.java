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

import br.com.hcf.HCFactory;

public final class HCFUtil {

	private HCFUtil() {

	}

	public static <T> String getId(Class<T> classe) {
		return Arrays.asList(classe.getDeclaredFields()).stream()
				.filter(f -> f.getAnnotation(Id.class) != null)
				.map(Field::getName)
				.findFirst().orElseThrow(() -> new NullPointerException("Undeclared id"));
	}

	public static void insertAnnotatedClasses() {
		HCFactory.setAnnotatedClasses(Set.of(Package.getPackages()).stream()
				.filter(new HCFUtil().checkPackages())
				.map(Package::getName).map(name -> new Reflections(name))
				.map(HCFPackageValidator.classValid(reflections -> reflections.getTypesAnnotatedWith(Entity.class)))
				.flatMap(Collection::stream)
				.collect(Collectors.toSet()));
	}
	
	/**
	 * org.jboss.security.acl.ACLImpl is an entity
	 * org.jboss.security.acl.ACLEntryImpl is an entity
	 */
	private Predicate<? super Package> checkPackages() {
		return p -> !p.getName().startsWith("org.jboss");
	}

}

@FunctionalInterface 
interface HCFPackageValidator<I, O, T extends Throwable> {

	Set<Class<?>> apply(I t) throws T;

	static <I, O, T extends Throwable> Function<I, Set<Class<?>>> classValid(HCFPackageValidator<I, Set<Class<?>>, T> validator) {
		return c -> {
			try {
				return validator.apply(c);
			} catch (Throwable e) {
				return new HashSet<Class<?>>();
			}
		};
	}

}
