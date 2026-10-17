package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Thực thể hình ảnh đính kèm bài đăng máy cũ (B13 - Bảng listing_images)
 * BR-12: Tối thiểu 1 hình ảnh thực tế cho mỗi bài đăng
 * Rule 2: Field-based mapping, implements Serializable
 * Dùng Long id theo thỏa thuận chung.
 */
@Entity
@Table(name = "listing_images")
public class ListingImage implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public ListingImage() {
    }

    public ListingImage(String imageUrl, Integer sortOrder) {
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public ListingImage(Listing listing, String imageUrl, Integer sortOrder) {
        this.listing = listing;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
