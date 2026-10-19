package com.electronics.marketplace.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * JPA Configuration & EntityManager management.
 * Connects to persistence unit 'Electronics_MarketplacePU' configured with HikariCP.
 */
public class JpaConfig {

    private static final String PERSISTENCE_UNIT_NAME = "Electronics_MarketplacePU";
    private static volatile EntityManagerFactory emf;

    private JpaConfig() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (JpaConfig.class) {
                if (emf == null) {
                    try {
                        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                    } catch (Exception e) {
                        System.err.println("Warning: EntityManagerFactory initialization deferred: " + e.getMessage());
                    }
                }
            }
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        EntityManagerFactory factory = getEntityManagerFactory();
        if (factory == null) {
            throw new IllegalStateException("EntityManagerFactory is not initialized or database is not reachable yet.");
        }
        return factory.createEntityManager();
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            synchronized (JpaConfig.class) {
                if (emf != null && emf.isOpen()) {
                    emf.close();
                }
            }
        }
    }
}
