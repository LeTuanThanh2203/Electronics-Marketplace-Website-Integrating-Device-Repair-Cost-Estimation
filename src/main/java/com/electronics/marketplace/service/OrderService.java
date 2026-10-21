package com.electronics.marketplace.service;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.Cart;
import com.electronics.marketplace.entity.CartItem;
import com.electronics.marketplace.entity.Listing;
import com.electronics.marketplace.entity.Order;
import com.electronics.marketplace.entity.OrderItem;
import com.electronics.marketplace.entity.Payment;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.ListingStatus;
import com.electronics.marketplace.entity.enums.OrderStatus;
import com.electronics.marketplace.entity.enums.OrderType;
import com.electronics.marketplace.entity.enums.PaymentMethod;
import com.electronics.marketplace.entity.enums.PaymentStatus;
import com.electronics.marketplace.entity.enums.ProductStatus;
import com.electronics.marketplace.entity.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Tao Order (2 kenh) va doi trang thai don.
 *
 * Moi method mo EM + transaction rieng, KHONG dung lai repository: BR-20,
 * BR-31/32 va BR-46 doi hoi kiem tra + ghi trong cung mot transaction.
 */
public class OrderService {

    /** BR-31: toi da 3 don Marketplace dang active tren moi Buyer. */
    private static final int MAX_ACTIVE_MARKETPLACE_ORDERS = 3;

