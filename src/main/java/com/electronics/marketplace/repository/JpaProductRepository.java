package com.electronics.marketplace.repository;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.entity.ProductSpecification;
import com.electronics.marketplace.entity.enums.ProductStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * JPA implementation of ProductRepository following the standard Murach JPA pattern (DBUtil/UserDB style):
 * - Obtains EntityManager from EntityManagerFactory (JpaConfig).
 * - Manages EntityTransaction explicitly: begin(), commit(), and rollback() in catch.
 * - Uses parameterized TypedQuery (:param) representing Prepared Statements underneath.
 * - Always closes EntityManager in finally block.
 * - Seamlessly falls back to Mock store when database is not running.
 */
public class JpaProductRepository implements ProductRepository {

    // Fallback store when DB is not yet running
    private static final Map<Long, Product> inMemoryStore = new ConcurrentHashMap<>();

    static {
        for (Product p : MockProductData.getSampleProducts()) {
            inMemoryStore.put(p.getId(), p);
        }
    }

    private EntityManager getEntityManagerSafely() {
        try {
            EntityManagerFactory emf = JpaConfig.getEntityManagerFactory();
            if (emf != null && emf.isOpen()) {
                return emf.createEntityManager();
            }
        } catch (Exception ignored) {
            // Database is not yet active
        }
        return null;
    }

