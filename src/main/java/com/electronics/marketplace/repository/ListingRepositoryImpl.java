package com.electronics.marketplace.repository;

import com.electronics.marketplace.entity.*;
import com.electronics.marketplace.entity.enums.ListingCondition;
import com.electronics.marketplace.entity.enums.ListingStatus;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Triển khai ListingRepository với cơ chế dự phòng an toàn (Fail-safe In-Memory Store & JPA Ready).
 * Dùng Long id và java.util.Date theo chuẩn chốt của dự án.
 */
public class ListingRepositoryImpl implements ListingRepository {

    // Kho lưu trữ trong bộ nhớ dùng cho phiên làm việc hiện tại và demo không lỗi kết nối CSDL
    private static final Map<Long, Listing> memoryStore = new ConcurrentHashMap<>();
    private static final AtomicLong idCounter = new AtomicLong(100L);

    static {
        initSeedData();
    }

    private static void initSeedData() {
        // Tạo người bán mẫu (User)
        User sellerDuc = new User(1L, "Trần Minh Đức", "duc.tran@gmail.com", "0912 888 999", "Quận 1, TP. Hồ Chí Minh");
        sellerDuc.setCompletedSalesCount(14);

        User sellerHoa = new User(2L, "Nguyễn Thị Mai Hoa", "hoa.nguyen@gmail.com", "0987 654 321", "Cầu Giấy, Hà Nội");
        sellerHoa.setCompletedSalesCount(8);

        User sellerQuan = new User(3L, "Lê Hồng Quân", "quan.le@gmail.com", "0903 112 233", "Hải Châu, Đà Nẵng");
        sellerQuan.setCompletedSalesCount(5);

        // Tạo danh mục thiết bị mẫu (Device)
        Device devThinkpad = new Device(1L, "ThinkPad T14 Gen 2", "lenovo", "20W00049US", 2021, "Intel Core i5-1135G7 (4.2GHz)", "16GB DDR4", "512GB NVMe SSD");
        Device devMacbook = new Device(2L, "MacBook Air M1 2020", "apple", "MGN63SA/A", 2020, "Apple M1 8-Core CPU", "8GB Unified", "256GB SSD");
        Device devAcer = new Device(3L, "Acer Nitro 5 Gaming", "acer", "AN515-55", 2021, "Intel Core i7-10750H", "16GB DDR4", "512GB NVMe SSD");
        Device devDell = new Device(4L, "Dell XPS 13 9310", "dell", "9310-70231", 2021, "Intel Core i7-1165G7", "16GB LPDDR4x", "512GB NVMe SSD");
        Device devAsus = new Device(5L, "Asus ROG Zephyrus G14", "asus", "GA401QM", 2021, "AMD Ryzen 7 5800HS", "16GB DDR4", "512GB NVMe SSD");
        Device devHp = new Device(6L, "HP EliteBook 840 G8", "hp", "3G0R7PA", 2021, "Intel Core i5-1145G7", "16GB DDR4", "256GB NVMe SSD");

        // Tạo lỗi mẫu (RepairProblem)
        RepairProblem probSpeaker = new RepairProblem(1L, "Loa rè / Rách màng loa");
        RepairProblem probScreen = new RepairProblem(2L, "Điểm chết màn hình (Dead Pixel)");
        RepairProblem probBattery = new RepairProblem(3L, "Pin chai trên 30%");

        long now = System.currentTimeMillis();
        long oneDay = 24L * 60 * 60 * 1000;

        // 1. Listing 1: ThinkPad T14 Gen 2
        Listing l1 = new Listing(1L, sellerDuc, devThinkpad, ListingCondition.USED_GOOD,
                "Máy cá nhân dùng làm việc kỹ thuật văn phòng tại nhà, giữ gìn rất cẩn thận. Mặt A dán skin nhôm carbon, màn hình sáng đẹp không tì vết. Bàn phím nảy tốt, kèm sạc zin Type-C 65W.",
                new BigDecimal("12800000"));
        l1.setIsApproved(true);
        l1.addImage(new ListingImage("https://lh3.googleusercontent.com/aida-public/AB6AXuBhsfIBalXmMjhm1NLSQt6Qkz_OmWr2qcpKxJyrVsvza8nGYMeKenQaqdW_amrvTYf5pRjv8UBUSw4A0wDt_dUw7SWUxo34ZS_r0k0CZi43jMo1RTEUvkVGpIc1bVGAIMEgdhXDI33Vm-9hUhDYA6j_1l4U54e0k94ozBGr9ID-NWC-wAa72xeVo2ukKbWRZA0oPGhsaat6O_hva43CM99e-XhMkJ24anxVCuySREIaIgoX6VxWOSUx", 0));
        l1.addImage(new ListingImage("https://lh3.googleusercontent.com/aida-public/AB6AXuDhODs7XyBLSORa0vjBrDnTSGOgz8Itfi0qe9RFOUcklVa9bjz0kA_7DU3yhTNs-Ee-6MSYumRVJLeIXGDZ2XaGoDMrQ7JIgKHk5sFcpAyx7uyxu3fr-glBX_IXlrI0v-z8642VBFy7r4LzkZYv7BaWH4_q5MFpWFkgB8fMNzrJqfI93ggGVEPMJzmpUQKYX6xpXGvtSRotwdqMFu-LyW5TRy7hNn0wqzC4bICbCoYBb752oVC2YLQB", 1));
        l1.setCreatedAt(new Date(now - oneDay));
        memoryStore.put(1L, l1);

        // 2. Listing 2: MacBook Air M1 2020
        Listing l2 = new Listing(2L, sellerHoa, devMacbook, ListingCondition.USED_FAIR,
                "MacBook Air M1 bản Space Gray, máy xước nhẹ viền cạnh đáy do mang đi cà phê nhiều. Mọi chức năng TrueTone, Touch ID nhạy, pin còn 87% (310 chu kỳ sạc).",
                new BigDecimal("13900000"));
        l2.setIsApproved(true);
        l2.addImage(new ListingImage("https://lh3.googleusercontent.com/aida-public/AB6AXuBH9khgwGVRWUr7pveEsWsrcrRTf1hc0XxMk3WqBU-iNkQ9vLl30BXCO8V3_eIq9V84sBWEa3GMJ-xWKkOjRgw2QsmmZY_4EgL3lwZ9vmdiM4v--QRzytRbb7wbkmOPA1ReAhPrYvATcEMIVJsCGWhLzMOMMItNI1l3ylHgHoaDY9SgTGoQNVrgcQUlq7PtuBWIPMJociJyfylwJ_6R_fvDDJWh33Yv3Y2t91jx9b0", 0));
        l2.setCreatedAt(new Date(now - 2 * oneDay));
        memoryStore.put(2L, l2);

        // 3. Listing 3: Acer Nitro 5 (Cần sửa chữa / DAMAGED)
        Listing l3 = new Listing(3L, sellerQuan, devAcer, ListingCondition.DAMAGED,
                "Máy gaming cày đồ họa nhiều. Loa bên phải bị rè khi bật âm lượng trên 80%. Màn hình có 1 chấm sáng nhỏ ở góc dưới nhưng không ảnh hưởng chiến game. Bán rẻ cho anh em thợ hoặc về cắm tai nghe xài.",
                new BigDecimal("9900000"));
        l3.setIsApproved(true);
        l3.addImage(new ListingImage("https://lh3.googleusercontent.com/aida-public/AB6AXuDiUuFj9nirVJgi2UB2s0CpHLB99-isqHObkrQVP4tE1GH5-Dxrwyv-Ni21oPfk9Y-00bjxVqJTbDdpplGnae49oFLSUi207gyj8DbIRrMataGx5YbzEl_wKQkcBE9TRH42EVYW1fPTpyRY7MepF4YMpVt6u7yy8XOjVm6R1mlEhDFN-_Fk8evmVFBMcosnqg8_3ICZaqkfcLfJtXaXUrglKwf1EzJrPutJ_dvH58E", 0));
        l3.addProblem(new ListingProblem(l3, probSpeaker, "Loa phải rè khi volume > 80%"));
        l3.addProblem(new ListingProblem(l3, probScreen, "1 đốm sáng góc dưới bên phải"));
        l3.setCreatedAt(new Date(now - 3 * oneDay));
        memoryStore.put(3L, l3);

        // 4. Listing 4: Dell XPS 13 9310
        Listing l4 = new Listing(4L, sellerDuc, devDell, ListingCondition.USED_GOOD,
                "Dell XPS 13 viền vô cực InfinityEdge tuyệt đẹp, pin sạc 88 lần. Vỏ nhôm nguyên khối màu Platinum Silver, chiếu nghỉ tay sợi carbon không bong tróc.",
                new BigDecimal("16500000"));
        l4.setIsApproved(true);
        l4.addImage(new ListingImage("https://lh3.googleusercontent.com/aida-public/AB6AXuDSDK_Y1Tn41n44-PHzjN4HBUkeJo0v_OSMIby1f3oGoKS1XIgWMT0OQ7x-70QP5_J2MFN6k8tKhSjLkWka7UUtU190qsoRx8njivi8sfEnxOxj0eqC61J_i-nvl-El0uUx2un5gtrIJ0X82JdlkRINC2sMFwV9_sGOrsFS2Xrq_UeQ-yYBeTdWKy0wfJSICusiILvie4qLItiyxiLfxq_uz5a50m5ER5NzpuL4wsU", 0));
        l4.setCreatedAt(new Date(now - 4 * oneDay));
        memoryStore.put(4L, l4);

        // 5. Listing 5: Asus ROG Zephyrus G14
        Listing l5 = new Listing(5L, sellerQuan, devAsus, ListingCondition.USED_GOOD,
                "Asus ROG Zephyrus G14 màu Moonlight White, ngoại hình đẹp 99%. Đầy đủ sạc zin 180W. Máy mát, hiệu năng cực khỏe làm Photoshop và Premiere mượt mà.",
                new BigDecimal("18200000"));
        l5.setIsApproved(true);
        l5.addImage(new ListingImage("https://lh3.googleusercontent.com/aida-public/AB6AXuDfW4DtavcquK1SUWDVWq4B-j-lm9TuqguAQN4Xa6MQg4U4aiGZnrk7v3ldHxLGKSeCe8M5zgisMD067mW34tC5KAdqFcN_v98c15IBo7SF0WlHF0EA-qXE2BsIcR-_VzT0LHni0Dr-Tlk-bNzyvpNTtHWVtuTOIxw8cQTE2qfcWCPYRDdie9vqz-Is7Wax6QMcB-0Odv_pp0ewcZPHPH8VU788RLkdImm_NIV1pXw", 0));
        l5.setCreatedAt(new Date(now - 5 * oneDay));
        memoryStore.put(5L, l5);

        // 6. Listing 6: HP EliteBook 840 G8
        Listing l6 = new Listing(6L, sellerHoa, devHp, ListingCondition.USED_FAIR,
                "Dòng doanh nhân siêu bền HP EliteBook. Ngoại hình 95%, bàn phím chống tràn nước tốt, loa Bang & Olufsen nghe nhạc rất hay.",
                new BigDecimal("10500000"));
        l6.setIsApproved(true);
        l6.addImage(new ListingImage("https://lh3.googleusercontent.com/aida-public/AB6AXuBhsfIBalXmMjhm1NLSQt6Qkz_OmWr2qcpKxJyrVsvza8nGYMeKenQaqdW_amrvTYf5pRjv8UBUSw4A0wDt_dUw7SWUxo34ZS_r0k0CZi43jMo1RTEUvkVGpIc1bVGAIMEgdhXDI33Vm-9hUhDYA6j_1l4U54e0k94ozBGr9ID-NWC-wAa72xeVo2ukKbWRZA0oPGhsaat6O_hva43CM99e-XhMkJ24anxVCuySREIaIgoX6VxWOSUx", 0));
        l6.setCreatedAt(new Date(now - 6 * oneDay));
        memoryStore.put(6L, l6);
    }

