package com.electronics.marketplace.dto;

import com.electronics.marketplace.entity.Product;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a recommended product with calculated matching score and explanations.
 */
public class RecommendedProduct implements Serializable, Comparable<RecommendedProduct> {

    private static final long serialVersionUID = 1L;

    private Product product;
    private int matchScore; // Score from 0 to 100%
    private List<String> matchHighlights = new ArrayList<>();

    public RecommendedProduct() {
    }

    public RecommendedProduct(Product product, int matchScore) {
        this.product = product;
        this.matchScore = matchScore;
    }

    public void addHighlight(String highlight) {
        this.matchHighlights.add(highlight);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public List<String> getMatchHighlights() {
        return matchHighlights;
    }

    public void setMatchHighlights(List<String> matchHighlights) {
        this.matchHighlights = matchHighlights;
    }

    @Override
    public int compareTo(RecommendedProduct other) {
        return Integer.compare(other.matchScore, this.matchScore); // Descending order
    }
}
