package com.electronics.marketplace.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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
        if ("remove".equals(req.getParameter("action"))) {
            // ponytail: Buoc 3 - xoa CartItem roi tra JSON {ok, subtotal, count}
            // dung hop dong da ghi trong cart.jsp. Chua lam duoc vi Cart/CartItem
            // chua map JPA (dang cho User.java cua Tan).
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }
        showCart(req, resp);
    }

    /** cart.jsp tu fallback sang sessionScope.cart nen chua can nap model. */
    private void showCart(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
