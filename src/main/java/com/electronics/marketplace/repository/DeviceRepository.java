package com.electronics.marketplace.repository;

import com.electronics.marketplace.entity.Device;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository {
    Optional<Device> findById(Long id);
    List<Device> findAll();
    List<Device> findByBrand(String brand);

    /**
     * Tìm đúng một dòng máy theo bộ ba (brand, series, model), không phân biệt hoa/thường.
     * Dùng để chặn admin thêm trùng dòng máy trước khi DB ném lỗi unique constraint.
     */
    Optional<Device> findByBrandSeriesModel(String brand, String series, String model);

    List<String> findAllBrands();
    Device save(Device device);
    Device update(Device device);
    boolean delete(Long id);

    /** Đếm số phiếu sửa chữa đang tham chiếu tới thiết bị (để chặn xóa cứng). */
    long countRepairRequests(Long deviceId);
}
