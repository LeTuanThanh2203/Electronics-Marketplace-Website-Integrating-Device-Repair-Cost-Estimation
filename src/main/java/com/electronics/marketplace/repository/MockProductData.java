package com.electronics.marketplace.repository;

import com.electronics.marketplace.entity.Device;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.entity.ProductSpecification;
import com.electronics.marketplace.entity.enums.ProductStatus;
import com.electronics.marketplace.entity.enums.SpecDataType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre-populated mock data for Products and Specifications based on the official UI template.
 * Allows full application functionality, filtering, and recommendation testing without requiring an active database.
 */
public class MockProductData {

    public static List<Product> getSampleProducts() {
        List<Product> list = new ArrayList<>();

        // 1. Dell XPS 13 Plus 9320
        Device devDell = new Device("Dell XPS 13", "Dell", "XPS 9320", 2023, "Intel Core i7-1360P", "16", "512");
        devDell.setId(1L);

        Product p1 = new Product(1L, "Dell XPS 13 Plus 9320", "Dell", new BigDecimal("35900000"), ProductStatus.AVAILABLE);
        p1.setId(1L);
        p1.setSku("XPS-9320-OLED");
        p1.setSeries("XPS SERIES");
        p1.setOriginalPrice(new BigDecimal("38900000"));
        p1.setDiscountTag("ƯU ĐÃI -3.000.000₫");
        p1.setBadge("SEAL 100%");
        p1.setImageUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuBgd9ksdOyJ2lnuUjd006sDwXcEnfIRfDJZTxHKikqqcuocKnck8V_k-2ZGgDHYD38OenXZfcj6CBM8LGeFjtqrZunmXgIu2syI1cXzGRi_HsoELH2imeygnlcU8b-ZYY2lNjU9r2hMhHccJ4MvD5cMpo9sO09pm491bfNCpOpGQ3rNQkKnUURFMklwZUgzblRRcwZWbpwJZw6n4Ni7De0dAuKI7ESIQIWX-rvdqUdXFpmxe3ZWP3Nd");
        p1.setWarranty("Bảo hành 24 tháng chính hãng tại nhà");
        p1.setDescription("Mỏng nhẹ đỉnh cao, phím cảm ứng điện dung zero-lattice, màn hình OLED 3.5K sắc nét.");
        p1.setTargetNeed("OFFICE");
        p1.setStockQuantity(8);
        p1.setDevice(devDell);

        p1.addSpecification(ProductSpecification.textSpec("CPU", "Intel Core i7-1360P (12 Nhân, 16 Luồng, tối đa 5.0 GHz)"));
        p1.addSpecification(ProductSpecification.numberSpec("RAM", 16, "GB"));
        p1.addSpecification(ProductSpecification.numberSpec("STORAGE", 512, "GB"));
        p1.addSpecification(ProductSpecification.textSpec("GPU", "Intel Iris Xe Graphics"));
        p1.addSpecification(ProductSpecification.numberSpec("SCREEN_SIZE", 13, "inch"));
        p1.addSpecification(ProductSpecification.textSpec("SCREEN_DETAIL", "13.4\" 3.5K (3456x2160) OLED Touch 400 nits"));
        p1.addSpecification(ProductSpecification.numberSpec("REFRESH_RATE", 60, "Hz"));
        p1.addSpecification(ProductSpecification.numberSpec("WEIGHT", 1, "kg"));
        list.add(p1);

        // 2. MacBook Air 15 M3 2024
        Device devApple = new Device("MacBook Air 15", "Apple", "MacBook Air M3", 2024, "Apple M3 (8-Core CPU)", "16", "512");
        devApple.setId(2L);

        Product p2 = new Product(1L, "MacBook Air 15 M3 2024", "Apple", new BigDecimal("37490000"), ProductStatus.AVAILABLE);
        p2.setId(2L);
        p2.setSku("MBA15-M3-16G");
        p2.setSeries("SILICON M3");
        p2.setOriginalPrice(new BigDecimal("39990000"));
        p2.setDiscountTag("TRỢ GIÁ SINH VIÊN");
        p2.setBadge("NEW SEAL 100%");
        p2.setImageUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuBjCxx8fCyApk9I6Q82W_rH51FNWJ3U8AsCeWH_H1nHHQh3JkiOYjWdlW-A8wK5j31QWdG-MVYCzZLgKbSfyl4-bFFuwqnaI5aLUC8K8xdDRhK_qvtvvmTafeT38S9kdXdFKIp77gaHEvfAv5UGD90JdL2hv5O1kkITvADAJcd7CgePWQXuHPzDMQqeGYVTGfLmyf8DjD9vocwcBp_T8I1MDKGKjRBdJxzJcQWeUut868vHf52tMIKZ");
        p2.setWarranty("Bảo hành 12 tháng Apple Care toàn quốc");
        p2.setDescription("Thiết kế unibody nhôm siêu mỏng, thời lượng pin 18 tiếng, màn hình Liquid Retina tuyệt đẹp.");
        p2.setTargetNeed("OFFICE");
        p2.setStockQuantity(12);
        p2.setDevice(devApple);

        p2.addSpecification(ProductSpecification.textSpec("CPU", "Apple M3 (8-Core CPU / 10-Core GPU / 16-Core Neural Engine)"));
        p2.addSpecification(ProductSpecification.numberSpec("RAM", 16, "GB"));
        p2.addSpecification(ProductSpecification.numberSpec("STORAGE", 512, "GB"));
        p2.addSpecification(ProductSpecification.textSpec("GPU", "Apple 10-Core GPU"));
        p2.addSpecification(ProductSpecification.numberSpec("SCREEN_SIZE", 15, "inch"));
        p2.addSpecification(ProductSpecification.textSpec("SCREEN_DETAIL", "15.3\" Liquid Retina (2880x1864) 500 nits P3 color"));
        p2.addSpecification(ProductSpecification.numberSpec("REFRESH_RATE", 60, "Hz"));
        p2.addSpecification(ProductSpecification.numberSpec("BATTERY_HOURS", 18, "h"));
        list.add(p2);

        // 3. ASUS ROG Strix G16 G614J
        Device devRog = new Device("ROG Strix G16", "ASUS", "G614J", 2024, "Intel Core i7-13650HX", "16", "1024");
        devRog.setId(3L);

        Product p3 = new Product(1L, "ASUS ROG Strix G16 G614J", "ASUS", new BigDecimal("34500000"), ProductStatus.AVAILABLE);
        p3.setId(3L);
        p3.setSku("ROG-G614J-4060");
        p3.setSeries("REPUBLIC OF GAMERS");
        p3.setOriginalPrice(new BigDecimal("37500000"));
        p3.setDiscountTag("TẶNG BALO ROG");
        p3.setBadge("SEAL 100%");
        p3.setImageUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuCZgCR0xRoRLdtbJkRl-k5Hz3LJ7FFaFe7RrnIvXCSw5jWTXWRPZO4sJHEHMbTniOhBQDvamdz60nugbLCACiLRVKwjViSDeVMvPy1fvvMs4HlRzLeZ0OWUjc2_cMetF_3EDB2a7DADvO3o5fSlOLlQXk3i2MGbZH2ZXVhSdNFaWJFfpiOUbnVDuBR61KdPYelo8Lo9WuS3tnIU8sgucRpldI1prm1VW1Rw2QR86Ld0wLP1Vt4xfxXs");
        p3.setWarranty("Bảo hành 24 tháng chính hãng Asus VN");
        p3.setDescription("Chiến binh gaming đỉnh cao với GPU RTX 4060, tản nhiệt 3 quạt buồng hơi, màn hình 165Hz.");
        p3.setTargetNeed("GAMING");
        p3.setStockQuantity(5);
        p3.setDevice(devRog);

        p3.addSpecification(ProductSpecification.textSpec("CPU", "Intel Core i7-13650HX (14 Nhân, 20 Luồng, tối đa 4.9 GHz)"));
        p3.addSpecification(ProductSpecification.numberSpec("RAM", 16, "GB"));
        p3.addSpecification(ProductSpecification.numberSpec("STORAGE", 1024, "GB"));
        p3.addSpecification(ProductSpecification.textSpec("GPU", "NVIDIA GeForce RTX 4060 8GB GDDR6 (TGP 140W)"));
        p3.addSpecification(ProductSpecification.numberSpec("SCREEN_SIZE", 16, "inch"));
        p3.addSpecification(ProductSpecification.textSpec("SCREEN_DETAIL", "16.0\" FHD+ (1920x1200) IPS 165Hz 100% sRGB"));
        p3.addSpecification(ProductSpecification.numberSpec("REFRESH_RATE", 165, "Hz"));
        list.add(p3);

        // 4. Lenovo ThinkPad X1 Carbon Gen 11
        Device devThinkPad = new Device("ThinkPad X1 Carbon", "Lenovo", "Gen 11", 2023, "Intel Core i7-1355U", "16", "512");
        devThinkPad.setId(4L);

        Product p4 = new Product(1L, "Lenovo ThinkPad X1 Carbon Gen 11", "Lenovo", new BigDecimal("39900000"), ProductStatus.AVAILABLE);
        p4.setId(4L);
        p4.setSku("THINKPAD-X1-G11");
        p4.setSeries("THINKPAD PREMIER");
        p4.setOriginalPrice(new BigDecimal("43900000"));
        p4.setDiscountTag("BẢO HIỂM RƠI VỠ 12T");
        p4.setBadge("CHÍNH HÃNG VN");
        p4.setImageUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuBTcjuIEbWhlhg_p0MDbytxqL0oLDAb-Phat6nBTHJT36cw2KL_9Q4dgP8N_L5pCrpux0-4082AlNgs15kxya8bCBSWfY9U7iAm_d-5eaR22KBYE93JNfhLDPOyQXo2yLTDAoD7178maRxMJvagedNLRYU-D7sOUwZs3vehNG22ZMkzghyOZgIoa4ctqA3nKrG5wQYAJaly_49li3cb_7go0lI9_DgoFcc-TN99HCVy0NvKBals1xdv");
        p4.setWarranty("Bảo hành 36 tháng Premier Support tận nơi");
        p4.setDescription("Huyền thoại doanh nhân siêu bền chuẩn quân đội, bàn phím gõ êm nhất thế giới, trọng lượng 1.12kg.");
        p4.setTargetNeed("OFFICE");
        p4.setStockQuantity(7);
        p4.setDevice(devThinkPad);

        p4.addSpecification(ProductSpecification.textSpec("CPU", "Intel Core i7-1355U (10 Nhân, 12 Luồng)"));
        p4.addSpecification(ProductSpecification.numberSpec("RAM", 16, "GB"));
        p4.addSpecification(ProductSpecification.numberSpec("STORAGE", 512, "GB"));
        p4.addSpecification(ProductSpecification.textSpec("GPU", "Intel Iris Xe Graphics"));
        p4.addSpecification(ProductSpecification.numberSpec("SCREEN_SIZE", 14, "inch"));
        p4.addSpecification(ProductSpecification.textSpec("SCREEN_DETAIL", "14.0\" 2.8K OLED (2880x1800) 400 nits 100% DCI-P3"));
        p4.addSpecification(ProductSpecification.numberSpec("REFRESH_RATE", 90, "Hz"));
        list.add(p4);

        // 5. ASUS Vivobook 15 OLED
        Device devVivo = new Device("Vivobook 15", "ASUS", "A1505", 2024, "Intel Core i5-13500H", "16", "512");
        devVivo.setId(5L);

        Product p5 = new Product(1L, "ASUS Vivobook 15 OLED A1505", "ASUS", new BigDecimal("17490000"), ProductStatus.AVAILABLE);
        p5.setId(5L);
        p5.setSku("VIVOBOOK-15-OLED");
        p5.setSeries("VIVOBOOK CLASSIC");
        p5.setOriginalPrice(new BigDecimal("19990000"));
        p5.setDiscountTag("TẶNG BALO + CHUỘT");
        p5.setBadge("SEAL 100%");
        p5.setImageUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuCYZrOchXXYzGtQ01eVG6H9S3nf-BXbWjEg9McOKK_0gfllqU7GQlRNkKoODJM-ABVxYUeMBRh1wZEiys7j1lV6MzlnaZXrJJiA9JqxfjdL6Lqd-QRCU80G3aawpilK2o2my2KGLa41HVJ9W2Db4QpSxWE0K-dbEFc6j9jyFdKLBwM-TvIJSE6QEpLN8FsCPeiCwPH6SzoeIHmMbPv3Sc-nQn6R1BI_4pyrATA0X1Y");
        p5.setWarranty("Bảo hành 24 tháng chính hãng Asus");
        p5.setDescription("Màn hình OLED 600 nits chuẩn màu 100% DCI-P3, chip H hiệu năng cao, giá cực tốt cho sinh viên & thiết kế.");
        p5.setTargetNeed("GRAPHICS");
        p5.setStockQuantity(15);
        p5.setDevice(devVivo);

        p5.addSpecification(ProductSpecification.textSpec("CPU", "Intel Core i5-13500H (12 Nhân, 16 Luồng, tối đa 4.7 GHz)"));
        p5.addSpecification(ProductSpecification.numberSpec("RAM", 16, "GB"));
        p5.addSpecification(ProductSpecification.numberSpec("STORAGE", 512, "GB"));
        p5.addSpecification(ProductSpecification.textSpec("GPU", "Intel Iris Xe Graphics"));
        p5.addSpecification(ProductSpecification.numberSpec("SCREEN_SIZE", 15, "inch"));
        p5.addSpecification(ProductSpecification.textSpec("SCREEN_DETAIL", "15.6\" FHD (1920x1080) OLED 600 nits HDR 100% DCI-P3"));
        p5.addSpecification(ProductSpecification.numberSpec("REFRESH_RATE", 60, "Hz"));
        list.add(p5);

        // 6. Acer Predator Helios Neo 16
        Device devHelios = new Device("Predator Helios Neo 16", "Acer", "PHN16", 2024, "Intel Core i7-14700HX", "32", "1024");
        devHelios.setId(6L);

        Product p6 = new Product(1L, "Acer Predator Helios Neo 16 2024", "Acer", new BigDecimal("41990000"), ProductStatus.AVAILABLE);
        p6.setId(6L);
        p6.setSku("HELIOS-NEO-16-4070");
        p6.setSeries("PREDATOR GAMING");
        p6.setOriginalPrice(new BigDecimal("45990000"));
        p6.setDiscountTag("QUÀ TẶNG GAMING 3TR");
        p6.setBadge("SEAL 100%");
        p6.setImageUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuCZgCR0xRoRLdtbJkRl-k5Hz3LJ7FFaFe7RrnIvXCSw5jWTXWRPZO4sJHEHMbTniOhBQDvamdz60nugbLCACiLRVKwjViSDeVMvPy1fvvMs4HlRzLeZ0OWUjc2_cMetF_3EDB2a7DADvO3o5fSlOLlQXk3i2MGbZH2ZXVhSdNFaWJFfpiOUbnVDuBR61KdPYelo8Lo9WuS3tnIU8sgucRpldI1prm1VW1Rw2QR86Ld0wLP1Vt4xfxXs");
        p6.setWarranty("Bảo hành 12 tháng 3S1 VIP của Acer");
        p6.setDescription("Cấu hình khủng RTX 4070, quạt kim loại AeroBlade 3D thế hệ 5, kim loại lỏng Liquid Metal tản nhiệt.");
        p6.setTargetNeed("GAMING");
        p6.setStockQuantity(4);
        p6.setDevice(devHelios);

        p6.addSpecification(ProductSpecification.textSpec("CPU", "Intel Core i7-14700HX (20 Nhân, 28 Luồng, tối đa 5.5 GHz)"));
        p6.addSpecification(ProductSpecification.numberSpec("RAM", 32, "GB"));
        p6.addSpecification(ProductSpecification.numberSpec("STORAGE", 1024, "GB"));
        p6.addSpecification(ProductSpecification.textSpec("GPU", "NVIDIA GeForce RTX 4070 8GB GDDR6 (TGP 140W)"));
        p6.addSpecification(ProductSpecification.numberSpec("SCREEN_SIZE", 16, "inch"));
        p6.addSpecification(ProductSpecification.textSpec("SCREEN_DETAIL", "16.0\" WQXGA (2560x1600) IPS 240Hz 100% sRGB 500 nits"));
        p6.addSpecification(ProductSpecification.numberSpec("REFRESH_RATE", 240, "Hz"));
        list.add(p6);

        return list;
    }
}
