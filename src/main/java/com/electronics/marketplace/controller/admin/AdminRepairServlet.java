package com.electronics.marketplace.controller.admin;

import com.electronics.marketplace.dto.ProblemStatisticDTO;
import com.electronics.marketplace.entity.Device;
import com.electronics.marketplace.entity.RepairPricing;
import com.electronics.marketplace.entity.RepairProblem;
import com.electronics.marketplace.entity.RepairRequest;
import com.electronics.marketplace.entity.enums.RepairStatus;
import com.electronics.marketplace.service.RepairService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(name = "AdminRepairServlet", urlPatterns = {
        "/admin/repairs",
        "/admin/repair/devices",
        "/admin/repair/problems",
        "/admin/repair/pricing",
        "/admin/repair/reports"
})
public class AdminRepairServlet extends HttpServlet {

    private final RepairService repairService = new RepairService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        String view = req.getParameter("view");

        // 1. Báo cáo thống kê: Lỗi nào bị hỏng nhiều nhất (GROUP BY, COUNT)
        if ("/admin/repair/reports".equals(path) || "reports".equalsIgnoreCase(view)) {
            handleReports(req, resp);
            return;
        }

        // 2. Quản lý dòng máy (Device)
        if ("/admin/repair/devices".equals(path) || "devices".equalsIgnoreCase(view)) {
            handleListDevices(req, resp);
            return;
        }

        // 3. Quản lý loại lỗi (RepairProblem)
        if ("/admin/repair/problems".equals(path) || "problems".equalsIgnoreCase(view)) {
            handleListProblems(req, resp);
            return;
        }

        // 4. Quản lý bảng giá dự toán sửa chữa (RepairPricing)
        if ("/admin/repair/pricing".equals(path) || "pricing".equalsIgnoreCase(view)) {
            handleListPricings(req, resp);
            return;
        }

