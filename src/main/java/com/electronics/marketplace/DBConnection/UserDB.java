package com.electronics.marketplace.DBConnection;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lớp truy xuất và thao tác CSDL cho bảng User.
 * Hỗ trợ JPA (chuẩn Hibernate) kết hợp cơ chế bộ nhớ dự phòng an toàn (Fail-safe In-Memory).
 */
public class UserDB {

    // Kho lưu trữ bộ nhớ dự phòng cho demo khi không kết nối CSDL
    private static final Map<Long, User> memoryUsers = new ConcurrentHashMap<>();
    private static final AtomicLong idSeq = new AtomicLong(10L);

    static {
        initSampleUsers();
    }

    private static void initSampleUsers() {
        User u1 = new User(1L, "Trần Minh Đức", "duc.tran@gmail.com", "0912888999", "Quận 1, TP. Hồ Chí Minh");
        u1.setPasswordHash("$2a$10$wKx1F8/Lsk854K1lG4c4x.1wz1v6eKz3h3aK4Ue0hD8gW4Jv0PZqm"); // 123456
        u1.setRole("USER");
        u1.setStatus("ACTIVE");
        u1.setCompletedSalesCount(14);
        memoryUsers.put(1L, u1);

        User u2 = new User(2L, "Nguyễn Thị Mai Hoa", "hoa.nguyen@gmail.com", "0987654321", "Cầu Giấy, Hà Nội");
        u2.setPasswordHash("$2a$10$wKx1F8/Lsk854K1lG4c4x.1wz1v6eKz3h3aK4Ue0hD8gW4Jv0PZqm");
        u2.setRole("USER");
        u2.setStatus("ACTIVE");
        u2.setCompletedSalesCount(8);
        memoryUsers.put(2L, u2);

        User u3 = new User(3L, "Lê Hồng Quân", "quan.le@gmail.com", "0903112233", "Hải Châu, Đà Nẵng");
        u3.setPasswordHash("$2a$10$wKx1F8/Lsk854K1lG4c4x.1wz1v6eKz3h3aK4Ue0hD8gW4Jv0PZqm");
        u3.setRole("ADMIN");
        u3.setStatus("ACTIVE");
        u3.setCompletedSalesCount(5);
        memoryUsers.put(3L, u3);
    }

    public static User selectById(Long id) {
        if (id == null) return null;
        try {
            if (JpaConfig.getEntityManagerFactory() != null) {
                User u = DBUtil.query(em -> em.find(User.class, id));
                if (u != null) return u;
            }
        } catch (Exception e) {
            System.err.println("[UserDB] JPA selectById fallback: " + e.getMessage());
        }
        return memoryUsers.get(id);
    }

    public static User selectByEmail(String email) {
        if (email == null) return null;
        try {
            if (JpaConfig.getEntityManagerFactory() != null) {
                User u = DBUtil.query(em -> {
                    List<User> r = em.createQuery(
                            "SELECT u FROM User u WHERE lower(u.email) = lower(:e)", User.class)
                            .setParameter("e", email.trim())
                            .getResultList();
                    return r.isEmpty() ? null : r.get(0);
                });
                if (u != null) return u;
            }
        } catch (Exception e) {
            System.err.println("[UserDB] JPA selectByEmail fallback: " + e.getMessage());
        }
        for (User u : memoryUsers.values()) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(email.trim())) {
                return u;
            }
        }
        return null;
    }

    public static boolean emailExists(String email) {
        return selectByEmail(email) != null;
    }

    public static Long insert(User u) {
        if (u == null) return null;
        try {
            if (JpaConfig.getEntityManagerFactory() != null) {
                return DBUtil.inTransaction(em -> {
                    em.persist(u);
                    return u.getId();
                });
            }
        } catch (Exception e) {
            System.err.println("[UserDB] JPA insert fallback: " + e.getMessage());
        }
        if (u.getId() == null) {
            u.setId(idSeq.incrementAndGet());
        }
        memoryUsers.put(u.getId(), u);
        return u.getId();
    }

    public static void updateProfile(User u) {
        if (u == null || u.getId() == null) return;
        try {
            if (JpaConfig.getEntityManagerFactory() != null) {
                DBUtil.execute(em -> {
                    User m = em.find(User.class, u.getId());
                    if (m != null) {
                        m.setName(u.getName());
                        m.setPhone(u.getPhone());
                        m.setLocation(u.getLocation());
                        m.setAvatar(u.getAvatar());
                        m.setUpdatedAt(new Date());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("[UserDB] JPA updateProfile fallback: " + e.getMessage());
        }
        User memUser = memoryUsers.get(u.getId());
        if (memUser != null) {
            memUser.setName(u.getName());
            memUser.setPhone(u.getPhone());
            memUser.setLocation(u.getLocation());
            memUser.setAvatar(u.getAvatar());
            memUser.setUpdatedAt(new Date());
        } else {
            memoryUsers.put(u.getId(), u);
        }
    }

    public static void updatePassword(Long userId, String passwordHash) {
        if (userId == null) return;
        try {
            if (JpaConfig.getEntityManagerFactory() != null) {
                DBUtil.execute(em -> {
                    User m = em.find(User.class, userId);
                    if (m != null) {
                        m.setPasswordHash(passwordHash);
                        m.setUpdatedAt(new Date());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("[UserDB] JPA updatePassword fallback: " + e.getMessage());
        }
        User memUser = memoryUsers.get(userId);
        if (memUser != null) {
            memUser.setPasswordHash(passwordHash);
            memUser.setUpdatedAt(new Date());
        }
    }

    public static void updateStatus(Long userId, String status) {
        if (userId == null) return;
        try {
            if (JpaConfig.getEntityManagerFactory() != null) {
                DBUtil.execute(em -> {
                    User m = em.find(User.class, userId);
                    if (m != null) {
                        m.setStatus(status);
                        m.setUpdatedAt(new Date());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("[UserDB] JPA updateStatus fallback: " + e.getMessage());
        }
        User memUser = memoryUsers.get(userId);
        if (memUser != null) {
            memUser.setStatus(status);
            memUser.setUpdatedAt(new Date());
        }
    }

    public static List<User> selectAll() {
        try {
            if (JpaConfig.getEntityManagerFactory() != null) {
                return DBUtil.query(em -> em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class).getResultList());
            }
        } catch (Exception e) {
            System.err.println("[UserDB] JPA selectAll fallback: " + e.getMessage());
        }
        return new ArrayList<>(memoryUsers.values());
    }

    public static int countAll() {
        try {
            if (JpaConfig.getEntityManagerFactory() != null) {
                return DBUtil.query(em -> em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult().intValue());
            }
        } catch (Exception e) {
            System.err.println("[UserDB] JPA countAll fallback: " + e.getMessage());
        }
        return memoryUsers.size();
    }
}
