package com.electronics.marketplace.controller.user;

import com.electronics.marketplace.dto.EstimateResultDTO;
import com.electronics.marketplace.entity.Device;
import com.electronics.marketplace.entity.RepairProblem;
import com.electronics.marketplace.entity.RepairRequest;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.filter.AuthenticationFilter;
import com.electronics.marketplace.service.RepairService;
import com.google.gson.Gson;
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
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "RepairRequestServlet", urlPatterns = {
        "/repair",
        "/repair/request",
        "/repair/estimate",
        "/repair/api/estimate",
        "/repair/track"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1, // 1MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 15    // 15MB
)
public class RepairRequestServlet extends HttpServlet {

    private final RepairService repairService = new RepairService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        String action = req.getParameter("action");

        // 1. AJAX Endpoint: Trả về JSON tính toán chi phí dự kiến tức thì
        if ("/repair/api/estimate".equals(path) || "estimate".equalsIgnoreCase(action)) {
            handleEstimateApi(req, resp);
            return;
        }

        // 2. Tra cứu chi tiết yêu cầu & báo giá dự kiến
        if ("/repair/track".equals(path) || "track".equalsIgnoreCase(action)) {
            handleTrackRequest(req, resp);
            return;
        }

        // 3. Mặc định: Hiển thị form tạo yêu cầu sửa chữa
        handleShowRequestForm(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getServletPath();
        String action = req.getParameter("action");

        if ("/repair/request".equals(path) || "create".equalsIgnoreCase(action) || path.equals("/repair")) {
            handleCreateRepairRequest(req, resp);
        } else {
            doGet(req, resp);
        }
    }

    /**
     * Tải danh sách thiết bị và lỗi để chuẩn bị dữ liệu cho form yêu cầu
     */
    private void handleShowRequestForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Device> devices = repairService.getAllDevices();
        List<RepairProblem> problems = repairService.getAllProblems(true); // Chỉ lấy các lỗi đang active

        req.setAttribute("devices", devices);
        req.setAttribute("problems", problems);

