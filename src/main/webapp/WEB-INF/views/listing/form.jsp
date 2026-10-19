<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đăng Bán Laptop Cũ — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="used" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-12 py-8">
        
        <!-- Breadcrumb Navigation -->
        <nav class="flex items-center gap-2 text-xs text-outline mb-4">
            <a class="hover:text-primary transition-colors flex items-center gap-1" href="${pageContext.request.contextPath}/home">
                <span class="material-symbols-outlined text-[16px]">home</span>
                <span>Trang chủ</span>
            </a>
            <span>/</span>
            <a class="hover:text-primary transition-colors" href="${pageContext.request.contextPath}/listing/marketplace">Chợ đồ cũ</a>
            <span>/</span>
            <span class="text-primary font-bold">
                <c:choose>
                    <c:when test="${not empty listing}">Chỉnh Sửa Tin Đăng</c:when>
                    <c:otherwise>Đăng Bán Laptop Cũ</c:otherwise>
                </c:choose>
            </span>
        </nav>

        <!-- Header Banner -->
        <div class="pb-6 border-b border-surface-dim mb-8 flex flex-col md:flex-row md:items-end justify-between gap-4">
            <div>
                <h1 class="text-3xl lg:text-4xl font-extrabold uppercase tracking-tight text-primary flex items-center gap-3">
                    <span class="w-2.5 h-8 bg-[#FF4A1C] inline-block"></span>
                    <c:choose>
                        <c:when test="${not empty listing}">CHỈNH SỬA TIN BÁN LAPTOP</c:when>
                        <c:otherwise>ĐĂNG BÁN LAPTOP CŨ</c:otherwise>
                    </c:choose>
                </h1>
                <p class="text-sm text-outline mt-2 max-w-2xl leading-relaxed">
                    Hệ thống kiểm định minh bạch: Khai báo trung thực tình trạng linh kiện và đính kèm ít nhất 1 ảnh thực tế (BR-12) để người mua tin cậy chốt đơn nhanh nhất.
                </p>
            </div>
            <div class="flex items-center gap-2 bg-surface-container border border-surface-dim px-3 py-1.5 self-start md:self-auto font-mono text-xs uppercase font-semibold">
                <span class="w-2 h-2 rounded-full bg-emerald-600"></span>
                <span>TIÊU CHUẨN ĐĂNG TIN C2C</span>
            </div>
        </div>

        <!-- Form Layout: 8 Cols Form / 4 Cols Sticky Valuation -->
        <form method="POST" action="${pageContext.request.contextPath}/listing/form" class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
            <input type="hidden" name="action" value="${not empty listing ? 'edit' : 'create'}">
            <input type="hidden" name="id" value="${listing.id}">

            <!-- LEFT COLUMN: Form Inputs (8 cols) -->
            <div class="lg:col-span-8 flex flex-col gap-6">
                
                <!-- Section 1: Thông tin cơ bản -->
                <section class="bg-surface-container-lowest border border-surface-dim p-6">
                    <div class="flex items-center justify-between pb-3 border-b border-surface-dim mb-4">
                        <h2 class="text-sm font-bold uppercase tracking-wider text-primary flex items-center gap-2">
                            <span class="w-2 h-3.5 bg-primary inline-block"></span> 1. THÔNG TIN THIẾT BỊ
                        </h2>
                        <span class="text-[11px] bg-surface-container text-secondary-container font-bold px-2 py-0.5 border border-surface-dim uppercase">Bắt buộc</span>
                    </div>

                    <div class="space-y-4">
                        <div>
                            <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">
                                Dòng máy (Device) <span class="text-secondary-container">*</span>
                            </label>
                            <select name="deviceId" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none cursor-pointer">
                                <option value="1" ${listing.device.id == 1 ? 'selected' : ''}>ThinkPad T14 Gen 2 (Lenovo)</option>
                                <option value="2" ${listing.device.id == 2 ? 'selected' : ''}>Dell XPS 13 9310 (Dell)</option>
                                <option value="3" ${listing.device.id == 3 ? 'selected' : ''}>MacBook Air M1 2020 (Apple)</option>
                                <option value="4" ${listing.device.id == 4 ? 'selected' : ''}>Acer Nitro 5 Gaming (Acer)</option>
                                <option value="5" ${listing.device.id == 5 ? 'selected' : ''}>Asus ZenBook 14 (Asus)</option>
                            </select>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">
                                    Thương hiệu <span class="text-secondary-container">*</span>
                                </label>
                                <input type="text" name="brand" value="${not empty listing.device ? listing.device.brand : 'Lenovo'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none" placeholder="Lenovo, Dell, Apple...">
                            </div>
                            <div>
                                <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">
                                    Model cụ thể
                                </label>
                                <input type="text" name="model" value="${not empty listing.device ? listing.device.model : 'T14 Gen 2'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none" placeholder="20W00049US, XPS 9310...">
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Section 2: Cấu hình kỹ thuật -->
                <section class="bg-surface-container-lowest border border-surface-dim p-6">
                    <div class="flex items-center justify-between pb-3 border-b border-surface-dim mb-4">
                        <h2 class="text-sm font-bold uppercase tracking-wider text-primary flex items-center gap-2">
                            <span class="w-2 h-3.5 bg-primary inline-block"></span> 2. CẤU HÌNH PHẦN CỨNG CHI TIẾT
                        </h2>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">Vi xử lý (CPU)</label>
                            <input type="text" name="cpu" value="${not empty listing.device ? listing.device.cpu : 'Intel Core i5-1135G7 (4.2GHz)'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none">
                        </div>
                        <div>
                            <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">Bộ nhớ RAM</label>
                            <input type="text" name="ram" value="${not empty listing.device ? listing.device.ram : '16GB DDR4 3200MHz'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none">
                        </div>
                        <div>
                            <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">Ổ cứng (Storage)</label>
                            <input type="text" name="storage" value="${not empty listing.device ? listing.device.storage : '512GB NVMe PCIe Gen 4'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none">
                        </div>
                        <div>
                            <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">Chu kỳ sạc / Tình trạng pin</label>
                            <input type="text" name="battery" value="142 lần sạc, pin còn 95%" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none">
                        </div>
                    </div>
                </section>

                <!-- Section 3: Tình trạng máy & Khai báo lỗi (ListingCondition & ListingProblem) -->
                <section class="bg-surface-container-lowest border border-surface-dim p-6">
                    <div class="flex items-center justify-between pb-3 border-b border-surface-dim mb-4">
                        <h2 class="text-sm font-bold uppercase tracking-wider text-primary flex items-center gap-2">
                            <span class="w-2 h-3.5 bg-primary inline-block"></span> 3. TÌNH TRẠNG & KHAI BÁO LỖI (BR-07, BR-08)
                        </h2>
                        <span class="text-[11px] bg-surface-container text-secondary-container font-bold px-2 py-0.5 border border-surface-dim uppercase">Rất quan trọng</span>
                    </div>

                    <!-- Condition Radio Selector -->
                    <div class="mb-6">
                        <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-2">
                            Mức độ tình trạng ngoại hình <span class="text-secondary-container">*</span>
                        </label>
                        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
                            <label class="border border-surface-dim p-3 flex items-start gap-2.5 cursor-pointer hover:border-primary transition-colors bg-surface-container-low">
                                <input type="radio" name="condition" value="USED_GOOD" checked class="mt-1 accent-primary">
                                <div>
                                    <div class="text-xs font-bold text-primary uppercase">LIKE NEW (99%)</div>
                                    <div class="text-[11px] text-outline mt-0.5">Không cấn móp, xước rất nhẹ khó thấy</div>
                                </div>
                            </label>
                            <label class="border border-surface-dim p-3 flex items-start gap-2.5 cursor-pointer hover:border-primary transition-colors bg-surface-container-low">
                                <input type="radio" name="condition" value="USED_FAIR" ${listing.condition == 'USED_FAIR' ? 'checked' : ''} class="mt-1 accent-primary">
                                <div>
                                    <div class="text-xs font-bold text-primary uppercase">NGOẠI HÌNH TẠM (95%)</div>
                                    <div class="text-[11px] text-outline mt-0.5">Có hao mòn/xước viền theo thời gian</div>
                                </div>
                            </label>
                            <label class="border border-surface-dim p-3 flex items-start gap-2.5 cursor-pointer hover:border-primary transition-colors bg-surface-container-low">
                                <input type="radio" name="condition" value="DAMAGED" ${listing.condition == 'DAMAGED' ? 'checked' : ''} class="mt-1 accent-primary">
                                <div>
                                    <div class="text-xs font-bold text-[#FF4A1C] uppercase">CẦN SỬA CHỮA</div>
                                    <div class="text-[11px] text-outline mt-0.5">Có linh kiện hỏng cần thay thế</div>
                                </div>
                            </label>
                        </div>
                    </div>

                    <!-- Defect Checklist (ListingProblem) -->
                    <div class="mb-6">
                        <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-2">
                            Khai báo các bộ phận có lỗi (nếu có):
                        </label>
                        <div class="grid grid-cols-2 sm:grid-cols-3 gap-2 text-xs">
                            <label class="p-2 border border-surface-dim bg-surface-container-low flex items-center gap-2 cursor-pointer">
                                <input type="checkbox" name="problems" value="1" class="accent-primary">
                                <span>Màn hình (sọc/điểm chết)</span>
                            </label>
                            <label class="p-2 border border-surface-dim bg-surface-container-low flex items-center gap-2 cursor-pointer">
                                <input type="checkbox" name="problems" value="2" class="accent-primary">
                                <span>Bàn phím (liệt phím)</span>
                            </label>
                            <label class="p-2 border border-surface-dim bg-surface-container-low flex items-center gap-2 cursor-pointer">
                                <input type="checkbox" name="problems" value="3" class="accent-primary">
                                <span>Pin chai / Phồng pin</span>
                            </label>
                            <label class="p-2 border border-surface-dim bg-surface-container-low flex items-center gap-2 cursor-pointer">
                                <input type="checkbox" name="problems" value="4" class="accent-primary">
                                <span>Bản lề lỏng / Nứt vỏ</span>
                            </label>
                            <label class="p-2 border border-surface-dim bg-surface-container-low flex items-center gap-2 cursor-pointer">
                                <input type="checkbox" name="problems" value="5" class="accent-primary">
                                <span>Loa rè / Mic hỏng</span>
                            </label>
                            <label class="p-2 border border-surface-dim bg-surface-container-low flex items-center gap-2 cursor-pointer">
                                <input type="checkbox" name="problems" value="6" class="accent-primary">
                                <span>Lỗi khác (ghi vào mô tả)</span>
                            </label>
                        </div>
                    </div>

                    <!-- Description Textarea -->
                    <div>
                        <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">
                            Mô tả chi tiết tình trạng máy <span class="text-secondary-container">*</span>
                        </label>
                        <textarea name="description" rows="5" class="w-full p-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none leading-relaxed" placeholder="Mô tả trung thực lịch sử sử dụng, nâng cấp RAM/SSD, phụ kiện đi kèm..."><c:out value="${listing.description != null ? listing.description : 'Máy cá nhân dùng làm việc kỹ thuật văn phòng tại nhà, giữ gìn rất kỹ. Màn hình IPS sáng đều, zero dead pixel. Bàn phím gõ êm, trackpoint hoàn hảo.'}"/></textarea>
                    </div>
                </section>

                <!-- Section 4: Giá bán & Ảnh thực tế -->
                <section class="bg-surface-container-lowest border border-surface-dim p-6">
                    <div class="flex items-center justify-between pb-3 border-b border-surface-dim mb-4">
                        <h2 class="text-sm font-bold uppercase tracking-wider text-primary flex items-center gap-2">
                            <span class="w-2 h-3.5 bg-primary inline-block"></span> 4. GIÁ BÁN & HÌNH ẢNH THỰC TẾ
                        </h2>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 items-start">
                        <div>
                            <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">
                                Giá niêm yết bán (VNĐ) <span class="text-secondary-container">*</span>
                            </label>
                            <div class="relative">
                                <input type="number" name="price" value="${not empty listing ? listing.price : 12800000}" class="w-full h-11 pl-3 pr-10 bg-surface-container-low border border-surface-dim text-base font-bold text-primary focus:border-primary focus:outline-none font-mono">
                                <span class="absolute right-3 top-1/2 -translate-y-1/2 font-bold text-outline">₫</span>
                            </div>
                            <div class="text-[11px] text-outline mt-1 font-mono">
                                Đơn vị: Đồng Việt Nam (không tính phụ phí đăng tin)
                            </div>
                        </div>

                        <div>
                            <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">
                                Đường dẫn hình ảnh thực tế (URL) <span class="text-secondary-container">*</span>
                            </label>
                            <input type="text" name="imageUrl" id="image-url-input" onchange="previewImageUrl(this)" onkeyup="previewImageUrl(this)" value="${not empty listing.images ? listing.images[0].imageUrl : 'https://lh3.googleusercontent.com/aida-public/AB6AXuBhsfIBalXmMjhm1NLSQt6Qkz_OmWr2qcpKxJyrVsvza8nGYMeKenQaqdW_amrvTYf5pRjv8UBUSw4A0wDt_dUw7SWUxo34ZS_r0k0CZi43jMo1RTEUvkVGpIc1bVGAIMEgdhXDI33Vm-9hUhDYA6j_1l4U54e0k94ozBGr9ID-NWC-wAa72xeVo2ukKbWRZA0oPGhsaat6O_hva43CM99e-XhMkJ24anxVCuySREIaIgoX6VxWOSUx'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-xs text-primary focus:border-primary focus:outline-none">
                            <div class="text-[11px] text-outline mt-1 font-mono">
                                Rule 5: Lưu URL hình ảnh, tối thiểu 1 ảnh rõ nét (BR-12).
                            </div>
                        </div>
                    </div>

                    <!-- Image Preview -->
                    <div class="mt-4 pt-4 border-t border-surface-dim">
                        <span class="block text-xs font-bold uppercase tracking-wider text-outline mb-2">Xem trước ảnh bìa:</span>
                        <div class="w-48 aspect-[4/3] bg-surface-container-low border border-surface-dim flex items-center justify-center overflow-hidden">
                            <img id="image-live-preview" src="${not empty listing.images ? listing.images[0].imageUrl : 'https://lh3.googleusercontent.com/aida-public/AB6AXuBhsfIBalXmMjhm1NLSQt6Qkz_OmWr2qcpKxJyrVsvza8nGYMeKenQaqdW_amrvTYf5pRjv8UBUSw4A0wDt_dUw7SWUxo34ZS_r0k0CZi43jMo1RTEUvkVGpIc1bVGAIMEgdhXDI33Vm-9hUhDYA6j_1l4U54e0k94ozBGr9ID-NWC-wAa72xeVo2ukKbWRZA0oPGhsaat6O_hva43CM99e-XhMkJ24anxVCuySREIaIgoX6VxWOSUx'}" class="w-full h-full object-cover">
                        </div>
                    </div>
                </section>
            </div>

            <!-- RIGHT COLUMN: Sticky Valuation Hint & Submit (4 cols) -->
            <div class="lg:col-span-4 flex flex-col gap-6 sticky top-20">
                <div class="bg-surface-container-lowest border border-surface-dim p-6">
                    <h3 class="text-xs font-bold uppercase tracking-wider text-primary pb-3 border-b border-surface-dim flex items-center gap-2">
                        <span class="material-symbols-outlined text-[#FF4A1C] text-[18px]">analytics</span>
                        <span>ĐỊNH GIÁ GỢI Ý TỪ THUẬT TOÁN</span>
                    </h3>
                    <div class="mt-4 space-y-3 text-xs text-on-surface-variant leading-relaxed">
                        <p>
                            Dữ liệu thị trường cho các dòng máy tương đương đang dao động từ 
                            <strong class="text-primary font-bold">11.500.000 ₫</strong> đến 
                            <strong class="text-primary font-bold">13.500.000 ₫</strong>.
                        </p>
                        <p class="p-3 bg-surface-container-low border border-surface-dim text-[11px] font-mono">
                            Tin đăng có mô tả chi tiết và khai báo lỗi rõ ràng được duyệt nhanh hơn 80% bởi Admin (BR-14).
                        </p>
                    </div>

                    <div class="mt-6 pt-4 border-t border-surface-dim flex flex-col gap-3">
                        <button type="submit" class="w-full h-12 bg-[#FF4A1C] text-black font-extrabold text-xs uppercase tracking-widest hover:brightness-110 active:scale-[0.99] transition-all shadow-md flex items-center justify-center gap-2">
                            <span class="material-symbols-outlined text-[18px]">publish</span>
                            <span>XÁC NHẬN ĐĂNG TIN BÁN</span>
                        </button>
                        <a href="${pageContext.request.contextPath}/listing/marketplace" class="w-full h-11 border border-surface-dim text-xs font-bold uppercase tracking-wider text-outline hover:text-primary hover:border-primary transition-colors flex items-center justify-center">
                            HỦY BỎ
                        </a>
                    </div>
                </div>

                <div class="bg-surface-container-lowest border border-surface-dim p-4 flex items-start gap-2.5">
                    <span class="material-symbols-outlined text-emerald-700 text-[20px] shrink-0 mt-0.5">verified_user</span>
                    <div class="text-[11px] text-outline leading-tight">
                        Tin đăng sẽ ở trạng thái <strong class="text-primary">AVAILABLE (Chờ duyệt)</strong> trước khi hiển thị công khai trên chợ.
                    </div>
                </div>
            </div>
        </form>
    </div>

<script src="${pageContext.request.contextPath}/js/listing.js"></script>
<%@ include file="/WEB-INF/layout/footer.jspf" %>
