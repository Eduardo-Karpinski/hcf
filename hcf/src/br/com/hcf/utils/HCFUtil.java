package br.com.hcf.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
		HCFUtil util = new HCFUtil();
		Set<Class<?>> classesWithAnnotations = new HashSet<>();
		Arrays.asList(Package.getPackages()).stream()
			.filter(util.checkPackages())
			.forEach(p -> classesWithAnnotations.addAll(util.findClasses(p.getName())));
		HCFactory.setAnnotatedClasses(classesWithAnnotations);
	}

	private Predicate<? super Package> checkPackages() {
		return p -> !p.getName().startsWith("sun") &&
					!p.getName().startsWith("java") &&
					!p.getName().startsWith("jdk") &&
					!p.getName().startsWith("javax") &&
					!p.getName().startsWith("jakarta") &&
					!p.getName().startsWith("br.com.hcf") &&
					!p.getName().startsWith("com.mysql") &&
					!p.getName().startsWith("com.sun") &&
					!p.getName().startsWith("io.smallrye") &&
					!p.getName().startsWith("io.undertow") &&
					!p.getName().startsWith("io.jaegertracing") &&
					!p.getName().startsWith("org.wildfly") &&
					!p.getName().startsWith("org.jboss") &&
					!p.getName().startsWith("org.jberet") &&
					!p.getName().startsWith("org.glassfish") &&
					!p.getName().startsWith("org.hibernate") &&
					!p.getName().startsWith("org.slf4j") &&
					!p.getName().startsWith("org.eclipse") &&
					!p.getName().startsWith("org.picketbox") &&
					!p.getName().startsWith("org.slf4j") &&
					!p.getName().startsWith("org.jcp") &&
					!p.getName().startsWith("org.ietf") &&
					!p.getName().startsWith("org.xml") &&
					!p.getName().startsWith("org.w3c") &&
					!p.getName().startsWith("org.graalvm") &&
					!p.getName().startsWith("org.apache");
	}
	
	private Set<Class<?>> findClasses(String packageName) {
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

	private Set<Class<?>> getClasses(File directory, String packageName) {
		try {
			Set<Class<?>> classes = new HashSet<Class<?>>();
			
			if (directory.getPath().startsWith("file:")) {
				getClassesFromZip(directory.getPath().substring(0, directory.getPath().indexOf("!")), packageName, classes);
				return classes;
            } else if (!directory.exists()) {
				return classes;
			} else {
				File[] files = directory.listFiles();
				for (File file : files) {
					if (file.isDirectory()) {
						classes.addAll(getClasses(file, packageName + "." + file.getName()));
					} else if (file.getName().endsWith(".class")) {
						classes.add(Class.forName(packageName + '.' + file.getName().replaceAll("[.]class", "")));
					}
				}
				return classes;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return new HashSet<Class<?>>();
		}
	}

	private void getClassesFromZip(String path, String packageName, Set<Class<?>> classes) {
		try (ZipInputStream zip = new ZipInputStream(new URL(path).openStream())) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (entry.getName().endsWith(".class")) {
					String className = entry.getName()
							.replaceAll("[$].*", "")
							.replaceAll("[.]class", "")
							.replace('/', '.');
					if (className.startsWith(packageName)) {
						try {
							classes.add(Class.forName(className));
						} catch (Exception e) {
							System.err.println("ERROR " + packageName+"."+className + " is unreadable.");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Set<Class<?>> getClassesWithAnnotation(Set<Class<?>> classes, Class<? extends Annotation> annotation) {
		Set<Class<?>> withAnnotation = new HashSet<Class<?>>();
		classes.forEach(c -> {
			if (Arrays.asList(c.getAnnotations()).stream().anyMatch(anotacao -> anotacao.annotationType().equals(annotation))) {
				withAnnotation.add(c);
			}
		});
		return withAnnotation;
	}

}