        // Chuyển tiếp đến JSP (View tier)
        req.getRequestDispatcher("/WEB-INF/views/repair/request_form.jsp").forward(req, resp);
    }

    /**
     * Xử lý AJAX request để tính chi phí ước lượng nhanh
     */
    private void handleEstimateApi(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        try {
            String deviceIdStr = req.getParameter("deviceId");
            String problemIdStr = req.getParameter("problemId");

            if (deviceIdStr == null || problemIdStr == null || deviceIdStr.trim().isEmpty() || problemIdStr.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson(EstimateResultDTO.notFound("Thiếu tham số deviceId hoặc problemId.")));
                return;
            }

            Long deviceId = Long.parseLong(deviceIdStr.trim());
            Long problemId = Long.parseLong(problemIdStr.trim());

            EstimateResultDTO estimate = repairService.estimateCost(deviceId, problemId);
            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(estimate));
            out.flush();
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(EstimateResultDTO.notFound("Định dạng ID thiết bị hoặc lỗi không hợp lệ.")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(EstimateResultDTO.notFound("Lỗi máy chủ khi tính toán báo giá: " + e.getMessage())));
        }
    }

    /**
     * Tiếp nhận submit form, tạo RepairRequest và tự động sinh RepairEstimate
     */
    private void handleCreateRepairRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String deviceIdStr = req.getParameter("deviceId");
            String problemIdStr = req.getParameter("problemId");
            String customerName = req.getParameter("customerName");
            String customerPhone = req.getParameter("customerPhone");
            String customerEmail = req.getParameter("customerEmail");
            String description = req.getParameter("description");
            String imageUrl = req.getParameter("imageUrl");

            // Xử lý tệp ảnh tải lên từ máy tính (nếu có)
            try {
                Part filePart = req.getPart("imageFile");
                if (filePart != null && filePart.getSize() > 0) {
                    String submittedName = filePart.getSubmittedFileName();
                    if (submittedName != null && !submittedName.trim().isEmpty()) {
                        String cleanName = Paths.get(submittedName).getFileName().toString();
                        String ext = "";
                        int dot = cleanName.lastIndexOf('.');
                        if (dot >= 0) {
                            ext = cleanName.substring(dot).toLowerCase();
                        }
                        if (!Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(ext)) {
                            req.setAttribute("errorMessage", "Định dạng ảnh không được hỗ trợ. Vui lòng chọn tệp PNG, JPG, JPEG hoặc WEBP.");
                            handleShowRequestForm(req, resp);
                            return;
                        }

                        String uploadDirPath = req.getServletContext().getRealPath("/uploads/repairs");
                        File uploadDir;
                        if (uploadDirPath != null) {
                            uploadDir = new File(uploadDirPath);
                        } else {
                            String catalinaBase = System.getProperty("catalina.base", System.getProperty("user.dir"));
                            uploadDir = new File(catalinaBase, "webapps" + req.getContextPath() + "/uploads/repairs");
                        }
                        if (!uploadDir.exists()) {
                            uploadDir.mkdirs();
                        }

                        String savedFileName = "repair_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
                        File targetFile = new File(uploadDir, savedFileName);
                        filePart.write(targetFile.getAbsolutePath());

                        String ctx = req.getContextPath();
                        imageUrl = (ctx != null ? ctx : "") + "/uploads/repairs/" + savedFileName;
                    }
                }
            } catch (Exception fileEx) {
                req.getServletContext().log("Lỗi khi lưu ảnh mô tả lỗi: " + fileEx.getMessage(), fileEx);
            }

            if (deviceIdStr == null || problemIdStr == null || deviceIdStr.trim().isEmpty() || problemIdStr.trim().isEmpty()) {
                req.setAttribute("errorMessage", "Vui lòng chọn thiết bị và loại lỗi.");
                handleShowRequestForm(req, resp);
                return;
            }

            Long deviceId = Long.parseLong(deviceIdStr.trim());
            Long problemId = Long.parseLong(problemIdStr.trim());

            // Lấy userId đăng nhập nếu có. Module Auth (Tân) lưu nguyên đối tượng User
            // vào session dưới key AuthenticationFilter.SESSION_KEY ("currentUser").
            Long currentUserId = currentUserId(req);

            // Gọi Service tạo RepairRequest và tự động tính toán RepairEstimate
            RepairRequest createdRequest = repairService.createRepairRequest(
                    currentUserId,
                    deviceId,
                    problemId,
                    customerName,
                    customerPhone,
                    customerEmail,
                    description,
                    imageUrl
            );

            // Đặt thuộc tính sang trang hiển thị kết quả báo giá chi tiết (repair/estimate.jsp)
            req.setAttribute("repairRequest", createdRequest);
            req.setAttribute("repairEstimate", createdRequest.getRepairEstimate());
            req.setAttribute("successMessage", "Yêu cầu sửa chữa của bạn đã được tiếp nhận thành công!");

            req.getRequestDispatcher("/WEB-INF/views/repair/estimate.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            req.setAttribute("errorMessage", "Mã thiết bị hoặc loại lỗi không hợp lệ.");
            handleShowRequestForm(req, resp);
        } catch (IllegalArgumentException e) {
            req.setAttribute("errorMessage", e.getMessage());
            handleShowRequestForm(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            handleShowRequestForm(req, resp);
        }
    }

    /**
     * Tra cứu tiến độ yêu cầu sửa chữa và báo giá đã lưu
     */
    private void handleTrackRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestIdStr = req.getParameter("id");
        if (requestIdStr == null || requestIdStr.trim().isEmpty()) {
            req.getRequestDispatcher("/WEB-INF/views/repair/track.jsp").forward(req, resp);
            return;
        }

        try {
            Long requestId = Long.parseLong(requestIdStr.trim());
            Optional<RepairRequest> requestOpt = repairService.getRepairRequestById(requestId);

            if (requestOpt.isPresent()) {
                RepairRequest repairRequest = requestOpt.get();

                // Nếu phiếu gắn với một tài khoản thì chỉ chủ phiếu hoặc admin được xem.
                // Phiếu khách vãng lai (userId == null) vẫn tra cứu bằng mã phiếu như trước.
                if (!canView(req, repairRequest)) {
                    req.setAttribute("errorMessage",
                            "Bạn không có quyền xem phiếu sửa chữa này. Vui lòng đăng nhập bằng tài khoản đã gửi yêu cầu.");
                } else {
                    req.setAttribute("repairRequest", repairRequest);
                    req.setAttribute("repairEstimate", repairRequest.getRepairEstimate());
                }
            } else {
                req.setAttribute("errorMessage", "Không tìm thấy yêu cầu sửa chữa mã: #" + requestId);
            }
        } catch (NumberFormatException e) {
            req.setAttribute("errorMessage", "Mã yêu cầu sửa chữa không hợp lệ.");
        }

        req.getRequestDispatcher("/WEB-INF/views/repair/track.jsp").forward(req, resp);
    }

    /**
     * Kiểm tra quyền xem phiếu. Tài khoản admin được xem tất cả; người dùng thường
     * chỉ xem được phiếu của chính mình.
     */
    private boolean canView(HttpServletRequest req, RepairRequest repairRequest) {
        Long ownerId = repairRequest.getUserId();
        if (ownerId == null) {
            return true; // phiếu khách vãng lai: tra cứu bằng mã phiếu
        }

        User currentUser = currentUser(req);
        if (currentUser == null) {
            return false;
        }
        if (currentUser.isAdmin()) {
            return true;
        }
        return ownerId.equals(currentUser.getId());
    }

    /**
     * Lấy User đang đăng nhập từ session, hoặc {@code null} nếu là khách.
     * Module Auth (Tân) lưu nguyên đối tượng {@link User} dưới key
     * {@link AuthenticationFilter#SESSION_KEY}.
     */
    private static User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(AuthenticationFilter.SESSION_KEY);
        return value instanceof User ? (User) value : null;
    }

    /** ID người dùng đang đăng nhập, hoặc {@code null} nếu là khách vãng lai. */
    private static Long currentUserId(HttpServletRequest req) {
        User user = currentUser(req);
        return user != null ? user.getId() : null;
    }
}
