package com.electronics.marketplace.service;

import com.electronics.marketplace.entity.Listing;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.ListingCondition;
import com.electronics.marketplace.entity.enums.ListingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử đơn vị cho MarketplaceService theo đặc tả Business Rules (BR) của dự án.
 */
class MarketplaceServiceTest {

    private MarketplaceService service;

    @BeforeEach
    void setUp() {
        service = new MarketplaceService();
    }

    @Test
    @DisplayName("BR-15: Lấy danh sách máy cũ khả dụng thành công")
    void testGetAvailableMarketplaceListings() {
        List<Listing> listings = service.getAvailableMarketplaceListings(null, null, null, null, null);
        assertNotNull(listings);
        assertFalse(listings.isEmpty(), "Danh sách bài đăng khả dụng không được rỗng");
        
        // Mọi bài đăng phải có status == AVAILABLE, isApproved == true, isRemovedByAdmin == false
        for (Listing l : listings) {
            assertEquals(ListingStatus.AVAILABLE, l.getStatus());
            assertTrue(l.getIsApproved());
            assertFalse(l.getIsRemovedByAdmin());
        }
    }

    @Test
    @DisplayName("Lọc theo thương hiệu và tình trạng")
    void testFilterByBrandAndCondition() {
        List<Listing> available = service.getAvailableMarketplaceListings(null, null, null, null, null);
        assertFalse(available.isEmpty());
        Listing sample = available.get(0);
        String brand = sample.getDevice().getBrand();
        String condition = sample.getCondition().name();

        List<Listing> filtered = service.getAvailableMarketplaceListings(null, brand, condition, null, null);
        assertNotNull(filtered);
        assertFalse(filtered.isEmpty());
        for (Listing l : filtered) {
            assertTrue(l.getDevice().getBrand().equalsIgnoreCase(brand) || l.getDevice().getName().toLowerCase().contains(brand.toLowerCase()));
            assertEquals(sample.getCondition(), l.getCondition());
        }
    }

    @Test
    @DisplayName("BR-08: Bắt buộc chọn tình trạng khi đăng tin")
    void testCreateListingRequiresCondition() {
        User seller = new User(1L, "Test Seller", "seller@test.com", "0900000000", "TP.HCM");
        assertThrows(IllegalArgumentException.class, () -> {
            service.createListing(seller, 1L, null, "Mô tả test", new BigDecimal("10000000"),
                    "http://image.jpg", "Lenovo", "T14", "i5", "16GB", "512GB", Collections.emptyList());
        });
    }

    @Test
    @DisplayName("BR-12: Tạo tin đăng thành công đính kèm hình ảnh và thông số")
    void testCreateListingSuccess() {
        User seller = new User(1L, "Test Seller", "seller@test.com", "0900000000", "TP.HCM");
        Listing created = service.createListing(
                seller, 1L, "USED_GOOD", "Máy văn phòng cực đẹp", new BigDecimal("12500000"),
                "https://example.com/laptop.jpg", "Lenovo", "ThinkPad T14", "Core i5", "16GB", "512GB",
                Collections.singletonList("Trầy xước nhẹ")
        );

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(ListingCondition.USED_GOOD, created.getCondition());
        assertEquals(ListingStatus.AVAILABLE, created.getStatus());
        assertFalse(created.getImages().isEmpty());
    }

    @Test
    @DisplayName("Hỗ trợ tải lên nhiều hình ảnh thực tế cho một bài đăng")
    void testCreateListingWithMultipleImages() {
        User seller = new User(1L, "Test Seller", "seller@test.com", "0900000000", "TP.HCM");
        List<String> images = Arrays.asList(
                "https://example.com/img1.jpg",
                "https://example.com/img2.jpg",
                "https://example.com/img3.jpg"
        );
        Listing created = service.createListing(
                seller, 1L, "USED_GOOD", "Máy đa góc chụp", new BigDecimal("15000000"),
                images, "Apple", "MacBook Pro", "M2", "16GB", "512GB",
                Collections.emptyList()
        );

        assertNotNull(created);
        assertEquals(3, created.getImages().size());
        assertEquals("https://example.com/img1.jpg", created.getMainImageUrl());
    }

