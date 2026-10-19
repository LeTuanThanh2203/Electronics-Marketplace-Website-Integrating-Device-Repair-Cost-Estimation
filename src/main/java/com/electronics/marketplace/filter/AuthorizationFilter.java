package com.electronics.marketplace.filter;

import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.util.HttpUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Chỉ ADMIN mới được vào /admin. */
@WebFilter(urlPatterns = {"/admin", "/admin/*"})
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession s = req.getSession(false);
        User u = (s == null) ? null : (User) s.getAttribute(AuthenticationFilter.SESSION_KEY);

        if (u != null && !u.isAdmin()) {
            if (HttpUtil.wantsJson(req)) {
                HttpUtil.writeJson(resp, HttpServletResponse.SC_FORBIDDEN,
                    "{\"ok\":false,\"code\":\"FORBIDDEN\",\"message\":\"Bạn không có quyền thực hiện thao tác này.\"}");
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            }
            return;
        }
        chain.doFilter(request, response);
    }
}