package com.electronics.marketplace.service;

import com.electronics.marketplace.entity.*;
import com.electronics.marketplace.entity.enums.ListingCondition;
import com.electronics.marketplace.entity.enums.ListingStatus;
import com.electronics.marketplace.repository.ListingRepository;
import com.electronics.marketplace.repository.ListingRepositoryImpl;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service xử lý toàn bộ Business Logic cho kênh Chợ Đồ Cũ (C2C Marketplace) - Task của Thành.
 * Dùng Long id theo chuẩn chốt của dự án.
 */
public class MarketplaceService {

    private final ListingRepository listingRepository;

    public MarketplaceService() {
        this.listingRepository = new ListingRepositoryImpl();
    }

    public MarketplaceService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * Lấy danh sách máy cũ khả dụng cho trang Chợ Đồ Cũ kèm theo lọc đa tiêu chí
     */
    public List<Listing> getAvailableMarketplaceListings(String keyword, String brand, 
                                                         String conditionStr, String priceRange, String ram) {
        ListingCondition condition = null;
        if (conditionStr != null && !conditionStr.trim().isEmpty()) {
            try {
                condition = ListingCondition.valueOf(conditionStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        if (priceRange != null && !priceRange.trim().isEmpty()) {
            switch (priceRange.trim().toLowerCase()) {
                case "u10":
                    maxPrice = new BigDecimal("10000000");
                    break;
                case "10-15":
                    minPrice = new BigDecimal("10000000");
                    maxPrice = new BigDecimal("15000000");
                    break;
                case "15-20":
                    minPrice = new BigDecimal("15000000");
                    maxPrice = new BigDecimal("20000000");
                    break;
                case "o20":
                    minPrice = new BigDecimal("20000000");
                    break;
            }
        }

        return listingRepository.searchAndFilter(keyword, brand, condition, minPrice, maxPrice, ram);
    }

    /**
     * Lấy thông tin chi tiết một bài đăng
     * Nếu không truyền id hoặc không tìm thấy, fallback về listing mẫu số 1 để demo luôn hiển thị đẹp
     */
    public Listing getListingDetail(Long id) {
        if (id == null) {
            id = 1L;
        }
        Listing listing = listingRepository.findById(id);
        if (listing == null) {
            listing = listingRepository.findById(1L);
        }
        return listing;
    }

    public static final String DEFAULT_FALLBACK_IMAGE = "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&auto=format&fit=crop&q=80";

    /**
     * Tạo mới bài đăng bán máy cũ hỗ trợ danh sách nhiều hình ảnh thực tế (BR-12).
     */
    public Listing createListing(User seller, Long deviceId, String conditionStr,
                                 String description, BigDecimal price, List<String> imageUrls,
                                 String brand, String model, String cpu, String ram, String storage,
                                 List<String> problemNames) {
        // Kiểm tra BR-08
        if (conditionStr == null || conditionStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn tình trạng thiết bị (BR-08)!");
        }
        ListingCondition condition = ListingCondition.valueOf(conditionStr.trim().toUpperCase());

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            price = new BigDecimal("10000000");
        }

        if (seller == null) {
            seller = new User(1L, "Trần Minh Đức (Demo Seller)", "duc.tran@gmail.com", "0912 888 999", "TP. Hồ Chí Minh");
        }

        // Tạo hoặc liên kết Device linh hoạt (không cố định dòng máy)
        String devBrand = (brand != null && !brand.trim().isEmpty()) ? brand.trim() : "Khác";
        String devModel = (model != null && !model.trim().isEmpty()) ? model.trim() : "Laptop";
        String devName = devBrand + " " + devModel;
        Long devId = (deviceId != null && deviceId > 0) ? deviceId : null;
        Device device = new Device(devBrand, "", devModel, 2022, cpu, ram, storage);
        device.setId(devId);
        device.setName(devName);

        Listing listing = new Listing(null, seller, device, condition, description, price);

        // Thêm danh sách hình ảnh thực tế (tối thiểu 1 ảnh theo BR-12)
        if (imageUrls != null && !imageUrls.isEmpty()) {
            int order = 0;
            for (String imgUrl : imageUrls) {
                if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                    listing.addImage(new ListingImage(listing, imgUrl.trim(), order++));
                }
            }
        }
        if (listing.getImages().isEmpty()) {
            listing.addImage(new ListingImage(listing, DEFAULT_FALLBACK_IMAGE, 0));
        }

        // Nếu là Admin đăng tin, tự động duyệt tin ngay
        if (seller != null && seller.getRole() != null && "ADMIN".equalsIgnoreCase(seller.getRole().name())) {
            listing.setIsApproved(true);
        }

        // Thêm các lỗi khai báo nếu có
        if (problemNames != null && !problemNames.isEmpty()) {
            for (String pName : problemNames) {
                RepairProblem rp = new RepairProblem(null, pName);
                listing.addProblem(new ListingProblem(listing, rp, "Phát hiện khi kiểm tra"));
            }
        }

        return listingRepository.save(listing);
    }

    /**
     * Overload tạo mới bài đăng cho trường hợp 1 ảnh chuỗi (Tương thích ngược)
     */
    public Listing createListing(User seller, Long deviceId, String conditionStr,
                                 String description, BigDecimal price, String imageUrl,
                                 String brand, String model, String cpu, String ram, String storage,
                                 List<String> problemNames) {
        List<String> urls = new ArrayList<>();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            urls.add(imageUrl.trim());
        }
        return createListing(seller, deviceId, conditionStr, description, price, urls,
                brand, model, cpu, ram, storage, problemNames);
    }

