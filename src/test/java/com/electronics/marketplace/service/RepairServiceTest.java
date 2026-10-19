package com.electronics.marketplace.service;

import com.electronics.marketplace.dto.EstimateResultDTO;
import com.electronics.marketplace.dto.ProblemStatisticDTO;
import com.electronics.marketplace.entity.*;
import com.electronics.marketplace.entity.enums.RepairStatus;
import com.electronics.marketplace.repository.DeviceRepository;
import com.electronics.marketplace.repository.RepairRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RepairServiceTest {

    private RepairService repairService;
    private MockDeviceRepository mockDeviceRepository;
    private MockRepairRepository mockRepairRepository;
    private Device laptop;
    private RepairProblem screenProblem;

    @BeforeEach
    void setUp() {
        mockRepairRepository = new MockRepairRepository();
        mockDeviceRepository = new MockDeviceRepository(mockRepairRepository);
        repairService = new RepairService(mockDeviceRepository, mockRepairRepository);

        // Chuẩn bị dữ liệu mẫu
        laptop = new Device("Dell", "XPS", "15 9520", 2022, "Core i7", "16GB", "512GB SSD");
        laptop.setId(1L);
        mockDeviceRepository.save(laptop);

        screenProblem = new RepairProblem("Vỡ màn hình", "Màn hình OLED bị nứt, chảy mực", true, 1, 3);
        screenProblem.setId(10L);
        mockRepairRepository.saveProblem(screenProblem);

        RepairPricing pricing = new RepairPricing(
                laptop,
                screenProblem,
                new BigDecimal("2500000"), // Linh kiện: 2.500.000 VNĐ
                new BigDecimal("300000"),  // Tiền công: 300.000 VNĐ
                new BigDecimal("0.10"),    // Biên độ 10%
                1,
                true
        );
        pricing.setId(100L);
        mockRepairRepository.savePricing(pricing);
    }

    // =========================================================================
    // TÍNH TOÁN CHI PHÍ
    // =========================================================================

    @Test
    void testRepairPricingCostCalculation() {
        RepairPricing pricing = mockRepairRepository.findPricingById(100L).orElseThrow();

        // Base Cost = 2.500.000 + 300.000 = 2.800.000
        BigDecimal baseCost = pricing.calculateBaseCost();
        assertEquals(new BigDecimal("2800000"), baseCost);

        // Margin = 10% của 2.800.000 = 280.000
        BigDecimal marginAmount = pricing.calculateMarginAmount();
        assertEquals(new BigDecimal("280000.00"), marginAmount);

        // Min Cost = 2.800.000 - 280.000 = 2.520.000
        BigDecimal minCost = pricing.calculateMinCost();
        assertEquals(new BigDecimal("2520000.00"), minCost);

        // Max Cost = 2.800.000 + 280.000 = 3.080.000
        BigDecimal maxCost = pricing.calculateMaxCost();
        assertEquals(new BigDecimal("3080000.00"), maxCost);
    }

    /**
     * Dữ liệu cũ có thể chứa estimation_margin >= 1 (do bug hiểu nhầm đơn vị).
     * Việc tính toán phải kẹp về [0, 1] thay vì cộng thẳng một khoản tiền lớn.
     */
    @Test
    void testLegacyMarginAboveOneIsClamped() {
        RepairPricing pricing = new RepairPricing(
                laptop, screenProblem,
                new BigDecimal("1000000"), new BigDecimal("0"),
                new BigDecimal("5"), // dữ liệu cũ: 5 được hiểu là 5 đồng trước đây
                1, true);

        // Kẹp về 1.0 => margin = 100% của 1.000.000
        assertEquals(new BigDecimal("1000000.00"), pricing.calculateMarginAmount());
        assertEquals(new BigDecimal("0.00"), pricing.calculateMinCost());
        assertEquals(new BigDecimal("2000000.00"), pricing.calculateMaxCost());
    }

    @Test
    void testEstimateCostSuccess() {
        EstimateResultDTO estimate = repairService.estimateCost(1L, 10L);

        assertTrue(estimate.isAvailable());
        assertEquals(1L, estimate.getDeviceId());
        assertEquals(10L, estimate.getProblemId());
        assertEquals(new BigDecimal("2520000.00"), estimate.getMinCost());
        assertEquals(new BigDecimal("3080000.00"), estimate.getMaxCost());
        assertEquals(1, estimate.getEstimatedMinDays());
        assertEquals(3, estimate.getEstimatedMaxDays());
    }

    @Test
    void testEstimateCostNotFound() {
        // Thiết bị không tồn tại
        EstimateResultDTO estimate = repairService.estimateCost(999L, 10L);
        assertFalse(estimate.isAvailable());
        assertTrue(estimate.getMessage().contains("Không tìm thấy"));
    }

    // =========================================================================
    // TIẾP NHẬN YÊU CẦU
    // =========================================================================

    @Test
    void testCreateRepairRequestWithAutoEstimateSnapshot() {
        RepairRequest request = repairService.createRepairRequest(
                null,
                1L,
                10L,
                "Nguyễn Văn A",
                "0912345678",
                "vana@example.com",
                "Bị va đập góc trái gây nứt vỡ",
                "https://example.com/images/broken_screen.jpg"
        );

        assertNotNull(request.getId());
        assertEquals("ESTIMATED", request.getStatus());
        assertEquals("Nguyễn Văn A", request.getCustomerName());

        // Kiểm tra tự động tạo RepairEstimate snapshot
        RepairEstimate estimate = request.getRepairEstimate();
        assertNotNull(estimate);
        assertEquals(new BigDecimal("2520000.00"), estimate.getMinCost());
        assertEquals(new BigDecimal("3080000.00"), estimate.getMaxCost());
        assertEquals(new BigDecimal("2500000"), estimate.getPartsCostSnapshot());
        assertEquals(new BigDecimal("300000"), estimate.getLaborCostSnapshot());
        assertEquals(1, estimate.getPricingVersion());
    }

    /**
     * Hồi quy: trước đây khi chưa có bảng giá, service tạo estimate 0đ và khách
     * hàng thấy "0 - 0 VNĐ" như thể miễn phí. Nay phải không có estimate.
     */
    @Test
    void testCreateRepairRequestWithoutPricingDoesNotFakeZeroEstimate() {
        RepairProblem noPriceProblem = new RepairProblem("Hỏng camera", "Mờ ống kính", true, 1, 2);
        noPriceProblem.setId(30L);
        mockRepairRepository.saveProblem(noPriceProblem);

        RepairRequest request = repairService.createRepairRequest(
                null, 1L, 30L, "Khách", "0900", "k@example.com", "Camera mờ", null);

        assertNull(request.getRepairEstimate(), "Không được tạo estimate 0 đồng");
        assertEquals(RepairStatus.WAITING_PRICING.name(), request.getStatus());
    }

    /**
     * Hồi quy: sau khi admin nhập bảng giá, các phiếu đang chờ phải được tự động
     * bổ sung báo giá thay vì kẹt vĩnh viễn ở trạng thái WAITING_PRICING.
     */
    @Test
    void testSavingPricingBackfillsWaitingRequests() {
        RepairProblem noPriceProblem = new RepairProblem("Hỏng camera", "Mờ ống kính", true, 1, 2);
        noPriceProblem.setId(30L);
        mockRepairRepository.saveProblem(noPriceProblem);

        RepairRequest waiting = repairService.createRepairRequest(
                null, 1L, 30L, "Khách", "0900", "k@example.com", "Camera mờ", null);
        assertEquals(RepairStatus.WAITING_PRICING.name(), waiting.getStatus());

        repairService.saveOrUpdatePricing(1L, 30L,
                new BigDecimal("1500000"), new BigDecimal("200000"),
                new BigDecimal("0.10"), true);

        RepairRequest refreshed = repairService.getRepairRequestById(waiting.getId()).orElseThrow();
        assertEquals(RepairStatus.ESTIMATED.name(), refreshed.getStatus());
        assertNotNull(refreshed.getRepairEstimate());
        // Base = 1.700.000, margin 10% = 170.000 => 1.530.000 - 1.870.000
        assertEquals(new BigDecimal("1530000.00"), refreshed.getRepairEstimate().getMinCost());
        assertEquals(new BigDecimal("1870000.00"), refreshed.getRepairEstimate().getMaxCost());
    }

    @Test
    void testCreateRepairRequestRejectsInactiveProblem() {
        RepairProblem inactive = new RepairProblem("Lỗi cũ", "Không còn nhận", false, 1, 1);
        inactive.setId(40L);
        mockRepairRepository.saveProblem(inactive);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repairService.createRepairRequest(null, 1L, 40L, "Khách", "0900", null, "x", null));
        assertTrue(ex.getMessage().contains("tạm ngưng"));
    }

    // =========================================================================
    // QUẢN LÝ BẢNG GIÁ
    // =========================================================================

    /**
     * Hồi quy lỗi nghiêm trọng: tạm dừng một bảng giá rồi nhập lại giá cho cùng
     * cặp (device, problem) từng tạo bản mới với version = 1 và vi phạm
     * UNIQUE(device_id, problem_id, version).
     */
    @Test
    void testReactivatingDeactivatedPricingIncrementsVersion() {
        // Tạm dừng bảng giá hiện có
        RepairPricing deactivated = repairService.saveOrUpdatePricing(
                1L, 10L, new BigDecimal("2500000"), new BigDecimal("300000"),
                new BigDecimal("0.10"), false);
        assertFalse(deactivated.isActive());
        assertEquals(2, deactivated.getVersion());

        // Nhập lại giá cho cùng cặp — phải tăng version, không được ném lỗi
        RepairPricing reactivated = assertDoesNotThrow(() ->
                repairService.saveOrUpdatePricing(1L, 10L,
                        new BigDecimal("2600000"), new BigDecimal("320000"),
                        new BigDecimal("0.08"), true));

        assertTrue(reactivated.isActive());
        assertEquals(3, reactivated.getVersion());
        assertEquals(new BigDecimal("2600000.00"), reactivated.getPartsCost());
    }

    @Test
    void testMarginMustBeLessThanOne() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repairService.saveOrUpdatePricing(1L, 10L,
                        new BigDecimal("1000000"), new BigDecimal("100000"),
                        new BigDecimal("10"), true));
        assertTrue(ex.getMessage().contains("nhỏ hơn 1.0"));
    }

    @Test
    void testNegativeCostIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> repairService.saveOrUpdatePricing(1L, 10L,
                        new BigDecimal("-1"), new BigDecimal("100000"),
                        new BigDecimal("0.10"), true));
    }

    // =========================================================================
    // TRẠNG THÁI
    // =========================================================================

    @Test
    void testUpdateRequestStatusRejectsUnknownValue() {
        RepairRequest request = repairService.createRepairRequest(
                null, 1L, 10L, "Khách", "0900", null, "x", null);

        assertThrows(IllegalArgumentException.class,
                () -> repairService.updateRequestStatus(request.getId(), "PROCESSING"));

        RepairRequest updated = repairService.updateRequestStatus(
                request.getId(), RepairStatus.IN_PROGRESS.name());
        assertEquals(RepairStatus.IN_PROGRESS.name(), updated.getStatus());
    }

    @Test
    void testUpdateRequestStatusRejectsWaitingPricing() {
        RepairRequest request = repairService.createRepairRequest(
                null, 1L, 10L, "Khách", "0900", null, "x", null);

        assertThrows(IllegalArgumentException.class,
                () -> repairService.updateRequestStatus(request.getId(), RepairStatus.WAITING_PRICING.name()));
    }

    // =========================================================================
    // QUẢN LÝ DATA GỐC
    // =========================================================================

    @Test
    void testDeleteDeviceBlockedWhenRequestsExist() {
        repairService.createRepairRequest(null, 1L, 10L, "Khách", "0900", null, "x", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repairService.deleteDevice(1L));
        assertTrue(ex.getMessage().contains("phiếu sửa chữa"));
    }

    @Test
    void testDuplicateDeviceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> repairService.addDevice("Dell", "XPS", "15 9520", 2022, "Core i7", "16GB", "512GB SSD"));
    }

    @Test
    void testDuplicateProblemNameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> repairService.addProblem("Vỡ màn hình", "trùng", true, 1, 2));
    }

    @Test
    void testDayRangeValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> repairService.addProblem("Lỗi mới", "x", true, 5, 2));
    }

    // =========================================================================
    // THỐNG KÊ
    // =========================================================================

    @Test
    void testProblemStatisticsReport() {
        // Tạo thêm vài yêu cầu sửa chữa
        repairService.createRepairRequest(null, 1L, 10L, "Khách 1", "0901", "k1@mail.com", "Lỗi 1", null);
        repairService.createRepairRequest(null, 1L, 10L, "Khách 2", "0902", "k2@mail.com", "Lỗi 2", null);

        // Lỗi 2: Hỏng Pin
        RepairProblem pinProblem = new RepairProblem("Chai pin", "Pin phồng", true, 1, 2);
        pinProblem.setId(20L);
        mockRepairRepository.saveProblem(pinProblem);
        repairService.createRepairRequest(null, 1L, 20L, "Khách 3", "0903", "k3@mail.com", "Lỗi pin", null);

        List<ProblemStatisticDTO> stats = repairService.getProblemStatistics();
        assertNotNull(stats);
        assertFalse(stats.isEmpty());

        // Lỗi "Vỡ màn hình" có 2 request được tạo
        ProblemStatisticDTO topProblem = stats.get(0);
        assertEquals("Vỡ màn hình", topProblem.getProblemName());
        assertEquals(2L, topProblem.getRequestCount());
    }

    // =========================================================================
    // MOCK REPOSITORIES FOR TESTING ISOLATED BUSINESS LOGIC
    // =========================================================================

    private static class MockDeviceRepository implements DeviceRepository {
        private final Map<Long, Device> db = new HashMap<>();
        private final MockRepairRepository repairRepository;
        private long seq = 1L;

        MockDeviceRepository(MockRepairRepository repairRepository) {
            this.repairRepository = repairRepository;
        }

        @Override
        public Optional<Device> findById(Long id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public List<Device> findAll() {
            return new ArrayList<>(db.values());
        }

        @Override
        public List<Device> findByBrand(String brand) {
            List<Device> list = new ArrayList<>();
            for (Device d : db.values()) {
                if (d.getBrand().equalsIgnoreCase(brand)) list.add(d);
            }
            return list;
        }

        @Override
        public Optional<Device> findByBrandSeriesModel(String brand, String series, String model) {
            String s = series != null ? series : "";
            for (Device d : db.values()) {
                if (d.getBrand().equalsIgnoreCase(brand)
                        && d.getSeries().equalsIgnoreCase(s)
                        && d.getModel().equalsIgnoreCase(model)) {
                    return Optional.of(d);
                }
            }
            return Optional.empty();
        }

        @Override
        public List<String> findAllBrands() {
            Set<String> brands = new HashSet<>();
            for (Device d : db.values()) brands.add(d.getBrand());
            return new ArrayList<>(brands);
        }

        @Override
        public Device save(Device device) {
            if (device.getId() == null) device.setId(seq++);
            db.put(device.getId(), device);
            return device;
        }

        @Override
        public Device update(Device device) {
            db.put(device.getId(), device);
            return device;
        }

        @Override
        public boolean delete(Long id) {
            return db.remove(id) != null;
        }

        @Override
        public long countRepairRequests(Long deviceId) {
            return repairRepository.countRequestsByDevice(deviceId);
        }
    }

    private static class MockRepairRepository implements RepairRepository {
        private final Map<Long, RepairProblem> problems = new HashMap<>();
        private final Map<Long, RepairPricing> pricings = new HashMap<>();
        private final Map<Long, RepairRequest> requests = new HashMap<>();
        private final Map<Long, RepairEstimate> estimates = new HashMap<>();
        private long probSeq = 1L;
        private long priceSeq = 1L;
        private long reqSeq = 1L;
        private long estSeq = 1L;

        @Override
        public Optional<RepairProblem> findProblemById(Long id) {
            return Optional.ofNullable(problems.get(id));
        }

        @Override
        public Optional<RepairProblem> findProblemByName(String name) {
            for (RepairProblem p : problems.values()) {
                if (p.getName().equalsIgnoreCase(name)) return Optional.of(p);
            }
            return Optional.empty();
        }

        @Override
        public List<RepairProblem> findAllProblems(boolean onlyActive) {
            List<RepairProblem> list = new ArrayList<>();
            for (RepairProblem p : problems.values()) {
                if (!onlyActive || p.isActive()) list.add(p);
            }
            return list;
        }

        @Override
        public RepairProblem saveProblem(RepairProblem problem) {
            if (problem.getId() == null) problem.setId(probSeq++);
            problems.put(problem.getId(), problem);
            return problem;
        }

        @Override
        public RepairProblem updateProblem(RepairProblem problem) {
            problems.put(problem.getId(), problem);
            return problem;
        }

        /** Đếm phiếu theo thiết bị — chỉ dùng trong mock để test chặn xóa device. */
        long countRequestsByDevice(Long deviceId) {
            long count = 0;
            for (RepairRequest r : requests.values()) {
                if (r.getDevice().getId().equals(deviceId)) count++;
            }
            return count;
        }

        @Override
        public Optional<RepairPricing> findPricingById(Long id) {
            return Optional.ofNullable(pricings.get(id));
        }

        @Override
        public Optional<RepairPricing> findActivePricing(Long deviceId, Long problemId) {
            return findLatestPricing(deviceId, problemId).filter(RepairPricing::isActive);
        }

        @Override
        public Optional<RepairPricing> findLatestPricing(Long deviceId, Long problemId) {
            RepairPricing best = null;
            for (RepairPricing p : pricings.values()) {
                if (p.getDevice().getId().equals(deviceId) && p.getProblem().getId().equals(problemId)) {
                    if (best == null || p.getVersion() > best.getVersion()) best = p;
                }
            }
            return Optional.ofNullable(best);
        }

        @Override
        public List<RepairPricing> findAllPricings() {
            return new ArrayList<>(pricings.values());
        }

        @Override
        public List<RepairPricing> findPricingsByDeviceId(Long deviceId) {
            List<RepairPricing> list = new ArrayList<>();
            for (RepairPricing p : pricings.values()) {
                if (p.getDevice().getId().equals(deviceId)) list.add(p);
            }
            return list;
        }

        @Override
        public RepairPricing savePricing(RepairPricing pricing) {
            if (pricing.getId() == null) pricing.setId(priceSeq++);
            pricings.put(pricing.getId(), pricing);
            return pricing;
        }

        @Override
        public RepairPricing updatePricing(RepairPricing pricing) {
            pricings.put(pricing.getId(), pricing);
            return pricing;
        }

        @Override
        public RepairRequest saveRequest(RepairRequest request) {
            if (request.getId() == null) request.setId(reqSeq++);
            requests.put(request.getId(), request);
            if (request.getRepairEstimate() != null) {
                saveEstimate(request.getRepairEstimate());
            }
            return request;
        }

        @Override
        public RepairRequest updateRequest(RepairRequest request) {
            requests.put(request.getId(), request);
            if (request.getRepairEstimate() != null && request.getRepairEstimate().getId() == null) {
                saveEstimate(request.getRepairEstimate());
            }
            return request;
        }

        @Override
        public Optional<RepairRequest> findRequestById(Long id) {
            return Optional.ofNullable(requests.get(id));
        }

        @Override
        public List<RepairRequest> findAllRequests() {
            return new ArrayList<>(requests.values());
        }

        @Override
        public List<RepairRequest> findRequestsByUserId(Long userId) {
            List<RepairRequest> list = new ArrayList<>();
            for (RepairRequest r : requests.values()) {
                if (r.getUserId() != null && r.getUserId().equals(userId)) list.add(r);
            }
            return list;
        }

        @Override
        public List<RepairRequest> findRequestsAwaitingPricing(Long deviceId, Long problemId) {
            List<RepairRequest> list = new ArrayList<>();
            for (RepairRequest r : requests.values()) {
                if (r.getDevice().getId().equals(deviceId)
                        && r.getProblem().getId().equals(problemId)
                        && RepairStatus.WAITING_PRICING.name().equals(r.getStatus())) {
                    list.add(r);
                }
            }
            return list;
        }

        @Override
        public RepairEstimate saveEstimate(RepairEstimate estimate) {
            if (estimate.getId() == null) estimate.setId(estSeq++);
            estimates.put(estimate.getId(), estimate);
            return estimate;
        }

        @Override
        public RepairEstimate updateEstimate(RepairEstimate estimate) {
            estimates.put(estimate.getId(), estimate);
            return estimate;
        }

        @Override
        public Optional<RepairEstimate> findEstimateByRequestId(Long requestId) {
            for (RepairEstimate e : estimates.values()) {
                if (e.getRepairRequest().getId().equals(requestId)) return Optional.of(e);
            }
            return Optional.empty();
        }

        @Override
        public List<ProblemStatisticDTO> getProblemStatistics() {
            // Giả lập câu JPQL GROUP BY và COUNT
            Map<Long, Long> countMap = new HashMap<>();
            for (RepairRequest r : requests.values()) {
                Long pid = r.getProblem().getId();
                countMap.put(pid, countMap.getOrDefault(pid, 0L) + 1);
            }

            List<ProblemStatisticDTO> stats = new ArrayList<>();
            for (RepairProblem p : problems.values()) {
                long count = countMap.getOrDefault(p.getId(), 0L);
                stats.add(new ProblemStatisticDTO(p.getId(), p.getName(), count, p.getEstimatedMinDays(), p.getEstimatedMaxDays()));
            }

            stats.sort((a, b) -> Long.compare(b.getRequestCount(), a.getRequestCount()));
            return stats;
        }
    }
}
