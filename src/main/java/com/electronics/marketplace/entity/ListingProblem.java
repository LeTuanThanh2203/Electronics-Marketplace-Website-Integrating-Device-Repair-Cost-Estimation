package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Thực thể lỗi đã khai báo của máy cũ (B13 - Bảng listing_problems)
 * BR-07, BR-09: Khai báo 0..N lỗi phần cứng tùy chọn kèm ghi chú
 * Rule 2: Field-based mapping, implements Serializable
 * Dùng Long id theo thỏa thuận chung.
 */
@Entity
@Table(name = "listing_problems")
public class ListingProblem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private RepairProblem problem;

    @Column(columnDefinition = "TEXT")
    private String note;

    public ListingProblem() {
    }

    public ListingProblem(Listing listing, RepairProblem problem, String note) {
        this.listing = listing;
        this.problem = problem;
        this.note = note;
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

    public RepairProblem getProblem() {
        return problem;
    }

    public void setProblem(RepairProblem problem) {
        this.problem = problem;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