    @Override
    public List<Listing> findAllAvailable() {
        // Lọc theo BR-15: status == AVAILABLE && isApproved == true && isRemovedByAdmin == false
        return memoryStore.values().stream()
                .filter(l -> l.getStatus() == ListingStatus.AVAILABLE
                        && Boolean.TRUE.equals(l.getIsApproved())
                        && !Boolean.TRUE.equals(l.getIsRemovedByAdmin()))
                .sorted(Comparator.comparing(Listing::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Listing> searchAndFilter(String keyword, String brand, ListingCondition condition,
                                          BigDecimal minPrice, BigDecimal maxPrice, String ram) {
        return findAllAvailable().stream()
                .filter(l -> {
                    // Lọc theo từ khóa
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String kw = keyword.toLowerCase().trim();
                        boolean matchName = l.getDevice() != null && l.getDevice().getName() != null 
                                && l.getDevice().getName().toLowerCase().contains(kw);
                        boolean matchDesc = l.getDescription() != null 
                                && l.getDescription().toLowerCase().contains(kw);
                        boolean matchModel = l.getDevice() != null && l.getDevice().getModel() != null
                                && l.getDevice().getModel().toLowerCase().contains(kw);
                        boolean matchBrand = l.getDevice() != null && l.getDevice().getBrand() != null
                                && l.getDevice().getBrand().toLowerCase().contains(kw);
                        boolean matchSeller = l.getSeller() != null && l.getSeller().getName() != null
                                && l.getSeller().getName().toLowerCase().contains(kw);
                        if (!matchName && !matchDesc && !matchModel && !matchBrand && !matchSeller) {
                            return false;
                        }
                    }

                    // Lọc theo thương hiệu (Brand) - Hỗ trợ cả tên thương hiệu lẫn dòng máy
                    if (brand != null && !brand.trim().isEmpty()) {
                        String b = brand.trim().toLowerCase();
                        if (l.getDevice() == null) {
                            return false;
                        }
                        String devBrand = l.getDevice().getBrand() != null ? l.getDevice().getBrand().toLowerCase() : "";
                        String devName = l.getDevice().getName() != null ? l.getDevice().getName().toLowerCase() : "";
                        boolean matchBrand = devBrand.contains(b) || devName.contains(b);
                        if ("thinkpad".equals(b) || "lenovo".equals(b)) {
                            matchBrand = matchBrand || devBrand.contains("lenovo") || devName.contains("thinkpad");
                        }
                        if (!matchBrand) {
                            return false;
                        }
                    }

                    // Lọc theo tình trạng (Condition)
                    if (condition != null) {
                        if (l.getCondition() != condition) {
                            return false;
                        }
                    }

                    // Lọc theo khoảng giá (Price Range)
                    if (minPrice != null && l.getPrice().compareTo(minPrice) < 0) {
                        return false;
                    }
                    if (maxPrice != null && l.getPrice().compareTo(maxPrice) > 0) {
                        return false;
                    }

                    // Lọc theo RAM
                    if (ram != null && !ram.trim().isEmpty()) {
                        String r = ram.trim().toLowerCase();
                        if (l.getDevice() == null || l.getDevice().getRam() == null) {
                            return false;
                        }
                        String devRam = l.getDevice().getRam().toLowerCase();
                        boolean matchRam = devRam.contains(r + "gb") || devRam.contains(r);
                        if (!matchRam) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Listing findById(Long id) {
        if (id == null) {
            return null;
        }
        return memoryStore.get(id);
    }

    @Override
    public List<Listing> findBySellerId(Long sellerId) {
        if (sellerId == null) {
            return Collections.emptyList();
        }
        return memoryStore.values().stream()
                .filter(l -> l.getSeller() != null && sellerId.equals(l.getSeller().getId()))
                .sorted(Comparator.comparing(Listing::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public Listing save(Listing listing) {
        if (listing.getId() == null) {
            listing.setId(idCounter.incrementAndGet());
        }
        if (listing.getCreatedAt() == null) {
            listing.setCreatedAt(new Date());
        }
        listing.setUpdatedAt(new Date());
        memoryStore.put(listing.getId(), listing);
        return listing;
    }

    @Override
    public Listing update(Listing listing) {
        if (listing == null || listing.getId() == null) {
            return null;
        }
        listing.setUpdatedAt(new Date());
        memoryStore.put(listing.getId(), listing);
        return listing;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return memoryStore.remove(id) != null;
    }
}
