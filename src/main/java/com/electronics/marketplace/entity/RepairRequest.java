package com.electronics.marketplace.entity;

import com.electronics.marketplace.entity.enums.RepairStatus;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "repair_requests")
public class RepairRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "problem_id", nullable = false)
    private RepairProblem problem;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 150)
    private String customerEmail;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    /**
     * Trạng thái phiếu, lưu dạng chuỗi để khớp Class Diagram ({@code status: String}).
     * Chỉ được gán giá trị từ {@link RepairStatus} — không dùng chuỗi tự do.
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status = RepairStatus.PENDING.name();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @OneToOne(mappedBy = "repairRequest", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private RepairEstimate repairEstimate;

    public RepairRequest() {
        this.createdAt = new Date();
    }

    public RepairRequest(Long userId, Device device, RepairProblem problem, String description, String imageUrl) {
        this.userId = userId;
        this.device = device;
        this.problem = problem;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = RepairStatus.PENDING.name();
        this.createdAt = new Date();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public RepairProblem getProblem() {
        return problem;
    }

    public void setProblem(RepairProblem problem) {
        this.problem = problem;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /** Gán trạng thái bằng enum — cách ưu tiên để tránh gõ sai chuỗi. */
    public void setStatus(RepairStatus status) {
        this.status = status != null ? status.name() : null;
    }

    /** Trạng thái dưới dạng enum, hoặc {@code null} nếu dữ liệu cũ không hợp lệ. */
    public RepairStatus getStatusEnum() {
        return RepairStatus.fromCode(status);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public RepairEstimate getRepairEstimate() {
        return repairEstimate;
    }

    public void setRepairEstimate(RepairEstimate repairEstimate) {
        this.repairEstimate = repairEstimate;
        if (repairEstimate != null && repairEstimate.getRepairRequest() != this) {
            repairEstimate.setRepairRequest(this);
        }
    }

    @Override
    public String toString() {
        return "RepairRequest{" +
                "id=" + id +
                ", userId=" + userId +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
