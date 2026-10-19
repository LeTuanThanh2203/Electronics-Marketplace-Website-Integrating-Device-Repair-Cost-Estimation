package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repair_problems", uniqueConstraints = {
    // Chặn trùng tên loại lỗi: nếu trùng, báo cáo GROUP BY sẽ tách một loại lỗi
    // thành nhiều dòng và thống kê "lỗi hỏng nhiều nhất" bị sai.
    @UniqueConstraint(name = "uq_repair_problems_name", columnNames = {"name"})
})
public class RepairProblem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "estimated_min_days")
    private Integer estimatedMinDays;

    @Column(name = "estimated_max_days")
    private Integer estimatedMaxDays;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RepairPricing> pricings = new ArrayList<>();

    public RepairProblem() {
    }

    public RepairProblem(String name, String description, boolean isActive, Integer estimatedMinDays, Integer estimatedMaxDays) {
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.estimatedMinDays = estimatedMinDays;
        this.estimatedMaxDays = estimatedMaxDays;
    }

    /**
     * Constructor rút gọn cho module Listing (Thành) — chỉ cần id + tên lỗi.
     * Giữ lại để code bên Thành compile được khi merge.
     */
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Alias JavaBeans cho module Listing (Thành) — bên đó đang gọi
     * {@code getIsActive()} / {@code setIsActive()}.
     */
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive != null ? isActive : true;
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

    public List<RepairPricing> getPricings() {
        return pricings;
    }

    public void setPricings(List<RepairPricing> pricings) {
        this.pricings = pricings;
    }

    @Override
    public String toString() {
        return "RepairProblem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                ", estimatedMinDays=" + estimatedMinDays +
                ", estimatedMaxDays=" + estimatedMaxDays +
                '}';
    }
}
