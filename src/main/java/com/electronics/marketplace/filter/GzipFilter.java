package com.electronics.marketplace.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

/**
 * Nén GZIP cho CSS, JS và JSON (AJAX).
 * Không áp dụng cho trang HTML được tạo bằng RequestDispatcher.forward(): Tomcat 11 chốt
 * phản hồi thật ngay sau forward nên bộ lọc không còn chỗ để ghi dữ liệu đã nén.
 */
@WebFilter("/*")
public class GzipFilter implements Filter {

    private static final int MIN_SIZE = 100;   // nhỏ hơn thì không cần nén

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (!shouldCompress(req)) {
            chain.doFilter(request, response);
            return;
        }
        BufferedResponse wrapper = new BufferedResponse(resp);
        chain.doFilter(request, wrapper);
        wrapper.finish(resp);
    }

    private boolean shouldCompress(HttpServletRequest req) {
        String accept = req.getHeader("Accept-Encoding");
        if (accept == null || !accept.contains("gzip")) return false;

        String uri = req.getRequestURI().toLowerCase();
        if (uri.endsWith(".css") || uri.endsWith(".js")) return true;     // file tĩnh

        String want = req.getHeader("Accept");                            // AJAX/JSON
        return want != null && want.contains("application/json");
    }

    /** Gom nội dung vào bộ nhớ, nén một lần ở cuối. */
    private static class BufferedResponse extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private ServletOutputStream stream;
        private PrintWriter writer;

        BufferedResponse(HttpServletResponse resp) { super(resp); }

        @Override
        public ServletOutputStream getOutputStream() {
            if (writer != null) throw new IllegalStateException("getWriter() da duoc goi");
            if (stream == null) {
                stream = new ServletOutputStream() {
                    @Override public void write(int b) { buffer.write(b); }
                    @Override public void write(byte[] b, int off, int len) { buffer.write(b, off, len); }
                    @Override public boolean isReady() { return true; }
                    @Override public void setWriteListener(WriteListener listener) { }
                };
            }
            return stream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (stream != null) throw new IllegalStateException("getOutputStream() da duoc goi");
            if (writer == null) {
                String enc = getCharacterEncoding();
                if (enc == null) enc = "UTF-8";
                writer = new PrintWriter(new OutputStreamWriter(buffer, enc));
            }
            return writer;
        }

        @Override public void flushBuffer() { }
        @Override public void resetBuffer() { if (writer != null) writer.flush(); buffer.reset(); }
        @Override public void setContentLength(int len) { }
        @Override public void setContentLengthLong(long len) { }
        @Override public void setHeader(String name, String value) {
            if (!"Content-Length".equalsIgnoreCase(name)) super.setHeader(name, value);
        }
        @Override public void addHeader(String name, String value) {
            if (!"Content-Length".equalsIgnoreCase(name)) super.addHeader(name, value);
        }

        private boolean compressible() {
            String ct = getContentType();
            if (ct == null) return false;
            ct = ct.toLowerCase();
            return ct.startsWith("text/") || ct.startsWith("application/json")
                || ct.contains("javascript");
        }

        void finish(HttpServletResponse real) throws IOException {
            if (writer != null) writer.flush();
            byte[] data = buffer.toByteArray();
            if (real.isCommitted() || data.length == 0) return;   // redirect/sendError hoặc rỗng

            if (compressible() && data.length >= MIN_SIZE) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (GZIPOutputStream gz = new GZIPOutputStream(bos)) {
                    gz.write(data);
                }
                byte[] zipped = bos.toByteArray();
                real.setHeader("Content-Encoding", "gzip");
                real.addHeader("Vary", "Accept-Encoding");
                real.setContentLength(zipped.length);
                real.getOutputStream().write(zipped);
            } else {
                real.setContentLength(data.length);
                real.getOutputStream().write(data);
            }
        }
    }
}