package com.electronics.marketplace.DBConnection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.hibernate.Session;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.util.MailUtilGmail;
import com.electronics.marketplace.util.PasswordUtil;


@WebListener
public class DatabaseInitializer implements ServletContextListener {

    public static final String ADMIN_EMAIL = "cntann06@gmail.com";
    public static final String ADMIN_PASSWORD = "Admin@123";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            createDatabaseIfMissing();
            DBConnection.getEmf();      // khởi tạo JPA, hbm2ddl=update tự tạo bảng users
            seedAdmin();
            sce.getServletContext().log("Database '" + DBConnection.dbName() + "' san sang.");
        } catch (PersistenceException e) {
            throw new IllegalStateException("Khong khoi tao duoc database. Kiem tra PostgreSQL va db.properties", e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBConnection.close();
        MailUtilGmail.shutdown();
    }

    private void createDatabaseIfMissing() {
        String db = DBConnection.dbName();
        if (!db.matches("[A-Za-z0-9_]+")) throw new IllegalStateException("Ten database khong hop le: " + db);

        EntityManagerFactory adminEmf = DBConnection.createAdminEmf();
        EntityManager em = adminEmf.createEntityManager();
        try {
            em.unwrap(Session.class).doWork(conn -> {
                conn.setAutoCommit(true);   // CREATE DATABASE không chạy được trong transaction
                boolean exists;
                try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
                    ps.setString(1, db);
                    try (ResultSet rs = ps.executeQuery()) {
                        exists = rs.next();
                    }
                }
                if (!exists) {
                    try (Statement st = conn.createStatement()) {
                        st.executeUpdate("CREATE DATABASE " + db);
                    }
                }
            });
        } finally {
            em.close();
            adminEmf.close();
        }
    }

    private void seedAdmin() {
        if (UserDB.existsAdmin()) return;
        User a = new User();
        a.setName("Quản trị viên");
        a.setEmail(ADMIN_EMAIL);
        a.setPhone("0900000000");
        a.setPasswordHash(PasswordUtil.hash(ADMIN_PASSWORD));
        a.setRole(Role.ADMIN);
        UserDB.insert(a);
    }
}
