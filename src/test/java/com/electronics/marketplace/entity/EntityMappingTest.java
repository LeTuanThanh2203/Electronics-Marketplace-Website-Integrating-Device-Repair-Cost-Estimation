package com.electronics.marketplace.entity;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.enums.ListingCondition;
import com.electronics.marketplace.entity.enums.OrderStatus;
import com.electronics.marketplace.entity.enums.OrderType;
import com.electronics.marketplace.entity.enums.PaymentStatus;
import com.electronics.marketplace.entity.enums.ProductStatus;
import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.entity.enums.UserStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Check duy nhat cho tang mapping cua module Cart/Order/Payment.
 *
 * Vi sao can: `mvn compile` chi kiem tra Java, KHONG kiem tra mapping. Hibernate
 * chi nem loi luc bootstrap (@ManyToOne vao class khong phai @Entity, thieu
 * @JoinColumn, ...). Test nay boot EMF that roi ghi/doc lai ca 5 bang - dung
 * thu ma compile khong the chung minh.
 *
 * Chay: mvn -o test -Dtest=EntityMappingTest
 * Can DB_URL / DB_USER / DB_PASSWORD trong moi truong.
 */
class EntityMappingTest {

    @Test
    void namEntityCuaModuleRoundTripQuaHibernate() {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Date now = new Date();

            User buyer = new User();
            buyer.setName("Nguoi Mua Test");
            buyer.setEmail("buyer-" + System.nanoTime() + "@test.local");
            buyer.setPasswordHash("$2a$10$khong-phaidau-that");
            buyer.setRole(Role.USER);
            buyer.setStatus(UserStatus.ACTIVE);
            buyer.setCreatedAt(now);
            em.persist(buyer);

            Product product = new Product();
            product.setName("Laptop Test");
            product.setBrand("TestBrand");
            product.setPrice(new BigDecimal("15000000.00"));
            product.setStatus(ProductStatus.AVAILABLE);
            em.persist(product);

            Cart cart = new Cart();
            cart.setCustomer(buyer);
            cart.setCreatedAt(now);
            em.persist(cart);

            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cartItem.setAddedAt(now);
            em.persist(cartItem);

            Order order = new Order();
            order.setBuyer(buyer);
            order.setOrderType(OrderType.NEW_DEVICE);
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(now);
            em.persist(order);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(1);
            em.persist(orderItem);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(product.getPrice());
            payment.setTransactionCode("TEST-" + System.nanoTime());
            payment.setCreatedAt(now);
            em.persist(payment);

            /*
             * flush + clear: day INSERT that su xuong DB (qua do kiem tra luon
             * FK, UNIQUE, precision) roi doc lai tu DB. Khong commit - finally
             * rollback nen DB dev khong bi bo lai rac sau moi lan chay test.
             */
            em.flush();
            em.clear();

            Cart reloaded = em.find(Cart.class, cart.getId());
            assertNotNull(reloaded, "Cart khong doc lai duoc");
            assertEquals(1, reloaded.getItems().size(), "Cart phai co dung 1 CartItem");
            assertEquals(0, new BigDecimal("15000000.00").compareTo(reloaded.getSubtotal()),
                    "getSubtotal() phai bang gia Product x quantity");

            Order reloadedOrder = em.find(Order.class, order.getId());
            assertNotNull(reloadedOrder, "Order khong doc lai duoc");
            assertEquals(1, reloadedOrder.getItems().size(), "Order phai co dung 1 OrderItem");
            assertEquals(0, product.getPrice().compareTo(reloadedOrder.getTotalAmount()),
                    "getTotalAmount() phai bang OrderItem.price x quantity");

            Payment reloadedPayment = em.find(Payment.class, payment.getId());
            assertNotNull(reloadedPayment, "Payment khong doc lai duoc");
            assertEquals(order.getId(), reloadedPayment.getOrder().getId());

            OrderItem reloadedItem = em.find(OrderItem.class, orderItem.getId());
            assertTrue(reloadedItem.getListing() == null,
                    "Order NEW_DEVICE khong duoc co listing_id");

            /*
             * OrderItem.listing phai la quan he @ManyToOne THAT, khong phai
             * @Transient. Check doc tu metamodel cua Hibernate nen khong phu
             * thuoc trang thai DB. Khong dung information_schema:
             * hbm2ddl.auto=update khong bao gio drop cot, nen cot cu con lai se
             * lam check kieu do pass gia.
             */
            assertNotNull(
                    em.getMetamodel().entity(OrderItem.class).getAttribute("listing"),
                    "OrderItem.listing phai la thuoc tinh persistent (khong duoc @Transient)");
            assertNotNull(
                    em.getMetamodel().entity(OrderItem.class).getAttribute("price"),
                    "OrderItem.price phai la thuoc tinh persistent");

            // Round-trip that: Order MARKETPLACE phai doc lai duoc listing + device.
            Device device = new Device();
            device.setName("ThinkPad Test");
            device.setBrand("lenovo");
            em.persist(device);

            Listing listing = new Listing();
            listing.setSeller(buyer);
            listing.setDevice(device);
            listing.setCondition(ListingCondition.USED_GOOD);
            listing.setDescription("May test");
            listing.setPrice(new BigDecimal("12000000.00"));
            em.persist(listing);

            OrderItem listingItem = new OrderItem();
            listingItem.setOrder(order);
            listingItem.setListing(listing);
            listingItem.setPrice(new BigDecimal("1.00"));
            listingItem.setQuantity(1);
            em.persist(listingItem);
            em.flush();
            em.clear();

            Listing reloadedListing = em.find(OrderItem.class, listingItem.getId()).getListing();
            assertNotNull(reloadedListing, "listing_id bi mat khi ghi/doc - Order MARKETPLACE mat lien ket Listing");
            assertEquals(listing.getId(), reloadedListing.getId());
            assertEquals("ThinkPad Test", reloadedListing.getDevice().getName(),
                    "history.jsp:134 doc item.listing.device.name - phai co du lieu");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}