package com.electronics.marketplace.service;

import com.electronics.marketplace.dto.RecommendationRequest;
import com.electronics.marketplace.dto.RecommendedProduct;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.entity.ProductSpecification;
import com.electronics.marketplace.repository.JpaProductRepository;
import com.electronics.marketplace.repository.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service handling business logic for the new product catalog and Recommendation Engine.
 * Belongs to Phước Anh.
 */
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService() {
        this(new JpaProductRepository());
    }

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> filterProducts(List<String> brands, BigDecimal minPrice, BigDecimal maxPrice,
                                       Integer minRam, String targetNeed, String sortBy) {
        return productRepository.filter(brands, minPrice, maxPrice, minRam, targetNeed, sortBy);
    }

    public List<Product> compareProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        return productIds.stream()
                .map(productRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Recommendation Engine: Calculates matching score (0-100%) based on budget, need, specs, and brand.
     */
    public List<RecommendedProduct> recommend(RecommendationRequest request) {
        List<Product> all = productRepository.findAll();
        List<RecommendedProduct> recommendations = new ArrayList<>();

        BigDecimal budget = request.getBudget();
        String need = request.getPrimaryNeed() != null ? request.getPrimaryNeed().toUpperCase() : "ALL";
        String brand = request.getPreferredBrand();
        Integer minRam = request.getMinRam();

        for (Product p : all) {
            int score = 0;
            List<String> highlights = new ArrayList<>();

            // 1. Budget Match (Max 35 points)
            if (budget != null && budget.compareTo(BigDecimal.ZERO) > 0) {
                if (p.getPrice().compareTo(budget) <= 0) {
                    // Under or exact budget
                    double ratio = p.getPrice().doubleValue() / budget.doubleValue();
                    if (ratio >= 0.70) {
                        score += 35; // optimal usage of budget
                        highlights.add("Tối ưu ngân sách (sử dụng hiệu quả ngân sách bạn đặt ra)");
                    } else {
                        score += 28;
                        highlights.add("Tiết kiệm chi phí đáng kể so với ngân sách");
                    }
                } else {
                    // Slightly over budget (within 10%)
                    double overRatio = (p.getPrice().doubleValue() - budget.doubleValue()) / budget.doubleValue();
                    if (overRatio <= 0.10) {
                        score += 15;
                        highlights.add("Vượt nhẹ ngân sách nhưng cấu hình và độ bền vượt trội");
                    } else {
                        continue; // Exceeds budget by more than 10%, filter out
                    }
                }
            } else {
                score += 25; // Default score if no budget provided
            }

            // Extract numeric & text specs from Lucidchart structure
            ProductSpecification ramSpec = p.getSpecification("RAM");
            int ramVal = (ramSpec != null && ramSpec.getNumericValue() != null) ? ramSpec.getNumericValue() : 8;

            ProductSpecification gpuSpec = p.getSpecification("GPU");
            String gpuText = (gpuSpec != null && gpuSpec.getTextValue() != null) ? gpuSpec.getTextValue() : "";

            ProductSpecification refreshRateSpec = p.getSpecification("REFRESH_RATE");
            int refreshRate = (refreshRateSpec != null && refreshRateSpec.getNumericValue() != null) ? refreshRateSpec.getNumericValue() : 60;

            ProductSpecification screenSizeSpec = p.getSpecification("SCREEN_SIZE");
            int screenSize = (screenSizeSpec != null && screenSizeSpec.getNumericValue() != null) ? screenSizeSpec.getNumericValue() : 15;

            // 2. Need Match (Max 40 points)
            if ("GAMING".equalsIgnoreCase(need)) {
                if (gpuText.contains("RTX") || "GAMING".equalsIgnoreCase(p.getTargetNeed())) {
                    score += 25;
                    highlights.add("Card đồ họa rời chuyên game mạnh mẽ");
                }
                if (ramVal >= 16) {
                    score += 15;
                    highlights.add("RAM " + ramVal + "GB chiến mượt mọi tựa game AAA");
                }
                if (refreshRate >= 144) {
                    score += 10;
                    highlights.add("Màn hình tần số quét " + refreshRate + "Hz mượt mà");
                }
            } else if ("GRAPHICS".equalsIgnoreCase(need)) {
                if ("GRAPHICS".equalsIgnoreCase(p.getTargetNeed()) || p.getName().contains("OLED") || p.getName().contains("Retina")) {
                    score += 25;
                    highlights.add("Màn hình chuẩn màu đồ họa chuyên nghiệp (OLED / Retina)");
                }
                if (ramVal >= 16) {
                    score += 15;
                    highlights.add("RAM " + ramVal + "GB đa nhiệm mượt các tác vụ dựng hình, 3D");
                }
            } else if ("OFFICE".equalsIgnoreCase(need)) {
                if ("OFFICE".equalsIgnoreCase(p.getTargetNeed()) || screenSize <= 14) {
                    score += 25;
                    highlights.add("Trọng lượng nhẹ, thiết kế di động sang trọng");
                }
                if (p.getBrand().equalsIgnoreCase("Apple") || p.getName().contains("ThinkPad") || p.getName().contains("XPS")) {
                    score += 15;
                    highlights.add("Bàn phím gõ êm, pin trâu phục vụ làm việc cả ngày");
                }
            } else {
                score += 30; // General match
            }

            // 3. Preferred Brand Match (Max 15 points)
            if (brand != null && !brand.trim().isEmpty() && !"ALL".equalsIgnoreCase(brand)) {
                if (p.getBrand().equalsIgnoreCase(brand.trim())) {
                    score += 15;
                    highlights.add("Đúng thương hiệu bạn yêu thích (" + p.getBrand() + ")");
                }
            } else {
                score += 10;
            }

            // 4. Min RAM Requirement (Max 10 points)
            if (minRam != null && minRam > 0) {
                if (ramVal >= minRam) {
                    score += 10;
                    highlights.add("Đạt mức RAM yêu cầu (" + ramVal + "GB >= " + minRam + "GB)");
                }
            } else {
                score += 5;
            }

            // Cap at 99% for realism
            int finalScore = Math.min(99, Math.max(50, score));

            RecommendedProduct rp = new RecommendedProduct(p, finalScore);
            for (String h : highlights) {
                rp.addHighlight(h);
            }
            recommendations.add(rp);
        }

        Collections.sort(recommendations);
        return recommendations;
    }
}
