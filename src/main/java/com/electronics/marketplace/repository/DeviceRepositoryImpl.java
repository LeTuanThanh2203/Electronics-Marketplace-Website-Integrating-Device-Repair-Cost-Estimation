package com.electronics.marketplace.repository;

import com.electronics.marketplace.entity.Device;
import com.electronics.marketplace.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DeviceRepositoryImpl implements DeviceRepository {

    @Override
    public Optional<Device> findById(Long id) {
        if (id == null) return Optional.empty();
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Device device = em.find(Device.class, id);
            return Optional.ofNullable(device);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Device> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Device> query = em.createQuery("SELECT d FROM Device d ORDER BY d.brand ASC, d.model ASC", Device.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Device> findByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            return Collections.emptyList();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Device> query = em.createQuery(
                "SELECT d FROM Device d WHERE LOWER(d.brand) = LOWER(:brand) ORDER BY d.model ASC", Device.class);
            query.setParameter("brand", brand.trim());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Device> findByBrandSeriesModel(String brand, String series, String model) {
        if (brand == null || brand.trim().isEmpty() || model == null || model.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalizedSeries = series != null ? series.trim() : "";
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Device> query = em.createQuery(
                "SELECT d FROM Device d " +
                "WHERE LOWER(d.brand) = LOWER(:brand) " +
                "AND LOWER(d.series) = LOWER(:series) " +
                "AND LOWER(d.model) = LOWER(:model)",
                Device.class
            );
            query.setParameter("brand", brand.trim());
            query.setParameter("series", normalizedSeries);
            query.setParameter("model", model.trim());
            query.setMaxResults(1);
            List<Device> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public long countRepairRequests(Long deviceId) {
        if (deviceId == null) return 0L;
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(r.id) FROM RepairRequest r WHERE r.device.id = :deviceId",
                Long.class
            );
            query.setParameter("deviceId", deviceId);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public List<String> findAllBrands() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<String> query = em.createQuery("SELECT DISTINCT d.brand FROM Device d WHERE d.brand IS NOT NULL ORDER BY d.brand ASC", String.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Device save(Device device) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(device);
            tx.commit();
            return device;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving device: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public Device update(Device device) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Device merged = em.merge(device);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating device: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Device device = em.find(Device.class, id);
            if (device != null) {
                em.remove(device);
                tx.commit();
                return true;
            }
            tx.commit();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error deleting device: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