    @Test
    @DisplayName("BR-11: Người khác không được quyền sửa tin của seller")
    void testUpdateListingPermissionCheck() {
        assertThrows(SecurityException.class, () -> {
            // Tin số 1 thuộc về seller ID 1, người dùng ID 999 cố ý sửa
            service.updateListing(1L, 999L, "USED_FAIR", "Cố ý sửa", new BigDecimal("5000000"), null);
        });
    }

    @Test
    @DisplayName("BR-33: Xem số điện thoại người bán yêu cầu đăng nhập")
    void testGetSellerPhoneRequiresLogin() {
        assertThrows(SecurityException.class, () -> {
            service.getSellerPhone(1L, null);
        });

        User buyer = new User(10L, "Buyer", "buyer@gmail.com", "0911111111", "Hà Nội");
        String phone = service.getSellerPhone(1L, buyer);
        assertNotNull(phone);
        assertFalse(phone.isEmpty());
    }

    @Test
    @DisplayName("Bộ lọc: Lọc theo từ khóa (Keyword)")
    void testFilterByKeyword() {
        List<Listing> all = service.getAvailableMarketplaceListings(null, null, null, null, null);
        assertFalse(all.isEmpty(), "Cần có dữ liệu bài đăng để test từ khóa");
        String keyword = all.get(0).getDevice().getName().split(" ")[0];

        List<Listing> result = service.getAvailableMarketplaceListings(keyword, null, null, null, null);
        assertFalse(result.isEmpty(), "Tìm kiếm với từ khóa '" + keyword + "' phải có kết quả");
        assertTrue(result.stream().anyMatch(l -> 
                (l.getDevice() != null && l.getDevice().getName() != null && l.getDevice().getName().toLowerCase().contains(keyword.toLowerCase())) ||
                (l.getDescription() != null && l.getDescription().toLowerCase().contains(keyword.toLowerCase()))
        ));

        List<Listing> noMatch = service.getAvailableMarketplaceListings("NonExistentLaptop999", null, null, null, null);
        assertTrue(noMatch.isEmpty(), "Từ khóa không tồn tại phải trả về danh sách rỗng");
    }

    @Test
    @DisplayName("Bộ lọc: Lọc theo mức giá (Price Range)")
    void testFilterByPriceRange() {
        // Kiểm tra phân khúc 15-20 triệu
        List<Listing> range15to20 = service.getAvailableMarketplaceListings(null, null, null, "15-20", null);
        if (!range15to20.isEmpty()) {
            assertTrue(range15to20.stream().allMatch(l -> 
                    l.getPrice().compareTo(new BigDecimal("15000000")) >= 0 && 
                    l.getPrice().compareTo(new BigDecimal("20000000")) <= 0));
        }

        // Kiểm tra phân khúc 10-15 triệu
        List<Listing> range10to15 = service.getAvailableMarketplaceListings(null, null, null, "10-15", null);
        if (!range10to15.isEmpty()) {
            assertTrue(range10to15.stream().allMatch(l -> 
                    l.getPrice().compareTo(new BigDecimal("10000000")) >= 0 && 
                    l.getPrice().compareTo(new BigDecimal("15000000")) <= 0));
        }

        // Tối thiểu một trong các khoảng giá chuẩn phải có kết quả
        List<Listing> anyRange = !range15to20.isEmpty() ? range15to20 : range10to15;
        assertFalse(anyRange.isEmpty(), "Ít nhất một khoảng giá phổ biến phải có máy");
    }

    @Test
    @DisplayName("Bộ lọc: Lọc theo RAM")
    void testFilterByRam() {
        List<Listing> ram16 = service.getAvailableMarketplaceListings(null, null, null, null, "16");
        assertFalse(ram16.isEmpty(), "Dữ liệu mẫu phải có máy RAM 16GB");
        assertTrue(ram16.stream().allMatch(l -> l.getDevice().getRam().contains("16")));
    }

    @Test
    @DisplayName("Bộ lọc: Lọc máy cũ có tình trạng hư hỏng linh kiện (DAMAGED)")
    void testFilterByConditionDamaged() {
        List<Listing> damaged = service.getAvailableMarketplaceListings(null, null, "DAMAGED", null, null);
        assertFalse(damaged.isEmpty());
        assertTrue(damaged.stream().allMatch(l -> l.getCondition() == ListingCondition.DAMAGED));
    }
}
