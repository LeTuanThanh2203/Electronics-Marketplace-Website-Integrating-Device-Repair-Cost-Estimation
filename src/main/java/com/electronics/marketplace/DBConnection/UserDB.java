package com.electronics.marketplace.DBConnection;

import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.entity.enums.UserStatus;
import java.util.List;

public class UserDB {

    public static Long insert(User u) {
        return DBUtil.inTransaction(em -> {
            em.persist(u);
            return u.getId();
        });
    }

    public static User selectByEmail(String email) {
        return DBUtil.query(em -> {
            List<User> r = em.createQuery(
                    "SELECT u FROM User u WHERE lower(u.email) = lower(:e)", User.class)
                    .setParameter("e", email)
                    .getResultList();
            return r.isEmpty() ? null : r.get(0);
        });
    }

    public static User getByEmail(String email) {
        return selectByEmail(email);
    }

    public static User selectById(Long id) {
        return DBUtil.query(em -> em.find(User.class, id));
    }

    public static boolean emailExists(String email) {
        return selectByEmail(email) != null;
    }

    public static boolean existsAdmin() {
        return DBUtil.query(em -> em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.role = :r", Long.class)
                .setParameter("r", Role.ADMIN)
                .getSingleResult() > 0);
    }

    public static List<User> selectAll() {
        return DBUtil.query(em -> em.createQuery(
                "SELECT u FROM User u ORDER BY u.id", User.class).getResultList());
    }

    public static int countAll() {
        return DBUtil.query(em -> em.createQuery(
                "SELECT COUNT(u) FROM User u", Long.class).getSingleResult().intValue());
    }

    public static int countByStatus(UserStatus s) {
        return DBUtil.query(em -> em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.status = :s", Long.class)
                .setParameter("s", s)
                .getSingleResult().intValue());
    }

    public static void updateProfile(User u) {
        DBUtil.execute(em -> {
            User m = em.find(User.class, u.getId());
            m.setName(u.getName());
            m.setPhone(u.getPhone());
            m.setLocation(u.getLocation());
            m.setAvatar(u.getAvatar());
        });
    }

    public static void updatePassword(Long userId, String passwordHash) {
        DBUtil.execute(em -> em.find(User.class, userId).setPasswordHash(passwordHash));
    }

    public static void updateStatus(Long userId, UserStatus status) {
        DBUtil.execute(em -> em.find(User.class, userId).setStatus(status));
    }
}