package com.electronics.marketplace.service;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.Cart;
import com.electronics.marketplace.entity.CartItem;
import com.electronics.marketplace.entity.Device;
import com.electronics.marketplace.entity.Listing;
import com.electronics.marketplace.entity.Order;
import com.electronics.marketplace.entity.Payment;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.ListingCondition;
import com.electronics.marketplace.entity.enums.ListingStatus;
import com.electronics.marketplace.entity.enums.OrderStatus;
import com.electronics.marketplace.entity.enums.OrderType;
import com.electronics.marketplace.entity.enums.PaymentMethod;
import com.electronics.marketplace.entity.enums.PaymentStatus;
import com.electronics.marketplace.entity.enums.ProductStatus;
import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.entity.enums.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Check cho 3 quy tac khong the thay bang compile: BR-20 (reserve Listing co
 * dieu kien), BR-21 (huy don tra Listing ve AVAILABLE) va BR-45/44 (checkout
 * tao Payment PENDING voi gia snapshot tu DB).
 *
 * OrderService tu commit nen test phai don rac sau moi lan chay - xem cleanup().
 * Chay: mvn -o test -Dtest=OrderServiceTest
 */
class OrderServiceTest {

    private final OrderService orderService = new OrderService();
    private final String tag = "it-" + System.nanoTime();

