package com.electronics.marketplace.listener;

import com.electronics.marketplace.DBConnection.UserDB;
import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.Device;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.entity.ProductSpecification;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.repository.MockProductData;
import com.electronics.marketplace.util.PasswordUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.List;

/**
 * Tạo tài khoản admin và nạp dữ liệu mẫu ban đầu vào PostgreSQL khi ứng dụng khởi động.
 */
@WebListener
public class AdminSeedListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        try {
            String email = System.getenv("ADMIN_EMAIL");
            String password = System.getenv("ADMIN_PASSWORD");
            if (email == null || email.trim().isEmpty()) {
                email = "admin@marketplace.com";
            }
            if (password == null || password.trim().isEmpty()) {
                password = "admin123";
            }

            if (!UserDB.existsAdmin()) {
                User a = new User();
                a.setName("Quản trị viên Hệ thống");
                a.setEmail(email.trim());
                a.setPasswordHash(PasswordUtil.hash(password));
                a.setRole(Role.ADMIN);
                UserDB.insert(a);
                ctx.log("Đã tạo tài khoản admin mặc định: " + email.trim() + " / " + password);
            }

            if (!UserDB.emailExists("user@marketplace.com")) {
                User u = new User();
                u.setName("Người dùng Mẫu");
                u.setEmail("user@marketplace.com");
                u.setPasswordHash(PasswordUtil.hash("user123"));
                u.setRole(Role.USER);
                UserDB.insert(u);
                ctx.log("Đã tạo tài khoản user mẫu: user@marketplace.com / user123");
            }

            // Tự động nạp sản phẩm máy mới mẫu vào PostgreSQL nếu bảng rỗng
            seedProductsIfEmpty(ctx);

        } catch (Exception e) {
            ctx.log("Lỗi khởi tạo tài khoản seed: " + e.getMessage());
        }
    }

    private void seedProductsIfEmpty(ServletContext ctx) {
        try {
            EntityManagerFactory emf = JpaConfig.getEntityManagerFactory();
            if (emf == null || !emf.isOpen()) return;
            EntityManager em = emf.createEntityManager();
            try {
                Long count = em.createQuery("SELECT COUNT(p) FROM Product p", Long.class).getSingleResult();
                if (count == 0) {
                    EntityTransaction tx = em.getTransaction();
                    tx.begin();
                    List<Device> devices = em.createQuery("SELECT d FROM Device d", Device.class).getResultList();
                    Device defaultDev = devices.isEmpty() ? null : devices.get(0);

                    for (Product p : MockProductData.getSampleProducts()) {
                        Product newP = new Product();
                        newP.setName(p.getName());
                        newP.setBrand(p.getBrand());
                        newP.setPrice(p.getPrice());
                        newP.setStatus(p.getStatus());
                        newP.setSku(p.getSku());
                        newP.setSeries(p.getSeries());
                        newP.setOriginalPrice(p.getOriginalPrice());
                        newP.setDiscountTag(p.getDiscountTag());
                        newP.setBadge(p.getBadge());
                        newP.setImageUrl(p.getImageUrl());
                        newP.setWarranty(p.getWarranty());
                        newP.setDescription(p.getDescription());
                        newP.setTargetNeed(p.getTargetNeed());
                        newP.setStockQuantity(p.getStockQuantity());
                        newP.setDevice(defaultDev);

                        for (ProductSpecification spec : p.getSpecifications()) {
                            ProductSpecification newSpec = new ProductSpecification(
                                    spec.getSpecKey(), spec.getDataType(), spec.getNumericValue(), spec.getTextValue(), spec.getUnit()
                            );
                            newP.addSpecification(newSpec);
                        }
                        em.persist(newP);
                    }
                    tx.commit();
                    ctx.log("AdminSeedListener: Đã tự động nạp 8 sản phẩm mẫu vào PostgreSQL");
                }
            } catch (Exception ex) {
                ctx.log("Không thể nạp dữ liệu sản phẩm mẫu: " + ex.getMessage());
            } finally {
                if (em.isOpen()) em.close();
            }
        } catch (Exception ignored) {
        }
    }
}