    @Override
    public Optional<Product> findById(Long id) {
        if (id == null) return Optional.empty();

        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            try {
                String qString = "SELECT p FROM Product p LEFT JOIN FETCH p.specifications WHERE p.id = :id";
                TypedQuery<Product> q = em.createQuery(qString, Product.class);
                q.setParameter("id", id);
                Product product = q.getSingleResult();
                return Optional.ofNullable(product);
            } catch (NoResultException e) {
                return Optional.empty();
            } catch (Exception e) {
                System.err.println("JPA findById warning: " + e.getMessage());
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        return Optional.ofNullable(inMemoryStore.get(id));
    }

    @Override
    public List<Product> findAll() {
        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            try {
                String qString = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.specifications WHERE p.status != :discontinued";
                TypedQuery<Product> q = em.createQuery(qString, Product.class);
                q.setParameter("discontinued", ProductStatus.DISCONTINUED);
                List<Product> list = q.getResultList();
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception e) {
                System.err.println("JPA findAll warning: " + e.getMessage());
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        return new ArrayList<>(inMemoryStore.values());
    }

    @Override
    public List<Product> findByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            return findAll();
        }

        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            try {
                String qString = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.specifications WHERE LOWER(p.brand) = LOWER(:brand)";
                TypedQuery<Product> q = em.createQuery(qString, Product.class);
                q.setParameter("brand", brand.trim());
                List<Product> list = q.getResultList();
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception e) {
                System.err.println("JPA findByBrand warning: " + e.getMessage());
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        return inMemoryStore.values().stream()
                .filter(p -> p.getBrand().equalsIgnoreCase(brand.trim()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByTargetNeed(String targetNeed) {
        if (targetNeed == null || targetNeed.trim().isEmpty()) {
            return findAll();
        }

        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            try {
                String qString = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.specifications WHERE p.targetNeed = :targetNeed";
                TypedQuery<Product> q = em.createQuery(qString, Product.class);
                q.setParameter("targetNeed", targetNeed.trim());
                List<Product> list = q.getResultList();
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception e) {
                System.err.println("JPA findByTargetNeed warning: " + e.getMessage());
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        return inMemoryStore.values().stream()
                .filter(p -> targetNeed.equalsIgnoreCase(p.getTargetNeed()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            try {
                StringBuilder jpql = new StringBuilder("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.specifications WHERE 1=1");
                if (minPrice != null) jpql.append(" AND p.price >= :minPrice");
                if (maxPrice != null) jpql.append(" AND p.price <= :maxPrice");

                TypedQuery<Product> q = em.createQuery(jpql.toString(), Product.class);
                if (minPrice != null) q.setParameter("minPrice", minPrice);
                if (maxPrice != null) q.setParameter("maxPrice", maxPrice);

                List<Product> list = q.getResultList();
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception e) {
                System.err.println("JPA findByPriceRange warning: " + e.getMessage());
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        return inMemoryStore.values().stream()
                .filter(p -> (minPrice == null || p.getPrice().compareTo(minPrice) >= 0) &&
                             (maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        String kw = keyword.trim().toLowerCase();
        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            try {
                String qString = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.specifications " +
                                 "WHERE LOWER(p.name) LIKE :kw OR LOWER(p.brand) LIKE :kw OR LOWER(p.series) LIKE :kw";
                TypedQuery<Product> q = em.createQuery(qString, Product.class);
                q.setParameter("kw", "%" + kw + "%");
                List<Product> list = q.getResultList();
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception e) {
                System.err.println("JPA search warning: " + e.getMessage());
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        return inMemoryStore.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(kw) ||
                             p.getBrand().toLowerCase().contains(kw) ||
                             (p.getSeries() != null && p.getSeries().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> filter(List<String> brands, BigDecimal minPrice, BigDecimal maxPrice,
                                Integer minRam, String targetNeed, String sortBy) {
        List<Product> results = findAll();

        // 1. Filter by brands
        if (brands != null && !brands.isEmpty()) {
            Set<String> brandSet = brands.stream().map(String::toLowerCase).collect(Collectors.toSet());
            results = results.stream()
                    .filter(p -> brandSet.contains(p.getBrand().toLowerCase()))
                    .collect(Collectors.toList());
        }

        // 2. Filter by price range
        if (minPrice != null) {
            results = results.stream()
                    .filter(p -> p.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }
        if (maxPrice != null) {
            results = results.stream()
                    .filter(p -> p.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }

        // 3. Filter by minRam
        if (minRam != null && minRam > 0) {
            results = results.stream()
                    .filter(p -> {
                        ProductSpecification ramSpec = p.getSpecification("RAM");
                        return ramSpec != null && ramSpec.getNumericValue() != null && ramSpec.getNumericValue() >= minRam;
                    })
                    .collect(Collectors.toList());
        }

        // 4. Filter by targetNeed
        if (targetNeed != null && !targetNeed.trim().isEmpty() && !targetNeed.equalsIgnoreCase("ALL")) {
            results = results.stream()
                    .filter(p -> targetNeed.equalsIgnoreCase(p.getTargetNeed()))
                    .collect(Collectors.toList());
        }

        // 5. Sorting
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "price_asc":
                case "gia_thap_den_cao":
                    results.sort(Comparator.comparing(Product::getPrice));
                    break;
                case "price_desc":
                case "gia_cao_den_thap":
                    results.sort(Comparator.comparing(Product::getPrice).reversed());
                    break;
                case "name":
                    results.sort(Comparator.comparing(Product::getName));
                    break;
                default:
                    break;
            }
        }

        return results;
    }

    @Override
    public Product save(Product product) {
        if (product == null) return null;

        EntityManager em = getEntityManagerSafely();
        EntityTransaction trans = null;
        if (em != null) {
            try {
                trans = em.getTransaction();
                trans.begin();
                if (product.getId() == null) {
                    em.persist(product);
                } else {
                    product = em.merge(product);
                }
                trans.commit();
                return product;
            } catch (Exception e) {
                System.err.println("Error saving product via JPA: " + e.getMessage());
                if (trans != null && trans.isActive()) {
                    trans.rollback();
                }
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        // In-memory fallback
        if (product.getId() == null) {
            long newId = inMemoryStore.keySet().stream().mapToLong(v -> v).max().orElse(0) + 1;
            product.setId(newId);
        }
        inMemoryStore.put(product.getId(), product);
        return product;
    }

    @Override
    public void delete(Long id) {
        if (id == null) return;

        EntityManager em = getEntityManagerSafely();
        EntityTransaction trans = null;
        if (em != null) {
            try {
                trans = em.getTransaction();
                trans.begin();
                Product p = em.find(Product.class, id);
                if (p != null) {
                    em.remove(p);
                }
                trans.commit();
            } catch (Exception e) {
                System.err.println("Error deleting product via JPA: " + e.getMessage());
                if (trans != null && trans.isActive()) {
                    trans.rollback();
                }
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        inMemoryStore.remove(id);
    }
}
