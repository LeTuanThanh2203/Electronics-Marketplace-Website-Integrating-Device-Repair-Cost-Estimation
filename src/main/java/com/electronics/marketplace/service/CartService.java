package com.electronics.marketplace.service;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.Cart;
import com.electronics.marketplace.entity.CartItem;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.ProductStatus;
import com.electronics.marketplace.repository.CartRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Date;
import java.util.List;

/**
 * Gio hang kenh NEW_DEVICE (BR-45: quantity luon 1, Cart khong reserve Product
 * va khong doi Product.status).
 *
 * Cac thao tac ghi mo mot EM + transaction rieng: khong the ghep tu nhieu
 * method EM ngan han cua repository ma van nguyen tu.
 */
public class CartService {

    private final CartRepository cartRepository = new CartRepository();

    public Cart viewCart(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    /**
     * Them mot Product vao gio. Da co san thi khong lam gi (BR-45: khong cong
     * don so luong, (cart_id, product_id) la duy nhat).
     */
    public void addItem(Long userId, Long productId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Product product = em.find(Product.class, productId);
            if (product == null) {
                throw new ServiceException(404, "PRODUCT_NOT_FOUND", "Khong tim thay san pham.");
            }
            if (product.getStatus() != ProductStatus.AVAILABLE) {
                throw new ServiceException(409, "PRODUCT_NOT_AVAILABLE", "San pham da ngung ban.");
            }

            Cart cart = findCart(em, userId);
            if (cart == null) {
                User customer = em.find(User.class, userId);
                if (customer == null) {
                    throw new ServiceException(401, "UNAUTHENTICATED", "Phien dang nhap khong hop le.");
                }
                cart = new Cart();
                cart.setCustomer(customer);
                cart.setCreatedAt(new Date());
                em.persist(cart);
            }

            Long existing = em.createQuery(
                            "SELECT COUNT(ci) FROM CartItem ci " +
                                    "WHERE ci.cart.id = :cartId AND ci.product.id = :productId",
                            Long.class)
                    .setParameter("cartId", cart.getId())
                    .setParameter("productId", productId)
                    .getSingleResult();
            if (existing == 0) {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setProduct(product);
                item.setQuantity(1);
                item.setAddedAt(new Date());
                em.persist(item);
            }

            cart.setUpdatedAt(new Date());
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

    /** Xoa mot dong. Chi chu cua Cart moi duoc xoa (IDOR -> 403). */
    public void removeItem(Long userId, Long itemId) {
        EntityManager em = JpaConfig.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            CartItem item = em.find(CartItem.class, itemId);
            if (item == null) {
                throw new ServiceException(404, "CART_ITEM_NOT_FOUND", "Khong tim thay dong trong gio.");
            }
            // getCart().getCustomer().getId(): ca hai deu LAZY nhung getId() tren
            // proxy khong kich hoat load.
            if (!userId.equals(item.getCart().getCustomer().getId())) {
                throw new ServiceException(403, "FORBIDDEN", "Day khong phai gio hang cua ban.");
            }

            Cart cart = item.getCart();
            em.remove(item);
            cart.setUpdatedAt(new Date());

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

    private Cart findCart(EntityManager em, Long userId) {
        List<Cart> carts = em.createQuery(
                        "SELECT c FROM Cart c WHERE c.customer.id = :uid",
                        Cart.class)
                .setParameter("uid", userId)
                .getResultList();
        return carts.isEmpty() ? null : carts.get(0);
    }
}
