package com.electronics.marketplace.repository;

import com.electronics.marketplace.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product operations.
 * Belongs to Phước Anh (E-commerce & Recommendation).
 */
public interface ProductRepository {

    Optional<Product> findById(Long id);

    List<Product> findAll();

    List<Product> findByBrand(String brand);

    List<Product> findByTargetNeed(String targetNeed);

    List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> search(String keyword);

    List<Product> filter(List<String> brands, BigDecimal minPrice, BigDecimal maxPrice,
                         Integer minRam, String targetNeed, String sortBy);

    Product save(Product product);

    void delete(Long id);
}
