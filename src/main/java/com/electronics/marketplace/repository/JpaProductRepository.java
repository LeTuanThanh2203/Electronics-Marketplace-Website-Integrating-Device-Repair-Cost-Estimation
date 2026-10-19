package com.electronics.marketplace.repository;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.entity.ProductSpecification;
import com.electronics.marketplace.entity.enums.ProductStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JPA implementation of ProductRepository.
 *
 * ponytail: khong co fallback in-memory. DB loi thi nem ra ngoai - de loi lo
 * ngay luc dev con hon de demo hien san pham gia.
 */
public class JpaProductRepository implements ProductRepository {

    @Override
    public Optional<Product> findById(Long id) {
        if (id == null) return Optional.empty();

        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            String qString = "SELECT p FROM Product p LEFT JOIN FETCH p.specifications WHERE p.id = :id";
            TypedQuery<Product> q = em.createQuery(qString, Product.class);
            q.setParameter("id", id);
            return Optional.ofNullable(q.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findAll() {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            String qString = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.specifications WHERE p.status != :discontinued";
            TypedQuery<Product> q = em.createQuery(qString, Product.class);
            q.setParameter("discontinued", ProductStatus.DISCONTINUED);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            return findAll();
        }

        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            String qString = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.specifications WHERE LOWER(p.brand) = LOWER(:brand)";
            TypedQuery<Product> q = em.createQuery(qString, Product.class);
            q.setParameter("brand", brand.trim());
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findByTargetNeed(String targetNeed) {
        if (targetNeed == null || targetNeed.trim().isEmpty()) {
            return findAll();
        }

        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            String qString = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.specifications WHERE p.targetNeed = :targetNeed";
            TypedQuery<Product> q = em.createQuery(qString, Product.class);
            q.setParameter("targetNeed", targetNeed.trim());
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.specifications WHERE 1=1");
            if (minPrice != null) jpql.append(" AND p.price >= :minPrice");
            if (maxPrice != null) jpql.append(" AND p.price <= :maxPrice");

            TypedQuery<Product> q = em.createQuery(jpql.toString(), Product.class);
            if (minPrice != null) q.setParameter("minPrice", minPrice);
            if (maxPrice != null) q.setParameter("maxPrice", maxPrice);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            String qString = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.specifications " +
                             "WHERE LOWER(p.name) LIKE :kw OR LOWER(p.brand) LIKE :kw OR LOWER(p.series) LIKE :kw";
            TypedQuery<Product> q = em.createQuery(qString, Product.class);
            q.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> filter(List<String> brands, BigDecimal minPrice, BigDecimal maxPrice,
                                Integer minRam, String targetNeed, String sortBy) {
        List<Product> results = findAll();

        if (brands != null && !brands.isEmpty()) {
            Set<String> brandSet = brands.stream().map(String::toLowerCase).collect(Collectors.toSet());
            results = results.stream()
                    .filter(p -> p.getBrand() != null && brandSet.contains(p.getBrand().toLowerCase()))
                    .collect(Collectors.toList());
        }

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

        if (minRam != null && minRam > 0) {
            results = results.stream()
                    .filter(p -> {
                        ProductSpecification ramSpec = p.getSpecification("RAM");
                        return ramSpec != null && ramSpec.getNumericValue() != null && ramSpec.getNumericValue() >= minRam;
                    })
                    .collect(Collectors.toList());
        }

        if (targetNeed != null && !targetNeed.trim().isEmpty() && !targetNeed.equalsIgnoreCase("ALL")) {
            results = results.stream()
                    .filter(p -> targetNeed.equalsIgnoreCase(p.getTargetNeed()))
                    .collect(Collectors.toList());
        }

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

        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (product.getId() == null) {
                em.persist(product);
            } else {
                product = em.merge(product);
            }
            tx.commit();
            return product;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) return;

        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product p = em.find(Product.class, id);
            if (p != null) {
                em.remove(p);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