        // 5. Mặc định: Danh sách toàn bộ yêu cầu sửa chữa
        handleListRepairRequests(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action.toLowerCase()) {
                case "adddevice":
                    handleAddDevice(req, resp);
                    break;
                case "updatedevice":
                    handleUpdateDevice(req, resp);
                    break;
                case "deletedevice":
                    handleDeleteDevice(req, resp);
                    break;
                case "addproblem":
                    handleAddProblem(req, resp);
                    break;
                case "updateproblem":
                    handleUpdateProblem(req, resp);
                    break;
                case "savepricing":
                    handleSavePricing(req, resp);
                    break;
                case "updaterequeststatus":
                    handleUpdateRequestStatus(req, resp);
                    break;
                default:
                    resp.sendRedirect(req.getContextPath() + "/admin/repairs");
                    break;
            }
        } catch (IllegalArgumentException e) {
            // Quay lại đúng trang vừa thao tác và mang theo thông báo lỗi.
            // Trước đây lỗi bị nuốt hoàn toàn: admin thấy trang load lại như thành công.
            forwardWithError(req, resp, e.getMessage());
        } catch (Exception e) {
            forwardWithError(req, resp, "Lỗi xử lý hệ thống: " + e.getMessage());
        }
    }

    /**
     * Hiển thị lại trang tương ứng với nguồn gửi form, kèm errorMessage.
     * Dùng forward thay vì redirect để giữ nguyên tham số và không mất thông báo.
     */
    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("errorMessage", message);
        String referer = req.getHeader("Referer");
        if (referer != null && referer.contains("/admin/repair/")) {
            if (referer.contains("/devices")) {
                handleListDevices(req, resp);
                return;
            }
            if (referer.contains("/problems")) {
                handleListProblems(req, resp);
                return;
            }
            if (referer.contains("/pricing")) {
                handleListPricings(req, resp);
                return;
            }
            if (referer.contains("/reports")) {
                handleReports(req, resp);
                return;
            }
        }
        handleListRepairRequests(req, resp);
    }

    // =========================================================================
    // XỬ LÝ GET
    // =========================================================================

    /**
     * Bảng Thống kê đếm xem lỗi nào đang bị hỏng nhiều nhất (GROUP BY, COUNT)
     */
    private void handleReports(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<ProblemStatisticDTO> problemStats = repairService.getProblemStatistics();
        req.setAttribute("problemStats", problemStats);
        req.getRequestDispatcher("/WEB-INF/views/admin/report.jsp").forward(req, resp);
    }

    private void handleListDevices(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Device> devices = repairService.getAllDevices();
        req.setAttribute("devices", devices);
        req.getRequestDispatcher("/WEB-INF/views/admin/device_list.jsp").forward(req, resp);
    }

    private void handleListProblems(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<RepairProblem> problems = repairService.getAllProblems(false);
        req.setAttribute("problems", problems);
        req.getRequestDispatcher("/WEB-INF/views/admin/problem_list.jsp").forward(req, resp);
    }

    private void handleListPricings(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<RepairPricing> pricings = repairService.getAllPricings();
        List<Device> devices = repairService.getAllDevices();
        List<RepairProblem> problems = repairService.getAllProblems(true);

        req.setAttribute("pricings", pricings);
        req.setAttribute("devices", devices);
        req.setAttribute("problems", problems);
        req.getRequestDispatcher("/WEB-INF/views/admin/pricing_list.jsp").forward(req, resp);
    }

    private void handleListRepairRequests(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<RepairRequest> requests = repairService.getAllRepairRequests();
        req.setAttribute("repairRequests", requests);
        req.setAttribute("selectableStatuses", RepairStatus.selectableByAdmin());
        req.getRequestDispatcher("/WEB-INF/views/admin/repair_requests.jsp").forward(req, resp);
    }

    // =========================================================================
    // XỬ LÝ POST
    // =========================================================================

    private void handleAddDevice(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String brand = req.getParameter("brand");
        String series = req.getParameter("series");
        String model = req.getParameter("model");
        Integer releaseYear = parseOptionalInt(req.getParameter("releaseYear"), "Năm sản xuất");
        String cpu = req.getParameter("cpu");
        String ram = req.getParameter("ram");
        String storage = req.getParameter("storage");

        repairService.addDevice(brand, series, model, releaseYear, cpu, ram, storage);
        redirectWithMessage(req, resp, "/admin/repair/devices", "Thêm dòng máy thành công.");
    }

    private void handleUpdateDevice(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = parseRequiredLong(req.getParameter("id"), "ID thiết bị");
        String brand = req.getParameter("brand");
        String series = req.getParameter("series");
        String model = req.getParameter("model");
        Integer releaseYear = parseOptionalInt(req.getParameter("releaseYear"), "Năm sản xuất");
        String cpu = req.getParameter("cpu");
        String ram = req.getParameter("ram");
        String storage = req.getParameter("storage");

        repairService.updateDevice(id, brand, series, model, releaseYear, cpu, ram, storage);
        redirectWithMessage(req, resp, "/admin/repair/devices", "Cập nhật dòng máy thành công.");
    }

    private void handleDeleteDevice(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = parseRequiredLong(req.getParameter("id"), "ID thiết bị");
        repairService.deleteDevice(id);
        redirectWithMessage(req, resp, "/admin/repair/devices", "Đã xóa dòng máy.");
    }

    private void handleAddProblem(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        boolean isActive = parseCheckbox(req.getParameter("isActive"));
        Integer minDays = parseOptionalInt(req.getParameter("estimatedMinDays"), "Số ngày tối thiểu");
        Integer maxDays = parseOptionalInt(req.getParameter("estimatedMaxDays"), "Số ngày tối đa");

        repairService.addProblem(name, description, isActive, minDays, maxDays);
        redirectWithMessage(req, resp, "/admin/repair/problems", "Thêm loại lỗi thành công.");
    }

    private void handleUpdateProblem(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = parseRequiredLong(req.getParameter("id"), "ID loại lỗi");
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        boolean isActive = parseCheckbox(req.getParameter("isActive"));
        Integer minDays = parseOptionalInt(req.getParameter("estimatedMinDays"), "Số ngày tối thiểu");
        Integer maxDays = parseOptionalInt(req.getParameter("estimatedMaxDays"), "Số ngày tối đa");

        repairService.updateProblem(id, name, description, isActive, minDays, maxDays);
        redirectWithMessage(req, resp, "/admin/repair/problems", "Cập nhật loại lỗi thành công.");
    }

    private void handleSavePricing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long deviceId = parseRequiredLong(req.getParameter("deviceId"), "Dòng thiết bị");
        Long problemId = parseRequiredLong(req.getParameter("problemId"), "Loại lỗi");
        BigDecimal partsCost = parseMoney(req.getParameter("partsCost"), "Tiền linh kiện");
        BigDecimal laborCost = parseMoney(req.getParameter("laborCost"), "Tiền công thợ");
        BigDecimal estimationMargin = parseMargin(req.getParameter("estimationMargin"));
        boolean isActive = !"false".equalsIgnoreCase(req.getParameter("isActive"));

        RepairPricing saved = repairService.saveOrUpdatePricing(
                deviceId, problemId, partsCost, laborCost, estimationMargin, isActive);

        redirectWithMessage(req, resp, "/admin/repair/pricing",
                "Đã lưu bảng giá (phiên bản v" + saved.getVersion() + ").");
    }

    private void handleUpdateRequestStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long requestId = parseRequiredLong(req.getParameter("requestId"), "Mã phiếu sửa chữa");
        String status = req.getParameter("status");
        repairService.updateRequestStatus(requestId, status);
        redirectWithMessage(req, resp, "/admin/repairs", "Đã cập nhật trạng thái phiếu #" + requestId + ".");
    }

    // =========================================================================
    // PARSE HELPERS — lỗi định dạng phải thành IllegalArgumentException
    // để forwardWithError hiển thị được, thay vì NumberFormatException thô.
    // =========================================================================

    private static Long parseRequiredLong(String raw, String label) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu " + label + ".");
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " không hợp lệ: " + raw);
        }
    }

    private static Integer parseOptionalInt(String raw, String label) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " phải là số nguyên: " + raw);
        }
    }

    private static BigDecimal parseMoney(String raw, String label) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " không được để trống.");
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " phải là số: " + raw);
        }
    }

    private static BigDecimal parseMargin(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return RepairPricing.DEFAULT_MARGIN;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Biên độ dự toán phải là số thập phân (ví dụ 0.10): " + raw);
        }
    }

    private static boolean parseCheckbox(String raw) {
        return "true".equalsIgnoreCase(raw) || "on".equalsIgnoreCase(raw) || "1".equals(raw);
    }

    private void redirectWithMessage(HttpServletRequest req, HttpServletResponse resp, String path, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + path + "?message=" + encoded);
    }
}
