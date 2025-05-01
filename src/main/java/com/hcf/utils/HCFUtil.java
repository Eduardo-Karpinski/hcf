package com.hcf.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.reflections.Reflections;

import com.hcf.HCFSearch;
import com.hcf.enums.HCFOperator;
import com.hcf.enums.HCFParameter;

import jakarta.persistence.Entity;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.Metamodel;

public final class HCFUtil {

    private static final Logger logger = Logger.getLogger("HCF");

    private HCFUtil() {

    }
    
    public static Set<Class<?>> getHCFEntityProviderImpl() {
    	ServiceLoader<HCFEntityProvider> serviceLoader = ServiceLoader.load(HCFEntityProvider.class);
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
            hcfSearches.add(new HCFSearch(parameters[i].toString(), parameters[i+1], (HCFParameter) parameters[i + 2], (HCFOperator) parameters[i + 3]));
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
    	
        Set<String> packages = Arrays.stream(Thread.currentThread().getContextClassLoader().getDefinedPackages())
                .map(Package::getName)
                .collect(Collectors.toSet());
        logger.info("Packages To Read - " + packages);
        Set<Class<?>> typesAnnotatedWith = getEntities(packages);
        logger.info("Annotated Classes - " + typesAnnotatedWith);
        return typesAnnotatedWith;
    }

    public static Set<Class<?>> getEntities(Set<String> packages) {
        Reflections reflections = new Reflections(packages);
        return reflections.getTypesAnnotatedWith(Entity.class);
    }

    public static void showError(Exception exception) {
        logger.severe("[HCF-ERROR] Exception caught:");

        String exceptionType = exception.getClass().getSimpleName();
        String message = exception.getMessage();

        StringBuilder stackTraceBuilder = new StringBuilder();
        for (StackTraceElement element : exception.getStackTrace()) {
            stackTraceBuilder.append("\tat ").append(element.toString()).append("\n");
        }

        String stackTrace = stackTraceBuilder.toString();

        logger.severe("[HCF-ERROR] Exception Type: " + exceptionType);
        logger.severe("[HCF-ERROR] Message: " + message);
        logger.severe("[HCF-ERROR] StackTrace:\n" + stackTrace);
    }

    public static Logger getLogger() {
        return logger;
    }

}