    @AfterEach
    void cleanup() {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Xoa theo thu tu FK: payment -> order_item -> order -> listing -> ...
            // Dung subquery thay vi join ngam: JPQL bulk delete khong cho phep
            // duong dan quan he nhieu cap trong WHERE.
            String buyerEmails = "SELECT o.id FROM Order o WHERE o.buyer.email LIKE :tag";
            em.createQuery("DELETE FROM Payment p WHERE p.order.id IN (" + buyerEmails + ")")
                    .setParameter("tag", tag + "%").executeUpdate();
            em.createQuery("DELETE FROM OrderItem i WHERE i.order.id IN (" + buyerEmails + ")")
                    .setParameter("tag", tag + "%").executeUpdate();
            em.createQuery("DELETE FROM Order o WHERE o.buyer.email LIKE :tag")
                    .setParameter("tag", tag + "%").executeUpdate();
            em.createQuery("DELETE FROM Listing l WHERE l.seller.email LIKE :tag")
                    .setParameter("tag", tag + "%").executeUpdate();
            String cartIds = "SELECT c.id FROM Cart c WHERE c.customer.email LIKE :tag";
            em.createQuery("DELETE FROM CartItem ci WHERE ci.cart.id IN (" + cartIds + ")")
                    .setParameter("tag", tag + "%").executeUpdate();
            em.createQuery("DELETE FROM Cart c WHERE c.customer.email LIKE :tag")
                    .setParameter("tag", tag + "%").executeUpdate();
            em.createQuery("DELETE FROM Product p WHERE p.name LIKE :tag")
                    .setParameter("tag", tag + "%").executeUpdate();
            em.createQuery("DELETE FROM Device d WHERE d.name LIKE :tag")
                    .setParameter("tag", tag + "%").executeUpdate();
            em.createQuery("DELETE FROM User u WHERE u.email LIKE :tag")
                    .setParameter("tag", tag + "%").executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Test
    void datListingThiReserveVaKhongChoNguoiThuHaiDat() {
        Long sellerId = persistUser("seller", Role.USER);
        Long buyerId = persistUser("buyer", Role.USER);
        Long otherId = persistUser("other", Role.USER);
        Long listingId = persistListing(sellerId);

        orderService.createMarketplaceOrder(buyerId, listingId);

        assertEquals(ListingStatus.RESERVED, readListing(listingId).getStatus(),
                "BR-20: dat hang phai chuyen Listing sang RESERVED");
        assertNotNull(readListing(listingId).getReservedByOrderId(),
                "BR-20: phai ghi reservedByOrderId");

        ServiceException e = assertThrows(ServiceException.class,
                () -> orderService.createMarketplaceOrder(otherId, listingId),
                "Listing da RESERVED thi nguoi thu hai phai bi tu choi");
        assertEquals("LISTING_NOT_AVAILABLE", e.getCode());
        assertEquals(409, e.getStatus());
    }

    @Test
    void huyDonThiTraListingVeAvailable() {
        Long sellerId = persistUser("seller", Role.USER);
        Long buyerId = persistUser("buyer", Role.USER);
        Long listingId = persistListing(sellerId);

        Long orderId = orderService.createMarketplaceOrder(buyerId, listingId);
        orderService.cancel(buyerId, orderId);

        Listing listing = readListing(listingId);
        assertEquals(ListingStatus.AVAILABLE, listing.getStatus(), "BR-21: huy don phai tra Listing ve AVAILABLE");
        assertNull(listing.getReservedByOrderId(), "BR-21: phai xoa reservedByOrderId");
        assertNull(listing.getReservedAt(), "BR-21: phai xoa reservedAt");
        assertEquals(OrderStatus.CANCELLED, readOrder(orderId).getStatus());
    }

    @Test
    void checkoutChotGiaTuDbVaTaoPaymentPending() {
        Long buyerId = persistUser("buyer", Role.USER);
        Long productId = persistProduct("May A", "15000000.00");

        // Gia tren Product doi SAU khi cho vao gio: Order phai giu gia luc checkout,
        // khong lay gia tu form.
        addToCart(buyerId, productId);

        Long orderId = orderService.checkoutCart(buyerId, PaymentMethod.MOMO);

        Order order = readOrder(orderId);
        assertEquals(OrderType.NEW_DEVICE, order.getOrderType());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(0, new BigDecimal("15000000.00").compareTo(order.getTotalAmount()),
                "BR-41: OrderItem.price la snapshot tai thoi diem dat");

        Payment payment = readPayment(orderId);
        assertEquals(PaymentStatus.PENDING, payment.getStatus(), "SDD 12.4: Payment tao o PENDING");
        assertEquals(PaymentMethod.MOMO, payment.getMethod(),
                "Phuong thuc Customer chon phai duoc luu vao Payment.method");
        assertEquals(0, order.getTotalAmount().compareTo(payment.getAmount()));
        assertNotNull(payment.getTransactionCode(), "BR-44: transactionCode sinh luc tao");

        assertEquals(0, readCartItemCount(buyerId), "Checkout xong gio phai rong");
    }

    @Test
    void completeChanKhiChuaThanhToan() {
        Long buyerId = persistUser("buyer", Role.USER);
        Long productId = persistProduct("May B", "100.00");
        addToCart(buyerId, productId);
        Long orderId = orderService.checkoutCart(buyerId, null);   // COD

        ServiceException e = assertThrows(ServiceException.class,
                () -> orderService.complete(buyerId, orderId));
        assertEquals(409, e.getStatus());
        assertEquals(OrderStatus.PENDING, readOrder(orderId).getStatus(),
                "complete that bai thi trang thai khong duoc doi");
    }

    // ---------- helpers ----------

    private Long persistUser(String role, Role r) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User user = new User();
            user.setName(tag + "-" + role);
            user.setEmail(tag + "-" + role + "@test.local");
            user.setPasswordHash("x");
            user.setRole(r);
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(new Date());
            em.persist(user);
            tx.commit();
            return user.getId();
        } finally {
            em.close();
        }
    }

    private Long persistProduct(String name, String price) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product product = new Product();
            product.setName(tag + "-" + name);
            product.setPrice(new BigDecimal(price));
            product.setStatus(ProductStatus.AVAILABLE);
            em.persist(product);
            tx.commit();
            return product.getId();
        } finally {
            em.close();
        }
    }

    private Long persistListing(Long sellerId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Device device = new Device();
            device.setName(tag + "-ThinkPad");
            device.setBrand("lenovo");
            em.persist(device);

            Listing listing = new Listing();
            listing.setSeller(em.find(User.class, sellerId));
            listing.setDevice(device);
            listing.setCondition(ListingCondition.USED_GOOD);
            listing.setDescription("May test");
            listing.setPrice(new BigDecimal("12000000.00"));
            listing.setIsApproved(true);
            listing.setIsRemovedByAdmin(false);
            em.persist(listing);
            tx.commit();
            return listing.getId();
        } finally {
            em.close();
        }
    }

    private void addToCart(Long userId, Long productId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Cart cart = new Cart();
            cart.setCustomer(em.find(User.class, userId));
            cart.setCreatedAt(new Date());
            em.persist(cart);

            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(em.find(Product.class, productId));
            item.setQuantity(1);
            item.setAddedAt(new Date());
            em.persist(item);
            tx.commit();
        } finally {
            em.close();
        }
    }

    private Listing readListing(Long id) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Listing.class, id);
        } finally {
            em.close();
        }
    }

    private Order readOrder(Long id) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            Order order = em.find(Order.class, id);
            order.getItems().size();   // nap LAZY truoc khi dong EM
            return order;
        } finally {
            em.close();
        }
    }

    private Payment readPayment(Long orderId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Payment p WHERE p.order.id = :id", Payment.class)
                    .setParameter("id", orderId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    private long readCartItemCount(Long userId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.customer.id = :uid", Long.class)
                    .setParameter("uid", userId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
