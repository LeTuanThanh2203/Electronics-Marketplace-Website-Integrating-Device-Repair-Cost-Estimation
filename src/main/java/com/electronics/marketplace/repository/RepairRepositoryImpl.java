package com.electronics.marketplace.repository;

import com.electronics.marketplace.dto.ProblemStatisticDTO;
import com.electronics.marketplace.entity.RepairEstimate;
import com.electronics.marketplace.entity.RepairPricing;
import com.electronics.marketplace.entity.RepairProblem;
import com.electronics.marketplace.entity.RepairRequest;
import com.electronics.marketplace.entity.enums.RepairStatus;
import com.electronics.marketplace.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RepairRepositoryImpl implements RepairRepository {

    // ==================== REPAIR PROBLEM ====================

    @Override
    public Optional<RepairProblem> findProblemById(Long id) {
        if (id == null) return Optional.empty();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            RepairProblem problem = em.find(RepairProblem.class, id);
            return Optional.ofNullable(problem);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<RepairProblem> findProblemByName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RepairProblem> query = em.createQuery(
                "SELECT p FROM RepairProblem p WHERE LOWER(p.name) = LOWER(:name)",
                RepairProblem.class
            );
            query.setParameter("name", name.trim());
            query.setMaxResults(1);
            List<RepairProblem> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public List<RepairProblem> findAllProblems(boolean onlyActive) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = onlyActive
                    ? "SELECT p FROM RepairProblem p WHERE p.isActive = true ORDER BY p.name ASC"
                    : "SELECT p FROM RepairProblem p ORDER BY p.name ASC";
            TypedQuery<RepairProblem> query = em.createQuery(jpql, RepairProblem.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public RepairProblem saveProblem(RepairProblem problem) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(problem);
            tx.commit();
            return problem;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving repair problem: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public RepairProblem updateProblem(RepairProblem problem) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            RepairProblem merged = em.merge(problem);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating repair problem: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // ==================== REPAIR PRICING ====================

    @Override
    public Optional<RepairPricing> findPricingById(Long id) {
        if (id == null) return Optional.empty();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            RepairPricing pricing = em.find(RepairPricing.class, id);
            return Optional.ofNullable(pricing);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<RepairPricing> findActivePricing(Long deviceId, Long problemId) {
        if (deviceId == null || problemId == null) return Optional.empty();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RepairPricing> query = em.createQuery(
                "SELECT rp FROM RepairPricing rp " +
                "WHERE rp.device.id = :deviceId AND rp.problem.id = :problemId AND rp.isActive = true " +
                "ORDER BY rp.version DESC",
                RepairPricing.class
            );
            query.setParameter("deviceId", deviceId);
            query.setParameter("problemId", problemId);
            query.setMaxResults(1);

            List<RepairPricing> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<RepairPricing> findLatestPricing(Long deviceId, Long problemId) {
        if (deviceId == null || problemId == null) return Optional.empty();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RepairPricing> query = em.createQuery(
                "SELECT rp FROM RepairPricing rp " +
                "WHERE rp.device.id = :deviceId AND rp.problem.id = :problemId " +
                "ORDER BY rp.version DESC",
                RepairPricing.class
            );
            query.setParameter("deviceId", deviceId);
            query.setParameter("problemId", problemId);
            query.setMaxResults(1);

            List<RepairPricing> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public List<RepairPricing> findAllPricings() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RepairPricing> query = em.createQuery(
                "SELECT rp FROM RepairPricing rp " +
                "JOIN FETCH rp.device " +
                "JOIN FETCH rp.problem " +
                "ORDER BY rp.device.brand ASC, rp.device.model ASC, rp.problem.name ASC",
                RepairPricing.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<RepairPricing> findPricingsByDeviceId(Long deviceId) {
        if (deviceId == null) return Collections.emptyList();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RepairPricing> query = em.createQuery(
                "SELECT rp FROM RepairPricing rp " +
                "JOIN FETCH rp.problem " +
                "WHERE rp.device.id = :deviceId AND rp.isActive = true " +
                "ORDER BY rp.problem.name ASC",
                RepairPricing.class
            );
            query.setParameter("deviceId", deviceId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public RepairPricing savePricing(RepairPricing pricing) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(pricing);
            tx.commit();
            return pricing;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving repair pricing: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public RepairPricing updatePricing(RepairPricing pricing) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            RepairPricing merged = em.merge(pricing);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating repair pricing: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // ==================== REPAIR REQUEST & ESTIMATE ====================

    @Override
    public RepairRequest saveRequest(RepairRequest request) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(request);
            tx.commit();
            return request;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving repair request: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public RepairRequest updateRequest(RepairRequest request) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            RepairRequest merged = em.merge(request);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating repair request: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<RepairRequest> findRequestById(Long id) {
        if (id == null) return Optional.empty();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            RepairRequest request = em.find(RepairRequest.class, id);
            return Optional.ofNullable(request);
        } finally {
            em.close();
        }
    }

    @Override
    public List<RepairRequest> findAllRequests() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RepairRequest> query = em.createQuery(
                "SELECT r FROM RepairRequest r " +
                "JOIN FETCH r.device " +
                "JOIN FETCH r.problem " +
                "LEFT JOIN FETCH r.repairEstimate " +
                "ORDER BY r.createdAt DESC",
                RepairRequest.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<RepairRequest> findRequestsByUserId(Long userId) {
        if (userId == null) return Collections.emptyList();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RepairRequest> query = em.createQuery(
                "SELECT r FROM RepairRequest r " +
                "JOIN FETCH r.device " +
                "JOIN FETCH r.problem " +
                "LEFT JOIN FETCH r.repairEstimate " +
                "WHERE r.userId = :userId " +
                "ORDER BY r.createdAt DESC",
                RepairRequest.class
            );
            query.setParameter("userId", userId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<RepairRequest> findRequestsAwaitingPricing(Long deviceId, Long problemId) {
        if (deviceId == null || problemId == null) return Collections.emptyList();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RepairRequest> query = em.createQuery(
                "SELECT r FROM RepairRequest r " +
                "WHERE r.device.id = :deviceId AND r.problem.id = :problemId " +
                "AND r.status = :status " +
                "ORDER BY r.createdAt ASC",
                RepairRequest.class
            );
            query.setParameter("deviceId", deviceId);
            query.setParameter("problemId", problemId);
            query.setParameter("status", RepairStatus.WAITING_PRICING.name());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public RepairEstimate saveEstimate(RepairEstimate estimate) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(estimate);
            tx.commit();
            return estimate;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving repair estimate: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public RepairEstimate updateEstimate(RepairEstimate estimate) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            RepairEstimate merged = em.merge(estimate);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating repair estimate: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<RepairEstimate> findEstimateByRequestId(Long requestId) {
        if (requestId == null) return Optional.empty();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RepairEstimate> query = em.createQuery(
                "SELECT e FROM RepairEstimate e WHERE e.repairRequest.id = :requestId",
                RepairEstimate.class
            );
            query.setParameter("requestId", requestId);
            query.setMaxResults(1);
            List<RepairEstimate> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    // ==================== STATISTICS (GROUP BY & COUNT) ====================

    @Override
    public List<ProblemStatisticDTO> getProblemStatistics() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT new com.electronics.marketplace.dto.ProblemStatisticDTO(" +
                    "p.id, p.name, COUNT(r.id), p.estimatedMinDays, p.estimatedMaxDays) " +
                    "FROM RepairProblem p " +
                    "LEFT JOIN RepairRequest r ON r.problem.id = p.id " +
                    "GROUP BY p.id, p.name, p.estimatedMinDays, p.estimatedMaxDays " +
                    "ORDER BY COUNT(r.id) DESC";

            TypedQuery<ProblemStatisticDTO> query = em.createQuery(jpql, ProblemStatisticDTO.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
