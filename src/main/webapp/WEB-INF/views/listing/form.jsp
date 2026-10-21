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

        <!-- Error Banner if any -->
        <c:if test="${not empty errorMessage}">
            <div class="mb-6 p-4 bg-red-50 border-l-4 border-red-500 text-red-900 text-sm flex items-start gap-3 shadow-sm">
                <span class="material-symbols-outlined text-red-600 text-[22px] shrink-0">error</span>
                <div>
                    <div class="font-bold">Đã xảy ra lỗi khi đăng tin:</div>
                    <div class="mt-0.5"><c:out value="${errorMessage}"/></div>
                </div>
            </div>
        </c:if>

        <!-- Form Layout: 8 Cols Form / 4 Cols Sticky Valuation -->
        <form method="POST" action="${pageContext.request.contextPath}/listing/form" enctype="multipart/form-data" class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
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
                            <div class="flex items-center justify-between mb-1">
                                <label class="block text-xs font-bold uppercase tracking-wider text-primary">
                                    Dòng máy (Device Model) <span class="text-secondary-container">*</span>
                                </label>
                                <span class="text-[11px] text-outline">Chọn từ danh mục hoặc chọn "Tự nhập"</span>
                            </div>
                            <select name="deviceId" id="device-select" onchange="onDeviceSelectChange(this)" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none cursor-pointer">
                                <option value="0" ${empty listing || empty listing.device || listing.device.id == 0 ? 'selected' : ''}>
                                    + TỰ NHẬP DÒNG MÁY KHÁC (Tùy chỉnh thương hiệu & model)
                                </option>
                                <c:forEach items="${devices}" var="d">
                                    <option value="${d.id}" 
                                            data-brand="${d.brand}" 
                                            data-model="${d.model}" 
                                            data-cpu="${d.cpu}" 
                                            data-ram="${d.ram}" 
                                            data-storage="${d.storage}"
                                            ${not empty listing.device && listing.device.id == d.id ? 'selected' : ''}>
                                        ${d.name} (${d.brand.toUpperCase()} - ${d.model})
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">
                                    Thương hiệu (Brand) <span class="text-secondary-container">*</span>
                                </label>
                                <input type="text" name="brand" id="input-brand" required value="${not empty listing.device ? listing.device.brand : 'Lenovo'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none" placeholder="Lenovo, Dell, Apple, Asus, HP, Acer...">
                            </div>
                            <div>
                                <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">
                                    Model cụ thể <span class="text-secondary-container">*</span>
                                </label>
                                <input type="text" name="model" id="input-model" required value="${not empty listing.device ? listing.device.model : 'T14 Gen 2'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none" placeholder="20W00049US, XPS 9310, MacBook Air M1...">
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
                            <input type="text" name="cpu" id="input-cpu" value="${not empty listing.device ? listing.device.cpu : 'Intel Core i5-1135G7 (4.2GHz)'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none">
                        </div>
                        <div>
                            <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">Bộ nhớ RAM</label>
                            <input type="text" name="ram" id="input-ram" value="${not empty listing.device ? listing.device.ram : '16GB DDR4 3200MHz'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none">
                        </div>
                        <div>
                            <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">Ổ cứng (Storage)</label>
                            <input type="text" name="storage" id="input-storage" value="${not empty listing.device ? listing.device.storage : '512GB NVMe PCIe Gen 4'}" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none">
                        </div>
                        <div>
                            <label class="block text-xs font-bold uppercase tracking-wider text-primary mb-1">Chu kỳ sạc / Tình trạng pin</label>
                            <input type="text" name="battery" id="input-battery" value="142 lần sạc, pin còn 95%" class="w-full h-11 px-3 bg-surface-container-low border border-surface-dim text-sm text-primary focus:border-primary focus:outline-none">
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
                            <div class="flex items-center justify-between mb-1">
                                <label class="block text-xs font-bold uppercase tracking-wider text-primary">
                                    Hình ảnh thực tế của máy <span class="text-secondary-container">*</span>
                                </label>
                                <span class="text-[11px] text-[#FF4A1C] font-mono font-bold">Có thể chọn nhiều ảnh</span>
                            </div>

                            <!-- Folder File Chooser: Multiple Files Upload Button -->
                            <div class="space-y-3">
                                <div>
                                    <label for="image-file-picker" 
                                           class="w-full h-12 px-4 bg-[#FF4A1C]/10 border-2 border-dashed border-[#FF4A1C] hover:bg-[#FF4A1C]/20 transition-all cursor-pointer flex items-center justify-center gap-2 text-xs font-bold uppercase tracking-wider text-primary group select-none">
                                        <span class="material-symbols-outlined text-[#FF4A1C] text-[22px] group-hover:scale-110 transition-transform">add_photo_alternate</span>
                                        <span id="file-chosen-text">+ THÊM ẢNH TỪ MÁY TÍNH</span>
                                    </label>
                                    <!-- Interactive Picker: user clicks repeatedly to add more images -->
                                    <input type="file" id="image-file-picker" accept="image/png, image/jpeg, image/jpg, image/webp" multiple class="hidden" onchange="handleImageSelection(this)">
                                    <!-- Actual Form Input sent with request, synced via DataTransfer -->
                                    <input type="file" name="imageFiles" id="image-file-input" accept="image/png, image/jpeg, image/jpg, image/webp" multiple class="hidden">
                                    <input type="hidden" name="imageSectionPresent" value="true">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Image Gallery Preview Container -->
                    <div class="mt-6 pt-5 border-t border-surface-dim">
                        <div class="flex items-center justify-between mb-3">
                            <span class="text-xs font-bold uppercase tracking-wider text-primary flex items-center gap-2">
                                <span class="material-symbols-outlined text-[18px] text-[#FF4A1C]">photo_library</span>
                                <span>XEM TRƯỚC HÌNH ẢNH ĐĂNG BÁN</span>
                            </span>
                            <span id="file-size-info" class="text-emerald-700 font-mono text-[11px] font-semibold"></span>
                        </div>

                        <div class="flex flex-col md:flex-row gap-5 items-start">
                            <!-- Main Large Preview -->
                            <div class="w-full md:w-56 aspect-[4/3] bg-surface-container-low border border-surface-dim flex items-center justify-center overflow-hidden shrink-0 relative group">
                                <!-- Empty Placeholder State -->
                                <div id="main-preview-empty" class="flex flex-col items-center justify-center p-4 text-center text-outline ${not empty listing.images ? 'hidden' : ''}">
                                    <span class="material-symbols-outlined text-[44px] text-zinc-400 mb-1">add_photo_alternate</span>
                                    <span class="text-xs font-bold uppercase text-zinc-600">Chưa có ảnh xem trước</span>
                                    <span class="text-[10px] text-zinc-400 mt-1">Bấm nút trên để chọn ảnh máy</span>
                                </div>

                                <!-- Live Active Preview Image -->
                                <img id="image-live-preview" 
                                     src="${not empty listing.images && not empty listing.images[0].imageUrl ? listing.images[0].imageUrl : ''}" 
                                     class="w-full h-full object-cover ${empty listing.images ? 'hidden' : ''}">
                                
                                <span id="main-preview-badge" class="absolute bottom-1 right-1 px-1.5 py-0.5 bg-black/80 text-white text-[10px] font-mono ${empty listing.images ? 'hidden' : ''}">
                                    Ảnh bìa chính
                                </span>
                            </div>

                            <!-- Multi-image Mini Thumbnails with (x) delete button -->
                            <div class="flex-1 w-full">
                                <div class="text-[11px] text-outline mb-2">Danh sách ảnh đã chọn (click để phóng to, bấm dấu ✕ để xóa ảnh):</div>
                                <div id="multi-preview-thumbnails" class="grid grid-cols-4 sm:grid-cols-5 gap-2.5">
                                    <c:choose>
                                        <c:when test="${not empty listing.images}">
                                            <c:forEach items="${listing.images}" var="img" varStatus="st">
                                                <div class="form-thumb-item existing-image-item aspect-[4/3] border ${st.first ? 'border-[#FF4A1C] ring-1 ring-[#FF4A1C]' : 'border-surface-dim'} bg-surface-container-low p-0.5 relative overflow-hidden transition-all group" data-url="${img.imageUrl}">
                                                    <input type="hidden" name="existingImageUrls" value="${img.imageUrl}">
                                                    <img src="${img.imageUrl}" 
                                                         onclick="selectFormPreview('${img.imageUrl}', this.parentElement)" 
                                                         class="w-full h-full object-cover cursor-pointer">
                                                    <span class="thumb-badge absolute bottom-0.5 left-0.5 px-1 py-0.2 bg-black/75 text-white text-[9px] font-mono">${st.first ? 'Bìa' : '#' += st.count}</span>
                                                    <button type="button" 
                                                            onclick="removeExistingImage(this)" 
                                                            class="absolute top-1 right-1 w-5 h-5 rounded-full bg-red-600 hover:bg-red-700 text-white flex items-center justify-center text-[11px] font-bold shadow z-10 transition-transform hover:scale-110 cursor-pointer border-0" 
                                                            title="Xóa ảnh này">✕</button>
                                                </div>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <div id="no-thumbs-msg" class="col-span-full py-4 px-3 text-center text-xs text-outline italic border border-dashed border-surface-dim bg-surface-container-low">
                                                Chưa chọn ảnh nào. Hãy bấm nút "+ THÊM ẢNH TỪ MÁY TÍNH" ở trên để chọn ảnh thiết bị của bạn.
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
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

