package com.electronics.marketplace.filter;

import com.electronics.marketplace.util.HttpUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Chưa đăng nhập thì không vào được các URI cần tài khoản. */
@WebFilter(urlPatterns = {"/profile", "/admin", "/admin/*", "/checkout", "/order", "/payment", "/cart", "/listing"})
public class AuthenticationFilter implements Filter {

    /** Tên attribute session chứa User đang đăng nhập (dùng thống nhất toàn dự án). */
    public static final String SESSION_KEY = "currentUser";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession s = req.getSession(false);
        boolean isLoggedIn = (s != null && s.getAttribute(SESSION_KEY) != null);

        // 1. Đã đăng nhập -> Cho qua mọi trang
        if (isLoggedIn) {
            chain.doFilter(request, response);
            return;
        }

        String path = req.getServletPath();
        String action = req.getParameter("action");

        // 2. GET /cart không có action = xem giỏ hàng -> Cho phép khách chưa đăng nhập
        if ("/cart".equals(path) && action == null) {
            chain.doFilter(request, response);
            return;
        }

        // 3. /listing: Khách xem chợ, xem chi tiết tin -> Cho qua; Chỉ chặn khi ĐĂNG TIN hoặc QUẢN LÝ TIN
        if ("/listing".equals(path)) {
            boolean isProtected = "create".equals(action) || "save".equals(action) 
                               || "my-listings".equals(action) || "delete".equals(action);
            if (!isProtected) {
                chain.doFilter(request, response);
                return;
            }
        }

        // 4. AJAX phải nhận JSON 401, không nhận 302
        if (HttpUtil.wantsJson(req)) {
            HttpUtil.writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                "{\"ok\":false,\"code\":\"UNAUTHENTICATED\",\"message\":\"Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.\"}");
            return;
        }

        // 5. Chuyển hướng về trang đăng nhập
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}