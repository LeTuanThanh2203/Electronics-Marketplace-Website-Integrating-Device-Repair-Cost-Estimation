package com.electronics.marketplace.filter;

import com.electronics.marketplace.util.HttpUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Chưa đăng nhập thì không vào được các URI cần tài khoản. */
@WebFilter(urlPatterns = {"/profile", "/admin", "/admin/*", "/checkout", "/order", "/payment", "/cart"})
public class AuthenticationFilter implements Filter {

    /** Tên attribute session chứa User đang đăng nhập (dùng thống nhất toàn dự án). */
    public static final String SESSION_KEY = "currentUser";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute(SESSION_KEY) != null) {
            chain.doFilter(request, response);
            return;
        }

        // GET /cart không có action = xem giỏ hàng, cho phép khách chưa đăng nhập
        if ("/cart".equals(req.getServletPath()) && req.getParameter("action") == null) {
            chain.doFilter(request, response);
            return;
        }

        // AJAX phải nhận JSON 401, không nhận 302 (nếu không fetch sẽ nhận HTML trang login)
        if (HttpUtil.wantsJson(req)) {
            HttpUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                "{\"ok\":false,\"code\":\"UNAUTHENTICATED\",\"message\":\"Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.\"}");
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}