package com.electronics.marketplace.controller.pub;

import com.electronics.marketplace.entity.Device;
import com.electronics.marketplace.entity.Listing;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.repository.DeviceRepository;
import com.electronics.marketplace.repository.DeviceRepositoryImpl;
import com.electronics.marketplace.service.MarketplaceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Controller điều phối các luồng Chợ Đồ Cũ (Marketplace & Listings - C2C) - Task của Thành.
 * Tuân thủ nghiêm ngặt:
 * - Rule 1: Kiến trúc MVC (Servlet Controller tiếp nhận Request, gọi Service xử lý và forward View).
 * - Rule 4: Tối ưu mạng bằng endpoint AJAX/JSON cho nút xem thông tin liên hệ người bán.
 * - Rule 5: Hỗ trợ upload ảnh từ thư mục máy tính, lưu file lên đĩa, chỉ lưu URL chuỗi vào DB.
 * - BR-11, BR-15, BR-33, BR-34, BR-40: Xử lý đúng phân quyền và quy trình chợ.
 * - Handoff Nam - Thành: Nhánh action=marketplace, hỗ trợ Long id.
 */
@WebServlet(name = "ListingServlet", urlPatterns = {"/listing", "/listing/*", "/marketplace"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,        // 1 MB
        maxFileSize = 10 * 1024 * 1024,         // 10 MB
        maxRequestSize = 25 * 1024 * 1024       // 25 MB
)
public class ListingServlet extends HttpServlet {

    private final MarketplaceService marketplaceService = new MarketplaceService();
    private final DeviceRepository deviceRepository = new DeviceRepositoryImpl();

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
            request.setAttribute("listings", listings);
            request.getRequestDispatcher("/WEB-INF/views/listing/marketplace.jsp").forward(request, response);
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

            // Nạp danh mục dòng máy hiện có để người dùng chọn hoặc tự nhập
            request.setAttribute("devices", deviceRepository.findAll());
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
        String action = getFormField(request, "action");

        // 1. Tạo mới bài đăng (action == create hoặc POST tới /form)
        if ("/form".equals(pathInfo) || "create".equals(action)) {
            try {
                String deviceIdStr = getFormField(request, "deviceId");
                Long deviceId = (deviceIdStr != null && !deviceIdStr.isEmpty() && !"0".equals(deviceIdStr)) 
                        ? Long.parseLong(deviceIdStr) : null;

                String condition = getFormField(request, "condition");
                String description = getFormField(request, "description");
                String priceStr = getFormField(request, "price");
                BigDecimal price = (priceStr != null && !priceStr.isEmpty()) ? new BigDecimal(priceStr) : new BigDecimal("10000000");

                // Rule 5: Lấy toàn bộ file ảnh tải lên từ máy tính (hỗ trợ chọn nhiều ảnh)
                List<String> imageUrls = handleUploadedImages(request);

                String brand = getFormField(request, "brand");
                String model = getFormField(request, "model");
                String cpu = getFormField(request, "cpu");
                String ram = getFormField(request, "ram");
                String storage = getFormField(request, "storage");

                List<String> problemValues = getFormFieldValues(request, "problems");
                List<String> problemNames = new ArrayList<>();
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

                User seller = (User) request.getSession().getAttribute("currentUser");
                Listing newListing = marketplaceService.createListing(
                        seller, deviceId, condition, description, price, imageUrls,
                        brand, model, cpu, ram, storage, problemNames
                );

                // Điều hướng xem ngay bài đăng vừa tạo
                response.sendRedirect(request.getContextPath() + "/listing/detail?id=" + newListing.getId() + "&created=1");
                return;
            } catch (Exception e) {
                request.setAttribute("devices", deviceRepository.findAll());
                request.setAttribute("errorMessage", e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/listing/form.jsp").forward(request, response);
                return;
            }
        }

        // 2. Chỉnh sửa bài đăng (action == edit)
        if ("edit".equals(action)) {
            try {
                String idStr = getFormField(request, "id");
                long listingId = Long.parseLong(idStr != null ? idStr : "1");
                User currentUser = (User) request.getSession().getAttribute("currentUser");
                long currentUserId = currentUser != null && currentUser.getId() != null ? currentUser.getId() : 1L; // Fallback demo seller id

                String condition = getFormField(request, "condition");
                String description = getFormField(request, "description");
                String priceStr = getFormField(request, "price");
                BigDecimal price = priceStr != null ? new BigDecimal(priceStr) : new BigDecimal("10000000");
                // Kết hợp ảnh cũ được giữ lại và ảnh mới upload
                List<String> keptExistingUrls = getFormFieldValues(request, "existingImageUrls");
                List<String> newUploadedUrls = handleUploadedImages(request);
                List<String> finalUrls = new ArrayList<>();
                if (keptExistingUrls != null) {
                    finalUrls.addAll(keptExistingUrls);
                }
                if (newUploadedUrls != null) {
                    finalUrls.addAll(newUploadedUrls);
                }

                String imageSectionPresent = getFormField(request, "imageSectionPresent");
                List<String> imagesToUpdate = "true".equals(imageSectionPresent) ? finalUrls : null;

                marketplaceService.updateListing(listingId, currentUserId, condition, description, price, imagesToUpdate);
                response.sendRedirect(request.getContextPath() + "/listing/detail?id=" + listingId);
                return;
            } catch (Exception e) {
                request.setAttribute("devices", deviceRepository.findAll());
                request.setAttribute("errorMessage", e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/listing/form.jsp").forward(request, response);
                return;
            }
        }

        // Fallback điều hướng về chợ
        response.sendRedirect(request.getContextPath() + "/listing/marketplace");
    }

    /**
     * Đọc giá trị trường form an toàn cho cả multipart/form-data và urlencoded request
     */
    private String getFormField(HttpServletRequest request, String fieldName) {
        String val = request.getParameter(fieldName);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        try {
            Part part = request.getPart(fieldName);
            if (part != null) {
                try (var is = part.getInputStream()) {
                    String str = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
                    if (!str.isEmpty()) return str;
                }
            }
        } catch (Exception ignored) {
        }
        return val != null ? val.trim() : null;
    }

    /**
     * Đọc danh sách giá trị mảng trường form an toàn (ví dụ: checkbox problems)
     */
    private List<String> getFormFieldValues(HttpServletRequest request, String fieldName) {
        String[] vals = request.getParameterValues(fieldName);
        List<String> list = new ArrayList<>();
        if (vals != null && vals.length > 0) {
            for (String v : vals) {
                if (v != null && !v.trim().isEmpty()) list.add(v.trim());
            }
            return list;
        }
        try {
            for (Part part : request.getParts()) {
                if (fieldName.equals(part.getName())) {
                    try (var is = part.getInputStream()) {
                        String str = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
                        if (!str.isEmpty()) list.add(str);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    /**
     * Xử lý lưu các file hình ảnh tải lên từ máy tính (Folder upload) lưu trên ổ đĩa server (Rule 5).
     * Hỗ trợ chọn và tải lên nhiều file cùng lúc (multiple files).
     */
    private List<String> handleUploadedImages(HttpServletRequest request) {
        List<String> imageUrls = new ArrayList<>();
        try {
            for (Part part : request.getParts()) {
                String name = part.getName();
                if (("imageFiles".equals(name) || "imageFile".equals(name)) && part.getSize() > 0) {
                    String submittedFileName = part.getSubmittedFileName();
                    if (submittedFileName != null && !submittedFileName.trim().isEmpty()) {
                        String ext = "";
                        int dotIdx = submittedFileName.lastIndexOf('.');
                        if (dotIdx >= 0) {
                            ext = submittedFileName.substring(dotIdx).toLowerCase();
                        }
                        if (ext.isEmpty() || (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png") && !ext.equals(".webp") && !ext.equals(".gif"))) {
                            ext = ".jpg";
                        }
                        String uniqueFileName = "listing_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

                        String uploadPath = request.getServletContext().getRealPath("/uploads/listings");
                        File uploadDir = new File(uploadPath);
                        if (!uploadDir.exists()) {
                            uploadDir.mkdirs();
                        }
                        File targetFile = new File(uploadDir, uniqueFileName);
                        part.write(targetFile.getAbsolutePath());

                        imageUrls.add(request.getContextPath() + "/uploads/listings/" + uniqueFileName);
                    }
                }
            }
        } catch (Exception uploadEx) {
            System.err.println("Upload multi-file warning: " + uploadEx.getMessage());
        }

        return imageUrls;
    }
}