    /**
     * Dat mot Listing. BR-20: UPDATE co dieu kien status = AVAILABLE trong cung
     * transaction; BR-31/32: khoa dong Buyer truoc khi dem quota.
     */
    public Long createMarketplaceOrder(Long buyerId, Long listingId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // BR-32: khoa dong Buyer de serialize cac request dong thoi cua cung Buyer.
            User buyer = em.find(User.class, buyerId, LockModeType.PESSIMISTIC_WRITE);
            if (buyer == null) {
                throw new ServiceException(401, "UNAUTHENTICATED", "Phien dang nhap khong hop le.");
            }

            Listing listing = em.find(Listing.class, listingId);
            if (listing == null) {
                throw new ServiceException(404, "LISTING_NOT_FOUND", "Khong tim thay tin dang.");
            }
            if (Boolean.TRUE.equals(listing.getIsRemovedByAdmin())
                    || !Boolean.TRUE.equals(listing.getIsApproved())) {
                throw new ServiceException(409, "LISTING_NOT_AVAILABLE", "Tin dang khong con ban.");
            }
            if (buyerId.equals(listing.getSeller().getId())) {
                throw new ServiceException(409, "CANNOT_BUY_OWN_LISTING", "Khong the mua tin cua chinh minh.");
            }

            // BR-31: dem don active cua Buyer. Da khoa dong Buyer o tren nen so dem
            // nay khong bi request dong thoi lam sai.
            Long active = em.createQuery(
                            "SELECT COUNT(o) FROM Order o " +
                                    "WHERE o.buyer.id = :buyerId AND o.orderType = :type " +
                                    "AND o.status IN :statuses",
                            Long.class)
                    .setParameter("buyerId", buyerId)
                    .setParameter("type", OrderType.MARKETPLACE)
                    .setParameter("statuses", activeStatuses())
                    .getSingleResult();
            if (active >= MAX_ACTIVE_MARKETPLACE_ORDERS) {
                throw new ServiceException(409, "MAX_ACTIVE_LISTING_ORDERS_REACHED",
                        "Ban dang co " + MAX_ACTIVE_MARKETPLACE_ORDERS + " don cho xac nhan.");
            }

            Order order = newOrder(buyer, OrderType.MARKETPLACE);
            em.persist(order);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setListing(listing);
            item.setPrice(listing.getPrice());   // BR-41: snapshot gia
            item.setQuantity(1);                 // BR-38
            order.getItems().add(item);
            em.persist(item);

            // BR-20: check-and-act trong MOT cau UPDATE, khong tach thanh hai buoc.
            int affected = em.createQuery(
                            "UPDATE Listing l SET l.status = :reserved, " +
                                    "l.reservedByOrderId = :orderId, l.reservedAt = :now " +
                                    "WHERE l.id = :listingId AND l.status = :available")
                    .setParameter("reserved", ListingStatus.RESERVED)
                    .setParameter("orderId", order.getId())
                    .setParameter("now", new Date())
                    .setParameter("listingId", listingId)
                    .setParameter("available", ListingStatus.AVAILABLE)
                    .executeUpdate();
            if (affected == 0) {
                // BR-20: khong con AVAILABLE -> rollback ca transaction.
                throw new ServiceException(409, "LISTING_NOT_AVAILABLE", "Tin dang vua bi nguoi khac dat.");
            }

            tx.commit();
            return order.getId();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Checkout gio NEW_DEVICE (SDD 12.4): mot transaction gom Order + OrderItem
     * snapshot gia + Payment PENDING. Gia doc lai tu DB, khong lay tu form.
     *
     * @param method phuong thuc Customer chon; null = COD (checkout.jsp:14)
     */
    public Long checkoutCart(Long buyerId, PaymentMethod method) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User buyer = em.find(User.class, buyerId);
            if (buyer == null) {
                throw new ServiceException(401, "UNAUTHENTICATED", "Phien dang nhap khong hop le.");
            }

            List<Cart> carts = em.createQuery(
                            "SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.items i " +
                                    "LEFT JOIN FETCH i.product WHERE c.customer.id = :uid",
                            Cart.class)
                    .setParameter("uid", buyerId)
                    .getResultList();
            Cart cart = carts.isEmpty() ? null : carts.get(0);
            if (cart == null || cart.getItems().isEmpty()) {
                throw new ServiceException(400, "CART_EMPTY", "Gio hang dang trong.");
            }

            Order order = newOrder(buyer, OrderType.NEW_DEVICE);
            em.persist(order);

            for (CartItem cartItem : cart.getItems()) {
                Product product = cartItem.getProduct();
                if (product.getStatus() != ProductStatus.AVAILABLE) {
                    throw new ServiceException(409, "PRODUCT_NOT_AVAILABLE",
                            "San pham \"" + product.getName() + "\" da ngung ban.");
                }
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProduct(product);
                item.setPrice(product.getPrice());   // BR-41: snapshot tai thoi diem dat
                item.setQuantity(cartItem.getQuantity());
                order.getItems().add(item);
                em.persist(item);
            }

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setMethod(method);           // null = COD, xem checkout.jsp:14
            payment.setAmount(order.getTotalAmount());
            payment.setTransactionCode("TXN-" + UUID.randomUUID().toString().replace("-", ""));
            payment.setCreatedAt(new Date());
            em.persist(payment);

            // BR-45: Cart khong reserve Product; don da tao xong thi gio khong con
            // giu gi. SDD khong noi phai xoa, nhung de lai se cho phep dat lai
            // chinh mon vua mua.
            for (CartItem cartItem : new ArrayList<>(cart.getItems())) {
                cart.getItems().remove(cartItem);
                em.remove(cartItem);
            }
            cart.setUpdatedAt(new Date());

            tx.commit();
            return order.getId();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Huy don truoc COMPLETED (BR-28). MARKETPLACE: tra Listing ve AVAILABLE va
     * xoa reservedByOrderId/reservedAt (BR-21). NEW_DEVICE: Payment PENDING ->
     * FAILED trong cung transaction (BR-46); PAID giu nguyen.
     */
    public void cancel(Long userId, Long orderId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Order order = em.find(Order.class, orderId);
            if (order == null) {
                throw new ServiceException(404, "ORDER_NOT_FOUND", "Khong tim thay don hang.");
            }
            if (!userId.equals(order.getBuyer().getId())) {
                throw new ServiceException(403, "FORBIDDEN", "Day khong phai don cua ban.");
            }
            if (order.getStatus() == OrderStatus.COMPLETED) {
                throw new ServiceException(409, "ORDER_NOT_CANCELLABLE", "Don da hoan tat, khong huy duoc.");
            }
            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new ServiceException(409, "ORDER_NOT_CANCELLABLE", "Don da huy.");
            }

            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(new Date());

            if (order.getOrderType() == OrderType.MARKETPLACE) {
                releaseListing(em, orderId);
            } else {
                Payment payment = findPayment(em, orderId);
                if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                    payment.setStatus(PaymentStatus.FAILED);
                }
            }

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

