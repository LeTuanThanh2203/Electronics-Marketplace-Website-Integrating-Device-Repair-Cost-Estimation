package com.electronics.marketplace.service;

import com.electronics.marketplace.entity.Listing;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.ListingCondition;
import com.electronics.marketplace.entity.enums.ListingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
        List<Listing> lenovoGood = service.getAvailableMarketplaceListings(null, "lenovo", "USED_GOOD", null, null);
        assertNotNull(lenovoGood);
        for (Listing l : lenovoGood) {
            assertTrue("lenovo".equalsIgnoreCase(l.getDevice().getBrand()));
            assertEquals(ListingCondition.USED_GOOD, l.getCondition());
        }
    }

    @Test
    @DisplayName("BR-08: Bắt buộc chọn tình trạng khi đăng tin")
    void testCreateListingRequiresCondition() {
        User seller = new User(1, "Test Seller", "seller@test.com", "0900000000", "TP.HCM");
        assertThrows(IllegalArgumentException.class, () -> {
            service.createListing(seller, 1, null, "Mô tả test", new BigDecimal("10000000"),
                    "http://image.jpg", "Lenovo", "T14", "i5", "16GB", "512GB", Collections.emptyList());
        });
    }

    @Test
    @DisplayName("BR-12: Tạo tin đăng thành công đính kèm hình ảnh và thông số")
    void testCreateListingSuccess() {
        User seller = new User(1, "Test Seller", "seller@test.com", "0900000000", "TP.HCM");
        Listing created = service.createListing(
                seller, 1, "USED_GOOD", "Máy văn phòng cực đẹp", new BigDecimal("12500000"),
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
    @DisplayName("BR-11: Người khác không được quyền sửa tin của seller")
    void testUpdateListingPermissionCheck() {
        assertThrows(SecurityException.class, () -> {
            // Tin số 1 thuộc về seller ID 1, người dùng ID 999 cố ý sửa
            service.updateListing(1, 999, "USED_FAIR", "Cố ý sửa", new BigDecimal("5000000"), null);
        });
    }

    @Test
    @DisplayName("BR-33: Xem số điện thoại người bán yêu cầu đăng nhập")
    void testGetSellerPhoneRequiresLogin() {
        assertThrows(SecurityException.class, () -> {
            service.getSellerPhone(1, null);
        });

        User buyer = new User(10, "Buyer", "buyer@gmail.com", "0911111111", "Hà Nội");
        String phone = service.getSellerPhone(1, buyer);
        assertNotNull(phone);
        assertFalse(phone.isEmpty());
    }

    @Test
    @DisplayName("Bộ lọc: Lọc theo từ khóa (Keyword)")
    void testFilterByKeyword() {
        List<Listing> result = service.getAvailableMarketplaceListings("ThinkPad", null, null, null, null);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(l -> l.getDevice().getName().contains("ThinkPad")));

        List<Listing> noMatch = service.getAvailableMarketplaceListings("NonExistentLaptop999", null, null, null, null);
        assertTrue(noMatch.isEmpty(), "Từ khóa không tồn tại phải trả về danh sách rỗng");
    }

    @Test
    @DisplayName("Bộ lọc: Lọc theo mức giá (Price Range)")
    void testFilterByPriceRange() {
        // u10: Dưới 10 triệu (phải có Acer Nitro 5 giá 9.9tr)
        List<Listing> under10M = service.getAvailableMarketplaceListings(null, null, null, "u10", null);
        assertFalse(under10M.isEmpty());
        assertTrue(under10M.stream().allMatch(l -> l.getPrice().compareTo(new BigDecimal("10000000")) <= 0));

        // 10-15: 10 - 15 triệu (ThinkPad 12.8tr, MacBook Air 13.9tr, HP 10.5tr)
        List<Listing> range10to15 = service.getAvailableMarketplaceListings(null, null, null, "10-15", null);
        assertFalse(range10to15.isEmpty());
        assertTrue(range10to15.stream().allMatch(l -> 
                l.getPrice().compareTo(new BigDecimal("10000000")) >= 0 && 
                l.getPrice().compareTo(new BigDecimal("15000000")) <= 0));

        // 15-20: 15 - 20 triệu (Dell XPS 16.5tr, Asus ROG 18.2tr)
        List<Listing> range15to20 = service.getAvailableMarketplaceListings(null, null, null, "15-20", null);
        assertFalse(range15to20.isEmpty());
        assertTrue(range15to20.stream().allMatch(l -> 
                l.getPrice().compareTo(new BigDecimal("15000000")) >= 0 && 
                l.getPrice().compareTo(new BigDecimal("20000000")) <= 0));
    }

    @Test
    @DisplayName("Bộ lọc: Lọc theo RAM")
    void testFilterByRam() {
        List<Listing> ram8 = service.getAvailableMarketplaceListings(null, null, null, null, "8");
        assertFalse(ram8.isEmpty());
        assertTrue(ram8.stream().anyMatch(l -> l.getDevice().getRam().contains("8")));

        List<Listing> ram16 = service.getAvailableMarketplaceListings(null, null, null, null, "16");
        assertFalse(ram16.isEmpty());
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
