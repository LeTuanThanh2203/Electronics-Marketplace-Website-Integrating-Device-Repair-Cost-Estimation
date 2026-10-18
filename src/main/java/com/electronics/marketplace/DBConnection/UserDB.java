package com.electronics.marketplace.DBConnection;

import java.util.List;
import com.electronics.marketplace.entity.enums.Status;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.Role;

public class UserDB {
    public static int insert(User u) {
        return DBUtil.inTransaction(em -> {
            em.persist(u);
            return u.getUserID();
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

    public static User selectById(int id) {
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
                "SELECT u FROM User u ORDER BY u.userID", User.class).getResultList());
    }

    public static int countAll() {
        return DBUtil.query(em -> em.createQuery(
                "SELECT COUNT(u) FROM User u", Long.class).getSingleResult().intValue());
    }

    public static int countByStatus(Status s) {
        return DBUtil.query(em -> em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.status = :s", Long.class)
                .setParameter("s", s)
                .getSingleResult().intValue());
    }

    public static void updateProfile(User u) {
        DBUtil.execute(em -> {
            User m = em.find(User.class, u.getUserID());
            m.setName(u.getName());
            m.setPhone(u.getPhone());
            m.setLocation(u.getLocation());
            m.setAvatar(u.getAvatar());
        });
    }

    public static void updatePassword(int userID, String passwordHash) {
        DBUtil.execute(em -> em.find(User.class, userID).setPasswordHash(passwordHash));
    }

    public static void updateStatus(int userID, Status status) {
        DBUtil.execute(em -> em.find(User.class, userID).setStatus(status));
    }
}
