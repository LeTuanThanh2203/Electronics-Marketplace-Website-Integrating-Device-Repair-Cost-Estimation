package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Entity
@Table(name = "repair_pricings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"device_id", "problem_id", "version"})
})
public class RepairPricing implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Biên độ mặc định 10%. */
    public static final BigDecimal DEFAULT_MARGIN = BigDecimal.valueOf(0.10);

    /** Biên độ tối đa cho phép: 100% (1.0). Lớn hơn mức này là nhập sai đơn vị. */
    public static final BigDecimal MAX_MARGIN = BigDecimal.ONE;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "problem_id", nullable = false)
    private RepairProblem problem;

    @Column(name = "parts_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal partsCost = BigDecimal.ZERO;

    @Column(name = "labor_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal laborCost = BigDecimal.ZERO;

    @Column(name = "estimation_margin", nullable = false, precision = 8, scale = 4)
    private BigDecimal estimationMargin = DEFAULT_MARGIN; // Mặc định 10% biên độ

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public RepairPricing() {
        this.updatedAt = new Date();
    }

    public RepairPricing(Device device, RepairProblem problem, BigDecimal partsCost, BigDecimal laborCost, BigDecimal estimationMargin, Integer version, boolean isActive) {
        this.device = device;
        this.problem = problem;
        this.partsCost = partsCost != null ? partsCost : BigDecimal.ZERO;
        this.laborCost = laborCost != null ? laborCost : BigDecimal.ZERO;
        this.estimationMargin = estimationMargin != null ? estimationMargin : DEFAULT_MARGIN;
        this.version = version != null ? version : 1;
        this.isActive = isActive;
        this.updatedAt = new Date();
    }

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getPartsCost() {
        return partsCost;
    }

    public void setPartsCost(BigDecimal partsCost) {
        this.partsCost = partsCost;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(BigDecimal laborCost) {
        this.laborCost = laborCost;
    }

    public BigDecimal getEstimationMargin() {
        return estimationMargin;
    }

    public void setEstimationMargin(BigDecimal estimationMargin) {
        this.estimationMargin = estimationMargin;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Base Cost = Parts Cost + Labor Cost
     */
    public BigDecimal calculateBaseCost() {
        BigDecimal parts = partsCost != null ? partsCost : BigDecimal.ZERO;
        BigDecimal labor = laborCost != null ? laborCost : BigDecimal.ZERO;
        return parts.add(labor);
    }

    /**
     * Tính độ biến thiên biên độ (Margin Amount).
     *
     * <p>{@code estimationMargin} LUÔN là tỷ lệ trong khoảng [0, 1], ví dụ 0.10 = 10%.
     * Trước đây code còn nhánh hiểu giá trị &ge; 1.0 là "số tiền tuyệt đối", khiến admin
     * nhập 10 (ý là 10%) lại thành biên độ 10 đồng. Nhánh đó đã bị bỏ; giá trị ngoài
     * khoảng sẽ được kẹp về biên hợp lệ để dữ liệu cũ không làm hỏng báo giá.</p>
     */
    public BigDecimal calculateMarginAmount() {
        BigDecimal base = calculateBaseCost();
        BigDecimal margin = normalizeMargin(estimationMargin);
        return base.multiply(margin).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Chuẩn hóa biên độ về khoảng [0, 1]. Giá trị null hoặc âm thành 0,
     * lớn hơn 1 thành 1 (tức tối đa 100%).
     */
    public static BigDecimal normalizeMargin(BigDecimal margin) {
        if (margin == null) {
            return BigDecimal.ZERO;
        }
        if (margin.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (margin.compareTo(MAX_MARGIN) > 0) {
            return MAX_MARGIN;
        }
        return margin;
    }

    /**
     * Chi phí dự kiến tối thiểu
     */
    public BigDecimal calculateMinCost() {
        BigDecimal base = calculateBaseCost();
        BigDecimal marginAmount = calculateMarginAmount();
        BigDecimal min = base.subtract(marginAmount);
        return min.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : min.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Chi phí dự kiến tối đa
     */
    public BigDecimal calculateMaxCost() {
        BigDecimal base = calculateBaseCost();
        BigDecimal marginAmount = calculateMarginAmount();
        return base.add(marginAmount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "RepairPricing{" +
                "id=" + id +
                ", partsCost=" + partsCost +
                ", laborCost=" + laborCost +
                ", estimationMargin=" + estimationMargin +
                ", version=" + version +
                ", isActive=" + isActive +
                '}';
    }
}
