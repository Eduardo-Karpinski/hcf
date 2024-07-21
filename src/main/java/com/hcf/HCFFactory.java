package com.hcf;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import com.hcf.utils.HCFUtil;

public enum HCFFactory {
    INSTANCE;

    private boolean internal = true;
    private SessionFactory sessionFactory = null;
    private String propertiesPath = "hibernate.properties";

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> HCFFactory.INSTANCE.shutdown()));
    }

    private HCFFactory() {}

    public SessionFactory getFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            synchronized (this) {
                if (sessionFactory == null || sessionFactory.isClosed()) {
                    StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
                    if (internal) {
                        registryBuilder.loadProperties(propertiesPath);
                    } else {
                        registryBuilder.loadProperties(new File(propertiesPath));
                    }
                    StandardServiceRegistry registry = registryBuilder.build();
                    try {
                        MetadataSources metadataSources = new MetadataSources(registry);
                        HCFUtil.getAnnotatedClasses().forEach(metadataSources::addAnnotatedClass);
                        sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
                    } catch (Exception e) {
                        HCFUtil.showError(e);
                        StandardServiceRegistryBuilder.destroy(registry);
                    }
                }
            }
        }
        return sessionFactory;
    }

    public void setDirectoryProperties(String pathname, boolean isInternal) {
        propertiesPath = pathname;
        internal = isInternal;
    }

    public SessionFactory getNewFactory(Map<String, String> propertiesInMap,
                                        String propertiesPath,
                                        boolean isFile,
                                        boolean replaceCurrent,
                                        boolean useHCFClassCollector,
                                        Package[] packages,
                                        Set<Class<?>> classes) {

        Configuration configuration = new Configuration();

        if (propertiesInMap == null) {
            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
            Properties properties = isFile ?
                    registryBuilder.getConfigLoader().loadProperties(new File(propertiesPath)) :
                    registryBuilder.getConfigLoader().loadProperties(propertiesPath);

            properties.forEach((key, value) -> configuration.setProperty(key.toString(), value.toString()));
        } else {
            propertiesInMap.forEach(configuration::setProperty);
        }

        if (useHCFClassCollector) {
            HCFUtil.getAnnotatedClasses().forEach(configuration::addAnnotatedClass);
        } else {
            if (packages != null) {
                Set<String> packageNames = Arrays.stream(packages)
                        .map(Package::getName)
                        .collect(Collectors.toSet());
                HCFUtil.getLogger().info("Packages To Read - " + packageNames);
                HCFUtil.getEntities(packageNames).forEach(configuration::addAnnotatedClass);
            }
            Optional.ofNullable(classes).ifPresent(classesOptional -> classesOptional.forEach(configuration::addAnnotatedClass));
        }

        SessionFactory newFactory = configuration.buildSessionFactory();

        if (replaceCurrent) {
            shutdown();
            sessionFactory = newFactory;
            getAnnotatedClasses();
            return sessionFactory;
        }

        return newFactory;
    }

    public void getAnnotatedClasses() {
        try (EntityManager em = sessionFactory.createEntityManager()) {
            HCFUtil.getLogger().info("Annotated Classes - " + em.getMetamodel().getEntities());
        } catch (Exception e) {
            HCFUtil.showError(e);
        }
    }

    public void shutdown() {
        try {
            if (sessionFactory != null && sessionFactory.isOpen()) {
                sessionFactory.close();
            }
        } catch (Exception e) {
            HCFUtil.showError(e);
        }
    }

}