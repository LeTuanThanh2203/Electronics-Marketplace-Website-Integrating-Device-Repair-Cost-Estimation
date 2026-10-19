package com.electronics.marketplace.controller.user;

import com.electronics.marketplace.entity.Cart;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.service.CartService;
import com.electronics.marketplace.service.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * FR-CART-01/02/03 - gio hang kenh NEW_DEVICE (BR-45: quantity luon = 1).
 *
 * Mot servlet cho mot khu vuc, dieu huong bang ?action= (rule
 * web_programming_requires muc 17). Dang ky bang @WebServlet, khong khai lai
 * trong web.xml.
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW = "/WEB-INF/views/cart/cart.jsp";

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Xoa la thao tac pha huy: chi nhan POST.
        if ("remove".equals(req.getParameter("action"))) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        showCart(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("add".equals(action)) {
            handleAdd(req, resp);
            return;
        }
        if ("remove".equals(action)) {
            handleRemove(req, resp);
            return;
        }
        showCart(req, resp);
    }

    /** cart.jsp: nut "Them vao gio" gui productId. */
    private void handleAdd(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Long productId = parseId(req.getParameter("productId"));
        if (productId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thieu productId.");
            return;
        }
        try {
            cartService.addItem(user.getId(), productId);
        } catch (ServiceException e) {
            resp.sendError(e.getStatus(), e.getMessage());
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    /**
     * AJAX cua cart.jsp: tra JSON {ok, subtotal, count}. JS tu xoa dong va cap
     * nhat tong tien, count = 0 thi reload trang.
     */
    private void handleRemove(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        User user = currentUser(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Long itemId = parseId(req.getParameter("itemId"));
        if (itemId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thieu itemId.");
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            cartService.removeItem(user.getId(), itemId);
            Cart cart = cartService.viewCart(user.getId());
            BigDecimal subtotal = cart == null ? BigDecimal.ZERO : cart.getSubtotal();
            int count = cart == null ? 0 : cart.getItems().size();
            // Chi gom so va boolean nen khong can escape chuoi.
            resp.getWriter().write("{\"ok\":true,\"subtotal\":" + subtotal.toPlainString()
                    + ",\"count\":" + count + "}");
        } catch (ServiceException e) {
            resp.setStatus(e.getStatus());
            resp.getWriter().write("{\"ok\":false,\"code\":\"" + e.getCode() + "\"}");
        }
    }

    private void showCart(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user != null) {
            req.setAttribute("cart", cartService.viewCart(user.getId()));
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    /** Rule 3: session chi giu User + Cart. Filter (Tan) chiu trach nhiem chan URI. */
    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("currentUser");
    }

    private Long parseId(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
