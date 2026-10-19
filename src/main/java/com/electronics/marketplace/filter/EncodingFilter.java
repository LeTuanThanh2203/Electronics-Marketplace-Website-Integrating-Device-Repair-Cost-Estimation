package com.electronics.marketplace.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Dam bao moi request/response deu su dung UTF-8 de tranh loi tieng Viet.
 */
@WebFilter("/*")
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding("UTF-8");
        }
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}

