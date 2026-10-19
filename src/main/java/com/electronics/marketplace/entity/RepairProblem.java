package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Thực thể RepairProblem (Bảng repair_problems) - Danh mục lỗi chuẩn hóa.
 * Field-based JPA mapping theo Rule 2.
 * Dùng Long id theo quy định chung.
 */
@Entity
@Table(name = "repair_problems")
public class RepairProblem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "estimated_min_days")
    private Integer estimatedMinDays;

    @Column(name = "estimated_max_days")
    private Integer estimatedMaxDays;

    public RepairProblem() {
    }

    public RepairProblem(Long id, String name) {
        this.id = id;
        this.name = name;
        this.isActive = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
}
