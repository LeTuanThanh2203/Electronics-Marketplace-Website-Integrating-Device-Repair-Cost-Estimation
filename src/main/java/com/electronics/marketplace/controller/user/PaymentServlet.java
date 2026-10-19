package com.electronics.marketplace.controller.user;

import com.electronics.marketplace.entity.Payment;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.repository.PaymentRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * FR-PAY-01/02 - man hinh thanh toan kenh NEW_DEVICE (SDD 13.8).
 * Chi mot action: view, nhan orderId tren query string.
 */
@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW = "/WEB-INF/views/order/payment.jsp";

    private final PaymentRepository paymentRepository = new PaymentRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Long orderId = parseId(req.getParameter("orderId"));
        if (orderId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thieu orderId.");
            return;
        }

        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // IDOR: chi Buyer cua don hoac Admin moi xem duoc thong tin thanh toan.
        boolean isBuyer = user.getId().equals(payment.getOrder().getBuyer().getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isBuyer && !isAdmin) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        req.setAttribute("payment", payment);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Khong co thao tac ghi: Admin xac nhan PAID thu cong (BR-44).
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

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