    /**
     * Buyer xac nhan da nhan hang (BR-24) hoac Customer xac nhan don NEW_DEVICE
     * da PAID (BR-46). MARKETPLACE: Listing -> SOLD va xoa reservation (BR-18).
     */
    public void complete(Long userId, Long orderId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Order order = em.find(Order.class, orderId);
            if (order == null) {
                throw new ServiceException(404, "ORDER_NOT_FOUND", "Khong tim thay don hang.");
            }
            if (!userId.equals(order.getBuyer().getId())) {
                throw new ServiceException(403, "FORBIDDEN", "Day khong phai don cua ban.");
            }
            if (order.getStatus() != OrderStatus.CONFIRMED) {
                throw new ServiceException(409, "ORDER_NOT_COMPLETABLE", "Chi don da xac nhan moi hoan tat duoc.");
            }

            Payment payment = findPayment(em, orderId);
            // BR-44: NEW_DEVICE chi COMPLETED khi Payment PAID. MARKETPLACE khong
            // co Payment nen khong xet.
            if (order.getOrderType() == OrderType.NEW_DEVICE) {
                if (payment == null || payment.getStatus() != PaymentStatus.PAID) {
                    throw new ServiceException(409, "PAYMENT_NOT_PAID", "Don chua thanh toan.");
                }
            }

            order.setStatus(OrderStatus.COMPLETED);
            order.setUpdatedAt(new Date());

            if (order.getOrderType() == OrderType.MARKETPLACE) {
                markListingSold(em, orderId);
                // completedSalesCount la chi so phia SELLER (SDD 13.2), tinh trong
                // cung transaction de khong lech khi co loi giua chung.
                Listing listing = findOrderListing(em, orderId);
                if (listing != null) {
                    User seller = em.find(User.class, listing.getSeller().getId());
                    seller.setCompletedSalesCount(seller.getCompletedSalesCount() + 1);
                }
            }

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

    /** Danh sach status duoc coi la "active" cho quota BR-31. */
    public static List<OrderStatus> activeStatuses() {
        List<OrderStatus> statuses = new ArrayList<>();
        statuses.add(OrderStatus.PENDING);
        statuses.add(OrderStatus.CONFIRMED);
        return statuses;
    }

    /**
     * Seller xac nhan PENDING -> CONFIRMED (BR-23). Khong thuoc 3 action cua
     * OrderServlet (do la man hinh cua Seller, Thành/Admin phu trach) nhung de
     * o day vi cung mot quy tac trang thai.
     */
    public void confirm(Long sellerId, Long orderId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Order order = em.find(Order.class, orderId);
            if (order == null) {
                throw new ServiceException(404, "ORDER_NOT_FOUND", "Khong tim thay don hang.");
            }
            Listing listing = findOrderListing(em, orderId);
            if (listing == null || !sellerId.equals(listing.getSeller().getId())) {
                throw new ServiceException(403, "FORBIDDEN", "Ban khong phai nguoi ban cua don nay.");
            }
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new ServiceException(409, "ORDER_NOT_CONFIRMABLE", "Chi don dang cho moi xac nhan duoc.");
            }

            order.setStatus(OrderStatus.CONFIRMED);
            order.setUpdatedAt(new Date());

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

    /**
     * Admin xac nhan da thu tien (BR-44). Admin la nguoi duy nhat ghi PAID.
     */
    public void confirmPaymentPaid(Long adminId, Long orderId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User admin = em.find(User.class, adminId);
            if (admin == null || admin.getRole() != Role.ADMIN) {
                throw new ServiceException(403, "FORBIDDEN", "Chi Admin moi xac nhan thanh toan.");
            }

            Order order = em.find(Order.class, orderId);
            if (order == null || order.getOrderType() != OrderType.NEW_DEVICE) {
                throw new ServiceException(404, "PAYMENT_NOT_FOUND", "Don nay khong co thanh toan.");
            }
            Payment payment = findPayment(em, orderId);
            if (payment == null) {
                throw new ServiceException(404, "PAYMENT_NOT_FOUND", "Khong tim thay thanh toan.");
            }
            if (payment.getStatus() == PaymentStatus.PAID) {
                throw new ServiceException(409, "PAYMENT_ALREADY_PAID", "Thanh toan da duoc xac nhan.");
            }

            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(new Date());

            // BR-46: NEW_DEVICE PENDING -> CONFIRMED khi PAID.
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
                order.setUpdatedAt(new Date());
            }

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

    /**
     * Cong thanh toan (MoMo) bao da thu du tien.
     *
     * Khac confirmPaymentPaid: khong co Admin va IDEMPOTENT - MoMo co the goi IPN
     * nhieu lan, hoac IPN den sau khi return URL da xu ly xong.
     * amount la so tien MoMo bao da thu, phai khop Payment.amount de chan IPN gia.
     */
    public void markPaidByGateway(Long paymentId, long amount) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Payment payment = em.find(Payment.class, paymentId);
            if (payment == null) {
                throw new ServiceException(404, "PAYMENT_NOT_FOUND", "Khong tim thay thanh toan.");
            }
            Order order = payment.getOrder();
            if (order.getOrderType() != OrderType.NEW_DEVICE) {
                throw new ServiceException(404, "PAYMENT_NOT_FOUND", "Don nay khong co thanh toan.");
            }
            if (payment.getStatus() == PaymentStatus.PAID) {
                return;     // IPN/return trung lap: da xu ly roi.
            }
            if (payment.getStatus() != PaymentStatus.PENDING) {
                throw new ServiceException(409, "PAYMENT_NOT_PENDING", "Giao dich da ket thuc.");
            }
            if (payment.getMethod() != PaymentMethod.MOMO) {
                throw new ServiceException(409, "PAYMENT_NOT_MOMO", "Don nay khong thanh toan qua MoMo.");
            }
            long expected = payment.getAmount().setScale(0, RoundingMode.HALF_UP).longValue();
            if (expected != amount) {
                throw new ServiceException(409, "PAYMENT_AMOUNT_MISMATCH", "So tien MoMo bao khong khop don hang.");
            }

            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(new Date());

            // BR-46: NEW_DEVICE PENDING -> CONFIRMED khi PAID.
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
                order.setUpdatedAt(new Date());
            }

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

