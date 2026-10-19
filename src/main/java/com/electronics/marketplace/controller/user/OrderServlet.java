package com.electronics.marketplace.controller.user;

import com.electronics.marketplace.entity.Order;
import com.electronics.marketplace.entity.Payment;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.OrderStatus;
import com.electronics.marketplace.repository.OrderRepository;
import com.electronics.marketplace.repository.PaymentRepository;
import com.electronics.marketplace.service.OrderService;
import com.electronics.marketplace.service.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lich su don hang + thao tac doi trang thai (BR-23/24/46).
 *
 * Ba action, tat ca deu POST tru hai history:
 *   history  - GET, phan trang + loc theo status
 *   cancel   - POST, Customer huy don truoc COMPLETED
 *   complete - POST, chi khi dong thoi CONFIRMED + Payment PAID
 */
@WebServlet("/order")
public class OrderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW = "/WEB-INF/views/order/history.jsp";

    private static final int PAGE_SIZE = 10;

    private final OrderRepository orderRepository = new OrderRepository();
    private final PaymentRepository paymentRepository = new PaymentRepository();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // history la action duy nhat dung GET; thieu action cung coi nhu history
        // de link ${ctx}/order van vao duoc.
        showHistory(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("cancel".equals(action) || "complete".equals(action)) {
            handleStatusChange(req, resp, action);
            return;
        }
        // POST la do action ngoai du lieu (F5 sau khi submit) - dua ve GET.
        resp.sendRedirect(req.getContextPath() + "/order?action=history");
    }

    private void showHistory(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        OrderStatus status = parseStatus(req.getParameter("status"));
        int page = parsePage(req.getParameter("page"));

        List<Order> orders = orderRepository.findHistory(user.getId(), status, page, PAGE_SIZE);
        long total = orderRepository.countHistory(user.getId(), status);
        int totalPages = (int) ((total + PAGE_SIZE - 1) / PAGE_SIZE);

        List<Long> orderIds = new ArrayList<>();
        for (Order order : orders) {
            orderIds.add(order.getId());
        }
        Map<Long, Payment> payments = paymentRepository.findByOrderIds(orderIds);

        req.setAttribute("orders", orders);
        req.setAttribute("currentStatus", status == null ? null : status.name());
        req.setAttribute("payments", payments);
        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);

        // Flash tu redirect (checkout/cancel/complete): chuyen sang request roi
        // xoa khoi session de khong hien lai o lan tai sau.
        moveFlash(req, "flashOk");
        moveFlash(req, "flashError");

        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private void moveFlash(HttpServletRequest req, String key) {
        Object value = req.getSession().getAttribute(key);
        if (value != null) {
            req.setAttribute(key, value);
            req.getSession().removeAttribute(key);
        }
    }

    private void handleStatusChange(HttpServletRequest req, HttpServletResponse resp, String action)
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

        String flashKey;
        String message;
        try {
            if ("cancel".equals(action)) {
                orderService.cancel(user.getId(), orderId);
                flashKey = "flashOk";
                message = "Da huy don DH-" + orderId + ".";
            } else {
                orderService.complete(user.getId(), orderId);
                flashKey = "flashOk";
                message = "Da xac nhan nhan hang don DH-" + orderId + ".";
            }
        } catch (ServiceException e) {
            if (e.getStatus() == HttpServletResponse.SC_FORBIDDEN) {
                resp.sendError(e.getStatus(), e.getMessage());
                return;
            }
            flashKey = "flashError";
            message = e.getMessage();
        }

        // Flash qua session de song sot qua redirect (POST-redirect-GET).
        req.getSession().setAttribute(flashKey, message);
        resp.sendRedirect(req.getContextPath() + "/order?action=history");
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("currentUser");
    }

    private OrderStatus parseStatus(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(raw.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int parsePage(String raw) {
        try {
            int page = Integer.parseInt(raw);
            return page < 1 ? 1 : page;
        } catch (NumberFormatException | NullPointerException e) {
            return 1;
        }
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
