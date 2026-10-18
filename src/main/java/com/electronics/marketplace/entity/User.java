package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.entity.enums.Status;

@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userID;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "avatar", length = 500)          // chỉ lưu URL
    private String avatar;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status = Status.ACTIVE;

    @Column(name = "completed_sales_count", nullable = false)
    private int completedSalesCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
    
    public User() {}
    
    @PrePersist
    void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getCompletedSalesCount() { return completedSalesCount; }
    public void setCompletedSalesCount(int c) { this.completedSalesCount = c; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Tiện cho JSP: ${user.admin}, ${user.active}...
    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean isActive() { return status == Status.ACTIVE; }
    public boolean isSuspended() { return status == Status.SUSPENDED; }
    public boolean isDeleted() { return status == Status.DELETED; }
    public String getInitial() {
        return (name == null || name.isBlank()) ? "?" : name.trim().substring(0, 1).toUpperCase();
    }
}

