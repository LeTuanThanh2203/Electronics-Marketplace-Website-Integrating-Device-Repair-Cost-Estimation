package com.electronics.marketplace.service;

import com.electronics.marketplace.dto.EstimateResultDTO;
import com.electronics.marketplace.dto.ProblemStatisticDTO;
import com.electronics.marketplace.entity.*;
import com.electronics.marketplace.entity.enums.RepairStatus;
import com.electronics.marketplace.repository.DeviceRepository;
import com.electronics.marketplace.repository.DeviceRepositoryImpl;
import com.electronics.marketplace.repository.RepairRepository;
import com.electronics.marketplace.repository.RepairRepositoryImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class RepairService {

    private final DeviceRepository deviceRepository;
    private final RepairRepository repairRepository;

    public RepairService() {
        this.deviceRepository = new DeviceRepositoryImpl();
        this.repairRepository = new RepairRepositoryImpl();
    }

    public RepairService(DeviceRepository deviceRepository, RepairRepository repairRepository) {
        this.deviceRepository = deviceRepository;
        this.repairRepository = repairRepository;
    }

    // =========================================================================
    // CHỨC NĂNG 1: TIẾP NHẬN & TỰ ĐỘNG TÍNH TOÁN BÁO GIÁ DỰ KIẾN (REPAIR ESTIMATE)
    // =========================================================================

    /**
     * Tính toán khoảng chi phí ước lượng nhanh dựa trên Device và Problem (đọc từ RepairPricing).
     * Phục vụ hiển thị trực tiếp cho khách hàng khi chọn máy và lỗi trên form.
     */
    public EstimateResultDTO estimateCost(Long deviceId, Long problemId) {
        if (deviceId == null || problemId == null) {
            return EstimateResultDTO.notFound("Vui lòng chọn đầy đủ thiết bị và loại lỗi.");
        }

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (!deviceOpt.isPresent()) {
            return EstimateResultDTO.notFound("Không tìm thấy thông tin thiết bị.");
        }

        Optional<RepairProblem> problemOpt = repairRepository.findProblemById(problemId);
        if (!problemOpt.isPresent()) {
            return EstimateResultDTO.notFound("Không tìm thấy thông tin loại lỗi.");
        }

        Device device = deviceOpt.get();
        RepairProblem problem = problemOpt.get();

        Optional<RepairPricing> pricingOpt = repairRepository.findActivePricing(deviceId, problemId);
        if (!pricingOpt.isPresent()) {
            return EstimateResultDTO.notFound("Chưa có bảng giá tiêu chuẩn cho dòng máy '" + device.getDisplayName() + "' với lỗi '" + problem.getName() + "'. Kỹ thuật viên sẽ liên hệ báo giá sau khi kiểm tra.");
        }

        return buildEstimateResult(device, problem, pricingOpt.get());
    }

    /**
     * Tiếp nhận yêu cầu sửa chữa và tự động snapshot chi phí dự kiến (RepairEstimate) theo sơ đồ Class Diagram.
     *
     * <p>Nếu chưa có bảng giá, phiếu được ghi nhận với trạng thái
     * {@link RepairStatus#WAITING_PRICING} và <b>không</b> tạo RepairEstimate.
     * Trước đây code tạo một estimate toàn số 0, khiến khách hàng thấy báo giá
     * "0 - 0 VNĐ" như thể sửa miễn phí. Khi admin bổ sung bảng giá,
     * {@link #saveOrUpdatePricing} sẽ tự tính lại báo giá cho các phiếu này.</p>
     */
    public RepairRequest createRepairRequest(Long userId, Long deviceId, Long problemId,
                                            String customerName, String customerPhone, String customerEmail,
                                            String description, String imageUrl) {
        if (deviceId == null || problemId == null) {
            throw new IllegalArgumentException("Thiết bị và loại lỗi không được để trống.");
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị có ID: " + deviceId));

        RepairProblem problem = repairRepository.findProblemById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại lỗi có ID: " + problemId));

        if (!problem.isActive()) {
            throw new IllegalArgumentException("Loại lỗi '" + problem.getName() + "' đã tạm ngưng tiếp nhận.");
        }

        // 1. Tạo đối tượng RepairRequest
        RepairRequest request = new RepairRequest();
        request.setUserId(userId);
        request.setDevice(device);
        request.setProblem(problem);
        request.setCustomerName(trimToNull(customerName));
        request.setCustomerPhone(trimToNull(customerPhone));
        request.setCustomerEmail(trimToNull(customerEmail));
        request.setDescription(description);
        request.setImageUrl(trimToNull(imageUrl));
        request.setCreatedAt(new Date());

        // 2. Tự động đọc bảng giá RepairPricing để sinh RepairEstimate snapshot
        Optional<RepairPricing> pricingOpt = repairRepository.findActivePricing(deviceId, problemId);
        if (pricingOpt.isPresent()) {
            request.setRepairEstimate(buildEstimateSnapshot(request, pricingOpt.get()));
            request.setStatus(RepairStatus.ESTIMATED);
        } else {
            // Không tạo estimate rỗng: để trống thì giao diện hiển thị "chưa có báo giá",
            // trung thực hơn là hiển thị 0 VNĐ.
            request.setStatus(RepairStatus.WAITING_PRICING);
        }

        // 3. Lưu vào Database
        return repairRepository.saveRequest(request);
    }

    public Optional<RepairRequest> getRepairRequestById(Long id) {
        return repairRepository.findRequestById(id);
    }

    public List<RepairRequest> getAllRepairRequests() {
        return repairRepository.findAllRequests();
    }

    public List<RepairRequest> getRepairRequestsByUserId(Long userId) {
        return repairRepository.findRequestsByUserId(userId);
    }

    /**
     * Đổi trạng thái phiếu. Chỉ chấp nhận giá trị thuộc {@link RepairStatus},
     * tránh ghi chuỗi rác vào cột status.
     */
    public RepairRequest updateRequestStatus(Long requestId, String newStatus) {
        RepairStatus status = RepairStatus.fromCode(newStatus);
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + newStatus);
        }
        if (status == RepairStatus.WAITING_PRICING) {
            throw new IllegalArgumentException(
                    "Trạng thái 'Chờ báo giá' do hệ thống tự xác định, không thể chọn thủ công.");
        }

        RepairRequest request = repairRepository.findRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu sửa chữa ID: " + requestId));
        request.setStatus(status);
        return repairRepository.updateRequest(request);
    }

    // =========================================================================
    // CHỨC NĂNG 2 (PHẦN 1): QUẢN LÝ DATA GỐC (DÒNG MÁY, LOẠI LỖI, BẢNG GIÁ)
    // =========================================================================

    // --- Device Management ---
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<String> getAllBrands() {
        return deviceRepository.findAllBrands();
    }

    public List<Device> getDevicesByBrand(String brand) {
        return deviceRepository.findByBrand(brand);
    }

    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    public Device addDevice(String brand, String series, String model, Integer releaseYear, String cpu, String ram, String storage) {
        if (brand == null || brand.trim().isEmpty() || model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Hãng sản xuất (Brand) và Tên dòng máy (Model) không được để trống.");
        }
        if (releaseYear != null && (releaseYear < 1990 || releaseYear > 2100)) {
            throw new IllegalArgumentException("Năm sản xuất không hợp lệ (phải trong khoảng 1990 - 2100).");
        }

        String cleanBrand = brand.trim();
        String cleanSeries = series != null ? series.trim() : "";
        String cleanModel = model.trim();

        if (deviceRepository.findByBrandSeriesModel(cleanBrand, cleanSeries, cleanModel).isPresent()) {
            throw new IllegalArgumentException(
                    "Dòng máy '" + cleanBrand + " " + cleanSeries + " " + cleanModel + "' đã tồn tại.");
        }

        Device device = new Device(cleanBrand, cleanSeries, cleanModel, releaseYear, trimToNull(cpu), trimToNull(ram), trimToNull(storage));
        return deviceRepository.save(device);
    }

    public Device updateDevice(Long id, String brand, String series, String model, Integer releaseYear, String cpu, String ram, String storage) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị có ID: " + id));

        if (brand != null && !brand.trim().isEmpty()) device.setBrand(brand.trim());
        if (model != null && !model.trim().isEmpty()) device.setModel(model.trim());
        device.setSeries(series != null ? series.trim() : "");
        if (releaseYear != null && (releaseYear < 1990 || releaseYear > 2100)) {
            throw new IllegalArgumentException("Năm sản xuất không hợp lệ (phải trong khoảng 1990 - 2100).");
        }
        device.setReleaseYear(releaseYear);
        device.setCpu(cpu);
        device.setRam(ram);
        device.setStorage(storage);

        return deviceRepository.update(device);
    }

    /**
     * Xóa thiết bị. Chặn xóa cứng khi đã có phiếu sửa chữa tham chiếu tới,
     * vì khóa ngoại {@code repair_requests.device_id} là ON DELETE RESTRICT.
     * Thay vì để DB ném lỗi khó hiểu, báo rõ lý do cho admin.
     */
    public boolean deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị có ID: " + id));

        long requestCount = deviceRepository.countRepairRequests(id);
        if (requestCount > 0) {
            throw new IllegalArgumentException(
                    "Không thể xóa '" + device.getDisplayName() + "': đang có " + requestCount
                            + " phiếu sửa chữa tham chiếu. Hãy tạm ngưng bảng giá thay vì xóa.");
        }

        return deviceRepository.delete(id);
    }

    // --- RepairProblem Management ---
    public List<RepairProblem> getAllProblems(boolean onlyActive) {
        return repairRepository.findAllProblems(onlyActive);
    }

    public Optional<RepairProblem> getProblemById(Long id) {
        return repairRepository.findProblemById(id);
    }

    public RepairProblem addProblem(String name, String description, boolean isActive, Integer minDays, Integer maxDays) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên lỗi không được để trống.");
        }
        validateDayRange(minDays, maxDays);

        String cleanName = name.trim();
        if (repairRepository.findProblemByName(cleanName).isPresent()) {
            throw new IllegalArgumentException("Loại lỗi '" + cleanName + "' đã tồn tại.");
        }

        RepairProblem problem = new RepairProblem(cleanName, trimToNull(description), isActive, minDays, maxDays);
        return repairRepository.saveProblem(problem);
    }

    public RepairProblem updateProblem(Long id, String name, String description, Boolean isActive, Integer minDays, Integer maxDays) {
        RepairProblem problem = repairRepository.findProblemById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại lỗi có ID: " + id));

        if (name != null && !name.trim().isEmpty()) {
            String cleanName = name.trim();
            Optional<RepairProblem> sameName = repairRepository.findProblemByName(cleanName);
            if (sameName.isPresent() && !sameName.get().getId().equals(id)) {
                throw new IllegalArgumentException("Loại lỗi '" + cleanName + "' đã tồn tại.");
            }
            problem.setName(cleanName);
        }
        if (description != null) problem.setDescription(description);
        if (isActive != null) problem.setActive(isActive);

        Integer effectiveMin = minDays != null ? minDays : problem.getEstimatedMinDays();
        Integer effectiveMax = maxDays != null ? maxDays : problem.getEstimatedMaxDays();
        validateDayRange(effectiveMin, effectiveMax);
        problem.setEstimatedMinDays(effectiveMin);
        problem.setEstimatedMaxDays(effectiveMax);

        return repairRepository.updateProblem(problem);
    }

    // --- RepairPricing Management ---
    public List<RepairPricing> getAllPricings() {
        return repairRepository.findAllPricings();
    }

    public List<RepairPricing> getPricingsByDeviceId(Long deviceId) {
        return repairRepository.findPricingsByDeviceId(deviceId);
    }

    public Optional<RepairPricing> getPricingById(Long id) {
        return repairRepository.findPricingById(id);
    }

    /**
     * Tạo mới hoặc cập nhật bảng giá tiêu chuẩn cho một cặp (dòng máy, loại lỗi).
     *
     * <p><b>Sửa lỗi nghiêm trọng:</b> bản cũ tìm bản giá đang áp dụng
     * ({@code isActive = true}). Khi admin tạm dừng một bảng giá rồi nhập lại giá
     * cho cùng cặp, không tìm thấy bản cũ nên tạo bản mới với {@code version = 1},
     * vi phạm {@code UNIQUE(device_id, problem_id, version)} và làm sập request.
     * Nay luôn lấy bản có version cao nhất (bất kể isActive) và cấp version kế tiếp.</p>
     *
     * <p>Sau khi lưu, các phiếu đang {@link RepairStatus#WAITING_PRICING} của đúng
     * cặp này được tự động bổ sung báo giá, nên khách hàng không phải tạo lại phiếu.</p>
     */
    public RepairPricing saveOrUpdatePricing(Long deviceId, Long problemId, BigDecimal partsCost,
                                            BigDecimal laborCost, BigDecimal estimationMargin, Boolean isActive) {
        if (deviceId == null || problemId == null) {
            throw new IllegalArgumentException("Device ID và Problem ID không được để trống.");
        }

        BigDecimal cleanParts = normalizeMoney(partsCost, "Tiền linh kiện");
        BigDecimal cleanLabor = normalizeMoney(laborCost, "Tiền công thợ");
        BigDecimal cleanMargin = normalizeAndValidateMargin(estimationMargin);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị có ID: " + deviceId));

        RepairProblem problem = repairRepository.findProblemById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại lỗi có ID: " + problemId));

        boolean targetActive = isActive == null || isActive;

        // Luôn nhìn bản version cao nhất, kể cả đang bị tạm dừng.
        Optional<RepairPricing> latestOpt = repairRepository.findLatestPricing(deviceId, problemId);

        RepairPricing saved;
        if (latestOpt.isPresent()) {
            RepairPricing latest = latestOpt.get();
            latest.setPartsCost(cleanParts);
            latest.setLaborCost(cleanLabor);
            latest.setEstimationMargin(cleanMargin);
            latest.setActive(targetActive);
            latest.setVersion(latest.getVersion() + 1);
            latest.setUpdatedAt(new Date());
            saved = repairRepository.updatePricing(latest);
        } else {
            RepairPricing pricing = new RepairPricing(
                    device, problem, cleanParts, cleanLabor, cleanMargin, 1, targetActive);
            saved = repairRepository.savePricing(pricing);
        }

        // Bổ sung báo giá cho các phiếu đang chờ giá của đúng cặp này.
        if (saved.isActive()) {
            recalculatePendingEstimates(device, problem, saved);
        }

        return saved;
    }

    /**
     * Tính lại báo giá cho mọi phiếu đang chờ giá của một cặp (dòng máy, loại lỗi).
     *
     * @return số phiếu đã được cập nhật
     */
    public int recalculatePendingEstimates(Long deviceId, Long problemId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        Optional<RepairProblem> problemOpt = repairRepository.findProblemById(problemId);
        Optional<RepairPricing> pricingOpt = repairRepository.findActivePricing(deviceId, problemId);
        if (!deviceOpt.isPresent() || !problemOpt.isPresent() || !pricingOpt.isPresent()) {
            return 0;
        }
        return recalculatePendingEstimates(deviceOpt.get(), problemOpt.get(), pricingOpt.get());
    }

    private int recalculatePendingEstimates(Device device, RepairProblem problem, RepairPricing pricing) {
        List<RepairRequest> waiting = repairRepository.findRequestsAwaitingPricing(device.getId(), problem.getId());
        int updated = 0;
        for (RepairRequest request : waiting) {
            RepairEstimate estimate = request.getRepairEstimate();
            if (estimate == null) {
                estimate = buildEstimateSnapshot(request, pricing);
                request.setRepairEstimate(estimate);
            } else {
                applyPricingToEstimate(estimate, pricing);
            }
            request.setStatus(RepairStatus.ESTIMATED);
            repairRepository.updateRequest(request);
            updated++;
        }
        return updated;
    }

    // =========================================================================
    // CHỨC NĂNG 2 (PHẦN 2): BÁO CÁO THỐNG KÊ (GROUP BY, COUNT)
    // =========================================================================

    /**
     * Thống kê đếm xem lỗi nào đang bị hỏng nhiều nhất.
     * Sử dụng câu lệnh JPQL/SQL GROUP BY gom nhóm và COUNT để xếp hạng lỗi thường gặp.
     */
    public List<ProblemStatisticDTO> getProblemStatistics() {
        return repairRepository.getProblemStatistics();
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private EstimateResultDTO buildEstimateResult(Device device, RepairProblem problem, RepairPricing pricing) {
        EstimateResultDTO result = new EstimateResultDTO();
        result.setDeviceId(device.getId());
        result.setDeviceName(device.getDisplayName());
        result.setProblemId(problem.getId());
        result.setProblemName(problem.getName());
        result.setPartsCost(pricing.getPartsCost());
        result.setLaborCost(pricing.getLaborCost());
        result.setBaseCost(pricing.calculateBaseCost());
        result.setMinCost(pricing.calculateMinCost());
        result.setMaxCost(pricing.calculateMaxCost());
        result.setEstimationMargin(pricing.getEstimationMargin());
        result.setPricingVersion(pricing.getVersion());
        result.setEstimatedMinDays(problem.getEstimatedMinDays());
        result.setEstimatedMaxDays(problem.getEstimatedMaxDays());
        result.setAvailable(true);
        result.setMessage("Tính toán chi phí dự kiến thành công.");
        return result;
    }

    private RepairEstimate buildEstimateSnapshot(RepairRequest request, RepairPricing pricing) {
        RepairEstimate estimate = new RepairEstimate();
        estimate.setRepairRequest(request);
        applyPricingToEstimate(estimate, pricing);
        return estimate;
    }

    /** Chép toàn bộ số liệu giá hiện hành vào bản snapshot (bất biến theo thời gian). */
    private void applyPricingToEstimate(RepairEstimate estimate, RepairPricing pricing) {
        estimate.setMinCost(pricing.calculateMinCost());
        estimate.setMaxCost(pricing.calculateMaxCost());
        estimate.setPricingVersion(pricing.getVersion());
        estimate.setPartsCostSnapshot(pricing.getPartsCost());
        estimate.setLaborCostSnapshot(pricing.getLaborCost());
        estimate.setEstimationMarginSnapshot(pricing.getEstimationMargin());
        estimate.setCalculatedAt(new Date());
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void validateDayRange(Integer minDays, Integer maxDays) {
        if (minDays != null && minDays < 0) {
            throw new IllegalArgumentException("Số ngày dự kiến tối thiểu không được âm.");
        }
        if (maxDays != null && maxDays < 0) {
            throw new IllegalArgumentException("Số ngày dự kiến tối đa không được âm.");
        }
        if (minDays != null && maxDays != null && minDays > maxDays) {
            throw new IllegalArgumentException(
                    "Số ngày tối thiểu (" + minDays + ") không được lớn hơn số ngày tối đa (" + maxDays + ").");
        }
    }

    private static BigDecimal normalizeMoney(BigDecimal value, String label) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(label + " không được âm.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Biên độ phải nằm trong [0, 1) — tức từ 0% đến dưới 100%.
     * Chặn trường hợp admin nhập 10 với ý định "10%" nhưng bị hiểu thành 10 đồng.
     */
    private static BigDecimal normalizeAndValidateMargin(BigDecimal margin) {
        if (margin == null) {
            return RepairPricing.DEFAULT_MARGIN;
        }
        if (margin.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Biên độ dự toán không được âm.");
        }
        if (margin.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException(
                    "Biên độ dự toán phải nhỏ hơn 1.0. Ví dụ 10% nhập là 0.10 (không nhập 10).");
        }
        return margin;
    }
}
