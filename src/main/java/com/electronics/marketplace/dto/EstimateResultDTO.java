package com.electronics.marketplace.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class EstimateResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long deviceId;
    private String deviceName;
    private Long problemId;
    private String problemName;
    private BigDecimal baseCost;
    private BigDecimal minCost;
    private BigDecimal maxCost;
    private BigDecimal partsCost;
    private BigDecimal laborCost;
    private BigDecimal estimationMargin;
    private Integer pricingVersion;
    private Integer estimatedMinDays;
    private Integer estimatedMaxDays;
    private boolean available;
    private String message;

    public EstimateResultDTO() {
    }

    public static EstimateResultDTO notFound(String message) {
        EstimateResultDTO dto = new EstimateResultDTO();
        dto.setAvailable(false);
        dto.setMessage(message);
        return dto;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public BigDecimal getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(BigDecimal baseCost) {
        this.baseCost = baseCost;
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

    public Integer getPricingVersion() {
        return pricingVersion;
    }

    public void setPricingVersion(Integer pricingVersion) {
        this.pricingVersion = pricingVersion;
    }

    public Integer getEstimatedMinDays() {
        return estimatedMinDays;
    }

    public void setEstimatedMinDays(Integer estimatedMinDays) {
        this.estimatedMinDays = estimatedMinDays;
    }

    public Integer getEstimatedMaxDays() {
        return estimatedMaxDays;
    }

    public void setEstimatedMaxDays(Integer estimatedMaxDays) {
        this.estimatedMaxDays = estimatedMaxDays;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
