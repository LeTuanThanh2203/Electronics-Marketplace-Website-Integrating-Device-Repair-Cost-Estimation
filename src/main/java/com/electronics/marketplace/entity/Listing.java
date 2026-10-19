package com.electronics.marketplace.entity;

import com.electronics.marketplace.entity.enums.ListingCondition;
import com.electronics.marketplace.entity.enums.ListingStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Thực thể bài đăng Chợ đồ cũ (B12 - Bảng listings)
 * Phục vụ kênh C2C Marketplace.
 * Quy định chuẩn hóa (Tân & Nam - Thành chốt):
 * - Dùng Long id cho Primary Key.
 * - Dùng java.util.Date cho các trường thời gian.
 * - FK = object reference (User seller, Device device, Order reservedByOrder).
 * - Hỗ trợ helper getter/setter getReservedByOrderId() / setReservedByOrderId(Long id) để Nam chạy SQL atomic UPDATE.
 * - BR-08: Tình trạng (condition) là bắt buộc.
 * - BR-12: Tối thiểu 1 ảnh đính kèm.
 * - BR-15: Điều kiện mở bán: status == AVAILABLE && isApproved == true && isRemovedByAdmin == false.
 *   Khi mới tạo: isApproved = false (chờ Admin duyệt).
 */
@Entity
@Table(name = "listings")
public class Listing implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status", nullable = false)
    private ListingCondition condition;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.AVAILABLE;

    /**
     * ID của Order đang giữ chỗ bài đăng này (BR-20).
     * Dùng cho câu SQL conditional UPDATE của Nam:
     * UPDATE listing SET status='RESERVED', reservedByOrderId=?, reservedAt=? WHERE id=? AND status='AVAILABLE'
     */
    @Column(name = "reserved_by_order_id")
    private Long reservedByOrderId;

    /**
     * Thời điểm giữ chỗ bài đăng (dùng cho TTL 24-48h hủy tự động BR-22).
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reserved_at")
    private Date reservedAt;

    @Column(name = "is_removed_by_admin", nullable = false)
    private Boolean isRemovedByAdmin = false;

    /**
     * Mặc định là false khi người bán đăng tin (BR-15 & SDD §13.6 - chờ Admin duyệt).
     */
    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt = new Date();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ListingImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ListingProblem> problems = new ArrayList<>();

    public Listing() {
    }

    public Listing(Long id, User seller, Device device, ListingCondition condition, String description, BigDecimal price) {
        this.id = id;
        this.seller = seller;
        this.device = device;
        this.condition = condition;
        this.description = description;
        this.price = price;
        this.status = ListingStatus.AVAILABLE;
        this.isApproved = false; // Chờ Admin duyệt
        this.isRemovedByAdmin = false;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Helper methods cho quan hệ Collection
    public void addImage(ListingImage image) {
        images.add(image);
        image.setListing(this);
    }

    public void removeImage(ListingImage image) {
        images.remove(image);
        image.setListing(null);
    }

    public void addProblem(ListingProblem problem) {
        problems.add(problem);
        problem.setListing(this);
    }

    public void removeProblem(ListingProblem problem) {
        problems.remove(problem);
        problem.setListing(null);
    }

    /**
     * Lấy URL ảnh đại diện đầu tiên để hiển thị trên giao diện thẻ sản phẩm
     */
    public String getMainImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageUrl();
        }
        return "https://lh3.googleusercontent.com/aida-public/AB6AXuBhsfIBalXmMjhm1NLSQt6Qkz_OmWr2qcpKxJyrVsvza8nGYMeKenQaqdW_amrvTYf5pRjv8UBUSw4A0wDt_dUw7SWUxo34ZS_r0k0CZi43jMo1RTEUvkVGpIc1bVGAIMEgdhXDI33Vm-9hUhDYA6j_1l4U54e0k94ozBGr9ID-NWC-wAa72xeVo2ukKbWRZA0oPGhsaat6O_hva43CM99e-XhMkJ24anxVCuySREIaIgoX6VxWOSUx";
    }

    public String getTitle() {
        if (device != null) {
            return device.getName();
        }
        return "Laptop Cũ #" + (id != null ? id : "");
    }

    public Long getReservedByOrderId() {
        return reservedByOrderId;
    }

    public void setReservedByOrderId(Long reservedByOrderId) {
        this.reservedByOrderId = reservedByOrderId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public ListingCondition getCondition() {
        return condition;
    }

    public void setCondition(ListingCondition condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }


    public Date getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(Date reservedAt) {
        this.reservedAt = reservedAt;
    }

    public Boolean getIsRemovedByAdmin() {
        return isRemovedByAdmin;
    }

    public void setIsRemovedByAdmin(Boolean isRemovedByAdmin) {
        this.isRemovedByAdmin = isRemovedByAdmin;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ListingImage> getImages() {
        return images;
    }

    public void setImages(List<ListingImage> images) {
        this.images = images;
    }

    public List<ListingProblem> getProblems() {
        return problems;
    }

    public void setProblems(List<ListingProblem> problems) {
        this.problems = problems;
    }
}
