package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "repair_estimates")
public class RepairEstimate implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_request_id", nullable = false, unique = true)
    private RepairRequest repairRequest;

    @Column(name = "min_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal minCost;

    @Column(name = "max_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal maxCost;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "calculated_at", nullable = false)
    private Date calculatedAt;

    @Column(name = "pricing_version")
    private Integer pricingVersion;

    @Column(name = "parts_cost_snapshot", precision = 12, scale = 2)
    private BigDecimal partsCostSnapshot;

    @Column(name = "labor_cost_snapshot", precision = 12, scale = 2)
    private BigDecimal laborCostSnapshot;

    @Column(name = "estimation_margin_snapshot", precision = 8, scale = 4)
    private BigDecimal estimationMarginSnapshot;

    public RepairEstimate() {
        this.calculatedAt = new Date();
    }

    public RepairEstimate(RepairRequest repairRequest, BigDecimal minCost, BigDecimal maxCost, Integer pricingVersion,
                          BigDecimal partsCostSnapshot, BigDecimal laborCostSnapshot, BigDecimal estimationMarginSnapshot) {
        this.repairRequest = repairRequest;
        this.minCost = minCost;
        this.maxCost = maxCost;
        this.pricingVersion = pricingVersion;
        this.partsCostSnapshot = partsCostSnapshot;
        this.laborCostSnapshot = laborCostSnapshot;
        this.estimationMarginSnapshot = estimationMarginSnapshot;
        this.calculatedAt = new Date();
    }

    @PrePersist
    public void prePersist() {
        if (this.calculatedAt == null) {
            this.calculatedAt = new Date();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RepairRequest getRepairRequest() {
        return repairRequest;
    }

    public void setRepairRequest(RepairRequest repairRequest) {
        this.repairRequest = repairRequest;
    }

    public BigDecimal getMinCost() {
        return minCost;
    }

    public void setMinCost(BigDecimal minCost) {
        this.minCost = minCost;
    }

    public BigDecimal getMaxCost() {
        return maxCost;
    }

    public void setMaxCost(BigDecimal maxCost) {
        this.maxCost = maxCost;
    }

    public Date getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Date calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Integer getPricingVersion() {
        return pricingVersion;
    }

    public void setPricingVersion(Integer pricingVersion) {
        this.pricingVersion = pricingVersion;
    }

    public BigDecimal getPartsCostSnapshot() {
        return partsCostSnapshot;
    }

    public void setPartsCostSnapshot(BigDecimal partsCostSnapshot) {
        this.partsCostSnapshot = partsCostSnapshot;
    }

    public BigDecimal getLaborCostSnapshot() {
        return laborCostSnapshot;
    }

    public void setLaborCostSnapshot(BigDecimal laborCostSnapshot) {
        this.laborCostSnapshot = laborCostSnapshot;
    }

    public BigDecimal getEstimationMarginSnapshot() {
        return estimationMarginSnapshot;
    }

    public void setEstimationMarginSnapshot(BigDecimal estimationMarginSnapshot) {
        this.estimationMarginSnapshot = estimationMarginSnapshot;
    }

    @Override
    public String toString() {
        return "RepairEstimate{" +
                "id=" + id +
                ", minCost=" + minCost +
                ", maxCost=" + maxCost +
                ", calculatedAt=" + calculatedAt +
                ", pricingVersion=" + pricingVersion +
                '}';
    }
}