<script>
    // Khởi tạo và đảm bảo toàn bộ hàm xử lý hình ảnh form luôn sẵn sàng (chống cache trình duyệt)
    window.uploadedFilesList = window.uploadedFilesList || [];

    window.syncFileInput = window.syncFileInput || function() {
        const input = document.getElementById('image-file-input');
        if (!input) return;
        try {
            const dt = new DataTransfer();
            window.uploadedFilesList.forEach(item => {
                if (item.file) dt.items.add(item.file);
            });
            input.files = dt.files;
        } catch (e) {
            console.warn('DataTransfer sync warning:', e);
        }
    };

    window.selectFormPreview = function(src, element) {
        const previewImg = document.getElementById('image-live-preview');
        const previewEmpty = document.getElementById('main-preview-empty');
        const previewBadge = document.getElementById('main-preview-badge');

        if (previewImg && src) {
            previewImg.src = src;
            previewImg.classList.remove('hidden');
        }
        if (previewEmpty) {
            previewEmpty.classList.add('hidden');
        }
        if (previewBadge) {
            previewBadge.classList.remove('hidden');
        }

        document.querySelectorAll('#multi-preview-thumbnails .form-thumb-item').forEach(item => {
            item.classList.remove('border-[#FF4A1C]', 'ring-1', 'ring-[#FF4A1C]');
            item.classList.add('border-surface-dim');
        });

        if (element) {
            element.classList.add('border-[#FF4A1C]', 'ring-1', 'ring-[#FF4A1C]');
            element.classList.remove('border-surface-dim');
            const badge = element.querySelector('.thumb-badge');
            if (previewBadge && badge) {
                previewBadge.textContent = badge.textContent === 'Bìa' ? 'Ảnh bìa chính' : 'Ảnh ' + badge.textContent;
            }
        }
    };

    window.updateGalleryState = function() {
        const previewImg = document.getElementById('image-live-preview');
        const previewEmpty = document.getElementById('main-preview-empty');
        const previewBadge = document.getElementById('main-preview-badge');
        const sizeInfo = document.getElementById('file-size-info');
        const chosenText = document.getElementById('file-chosen-text');
        const thumbnailsContainer = document.getElementById('multi-preview-thumbnails');
        const allItems = document.querySelectorAll('#multi-preview-thumbnails .form-thumb-item');

        const totalCount = allItems.length;

        if (totalCount === 0) {
            if (previewImg) {
                previewImg.src = '';
                previewImg.classList.add('hidden');
            }
            if (previewEmpty) {
                previewEmpty.classList.remove('hidden');
            }
            if (previewBadge) {
                previewBadge.classList.add('hidden');
            }
            if (sizeInfo) {
                sizeInfo.textContent = 'Chưa có ảnh nào';
            }
            if (chosenText) {
                chosenText.textContent = '+ THÊM ẢNH TỪ MÁY TÍNH';
            }
            if (thumbnailsContainer && !document.getElementById('no-thumbs-msg')) {
                const emptyMsg = document.createElement('div');
                emptyMsg.id = 'no-thumbs-msg';
                emptyMsg.className = 'col-span-full py-4 px-3 text-center text-xs text-outline italic border border-dashed border-surface-dim bg-surface-container-low';
                emptyMsg.textContent = 'Chưa chọn ảnh nào. Hãy bấm nút "+ THÊM ẢNH TỪ MÁY TÍNH" ở trên để chọn ảnh thiết bị của bạn.';
                thumbnailsContainer.appendChild(emptyMsg);
            }
            return;
        }

        const noMsg = document.getElementById('no-thumbs-msg');
        if (noMsg) noMsg.remove();

        let hasActive = false;
        let activeItem = null;
        allItems.forEach((item, idx) => {
            const badge = item.querySelector('.thumb-badge');
            if (badge) {
                badge.textContent = (idx === 0) ? 'Bìa' : '#' + (idx + 1);
            }
            if (item.classList.contains('border-[#FF4A1C]')) {
                hasActive = true;
                activeItem = item;
            }
        });

        if (!hasActive && allItems[0]) {
            const firstImg = allItems[0].querySelector('img');
            if (firstImg) {
                selectFormPreview(firstImg.src, allItems[0]);
            }
        } else if (activeItem) {
            const activeImg = activeItem.querySelector('img');
            if (activeImg) {
                selectFormPreview(activeImg.src, activeItem);
            }
        }

        if (chosenText) {
            chosenText.textContent = '+ THÊM ẢNH KHÁC TỪ MÁY TÍNH';
        }

        let totalBytes = 0;
        window.uploadedFilesList.forEach(item => {
            if (item.file && item.file.size) totalBytes += item.file.size;
        });
        const mbText = totalBytes > 0 ? ' (' + (totalBytes / (1024 * 1024)).toFixed(1) + ' MB mới)' : '';
        if (sizeInfo) {
            sizeInfo.textContent = 'Đã chọn ' + totalCount + ' ảnh' + mbText;
        }
    };

    window.handleImageSelection = function(pickerInput) {
        if (!pickerInput || !pickerInput.files || pickerInput.files.length === 0) return;

        const newFiles = Array.from(pickerInput.files);
        const thumbnailsContainer = document.getElementById('multi-preview-thumbnails');
        const noMsg = document.getElementById('no-thumbs-msg');
        if (noMsg) noMsg.remove();

        newFiles.forEach(file => {
            const fileId = 'f_' + Date.now() + '_' + Math.random().toString(36).substring(2, 8);
            const objectUrl = URL.createObjectURL(file);

            window.uploadedFilesList.push({
                id: fileId,
                file: file,
                url: objectUrl
            });

            const thumbDiv = document.createElement('div');
            thumbDiv.id = 'thumb-new-' + fileId;
            thumbDiv.className = 'form-thumb-item new-image-item aspect-[4/3] border border-surface-dim bg-surface-container-low p-0.5 relative overflow-hidden transition-all group';

            const img = document.createElement('img');
            img.src = objectUrl;
            img.className = 'w-full h-full object-cover cursor-pointer';
            img.onclick = function() {
                selectFormPreview(objectUrl, thumbDiv);
            };

            const badge = document.createElement('span');
            badge.className = 'thumb-badge absolute bottom-0.5 left-0.5 px-1 py-0.2 bg-black/75 text-white text-[9px] font-mono';
            badge.textContent = '#';

            const removeBtn = document.createElement('button');
            removeBtn.type = 'button';
            removeBtn.className = 'absolute top-1 right-1 w-5 h-5 rounded-full bg-red-600 hover:bg-red-700 text-white flex items-center justify-center text-[11px] font-bold shadow z-10 transition-transform hover:scale-110 cursor-pointer border-0';
            removeBtn.title = 'Xóa ảnh này';
            removeBtn.textContent = '✕';
            removeBtn.onclick = function(ev) {
                ev.stopPropagation();
                removeNewUploadedFile(fileId);
            };

            thumbDiv.appendChild(img);
            thumbDiv.appendChild(badge);
            thumbDiv.appendChild(removeBtn);
            if (thumbnailsContainer) {
                thumbnailsContainer.appendChild(thumbDiv);
            }
        });

        pickerInput.value = '';

        syncFileInput();
        updateGalleryState();
    };

    window.removeNewUploadedFile = function(fileId) {
        const itemObj = window.uploadedFilesList.find(f => f.id === fileId);
        if (itemObj && itemObj.url) {
            try { URL.revokeObjectURL(itemObj.url); } catch (e) {}
        }
        window.uploadedFilesList = window.uploadedFilesList.filter(f => f.id !== fileId);
        const item = document.getElementById('thumb-new-' + fileId);
        if (item) {
            item.remove();
        }
        syncFileInput();
        updateGalleryState();
    };

    window.removeExistingImage = function(buttonElem) {
        if (!buttonElem) return;
        const item = buttonElem.closest('.form-thumb-item');
        if (item) {
            item.remove();
            updateGalleryState();
        }
    };

    // Khởi tạo trạng thái ban đầu
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', updateGalleryState);
    } else {
        updateGalleryState();
    }
</script>
<script src="${pageContext.request.contextPath}/js/listing.js?v=20261020_v3"></script>
<%@ include file="/WEB-INF/layout/footer.jspf" %>
