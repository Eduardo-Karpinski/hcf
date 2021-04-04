package br.com.hcf.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Id;

import br.com.hcf.HCFactory;

public final class HCFUtil {
	
	private HCFUtil() {
		
	}
	
	public static <T> String getId(Class<T> classe) {
		return Arrays.asList(classe.getDeclaredFields()).stream()
		.filter(f -> Arrays.asList(f.getAnnotations()).stream().anyMatch(a -> a instanceof Id))
		.map(Field::getName)
		.findFirst().orElseThrow(() -> new NullPointerException("Undeclared id"));
	}
	
	public static void insertAnnotatedClasses() {
		Set<Class<?>> classesWithAnnotations = new HashSet<>();
		Arrays.asList(Package.getPackages()).stream()
			.filter(p -> !p.getName().startsWith("sun") && !p.getName().startsWith("java") && !p.getName().startsWith("jdk"))
			.forEach(p -> classesWithAnnotations.addAll(findClasses(p.getName())));
		HCFactory.setAnnotatedClasses(classesWithAnnotations);
	}
	
	private static Set<Class<?>> findClasses(String packageName) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			String path = packageName.replace('.', '/');
			Enumeration<URL> resources = classLoader.getResources(path);
			Set<File> dirs = new HashSet<File>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				dirs.add(new File(resource.getFile()));
			}
			Set<Class<?>> classes = new HashSet<Class<?>>();
			for (File directory : dirs) {
				classes.addAll(getClasses(directory, packageName));
			}
			return getClassesWithAnnotation(classes, Entity.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new HashSet<Class<?>>();
		}
	}

	private static Set<Class<?>> getClasses(File directory, String packageName) {
		try {
			Set<Class<?>> classes = new HashSet<Class<?>>();
			if (!directory.exists()) {
				return classes;
			}
			File[] files = directory.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					classes.addAll(getClasses(file, packageName + "." + file.getName()));
				} else if (file.getName().endsWith(".class")) {
					classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
				}
			}
			return classes;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashSet<Class<?>>();
		}
	}
	
	private static Set<Class<?>> getClassesWithAnnotation(Set<Class<?>> classes, Class<? extends Annotation> annotation) {
		Set<Class<?>> withAnnotation = new HashSet<Class<?>>();
		classes.forEach(c -> {
			if (Arrays.asList(c.getAnnotations()).stream().anyMatch(anotacao -> anotacao.annotationType().equals(annotation))) {
				withAnnotation.add(c);
			}
		});
		return withAnnotation;
	}

}
