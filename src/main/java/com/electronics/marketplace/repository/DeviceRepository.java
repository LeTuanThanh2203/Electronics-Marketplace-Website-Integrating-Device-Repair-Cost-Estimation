package com.electronics.marketplace.repository;

import com.electronics.marketplace.entity.Device;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Device operations.
 * Belongs to Phước Anh (E-commerce & Recommendation).
 */
public interface DeviceRepository {

    Optional<Device> findById(Long id);

    List<Device> findAll();

    List<Device> findByBrand(String brand);

    Device save(Device device);

    void delete(Long id);
}
