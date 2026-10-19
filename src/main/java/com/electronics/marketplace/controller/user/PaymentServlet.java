package com.electronics.marketplace.controller.user;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // ponytail: Buoc 3 - nap ${payment} theo orderId, va BAT BUOC kiem tra
        // payment.order.buyer.id == currentUser.id truoc khi hien (IDOR: xem duoc
        // thong tin thanh toan cua nguoi khac neu thieu check nay).
        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Khong co thao tac ghi: Admin xac nhan PAID thu cong (BR-44).
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
