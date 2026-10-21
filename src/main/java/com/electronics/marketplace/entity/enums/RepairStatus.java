package com.electronics.marketplace.entity.enums;

/**
 * Vòng đời của một phiếu yêu cầu sửa chữa (RepairRequest.status).
 *
 * <p>Đây là nguồn sự thật DUY NHẤT cho các giá trị trạng thái. Entity vẫn lưu
 * {@code String} để khớp Class Diagram ({@code status: String}) và để cột
 * {@code repair_requests.status} giữ nguyên kiểu VARCHAR, nhưng mọi chỗ đọc/ghi
 * phải đi qua enum này để tránh lệch giá trị giữa Service, Servlet và JSP.</p>
 *
 * <p>Danh sách này phải khớp với CHECK constraint
 * {@code chk_repair_requests_status} trong
 * {@code src/main/resources/database/schema_and_seed_repair.sql}.</p>
 */
public enum RepairStatus {

    /** Vừa tiếp nhận, chưa có bảng giá nên chưa thể báo giá. */
    PENDING("Chờ xử lý"),

    /** Đã tiếp nhận nhưng chưa có bảng giá chuẩn cho cặp (dòng máy, loại lỗi). */
    WAITING_PRICING("Chờ báo giá"),

    /** Đã có báo giá dự kiến (RepairEstimate snapshot). */
    ESTIMATED("Đã báo giá"),

    /** Kỹ thuật viên đang sửa chữa. */
    IN_PROGRESS("Đang sửa chữa"),

    /** Đã hoàn tất và trả máy. */
    COMPLETED("Hoàn thành"),

    /** Khách hàng hoặc cửa hàng hủy phiếu. */
    CANCELLED("Đã hủy");

    private final String label;

    RepairStatus(String label) {
        this.label = label;
    }

    /** Nhãn tiếng Việt dùng để hiển thị trên giao diện. */
    public String getLabel() {
        return label;
    }

    /**
     * Kiểm tra một chuỗi có phải trạng thái hợp lệ hay không.
     *
     * @param code giá trị lấy từ form/DB, có thể null
     * @return true nếu khớp chính xác tên một hằng số (phân biệt hoa/thường)
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    /**
     * Chuyển chuỗi sang enum, không ném exception.
     *
     * @param code giá trị cần chuyển, có thể null
     * @return enum tương ứng, hoặc {@code null} nếu không hợp lệ
     */
    public static RepairStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        for (RepairStatus status : values()) {
            if (status.name().equals(trimmed)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Các trạng thái mà quản trị viên được phép chọn thủ công trên giao diện.
     * {@link #WAITING_PRICING} bị loại vì đây là trạng thái do hệ thống tự suy ra
     * từ việc có/không có bảng giá, không phải trạng thái nghiệp vụ admin chọn.
     */
    public static RepairStatus[] selectableByAdmin() {
        return new RepairStatus[]{ESTIMATED, IN_PROGRESS, COMPLETED, CANCELLED};
    }
}
