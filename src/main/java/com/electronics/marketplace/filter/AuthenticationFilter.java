package com.electronics.marketplace.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filter xác thực đăng nhập người dùng.
 * Thống nhất SESSION_KEY = "currentUser" trên toàn dự án.
 */
@WebFilter(urlPatterns = {"/profile", "/auth/profile"})
public class AuthenticationFilter implements Filter {

    /** Tên attribute session chứa User đang đăng nhập (dùng thống nhất toàn dự án). */
    public static final String SESSION_KEY = "currentUser";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession s = req.getSession(false);
        boolean isLoggedIn = (s != null && s.getAttribute(SESSION_KEY) != null);

        if (isLoggedIn) {
            chain.doFilter(request, response);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/login");
    }

    @Override
    public void destroy() {
    }
}