    private Order newOrder(User buyer, OrderType type) {
        Date now = new Date();
        Order order = new Order();
        order.setBuyer(buyer);
        order.setOrderType(type);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return order;
    }

    private Payment findPayment(EntityManager em, Long orderId) {
        List<Payment> payments = em.createQuery(
                        "SELECT p FROM Payment p WHERE p.order.id = :orderId",
                        Payment.class)
                .setParameter("orderId", orderId)
                .getResultList();
        return payments.isEmpty() ? null : payments.get(0);
    }

    private Listing findOrderListing(EntityManager em, Long orderId) {
        List<Listing> listings = em.createQuery(
                        "SELECT i.listing FROM OrderItem i WHERE i.order.id = :orderId AND i.listing IS NOT NULL",
                        Listing.class)
                .setParameter("orderId", orderId)
                .getResultList();
        return listings.isEmpty() ? null : listings.get(0);
    }

    /** BR-21: huy don thi tra Listing ve AVAILABLE va xoa reservation. */
    private void releaseListing(EntityManager em, Long orderId) {
        em.createQuery(
                        "UPDATE Listing l SET l.status = :available, " +
                                "l.reservedByOrderId = NULL, l.reservedAt = NULL " +
                                "WHERE l.reservedByOrderId = :orderId")
                .setParameter("available", ListingStatus.AVAILABLE)
                .setParameter("orderId", orderId)
                .executeUpdate();
    }

    /** BR-18: COMPLETED -> SOLD, dong thoi xoa reservation. */
    private void markListingSold(EntityManager em, Long orderId) {
        em.createQuery(
                        "UPDATE Listing l SET l.status = :sold, " +
                                "l.reservedByOrderId = NULL, l.reservedAt = NULL " +
                                "WHERE l.reservedByOrderId = :orderId")
                .setParameter("sold", ListingStatus.SOLD)
                .setParameter("orderId", orderId)
                .executeUpdate();
    }
}
