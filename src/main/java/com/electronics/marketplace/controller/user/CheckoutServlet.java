package com.electronics.marketplace.controller.user;

import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.PaymentMethod;
import com.electronics.marketplace.service.CartService;
import com.electronics.marketplace.service.OrderService;
import com.electronics.marketplace.service.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * FR-CART-04 - checkout gio NEW_DEVICE (SDD 12.4, BR-43/44/45).
 *
 * GET  : hien form, nap ${cart}.
 * POST : mot transaction tao Order + OrderItem (snapshot gia) + Payment PENDING,
 *        roi redirect sang history (POST-redirect-GET, chong F5 tao don trung).
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW = "/WEB-INF/views/order/checkout.jsp";

    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user != null) {
            req.setAttribute("cart", cartService.viewCart(user.getId()));
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            // Gia KHONG lay tu form: OrderService doc lai Product.price tu DB.
            orderService.checkoutCart(user.getId(), parseMethod(req.getParameter("paymentMethod")));
        } catch (ServiceException e) {
            // Du lieu khong hop le hoac hang vua het: ve lai form kem ly do.
            req.setAttribute("flashError", e.getMessage());
            req.setAttribute("cart", cartService.viewCart(user.getId()));
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }

        // Flash qua session de song sot qua redirect (POST-redirect-GET).
        req.getSession().setAttribute("flashOk", "Dat hang thanh cong. Vui long cho xac nhan.");
        resp.sendRedirect(req.getContextPath() + "/order?action=history");
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("currentUser");
    }

    /**
     * checkout.jsp:14 - COD gui chuoi rong (method = null), QR/MOMO/BANK map
     * thang vao enum. Gia tri la sai thi coi nhu COD, khong nem loi: form chi
     * co 4 radio, con lai la nguoi gui tay.
     */
    private PaymentMethod parseMethod(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return PaymentMethod.valueOf(raw.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
