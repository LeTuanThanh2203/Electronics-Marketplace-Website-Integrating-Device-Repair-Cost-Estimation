package com.electronics.marketplace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.entity.enums.UserStatus;

@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")                    // đối chiếu tên cột với bản của Nam
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "avatar", length = 255)       // chỉ lưu URL/đường dẫn
    private String avatar;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;               // đổi cho khớp enum Role của nhóm

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "completed_sales_count", nullable = false)
    private int completedSalesCount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    public User() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public int getCompletedSalesCount() { return completedSalesCount; }
    public void setCompletedSalesCount(int c) { this.completedSalesCount = c; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Dùng trong JSP: ${currentUser.admin}, ${u.active}, ${u.initial}
    public boolean isAdmin()     { return role == Role.ADMIN; }
    public boolean isActive()    { return status == UserStatus.ACTIVE; }
    public boolean isSuspended() { return status == UserStatus.SUSPENDED; }
    public boolean isDeleted()   { return status == UserStatus.DELETED; }

    public String getInitial() {
        return (name == null || name.trim().isEmpty()) ? "?" : name.trim().substring(0, 1).toUpperCase();
    }

    @PrePersist
    void onCreate() {
        Date now = new Date();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = new Date();
    }
}
