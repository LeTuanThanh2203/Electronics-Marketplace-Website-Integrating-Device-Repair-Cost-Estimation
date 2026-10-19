package com.electronics.marketplace.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // history la action duy nhat dung GET; thieu action cung coi nhu history
        // de link ${ctx}/order van vao duoc.
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("cancel".equals(action) || "complete".equals(action)) {
            // ponytail: Buoc 3 - cancel() 1 transaction 2 bang (Order -> CANCELLED,
            // Payment PENDING -> FAILED), complete() chan neu khong dong thoi
            // CONFIRMED + PAID. Ca hai phai kiem tra quyen so huu (IDOR): don
            // khong thuoc currentUser thi tra 403, khong duoc sua.
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }
        // POST la do action ngoai du lieu (F5 sau khi submit) - dua ve GET.
        resp.sendRedirect(req.getContextPath() + "/order?action=history");
    }
}
