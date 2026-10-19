package com.electronics.marketplace.controller.user;

import com.electronics.marketplace.config.MomoConfig;
import com.electronics.marketplace.entity.Payment;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.PaymentMethod;
import com.electronics.marketplace.entity.enums.PaymentStatus;
import com.electronics.marketplace.entity.enums.Role;
import com.electronics.marketplace.repository.PaymentRepository;
import com.electronics.marketplace.service.MomoGateway;
import com.electronics.marketplace.service.OrderService;
import com.electronics.marketplace.service.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * FR-PAY-01/02 - man hinh thanh toan kenh NEW_DEVICE (SDD 13.8).
 *
 * Action (mac dinh view):
 *   view    - GET  hien trang thai Payment
 *   create  - GET  tao giao dich MoMo roi redirect sang payUrl
 *   return  - GET  MoMo redirect nguoi dung ve; doi chieu bang query API
 *   ipn     - POST MoMo goi server-to-server, tra JSON 200
 *
 * BR-44 (MVP xac nhan thu cong) da duoc thay cho MOMO: cong thanh toan tu ghi
 * PAID. COD van PENDING cho Admin xac nhan thu cong.
 */
@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW = "/WEB-INF/views/order/payment.jsp";

    /** Hau to ngau nhien trong orderId gui MoMo (xem momoOrderId). */
    private static final int MOMO_SUFFIX_LENGTH = 8;

    private final PaymentRepository paymentRepository = new PaymentRepository();
    private final OrderService orderService = new OrderService();
    private final MomoGateway momoGateway = new MomoGateway();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = action(req);
        if ("create".equals(action)) {
            create(req, resp);
        } else if ("return".equals(action)) {
            handleReturn(req, resp);
        } else {
            view(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("ipn".equals(action(req))) {
            ipn(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    // ------------------------------------------------------------------ view

    private void view(HttpServletRequest req, HttpServletResponse resp)
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
        if (!canView(user, payment)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        req.setAttribute("payment", payment);
        // Flash dat o create/handleReturn (session) chi hien mot lan.
        Object flash = req.getSession().getAttribute("flashError");
        if (flash != null) {
            req.setAttribute("flashError", flash);
            req.getSession().removeAttribute("flashError");
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    // ---------------------------------------------------------------- create

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
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
        if (!user.getId().equals(payment.getOrder().getBuyer().getId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (payment.getMethod() != PaymentMethod.MOMO
                || payment.getStatus() != PaymentStatus.PENDING) {
            redirectToView(req, resp, orderId);
            return;
        }

        try {
            MomoGateway.CreateResult result = momoGateway.create(
                    momoOrderId(payment),
                    toVnd(payment),
                    "Thanh toan don hang DH-" + orderId,
                    baseUrl(req) + "/payment?action=return",
                    baseUrl(req) + "/payment?action=ipn",
                    "");
            if (result.resultCode == 0 && result.payUrl != null) {
                resp.sendRedirect(result.payUrl);
                return;
            }
            req.getSession().setAttribute("flashError",
                    "MoMo tu choi giao dich (" + result.resultCode + "): " + result.message);
        } catch (IOException e) {
            req.getSession().setAttribute("flashError",
                    "Khong ket noi duoc MoMo. Kiem tra ket noi mang roi thu lai.");
        } catch (IllegalStateException e) {
            // Thieu bien moi truong MOMO_* - cau hinh sai, khong phai loi nguoi dung.
            req.getSession().setAttribute("flashError",
                    "Cong thanh toan chua duoc cau hinh. Vui long chon COD hoac lien he ho tro.");
        }
        redirectToView(req, resp, orderId);
    }

    // ---------------------------------------------------------------- return

    /**
     * MoMo redirect nguoi dung ve day kem ket qua. Khong tin tham so tren URL:
     * goi lai query API de lay ket qua that (URL co the bi sua tay).
     * Dong thoi day cung la duong doi soat khi IPN khong toi duoc localhost.
     */
    private void handleReturn(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Payment payment = findByMomoOrderId(req.getParameter("orderId"));
        if (payment == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            MomoGateway.QueryResult result = momoGateway.query(req.getParameter("orderId").trim());
            if (result.resultCode == 0) {
                orderService.markPaidByGateway(payment.getId(), result.amount);
            }
        } catch (ServiceException e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        } catch (IOException e) {
            req.getSession().setAttribute("flashError",
                    "Chua xac nhan duoc voi MoMo. Trang thai se tu cap nhat khi MoMo gui thong bao.");
        }
        redirectToView(req, resp, payment.getOrder().getId());
    }

    // ------------------------------------------------------------------- ipn

    private void ipn(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((key, values) ->
                params.put(key, values.length == 0 ? "" : values[0]));

        if (!MomoGateway.verifyIpn(params)) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, 400, "Invalid signature");
            return;
        }

        Payment payment = findByMomoOrderId(params.get("orderId"));
        if (payment == null) {
            json(resp, HttpServletResponse.SC_OK, 0, "Payment not found");
            return;
        }

        if ("0".equals(params.get("resultCode"))) {
            try {
                orderService.markPaidByGateway(payment.getId(), parseLong(params.get("amount")));
            } catch (ServiceException e) {
                // Da PAID la truong hop binh thuong (IPN trung lap): van tra 200.
                if (payment.getStatus() != PaymentStatus.PAID) {
                    json(resp, HttpServletResponse.SC_OK, 1, e.getMessage());
                    return;
                }
            }
        }
        json(resp, HttpServletResponse.SC_OK, 0, "ok");
    }

    // --------------------------------------------------------------- helpers

    /**
     * MoMo tu choi orderId da dung (resultCode 41) ke ca khi requestId trung,
     * nen moi lan bam "Thanh toan" phai co orderId moi. orderId = transactionCode
     * + hau to ngau nhien; doc nguoc lai bang cach cat hau to.
     */
    private String momoOrderId(Payment payment) {
        return payment.getTransactionCode() + "-"
                + UUID.randomUUID().toString().replace("-", "")
                .substring(0, MOMO_SUFFIX_LENGTH);
    }

    private Payment findByMomoOrderId(String momoOrderId) {
        if (momoOrderId == null) {
            return null;
        }
        String value = momoOrderId.trim();
        int dash = value.lastIndexOf('-');
        // Chi cat hau to 8 hex; khong khop thi coi nhu transactionCode nguyen ven.
        boolean hasSuffix = dash > 0
                && value.length() - dash - 1 == MOMO_SUFFIX_LENGTH
                && value.substring(dash + 1).matches("[0-9a-f]{8}");
        String transactionCode = hasSuffix ? value.substring(0, dash) : value;
        return paymentRepository.findByTransactionCode(transactionCode);
    }

    private boolean canView(User user, Payment payment) {
        return user.getId().equals(payment.getOrder().getBuyer().getId())
                || user.getRole() == Role.ADMIN;
    }

    private void redirectToView(HttpServletRequest req, HttpServletResponse resp, Long orderId)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + "/payment?orderId=" + orderId);
    }

    /** MoMo nhan so tien VND nguyen, khong co phan thap phan. */
    private long toVnd(Payment payment) {
        return payment.getAmount().setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /** MOMO_PUBLIC_BASE_URL khi chay sau tunnel/deploy, nguoc lai lay tu request. */
    private String baseUrl(HttpServletRequest req) {
        String configured = MomoConfig.get().getPublicBaseUrl();
        if (configured != null) {
            return configured;
        }
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath();
    }

    private void json(HttpServletResponse resp, int status, int resultCode, String message)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("{\"resultCode\":" + resultCode + ",\"message\":\"" + message + "\"}");
        out.flush();
    }

    private static String action(HttpServletRequest req) {
        String action = req.getParameter("action");
        return action == null || action.trim().isEmpty() ? "view" : action.trim();
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

    private long parseLong(String raw) {
        try {
            return raw == null ? 0L : Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