    /**
     * Cập nhật tin đăng hỗ trợ danh sách nhiều ảnh theo BR-11 và BR-40:
     * Chỉ chủ sở hữu (sellerId == currentUserId) mới được sửa, và chỉ khi trạng thái là AVAILABLE hoặc HIDDEN
     */
    public boolean updateListing(Long listingId, Long currentUserId, String conditionStr,
                                 String description, BigDecimal price, List<String> imageUrls) {
        Listing listing = listingRepository.findById(listingId);
        if (listing == null) {
            return false;
        }

        // BR-11: Kiểm tra quyền sở hữu
        if (listing.getSeller() != null && !listing.getSeller().getId().equals(currentUserId)) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa tin đăng của người khác (BR-11)!");
        }

        // BR-40: Không được sửa tin khi đã RESERVED hoặc SOLD
        if (listing.getStatus() != ListingStatus.AVAILABLE && listing.getStatus() != ListingStatus.HIDDEN) {
            throw new IllegalStateException("Không thể chỉnh sửa tin đăng đã được giữ chỗ hoặc đã bán (BR-40)!");
        }

        if (conditionStr != null && !conditionStr.trim().isEmpty()) {
            listing.setCondition(ListingCondition.valueOf(conditionStr.trim().toUpperCase()));
        }
        if (description != null) {
            listing.setDescription(description);
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            listing.setPrice(price);
        }
        if (imageUrls != null) {
            listing.getImages().clear();
            if (!imageUrls.isEmpty()) {
                int order = 0;
                for (String url : imageUrls) {
                    if (url != null && !url.trim().isEmpty()) {
                        listing.addImage(new ListingImage(listing, url.trim(), order++));
                    }
                }
            } else {
                listing.addImage(new ListingImage(listing, DEFAULT_FALLBACK_IMAGE, 0));
            }
        }

        listingRepository.update(listing);
        return true;
    }



    /**
     * Ẩn tin bán (Chuyển trạng thái sang HIDDEN)
     */
    public boolean hideListing(Long listingId, Long currentUserId) {
        Listing listing = listingRepository.findById(listingId);
        if (listing == null) {
            return false;
        }
        if (listing.getSeller() != null && !listing.getSeller().getId().equals(currentUserId)) {
            throw new SecurityException("Chỉ người bán mới có quyền ẩn bài đăng (BR-11)!");
        }
        listing.setStatus(ListingStatus.HIDDEN);
        listingRepository.update(listing);
        return true;
    }

    /**
     * Xóa tin bán
     */
    public boolean deleteListing(Long listingId, Long currentUserId) {
        Listing listing = listingRepository.findById(listingId);
        if (listing == null) {
            return false;
        }
        if (listing.getSeller() != null && !listing.getSeller().getId().equals(currentUserId)) {
            throw new SecurityException("Chỉ người bán mới có quyền xóa bài đăng (BR-11)!");
        }
        return listingRepository.delete(listingId);
    }

    /**
     * Lấy số điện thoại người bán theo BR-33, BR-34, BR-35:
     * - Buyer phải được đăng nhập (currentUser != null).
     * - Việc xem số KHÔNG tự động reserve hay tạo đơn hàng.
     */
    public String getSellerPhone(Long listingId, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Yêu cầu đăng nhập để xem thông tin liên hệ của người bán (BR-33)!");
        }
        Listing listing = listingRepository.findById(listingId);
        if (listing == null || listing.getSeller() == null) {
            return "0912 888 999";
        }
        return listing.getSeller().getPhone();
    }
}
