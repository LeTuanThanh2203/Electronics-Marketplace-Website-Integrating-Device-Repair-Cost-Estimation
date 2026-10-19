package com.electronics.marketplace.repository;

import com.electronics.marketplace.dto.ProblemStatisticDTO;
import com.electronics.marketplace.entity.RepairEstimate;
import com.electronics.marketplace.entity.RepairPricing;
import com.electronics.marketplace.entity.RepairProblem;
import com.electronics.marketplace.entity.RepairRequest;

import java.util.List;
import java.util.Optional;

public interface RepairRepository {
    // RepairProblem CRUD
    Optional<RepairProblem> findProblemById(Long id);
    Optional<RepairProblem> findProblemByName(String name);
    List<RepairProblem> findAllProblems(boolean onlyActive);
    RepairProblem saveProblem(RepairProblem problem);
    RepairProblem updateProblem(RepairProblem problem);

    // RepairPricing CRUD & Search
    Optional<RepairPricing> findPricingById(Long id);
    Optional<RepairPricing> findActivePricing(Long deviceId, Long problemId);

    /**
     * Lấy bản giá có version cao nhất của một cặp (dòng máy, loại lỗi),
     * KHÔNG lọc theo isActive. Dùng để tính version kế tiếp và để tránh
     * vi phạm unique constraint (device_id, problem_id, version).
     */
    Optional<RepairPricing> findLatestPricing(Long deviceId, Long problemId);

    List<RepairPricing> findAllPricings();
    List<RepairPricing> findPricingsByDeviceId(Long deviceId);
    RepairPricing savePricing(RepairPricing pricing);
    RepairPricing updatePricing(RepairPricing pricing);

    // RepairRequest & Estimate
    RepairRequest saveRequest(RepairRequest request);
    RepairRequest updateRequest(RepairRequest request);
    Optional<RepairRequest> findRequestById(Long id);
    List<RepairRequest> findAllRequests();
    List<RepairRequest> findRequestsByUserId(Long userId);

    /** Các phiếu đang chờ báo giá của đúng một cặp (dòng máy, loại lỗi). */
    List<RepairRequest> findRequestsAwaitingPricing(Long deviceId, Long problemId);

    RepairEstimate saveEstimate(RepairEstimate estimate);
    RepairEstimate updateEstimate(RepairEstimate estimate);
    Optional<RepairEstimate> findEstimateByRequestId(Long requestId);

    // Statistics (GROUP BY, COUNT)
    List<ProblemStatisticDTO> getProblemStatistics();
}
