package com.electronics.marketplace.repository;

import com.electronics.marketplace.config.JpaConfig;
import com.electronics.marketplace.entity.*;
import com.electronics.marketplace.entity.enums.ListingCondition;
import com.electronics.marketplace.entity.enums.ListingStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Triển khai ListingRepository với JPA / Hibernate kết nối PostgreSQL thật
 * và có cơ chế fallback in-memory an toàn (Fail-safe Store).
 * Tuân thủ Rule 2: Chuẩn hóa truy xuất CSDL qua tầng DAO/Repository.
 */
public class ListingRepositoryImpl implements ListingRepository {

    // Kho lưu trữ trong bộ nhớ dùng cho fallback khi CSDL chưa bật hoặc kiểm thử unit test
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

        // 3. Listing 3: Acer Nitro 5
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

    private EntityManager getEntityManagerSafely() {
        try {
            EntityManagerFactory emf = JpaConfig.getEntityManagerFactory();
            if (emf != null && emf.isOpen()) {
                return emf.createEntityManager();
            }
        } catch (Exception ignored) {
            // Database chưa kết nối được
        }
        return null;
    }

    @Override
    public List<Listing> findAllAvailable() {
        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            try {
                String jpql = "SELECT DISTINCT l FROM Listing l " +
                        "LEFT JOIN FETCH l.device " +
                        "LEFT JOIN FETCH l.seller " +
                        "LEFT JOIN FETCH l.images " +
                        "WHERE l.status = :status AND l.isApproved = true AND (l.isRemovedByAdmin IS NULL OR l.isRemovedByAdmin = false) " +
                        "ORDER BY l.createdAt DESC";
                TypedQuery<Listing> query = em.createQuery(jpql, Listing.class);
                query.setParameter("status", ListingStatus.AVAILABLE);
                List<Listing> list = query.getResultList();
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception e) {
                System.err.println("JPA findAllAvailable warning: " + e.getMessage());
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        // Fallback in-memory
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
        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            try {
                StringBuilder jpql = new StringBuilder(
                        "SELECT DISTINCT l FROM Listing l " +
                        "LEFT JOIN FETCH l.device d " +
                        "LEFT JOIN FETCH l.seller s " +
                        "LEFT JOIN FETCH l.images " +
                        "WHERE l.status = :status AND l.isApproved = true AND (l.isRemovedByAdmin IS NULL OR l.isRemovedByAdmin = false)"
                );
                Map<String, Object> params = new HashMap<>();
                params.put("status", ListingStatus.AVAILABLE);

                if (brand != null && !brand.trim().isEmpty()) {
                    String b = brand.trim().toLowerCase();
                    if ("thinkpad".equals(b) || "lenovo".equals(b)) {
                        jpql.append(" AND (LOWER(d.brand) LIKE :lenovo OR LOWER(d.name) LIKE :thinkpad)");
                        params.put("lenovo", "%lenovo%");
                        params.put("thinkpad", "%thinkpad%");
                    } else {
                        jpql.append(" AND (LOWER(d.brand) LIKE :brand OR LOWER(d.name) LIKE :brand)");
                        params.put("brand", "%" + b + "%");
                    }
                }
                if (condition != null) {
                    jpql.append(" AND l.condition = :condition");
                    params.put("condition", condition);
                }
                if (minPrice != null) {
                    jpql.append(" AND l.price >= :minPrice");
                    params.put("minPrice", minPrice);
                }
                if (maxPrice != null) {
                    jpql.append(" AND l.price <= :maxPrice");
                    params.put("maxPrice", maxPrice);
                }
                if (ram != null && !ram.trim().isEmpty()) {
                    jpql.append(" AND LOWER(d.ram) LIKE :ram");
                    params.put("ram", "%" + ram.trim().toLowerCase() + "%");
                }
                if (keyword != null && !keyword.trim().isEmpty()) {
                    jpql.append(" AND (LOWER(d.name) LIKE :kw OR LOWER(d.model) LIKE :kw OR LOWER(l.description) LIKE :kw OR LOWER(s.name) LIKE :kw)");
                    params.put("kw", "%" + keyword.trim().toLowerCase() + "%");
                }
                jpql.append(" ORDER BY l.createdAt DESC");

                TypedQuery<Listing> query = em.createQuery(jpql.toString(), Listing.class);
                params.forEach(query::setParameter);
                List<Listing> list = query.getResultList();
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception e) {
                System.err.println("JPA searchAndFilter warning: " + e.getMessage());
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        // Fallback in-memory
        return findAllAvailable().stream()
                .filter(l -> {
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

                    if (brand != null && !brand.trim().isEmpty()) {
                        String b = brand.trim().toLowerCase();
                        if (l.getDevice() == null) return false;
                        String devBrand = l.getDevice().getBrand() != null ? l.getDevice().getBrand().toLowerCase() : "";
                        String devName = l.getDevice().getName() != null ? l.getDevice().getName().toLowerCase() : "";
                        boolean matchBrand = devBrand.contains(b) || devName.contains(b);
                        if ("thinkpad".equals(b) || "lenovo".equals(b)) {
                            matchBrand = matchBrand || devBrand.contains("lenovo") || devName.contains("thinkpad");
                        }
                        if (!matchBrand) return false;
                    }

                    if (condition != null && l.getCondition() != condition) return false;
                    if (minPrice != null && l.getPrice().compareTo(minPrice) < 0) return false;
                    if (maxPrice != null && l.getPrice().compareTo(maxPrice) > 0) return false;

                    if (ram != null && !ram.trim().isEmpty()) {
                        String r = ram.trim().toLowerCase();
                        if (l.getDevice() == null || l.getDevice().getRam() == null) return false;
                        String devRam = l.getDevice().getRam().toLowerCase();
                        boolean matchRam = devRam.contains(r + "gb") || devRam.contains(r);
                        if (!matchRam) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Listing findById(Long id) {
        if (id == null) return null;

        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            try {
                String jpql = "SELECT DISTINCT l FROM Listing l " +
                        "LEFT JOIN FETCH l.device " +
                        "LEFT JOIN FETCH l.seller " +
                        "LEFT JOIN FETCH l.images " +
                        "WHERE l.id = :id";
                List<Listing> list = em.createQuery(jpql, Listing.class)
                        .setParameter("id", id)
                        .getResultList();
                if (!list.isEmpty()) {
                    Listing l = list.get(0);
                    try {
                        em.createQuery("SELECT DISTINCT l FROM Listing l LEFT JOIN FETCH l.problems lp LEFT JOIN FETCH lp.problem WHERE l.id = :id", Listing.class)
                                .setParameter("id", id)
                                .getResultList();
                    } catch (Exception ignored) {
                    }
                    return l;
                }
            } catch (Exception e) {
                System.err.println("JPA findById warning: " + e.getMessage());
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        return memoryStore.get(id);
    }

    @Override
    public List<Listing> findBySellerId(Long sellerId) {
        if (sellerId == null) return Collections.emptyList();

        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            try {
                String jpql = "SELECT DISTINCT l FROM Listing l " +
                        "LEFT JOIN FETCH l.device " +
                        "LEFT JOIN FETCH l.seller " +
                        "LEFT JOIN FETCH l.images " +
                        "WHERE l.seller.id = :sellerId " +
                        "ORDER BY l.createdAt DESC";
                List<Listing> list = em.createQuery(jpql, Listing.class)
                        .setParameter("sellerId", sellerId)
                        .getResultList();
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception e) {
                System.err.println("JPA findBySellerId warning: " + e.getMessage());
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        return memoryStore.values().stream()
                .filter(l -> l.getSeller() != null && sellerId.equals(l.getSeller().getId()))
                .sorted(Comparator.comparing(Listing::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public Listing save(Listing listing) {
        if (listing.getCreatedAt() == null) {
            listing.setCreatedAt(new Date());
        }
        listing.setUpdatedAt(new Date());

        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                // 1. Gắn seller thật trong DB
                User seller = listing.getSeller();
                if (seller != null && seller.getId() != null) {
                    User dbUser = em.find(User.class, seller.getId());
                    if (dbUser != null) {
                        listing.setSeller(dbUser);
                    } else {
                        List<User> users = em.createQuery("SELECT u FROM User u", User.class).setMaxResults(1).getResultList();
                        if (!users.isEmpty()) {
                            listing.setSeller(users.get(0));
                        }
                    }
                }

                // 2. Gắn device thật trong DB
                Device dev = listing.getDevice();
                if (dev != null) {
                    Device dbDev = null;
                    if (dev.getId() != null && dev.getId() > 0) {
                        dbDev = em.find(Device.class, dev.getId());
                    }
                    if (dbDev == null && dev.getBrand() != null && dev.getModel() != null) {
                        List<Device> list = em.createQuery(
                                "SELECT d FROM Device d WHERE LOWER(d.brand) = LOWER(:b) AND LOWER(d.model) = LOWER(:m)", Device.class)
                                .setParameter("b", dev.getBrand().trim())
                                .setParameter("m", dev.getModel().trim())
                                .setMaxResults(1)
                                .getResultList();
                        if (!list.isEmpty()) {
                            dbDev = list.get(0);
                        }
                    }
                    if (dbDev == null) {
                        String series = dev.getSeries() != null ? dev.getSeries() : "";
                        String devBrand = (dev.getBrand() != null && !dev.getBrand().trim().isEmpty()) ? dev.getBrand().trim() : "Khác";
                        String devModel = (dev.getModel() != null && !dev.getModel().trim().isEmpty()) ? dev.getModel().trim() : "Laptop";
                        dbDev = new Device(devBrand, series, devModel, 
                                           dev.getReleaseYear() != null ? dev.getReleaseYear() : 2022, 
                                           dev.getCpu(), dev.getRam(), dev.getStorage());
                        if (dev.getName() != null && !dev.getName().trim().isEmpty()) {
                            dbDev.setName(dev.getName().trim());
                        }
                        em.persist(dbDev);
                    }
                    listing.setDevice(dbDev);
                }

                // 3. Liên kết hình ảnh và lỗi
                if (listing.getImages() != null) {
                    for (ListingImage img : listing.getImages()) {
                        img.setListing(listing);
                    }
                }
                if (listing.getProblems() != null) {
                    for (ListingProblem lp : listing.getProblems()) {
                        lp.setListing(listing);
                        if (lp.getProblem() != null) {
                            RepairProblem rp = null;
                            if (lp.getProblem().getId() != null && lp.getProblem().getId() > 0) {
                                rp = em.find(RepairProblem.class, lp.getProblem().getId());
                            }
                            if (rp == null && lp.getProblem().getName() != null) {
                                List<RepairProblem> list = em.createQuery(
                                        "SELECT r FROM RepairProblem r WHERE LOWER(r.name) = LOWER(:n)", RepairProblem.class)
                                        .setParameter("n", lp.getProblem().getName().trim())
                                        .setMaxResults(1)
                                        .getResultList();
                                if (!list.isEmpty()) {
                                    rp = list.get(0);
                                } else {
                                    rp = new RepairProblem(null, lp.getProblem().getName().trim());
                                    em.persist(rp);
                                }
                            }
                            lp.setProblem(rp);
                        }
                    }
                }

                if (listing.getId() == null) {
                    em.persist(listing);
                } else {
                    listing = em.merge(listing);
                }
                tx.commit();

                // Đồng bộ memoryStore
                memoryStore.put(listing.getId(), listing);
                return listing;
            } catch (Exception e) {
                System.err.println("JPA save listing warning: " + e.getMessage());
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        // Fallback in-memory
        if (listing.getId() == null) {
            listing.setId(idCounter.incrementAndGet());
        }
        memoryStore.put(listing.getId(), listing);
        return listing;
    }

    @Override
    public Listing update(Listing listing) {
        if (listing == null || listing.getId() == null) {
            return null;
        }
        listing.setUpdatedAt(new Date());

        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Listing merged = em.merge(listing);
                tx.commit();
                memoryStore.put(merged.getId(), merged);
                return merged;
            } catch (Exception e) {
                System.err.println("JPA update listing warning: " + e.getMessage());
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        memoryStore.put(listing.getId(), listing);
        return listing;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }

        EntityManager em = getEntityManagerSafely();
        if (em != null) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Listing l = em.find(Listing.class, id);
                if (l != null) {
                    em.remove(l);
                    tx.commit();
                    memoryStore.remove(id);
                    return true;
                }
                tx.commit();
            } catch (Exception e) {
                System.err.println("JPA delete listing warning: " + e.getMessage());
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

        return memoryStore.remove(id) != null;
    }
}
