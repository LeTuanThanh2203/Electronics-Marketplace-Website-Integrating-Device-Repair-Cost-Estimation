# TÀI LIỆU QUY CHUẨN: CÔNG THỨC & CƠ CHẾ DỰ TOÁN CHI PHÍ SỬA CHỮA
> **Phân hệ:** Dịch vụ sửa chữa & Quản trị định mức giá (Repair Service & Admin)  
> **Dự án:** Website Chợ Đồ Công Nghệ Tích Hợp Dự Toán Chi Phí Sửa Chữa Thiết Bị  
> **Cập nhật ngày:** 20/10/2026

---

## 1. Tổng quan kiến trúc & Các thực thể dữ liệu

Cơ chế tính toán dự toán chi phí sửa chữa được xây dựng khép kín theo mô hình Domain-Driven & Snapshot Audit Trail, gắn kết qua các thực thể:

* **[Device](file:///d:/Work/WebProgramming/Electronics-Marketplace-Website-Integrating-Device-Repair-Cost-Estimation/src/main/java/com/electronics/marketplace/entity/Device.java)**: Định danh dòng thiết bị phần cứng (`brand`, `series`, `model`, `releaseYear`...).
* **[RepairProblem](file:///d:/Work/WebProgramming/Electronics-Marketplace-Website-Integrating-Device-Repair-Cost-Estimation/src/main/java/com/electronics/marketplace/entity/RepairProblem.java)**: Danh mục loại sự cố phần cứng (`name`, `description`, `estimatedMinDays`, `estimatedMaxDays`).
* **[RepairPricing](file:///d:/Work/WebProgramming/Electronics-Marketplace-Website-Integrating-Device-Repair-Cost-Estimation/src/main/java/com/electronics/marketplace/entity/RepairPricing.java)**: Bảng định mức giá cơ sở cho từng cặp `(device_id, problem_id)`.
* **[RepairRequest](file:///d:/Work/WebProgramming/Electronics-Marketplace-Website-Integrating-Device-Repair-Cost-Estimation/src/main/java/com/electronics/marketplace/entity/RepairRequest.java)**: Phiếu yêu cầu sửa chữa do khách hàng khởi tạo.
* **[RepairEstimate](file:///d:/Work/WebProgramming/Electronics-Marketplace-Website-Integrating-Device-Repair-Cost-Estimation/src/main/java/com/electronics/marketplace/entity/RepairEstimate.java)**: Bản chụp dữ liệu (Snapshot) báo giá độc lập gắn liền với phiếu sửa chữa.

---

## 2. Công thức toán học tính toán chi phí

Chi phí dự toán được biểu diễn dưới dạng **khoảng giá `[Min Cost, Max Cost]`** thay vì một con số cố định, phản ánh đúng bản chất linh kiện công nghệ và rủi ro hao mòn phần cứng thực tế.

### Bước 1: Chi phí cơ sở (Base Cost)
$$\text{Base Cost} = \text{parts\_cost} + \text{labor\_cost}$$

Trong đó:
* **`parts_cost` (Tiền linh kiện)**: Giá thị trường của linh kiện chuẩn cần thay thế (màn hình, cell pin, cụm phím, chip IC, v.v.).
* **`labor_cost` (Tiền công kỹ thuật)**: Chi phí nhân công đo kiểm, tháo ráp, hàn khò bo mạch và kiểm thử chất lượng sau sửa chữa.

### Bước 2: Độ lệch biên độ dự toán (Margin Amount)
$$\text{Margin Amount} = \text{Base Cost} \times \text{estimation\_margin}$$

Trong đó:
* **`estimation_margin` (Tỷ lệ biên độ)**: Là tỷ số phần trăm trong khoảng $[0, 1)$ (ví dụ: `0.1000` tương đương $10\%$, `0.0500` tương đương $5\%$).
* **Ý nghĩa thực tế**: Dự phòng dao động giữa các loại linh kiện (hàng zin bóc máy, chính hãng mới 100%, thương hiệu thứ 3 cao cấp) hoặc độ phức tạp phát sinh ngoài dự kiến khi tiếp nhận trực tiếp tại quầy lab.

### Bước 3: Xác định Khoảng chi phí dự toán `[Min Cost, Max Cost]`
$$\text{Min Cost} = \text{Base Cost} - \text{Margin Amount} = (\text{parts\_cost} + \text{labor\_cost}) \times (1 - \text{estimation\_margin})$$
$$\text{Max Cost} = \text{Base Cost} + \text{Margin Amount} = (\text{parts\_cost} + \text{labor\_cost}) \times (1 + \text{estimation\_margin})$$

> **Ràng buộc an toàn:**
> * Giá trị `Min Cost` luôn được chặn dưới $\ge 0$ (không cho phép âm).
> * Mọi kết quả đều được làm tròn 2 chữ số thập phân (`RoundingMode.HALF_UP`).

---

## 3. Cài đặt chi tiết trong mã nguồn Java

Logic tính toán được đóng gói chuẩn hướng đối tượng trong thực thể [RepairPricing.java](file:///d:/Work/WebProgramming/Electronics-Marketplace-Website-Integrating-Device-Repair-Cost-Estimation/src/main/java/com/electronics/marketplace/entity/RepairPricing.java):

```java
// 1. Tính chi phí cơ sở
public BigDecimal calculateBaseCost() {
    BigDecimal parts = partsCost != null ? partsCost : BigDecimal.ZERO;
    BigDecimal labor = laborCost != null ? laborCost : BigDecimal.ZERO;
    return parts.add(labor);
}

// 2. Tính số tiền biên độ
public BigDecimal calculateMarginAmount() {
    BigDecimal base = calculateBaseCost();
    BigDecimal margin = normalizeMargin(estimationMargin);
    return base.multiply(margin).setScale(2, RoundingMode.HALF_UP);
}

// 3. Tính chi phí dự toán tối thiểu
public BigDecimal calculateMinCost() {
    BigDecimal base = calculateBaseCost();
    BigDecimal marginAmount = calculateMarginAmount();
    BigDecimal min = base.subtract(marginAmount);
    return min.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : min.setScale(2, RoundingMode.HALF_UP);
}

// 4. Tính chi phí dự toán tối đa
public BigDecimal calculateMaxCost() {
    BigDecimal base = calculateBaseCost();
    BigDecimal marginAmount = calculateMarginAmount();
    return base.add(marginAmount).setScale(2, RoundingMode.HALF_UP);
}
```

---

## 4. Ví dụ tính toán bằng số liệu thực tế

### Trường hợp 1: Thay màn hình iPhone 16 Pro Max
* **Thiết bị:** Apple iPhone 16 Pro Max
* **Lỗi:** Vỡ màn hình / Chảy mực
* **Thông số bảng giá:**
  * `parts_cost` = $6.500.000$ VNĐ
  * `labor_cost` = $350.000$ VNĐ
  * `estimation_margin` = $0.1000$ ($10\%$)
* **Các bước tính:**
  1. $\text{Base Cost} = 6.500.000 + 350.000 = 6.850.000$ VNĐ
  2. $\text{Margin Amount} = 6.850.000 \times 10\% = 685.000$ VNĐ
  3. $\text{Min Cost} = 6.850.000 - 685.000 = \mathbf{6.165.000}$ VNĐ
  4. $\text{Max Cost} = 6.850.000 + 685.000 = \mathbf{7.535.000}$ VNĐ
* **Hiển thị trên UI:** `6.165.000 VNĐ - 7.535.000 VNĐ`

---

### Trường hợp 2: Thay bàn phím Dell XPS 15 9530
* **Thiết bị:** Dell XPS 15 9530
* **Lỗi:** Liệt bàn phím / Kẹt phím
* **Thông số bảng giá:**
  * `parts_cost` = $1.200.000$ VNĐ
  * `labor_cost` = $300.000$ VNĐ
  * `estimation_margin` = $0.1000$ ($10\%$)
* **Các bước tính:**
  1. $\text{Base Cost} = 1.200.000 + 300.000 = 1.500.000$ VNĐ
  2. $\text{Margin Amount} = 1.500.000 \times 10\% = 150.000$ VNĐ
  3. $\text{Min Cost} = 1.500.000 - 150.000 = \mathbf{1.350.000}$ VNĐ
  4. $\text{Max Cost} = 1.500.000 + 150.000 = \mathbf{1.650.000}$ VNĐ
* **Hiển thị trên UI:** `1.350.000 VNĐ - 1.650.000 VNĐ`

---

## 5. Cơ chế Snapshot lưu vết báo giá (Audit Trail)

Khi khách hàng bấm xác nhận tạo phiếu yêu cầu sửa chữa trên giao diện web:

1. **Nếu đã có định mức giá trong `repair_pricings`:**
   * Hệ thống tính toán và tự động chép bản ghi độc lập vào bảng [repair_estimates](file:///d:/Work/WebProgramming/Electronics-Marketplace-Website-Integrating-Device-Repair-Cost-Estimation/src/main/resources/database/schema_and_seed_repair.sql#L120-L131):
     * `min_cost`
     * `max_cost`
     * `pricing_version`
     * `parts_cost_snapshot`
     * `labor_cost_snapshot`
     * `estimation_margin_snapshot`
   * Phiếu yêu cầu chuyển trạng thái: `ESTIMATED`.
   * **Bảo vệ tính toàn vẹn:** Khi Admin thay đổi giá linh kiện hay biên độ sau này, mức giá chốt trong đơn của khách hàng **không bao giờ bị thay đổi sai lệch**.

2. **Nếu chưa có định mức giá:**
   * Trạng thái phiếu ghi nhận là `WAITING_PRICING`.
   * **Tuyệt đối không sinh giá ảo 0 đồng**.
   * Khi Admin bổ sung bảng giá mới qua màn hình `/admin/repair/pricing`, phương thức [RepairService.saveOrUpdatePricing()](file:///d:/Work/WebProgramming/Electronics-Marketplace-Website-Integrating-Device-Repair-Cost-Estimation/src/main/java/com/electronics/marketplace/service/RepairService.java#L236-L265) sẽ tự động quét và tính toán bổ sung snapshot cho các đơn đang chờ định mức này.

---

## 6. Tra cứu tự động qua AJAX Endpoint

Khách hàng khi thao tác trên trang [Yêu cầu sửa chữa (request_form.jsp)](file:///d:/Work/WebProgramming/Electronics-Marketplace-Website-Integrating-Device-Repair-Cost-Estimation/src/main/webapp/WEB-INF/views/repair/request_form.jsp) chỉ cần chọn `deviceId` và `problemId`, trình duyệt sẽ gọi ngầm:

* **Endpoint:** `GET /repair/api/estimate?deviceId={id}&problemId={id}`
* **Phản hồi mẫu (JSON):**
```json
{
  "success": true,
  "deviceName": "Apple iPhone 16 Pro Max",
  "problemName": "Vỡ màn hình / Chảy mực",
  "minCost": 6165000.00,
  "maxCost": 7535000.00,
  "formattedMinCost": "6.165.000 ₫",
  "formattedMaxCost": "7.535.000 ₫",
  "estimatedMinDays": 1,
  "estimatedMaxDays": 2
}
```
Giao diện ngay lập tức cập nhật hộp dự toán chi phí và thời gian dự kiến trả máy trong vòng **< 100ms** mà không cần tải lại trang.
