package com.electronics.marketplace.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import com.electronics.marketplace.entity.User;

/** Chỉ ADMIN mới được vào /admin. */
@WebFilter(urlPatterns = {"/admin", "/admin/*"})
public class AuthorizationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession s = req.getSession(false);
        User u = (s == null) ? null : (User) s.getAttribute("currentUser");
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
        } else if (!u.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
        } else {
            chain.doFilter(request, response);
        }
    }
}