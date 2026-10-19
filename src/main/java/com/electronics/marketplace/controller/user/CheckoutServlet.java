package com.electronics.marketplace.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Dat hang kenh NEW_DEVICE: GET hien form, POST tao Order + Payment (BR-43/44/45).
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW = "/WEB-INF/views/order/checkout.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // ponytail: Buoc 3 - 1 transaction: doc lai Product.price tu DB (KHONG lay
        // gia tu form) -> Order + OrderItem snapshot gia + Payment PENDING, roi
        // sendRedirect sang /order?action=history (POST-redirect-GET, chong F5
        // tao don trung). Chua lam duoc vi Order/Payment chua map JPA.
        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
}
