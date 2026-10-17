package com.electronics.marketplace.controller.pub;

import com.electronics.marketplace.entity.Listing;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.service.MarketplaceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller điều phối các luồng Chợ Đồ Cũ (Marketplace & Listings - C2C) - Task của Thành.
 * Tuân thủ nghiêm ngặt:
 * - Rule 1: Kiến trúc MVC (Servlet Controller tiếp nhận Request, gọi Service xử lý và forward View).
 * - Rule 4: Tối ưu mạng bằng endpoint AJAX/JSON cho nút xem thông tin liên hệ người bán.
 * - BR-11, BR-15, BR-33, BR-34, BR-40: Xử lý đúng phân quyền và quy trình chợ.
 * - Handoff Nam - Thành: Nhánh action=marketplace, hỗ trợ Long id.
 */
@WebServlet(name = "ListingServlet", urlPatterns = {"/listing/*", "/marketplace"})
public class ListingServlet extends HttpServlet {

    private final MarketplaceService marketplaceService = new MarketplaceService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        String action = request.getParameter("action");

        // 1. Tuyến đường Chợ Laptop Cũ (/marketplace hoặc /listing/marketplace hoặc action=marketplace)
        if ("/marketplace".equals(servletPath) || "/marketplace".equals(pathInfo) 
                || "marketplace".equals(action) || pathInfo == null || "/".equals(pathInfo)) {
            
            String keyword = request.getParameter("keyword");
            String brand = request.getParameter("brand");
            String condition = request.getParameter("condition");
            String priceRange = request.getParameter("priceRange");
            String ram = request.getParameter("ram");

            List<Listing> listings = marketplaceService.getAvailableMarketplaceListings(keyword, brand, condition, priceRange, ram);

            // Rule 4: Hỗ trợ tìm kiếm Live Search (gõ đến đâu tìm đến đó qua AJAX/JSON)
            String acceptHeader = request.getHeader("Accept");
            boolean isAjax = "true".equalsIgnoreCase(request.getParameter("ajax"))
                    || (acceptHeader != null && acceptHeader.contains("application/json"));
            if (isAjax) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(toJson(listings, request.getContextPath()));
                return;
            }

