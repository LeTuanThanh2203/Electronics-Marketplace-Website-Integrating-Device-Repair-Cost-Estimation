package com.electronics.marketplace.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO containing user criteria for product recommendation.
 */
public class RecommendationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal budget;
    private String primaryNeed; // OFFICE, GAMING, GRAPHICS
    private Integer minRam;     // e.g. 8, 16, 32
    private String preferredBrand; // Dell, Apple, ASUS, Lenovo, etc.

    public RecommendationRequest() {
    }

    public RecommendationRequest(BigDecimal budget, String primaryNeed, Integer minRam, String preferredBrand) {
        this.budget = budget;
        this.primaryNeed = primaryNeed;
        this.minRam = minRam;
        this.preferredBrand = preferredBrand;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getPrimaryNeed() {
        return primaryNeed;
    }

    public void setPrimaryNeed(String primaryNeed) {
        this.primaryNeed = primaryNeed;
    }

    public Integer getMinRam() {
        return minRam;
    }

    public void setMinRam(Integer minRam) {
        this.minRam = minRam;
    }

    public String getPreferredBrand() {
        return preferredBrand;
    }

    public void setPreferredBrand(String preferredBrand) {
        this.preferredBrand = preferredBrand;
    }
}
