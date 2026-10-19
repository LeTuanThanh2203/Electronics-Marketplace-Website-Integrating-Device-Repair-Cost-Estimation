package com.electronics.marketplace.entity;

import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.entity.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.io.Serializable;
import java.util.Date;

/**
 * Nguoi dung he thong (SDD 13.2, BR-06). Field bam theo class diagram.
 *
 * role la quyen cap HE THONG, chi co USER | ADMIN. Buyer/Seller la vai tro
 * nghiep vu suy ra tu ngu canh, KHONG phai cot rieng.
 *
 * ponytail: khong khai bao quan he 1-N nguoc ve Order/Listing/Product du
 * diagram co ve. Chieu xuoi (Order.buyer, Listing.seller) da du de truy van,
 * con chieu nguoc chi them eager-load va vong lap serialization khi User nam
 * trong session. Them khi nao co man hinh that su can "don cua toi" qua User.
 *
 * File nay do Nam viet tam de mo khoa module Cart/Order/Payment. Tan viet ban
 * chinh thuc thi ghi de - giu nguyen ten bang "users" va cac cot ben duoi.
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    /** BCrypt. Khong bao gio tra ve client, khong bao gio log (Rule bao mat). */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(length = 255)
    private String avatar;

    @Column(length = 150)
    private String location;

    /** So don MARKETPLACE da COMPLETED voi vai tro Seller (SDD: hien thi do tin cay). */
    @Column(name = "completed_sales_count", nullable = false)
    private int completedSalesCount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    public User() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCompletedSalesCount() {
        return completedSalesCount;
    }

    public void setCompletedSalesCount(int completedSalesCount) {
        this.completedSalesCount = completedSalesCount;
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
}