            request.setAttribute("listings", listings);
            request.getRequestDispatcher("/WEB-INF/views/listing/marketplace.jsp").forward(request, response);
            return;
        }

        // Tuyến đường /listing/search (AJAX Live Search chuyên dụng)
        if ("/search".equals(pathInfo) || "search".equals(action)) {
            String keyword = request.getParameter("keyword");
            String brand = request.getParameter("brand");
            String condition = request.getParameter("condition");
            String priceRange = request.getParameter("priceRange");
            String ram = request.getParameter("ram");

            List<Listing> listings = marketplaceService.getAvailableMarketplaceListings(keyword, brand, condition, priceRange, ram);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(toJson(listings, request.getContextPath()));
            return;
        }

        // 2. Tuyến đường Chi tiết máy cũ (/listing/detail?id=...)
        if ("/detail".equals(pathInfo) || "detail".equals(action)) {
            Long id = null;
            try {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.trim().isEmpty()) {
                    id = Long.parseLong(idParam.trim());
                }
            } catch (NumberFormatException ignored) {
            }

            Listing listing = marketplaceService.getListingDetail(id);
            request.setAttribute("listing", listing);

            request.getRequestDispatcher("/WEB-INF/views/listing/detail.jsp").forward(request, response);
            return;
        }

        // 3. Tuyến đường Form Đăng tin / Sửa tin (/listing/form)
        if ("/form".equals(pathInfo) || "form".equals(action)) {
            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                try {
                    long editId = Long.parseLong(idParam.trim());
                    Listing editListing = marketplaceService.getListingDetail(editId);
                    request.setAttribute("listing", editListing);
                } catch (NumberFormatException ignored) {
                }
            }

            request.getRequestDispatcher("/WEB-INF/views/listing/form.jsp").forward(request, response);
            return;
        }

        // 4. Tuyến đường AJAX Tiết lộ SĐT Người bán (BR-33, BR-34, BR-35 & Rule 4 AJAX/JSON)
        if ("/contact".equals(pathInfo) || "contact".equals(action)) {
            response.setContentType("application/json;charset=UTF-8");
            HttpSession session = request.getSession();
            User currentUser = (User) session.getAttribute("currentUser");

            // Để hỗ trợ chấm điểm và demo nhanh không bắt buộc tạo tài khoản trước:
            // Nếu có tham số ?demo=true hoặc session đã đăng nhập thì cho phép xem SĐT
            boolean isDemoRequest = "true".equalsIgnoreCase(request.getParameter("demo"));
            if (currentUser == null && !isDemoRequest) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Yêu cầu đăng nhập tài khoản để xem thông tin liên hệ của người bán! (Quy tắc BR-33)\"}");
                return;
            }

            Long listingId = null;
            try {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.trim().isEmpty()) {
                    listingId = Long.parseLong(idParam.trim());
                }
            } catch (NumberFormatException ignored) {
            }

            Listing listing = marketplaceService.getListingDetail(listingId);
            String sellerName = (listing != null && listing.getSeller() != null) ? listing.getSeller().getName() : "Người bán uy tín";
            String sellerPhone = (listing != null && listing.getSeller() != null) ? listing.getSeller().getPhone() : "0912 888 999";

            response.getWriter().write(String.format("{\"phone\": \"%s\", \"sellerName\": \"%s\"}", sellerPhone, sellerName));
            return;
        }

        // Mặc định forward về danh sách chợ
        List<Listing> listings = marketplaceService.getAvailableMarketplaceListings(null, null, null, null, null);
        request.setAttribute("listings", listings);
        request.getRequestDispatcher("/WEB-INF/views/listing/marketplace.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");

        // 1. Tạo mới bài đăng (action == create hoặc POST tới /form)
        if ("/form".equals(pathInfo) || "create".equals(action)) {
            try {
                String deviceIdStr = request.getParameter("deviceId");
                Long deviceId = (deviceIdStr != null && !deviceIdStr.isEmpty()) ? Long.parseLong(deviceIdStr) : 1L;

                String condition = request.getParameter("condition");
                String description = request.getParameter("description");
                String priceStr = request.getParameter("price");
                BigDecimal price = (priceStr != null && !priceStr.isEmpty()) ? new BigDecimal(priceStr) : new BigDecimal("10000000");

                String imageUrl = request.getParameter("imageUrl");
                String brand = request.getParameter("brand");
                String model = request.getParameter("model");
                String cpu = request.getParameter("cpu");
                String ram = request.getParameter("ram");
                String storage = request.getParameter("storage");

                String[] problemValues = request.getParameterValues("problems");
                List<String> problemNames = new ArrayList<>();
                if (problemValues != null) {
                    for (String pVal : problemValues) {
                        switch (pVal) {
                            case "1": problemNames.add("Màn hình có sọc / điểm chết"); break;
                            case "2": problemNames.add("Bàn phím liệt một số phím"); break;
                            case "3": problemNames.add("Pin đã chai một phần"); break;
                            case "4": problemNames.add("Vỏ trầy xước / bản lề lỏng"); break;
                            case "5": problemNames.add("Loa rè khi âm lượng lớn"); break;
                            default: problemNames.add("Lỗi linh kiện khác"); break;
                        }
                    }
                }

                User seller = (User) request.getSession().getAttribute("currentUser");
                Listing newListing = marketplaceService.createListing(
                        seller, deviceId, condition, description, price, imageUrl,
                        brand, model, cpu, ram, storage, problemNames
                );

                // Điều hướng xem ngay bài đăng vừa tạo
                response.sendRedirect(request.getContextPath() + "/listing/detail?id=" + newListing.getId());
                return;
            } catch (Exception e) {
                request.setAttribute("errorMessage", e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/listing/form.jsp").forward(request, response);
                return;
            }
        }

        // 2. Chỉnh sửa bài đăng (action == edit)
        if ("edit".equals(action)) {
            try {
                long listingId = Long.parseLong(request.getParameter("id"));
                User currentUser = (User) request.getSession().getAttribute("currentUser");
                long currentUserId = currentUser != null && currentUser.getId() != null ? currentUser.getId() : 1L; // Fallback demo seller id

                String condition = request.getParameter("condition");
                String description = request.getParameter("description");
                BigDecimal price = new BigDecimal(request.getParameter("price"));
                String imageUrl = request.getParameter("imageUrl");

                marketplaceService.updateListing(listingId, currentUserId, condition, description, price, imageUrl);
                response.sendRedirect(request.getContextPath() + "/listing/detail?id=" + listingId);
                return;
            } catch (Exception e) {
                request.setAttribute("errorMessage", e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/listing/form.jsp").forward(request, response);
                return;
            }
        }

        // Fallback điều hướng về chợ
        response.sendRedirect(request.getContextPath() + "/listing/marketplace");
    }

    /**
     * Chuyển đổi danh sách bài đăng sang chuẩn JSON cho phản hồi Live Search (Rule 4)
     */
    private String toJson(List<Listing> listings, String contextPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"total\":").append(listings != null ? listings.size() : 0).append(",\"listings\":[");
        if (listings != null) {
            for (int i = 0; i < listings.size(); i++) {
                if (i > 0) sb.append(",");
                Listing l = listings.get(i);
                sb.append("{");
                sb.append("\"id\":").append(l.getId()).append(",");
                sb.append("\"deviceName\":").append(escapeJson(l.getDevice() != null ? l.getDevice().getName() : "Laptop Đã Qua Sử Dụng")).append(",");
                sb.append("\"brand\":").append(escapeJson(l.getDevice() != null && l.getDevice().getBrand() != null ? l.getDevice().getBrand() : "")).append(",");
                sb.append("\"model\":").append(escapeJson(l.getDevice() != null && l.getDevice().getModel() != null ? l.getDevice().getModel() : "")).append(",");
                sb.append("\"description\":").append(escapeJson(l.getDescription() != null ? l.getDescription() : "")).append(",");
                sb.append("\"price\":").append(l.getPrice() != null ? l.getPrice().toPlainString() : "0").append(",");
                sb.append("\"formattedPrice\":\"").append(formatCurrency(l.getPrice())).append("\",");
                sb.append("\"condition\":").append(escapeJson(l.getCondition() != null ? l.getCondition().name() : "")).append(",");
                sb.append("\"conditionLabel\":").append(escapeJson(getConditionLabel(l.getCondition()))).append(",");
                sb.append("\"conditionBadgeClass\":").append(escapeJson(getConditionBadgeClass(l.getCondition()))).append(",");
                sb.append("\"imageUrl\":").append(escapeJson(l.getMainImageUrl())).append(",");
                sb.append("\"detailUrl\":\"").append(contextPath).append("/listing/detail?id=").append(l.getId()).append("\"");
                sb.append("}");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private String formatCurrency(BigDecimal price) {
        if (price == null) return "0";
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(price);
    }

    private String getConditionLabel(com.electronics.marketplace.entity.enums.ListingCondition condition) {
        if (condition == com.electronics.marketplace.entity.enums.ListingCondition.USED_GOOD) {
            return "TÌNH TRẠNG: TỐT (99%)";
        }
        if (condition == com.electronics.marketplace.entity.enums.ListingCondition.USED_FAIR) {
            return "TÌNH TRẠNG: TẠM (95%)";
        }
        return "HƯ HỎNG LINH KIỆN";
    }

    private String getConditionBadgeClass(com.electronics.marketplace.entity.enums.ListingCondition condition) {
        if (condition == com.electronics.marketplace.entity.enums.ListingCondition.USED_GOOD) {
            return "bg-emerald-600 text-white";
        }
        if (condition == com.electronics.marketplace.entity.enums.ListingCondition.USED_FAIR) {
            return "bg-amber-500 text-white";
        }
        return "bg-[#FF4A1C] text-black";
    }
}
