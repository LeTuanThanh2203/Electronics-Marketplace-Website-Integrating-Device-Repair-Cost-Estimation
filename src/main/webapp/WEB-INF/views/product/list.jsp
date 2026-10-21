<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Cửa Hàng Laptop Mới Chính Hãng &amp; AI Gợi Ý — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="new" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>


    <!-- 1. Technical Info & Breadcrumb Bar -->
    <section class="w-full bg-brand-black text-white border-b border-brand-border">
        <div class="max-w-[1360px] mx-auto px-6 py-3 flex flex-col md:flex-row md:items-center justify-between gap-3 text-xs font-mono">
            <div class="flex flex-wrap items-center gap-2 text-white/70">
                <a class="hover:text-white transition-colors" href="${pageContext.request.contextPath}/">TRANG CHỦ</a>
                <span class="text-white/30">/</span>
                <span class="hover:text-white transition-colors">LAPTOP MỚI</span>
                <span class="text-white/30">/</span>
                <span class="text-brand-orange font-semibold tracking-wide">07 PRODUCT LIST // CỬA HÀNG MÁY MỚI CHÍNH HÃNG</span>
            </div>
            <div class="flex flex-wrap items-center gap-4 text-[11px] text-white/80">
                <div class="flex items-center gap-1.5">
                    <span class="w-2 h-2 rounded-full bg-emerald-400 inline-block animate-pulse"></span>
                    <span>KIỂM ĐỊNH SEAL: <strong class="text-white font-semibold">100% CHÍNH HÃNG</strong></span>
                </div>
                <span class="text-white/20 hidden sm:inline">|</span>
                <div class="flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-[14px] text-white/60">verified_user</span>
                    <span>BẢO HÀNH CHÍNH HÃNG 24T TẬN NƠI</span>
                </div>
            </div>
        </div>
    </section>

    <!-- 2. RECOMMENDATION RESULTS BANNER (Hiển thị khi khách vừa bấm gợi ý) -->
    <c:if test="${not empty recommendations}">
        <section class="w-full bg-amber-50 border-b border-amber-200 py-6">
            <div class="max-w-[1360px] mx-auto px-6">
                <div class="flex items-center justify-between pb-3 mb-4 border-b border-amber-200">
                    <div class="flex items-center gap-2">
                        <span class="material-symbols-outlined text-brand-orange text-2xl">magic_button</span>
                        <div>
                            <h2 class="font-bold text-sm font-space uppercase text-brand-black">KẾT QUẢ GỢI Ý THÔNG MINH CHO BẠN</h2>
                            <p class="text-xs text-brand-gray font-mono mt-0.5">
                                Ngân sách: <strong class="text-brand-black"><fmt:formatNumber value="${recBudget}" pattern="#,###"/>₫</strong> 
                                | Tác vụ: <strong class="text-brand-black">${recNeed}</strong>
                                <c:if test="${not empty recBrand && recBrand != 'ALL'}"> | Hãng: <strong class="text-brand-black">${recBrand}</strong></c:if>
                            </p>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/products" class="text-xs font-mono text-brand-orange hover:underline">Xem toàn bộ kho máy</a>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
                    <c:forEach var="rec" items="${recommendations}" varStatus="status">
                        <c:if test="${status.index < 3}">
                            <div class="bg-white border-2 border-brand-orange p-4 flex flex-col justify-between relative shadow-sm">
                                <div class="absolute -top-3 right-4 bg-brand-orange text-white text-[11px] font-mono font-bold px-2 py-0.5">
                                    MATCH: ${rec.matchScore}%
                                </div>
                                <div>
                                    <span class="text-[10px] font-mono uppercase text-brand-gray">${rec.product.brand} • TOP ${status.index + 1} PHÙ HỢP NHẤT</span>
                                    <h3 class="font-bold text-sm font-space text-brand-black mt-1 leading-snug">${rec.product.name}</h3>
                                    <div class="text-base font-bold font-space text-brand-orange mt-2">
                                        <fmt:formatNumber value="${rec.product.price}" pattern="#,###"/> ₫
                                    </div>
                                    <ul class="mt-3 space-y-1 text-xs text-[#444748] border-t border-gray-100 pt-2 font-mono">
                                        <c:forEach var="highlight" items="${rec.matchHighlights}">
                                            <li class="flex items-start gap-1.5">
                                                <span class="text-emerald-600 font-bold">✓</span>
                                                <span>${highlight}</span>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </div>
                                <div class="mt-4 pt-3 border-t border-gray-100 flex gap-2">
                                    <a href="${pageContext.request.contextPath}/products?action=detail&id=${rec.product.id}" 
                                       class="w-full text-center py-2 bg-brand-black text-white hover:bg-brand-orange text-xs font-mono font-semibold uppercase tracking-wider transition-colors">
                                        XEM CHI TIẾT →
                                    </a>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>
            </div>
        </section>
    </c:if>

    <!-- 3. STORE WORKSPACE (SIDEBAR + MAIN STORE) -->
    <div class="w-full max-w-[1360px] mx-auto px-6 py-8 flex flex-col lg:flex-row gap-8 items-start">

        <!-- LEFT SIDEBAR: FILTERS -->
        <aside class="w-full lg:w-[280px] shrink-0 border border-brand-border bg-white p-5">
            <form id="filterForm" action="${pageContext.request.contextPath}/products" method="GET">
                <input type="hidden" name="action" value="list">

                <!-- Filter Header -->
                <div class="flex justify-between items-center pb-3 border-b border-brand-border">
                    <div class="flex items-center gap-2">
                        <span class="w-2 h-2 bg-brand-black inline-block"></span>
                        <h2 class="font-bold text-xs font-mono uppercase tracking-wider text-brand-black">BỘ LỌC TÌM KIẾM</h2>
                    </div>
                    <a href="${pageContext.request.contextPath}/products" class="text-xs text-brand-orange hover:underline font-mono">Xóa bộ lọc</a>
                </div>

                <!-- 1. HÃNG SẢN XUẤT -->
                <div class="py-4 border-b border-brand-border">
                    <h3 class="text-xs font-bold text-brand-black uppercase font-mono tracking-wider mb-3">1. HÃNG SẢN XUẤT</h3>
                    <div class="space-y-2 text-xs">
                        <c:forEach var="brandName" items="${['Dell', 'Apple', 'ASUS', 'Lenovo', 'Acer', 'HP']}">
                            <label class="flex items-center justify-between cursor-pointer group">
                                <div class="flex items-center gap-2">
                                    <input type="checkbox" name="brand" value="${brandName}" 
                                           ${selectedBrands != null && selectedBrands.contains(brandName) ? 'checked' : ''}
                                           class="w-4 h-4 rounded-none border border-brand-border text-brand-black accent-brand-black">
                                    <span class="group-hover:text-brand-black text-[#444748]">${brandName}</span>
                                </div>
                            </label>
                        </c:forEach>
                    </div>
                </div>

                <!-- 2. KHOẢNG GIÁ (VND) -->
                <div class="py-4 border-b border-brand-border">
                    <h3 class="text-xs font-bold text-brand-black uppercase font-mono tracking-wider mb-3">2. KHOẢNG GIÁ (VND)</h3>
                    <div class="space-y-2">
                        <div class="flex items-center gap-2">
                            <span class="text-[11px] font-mono text-gray-400 w-7">TỪ</span>
                            <input class="w-full border border-brand-border px-2 py-1 text-xs font-mono text-right focus:border-brand-black focus:outline-none" 
                                   type="text" name="minPrice" id="minPriceInput" value="${minPrice}" placeholder="15.000.000">
                        </div>
                        <div class="flex items-center gap-2">
                            <span class="text-[11px] font-mono text-gray-400 w-7">ĐẾN</span>
                            <input class="w-full border border-brand-border px-2 py-1 text-xs font-mono text-right focus:border-brand-black focus:outline-none" 
                                   type="text" name="maxPrice" id="maxPriceInput" value="${maxPrice}" placeholder="45.000.000">
                        </div>
                    </div>
                    <!-- Quick Pills -->
                    <div class="grid grid-cols-2 gap-1.5 mt-3">
                        <button type="button" onclick="setPriceRange('0', '15000000')" class="border border-brand-border text-[10px] font-mono py-1 px-1.5 hover:border-brand-black text-center transition-colors">&lt; 15tr</button>
                        <button type="button" onclick="setPriceRange('15000000', '25000000')" class="border border-brand-border text-[10px] font-mono py-1 px-1.5 hover:border-brand-black text-center transition-colors">15 - 25tr</button>
                        <button type="button" onclick="setPriceRange('25000000', '40000000')" class="border border-brand-border text-[10px] font-mono py-1 px-1.5 hover:border-brand-black text-center transition-colors">25 - 40tr</button>
                        <button type="button" onclick="setPriceRange('40000000', '100000000')" class="border border-brand-border text-[10px] font-mono py-1 px-1.5 hover:border-brand-black text-center transition-colors">&gt; 40tr</button>
                    </div>
                </div>

                <!-- 3. NHU CẦU SỬ DỤNG (TARGET NEED) -->
                <div class="py-4 border-b border-brand-border">
                    <h3 class="text-xs font-bold text-brand-black uppercase font-mono tracking-wider mb-3">3. NHU CẦU SỬ DỤNG</h3>
                    <div class="space-y-1.5 text-xs font-mono">
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="need" value="ALL" ${empty selectedNeed || selectedNeed == 'ALL' ? 'checked' : ''} class="accent-brand-black">
                            <span>TẤT CẢ</span>
                        </label>
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="need" value="OFFICE" ${selectedNeed == 'OFFICE' ? 'checked' : ''} class="accent-brand-black">
                            <span>VĂN PHÒNG & DI ĐỘNG</span>
                        </label>
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="need" value="GAMING" ${selectedNeed == 'GAMING' ? 'checked' : ''} class="accent-brand-black">
                            <span>GAMING HIỆU NĂNG CAO</span>
                        </label>
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="need" value="GRAPHICS" ${selectedNeed == 'GRAPHICS' ? 'checked' : ''} class="accent-brand-black">
                            <span>ĐỒ HỌA CHUẨN MÀU OLED</span>
                        </label>
                    </div>
                </div>

                <!-- 4. DUNG LƯỢNG RAM -->
                <div class="py-4 border-b border-brand-border">
                    <h3 class="text-xs font-bold text-brand-black uppercase font-mono tracking-wider mb-3">4. RAM TỐI THIỂU</h3>
                    <div class="grid grid-cols-4 gap-1 text-xs font-mono">
                        <button type="button" onclick="setRam(8)" class="border border-brand-border py-1.5 text-center hover:border-brand-black ${minRam == '8' ? 'bg-brand-black text-white font-bold' : ''}">8GB</button>
                        <button type="button" onclick="setRam(16)" class="border border-brand-border py-1.5 text-center hover:border-brand-black ${minRam == '16' ? 'bg-brand-black text-white font-bold' : ''}">16GB</button>
                        <button type="button" onclick="setRam(32)" class="border border-brand-border py-1.5 text-center hover:border-brand-black ${minRam == '32' ? 'bg-brand-black text-white font-bold' : ''}">32GB</button>
                        <button type="button" onclick="setRam(64)" class="border border-brand-border py-1.5 text-center hover:border-brand-black ${minRam == '64' ? 'bg-brand-black text-white font-bold' : ''}">64GB</button>
                    </div>
                    <input type="hidden" name="minRam" id="minRamInput" value="${minRam}">
                </div>

                <!-- Submit Button -->
                <div class="pt-4">
                    <button type="submit" class="w-full bg-brand-black text-white py-2.5 text-xs font-mono font-bold uppercase tracking-wider hover:bg-brand-orange transition-colors">
                        ÁP DỤNG BỘ LỌC
                    </button>
                </div>
            </form>
        </aside>

        <!-- RIGHT COLUMN: MAIN STORE LISTING -->
        <div class="flex-1 w-full flex flex-col">

            <!-- 3 Bento Category Cards -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <!-- Card 1 -->
                <div class="bg-white border border-brand-border p-5 flex flex-col justify-between hover:border-brand-black transition-colors">
                    <div>
                        <span class="text-[11px] font-mono text-gray-500 uppercase tracking-wider block mb-2">[01] DI ĐỘNG & PIN 18H</span>
                        <h3 class="font-bold text-base font-space uppercase text-brand-black">LAPTOP VĂN PHÒNG</h3>
                        <p class="text-xs text-[#444748] mt-1.5 leading-relaxed">
                            Mỏng nhẹ dưới 1.3kg, thời lượng pin cả ngày, phím êm và nhận diện khuôn mặt.
                        </p>
                    </div>
                    <a class="text-xs font-semibold text-brand-black mt-4 inline-flex items-center gap-1 hover:text-brand-orange" 
                       href="${pageContext.request.contextPath}/products?action=list&need=OFFICE">
                        Xem dòng máy này →
                    </a>
                </div>

                <!-- Card 2: Gaming -->
                <div class="bg-brand-black border border-brand-black p-5 flex flex-col justify-between text-white">
                    <div>
                        <span class="text-[11px] font-mono text-brand-orange uppercase tracking-wider block mb-2">[02] HIỆU NĂNG TỐI ĐA</span>
                        <h3 class="font-bold text-base font-space uppercase text-white">LAPTOP GAMING</h3>
                        <p class="text-xs text-white/70 mt-1.5 leading-relaxed">
                            Card RTX 40 Series, tản nhiệt buồng hơi, màn hình tần số quét 165Hz - 240Hz.
                        </p>
                    </div>
                    <a class="text-xs font-semibold text-brand-orange mt-4 inline-flex items-center gap-1 hover:underline" 
                       href="${pageContext.request.contextPath}/products?action=list&need=GAMING">
                        Xem dòng máy này →
                    </a>
                </div>

                <!-- Card 3: Graphics -->
                <div class="bg-brand-card border border-brand-border p-5 flex flex-col justify-between hover:border-brand-black transition-colors">
                    <div>
                        <span class="text-[11px] font-mono text-gray-500 uppercase tracking-wider block mb-2">[03] CHUẨN MÀU DCI-P3</span>
                        <h3 class="font-bold text-base font-space uppercase text-brand-black">LAPTOP ĐỒ HỌA</h3>
                        <p class="text-xs text-[#444748] mt-1.5 leading-relaxed">
                            Màn hình OLED / Liquid Retina XDR, độ phủ màu 100% DCI-P3, RAM từ 16GB.
                        </p>
                    </div>
                    <a class="text-xs font-semibold text-brand-black mt-4 inline-flex items-center gap-1 hover:text-brand-orange" 
                       href="${pageContext.request.contextPath}/products?action=list&need=GRAPHICS">
                        Xem dòng máy này →
                    </a>
                </div>
            </div>

            <!-- SORT BAR & RESULTS COUNT -->
            <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center py-3 border-y border-brand-border mb-6 bg-white px-4 gap-3">
                <div class="flex items-center gap-2.5">
                    <span class="font-bold text-sm text-brand-black font-space tracking-tight">
                        <c:choose>
                            <c:when test="${not empty totalResults}">${totalResults}</c:when>
                            <c:otherwise>${products.size()}</c:otherwise>
                        </c:choose> SẢN PHẨM CHÍNH HÃNG
                    </span>
                    <span class="text-brand-border">/</span>
                    <div class="flex items-center gap-1 text-[11px] font-mono text-emerald-700 bg-emerald-50 px-2 py-0.5 border border-emerald-200">
                        <span class="w-1.5 h-1.5 rounded-full bg-emerald-600"></span>
                        NGUYÊN SEAL NHÀ MÁY
                    </div>
                </div>

                <!-- Sort dropdown -->
                <div class="flex items-center gap-4">
                    <div class="flex items-center gap-2 text-xs font-mono">
                        <span class="text-gray-500 text-[11px]">SẮP XẾP:</span>
                        <select onchange="applySort(this.value)" class="border border-brand-border text-xs px-2.5 py-1.5 bg-white font-medium focus:border-brand-black focus:outline-none">
                            <option value="" ${empty selectedSort ? 'selected' : ''}>Mặc định</option>
                            <option value="price_asc" ${selectedSort == 'price_asc' ? 'selected' : ''}>Giá: Thấp đến Cao</option>
                            <option value="price_desc" ${selectedSort == 'price_desc' ? 'selected' : ''}>Giá: Cao đến Thấp</option>
                            <option value="name" ${selectedSort == 'name' ? 'selected' : ''}>Tên A-Z</option>
                        </select>
                    </div>
                </div>
            </div>

            <!-- PRODUCT GRID (Lặp qua danh sách sản phẩm bằng JSTL) -->
            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
                <c:choose>
                    <c:when test="${empty products}">
                        <div class="col-span-full py-16 text-center border border-dashed border-brand-border bg-white">
                            <span class="material-symbols-outlined text-4xl text-gray-400">devices</span>
                            <p class="font-mono text-sm text-gray-500 mt-2">Không tìm thấy máy phù hợp với bộ lọc hiện tại.</p>
                            <a href="${pageContext.request.contextPath}/products" class="inline-block mt-3 px-4 py-2 bg-brand-black text-white text-xs font-mono uppercase font-bold hover:bg-brand-orange transition-colors">
                                Xóa bộ lọc
                            </a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="p" items="${products}">
                            <article class="bg-white border border-brand-border p-4 flex flex-col justify-between hover:border-brand-black transition-all relative group">
                                <div>
                                    <!-- Badge Header -->
                                    <div class="flex items-center justify-between gap-1 mb-2">
                                        <span class="font-mono text-[10px] text-gray-500 tracking-wider uppercase">${p.brand} OFFICIAL</span>
                                        <div class="flex items-center gap-1">
                                            <c:if test="${not empty p.discountTag}">
                                                <span class="bg-brand-orange text-white text-[10px] font-bold px-1.5 py-0.5 uppercase tracking-wide">${p.discountTag}</span>
                                            </c:if>
                                            <span class="border border-emerald-500 text-emerald-700 text-[10px] px-1 py-0.5 font-mono">${p.badge}</span>
                                        </div>
                                    </div>

                                    <!-- Product Image -->
                                    <div class="aspect-[4/3] bg-white flex items-center justify-center p-3 my-2 border border-[#EAEAE8] overflow-hidden relative">
                                        <c:choose>
                                            <c:when test="${not empty p.imageUrl}">
                                                <img class="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300" 
                                                     src="${p.imageUrl}" alt="${p.name}">
                                            </c:when>
                                            <c:otherwise>
                                                <span class="material-symbols-outlined text-5xl text-gray-300">laptop</span>
                                            </c:otherwise>
                                        </c:choose>
                                        <span class="absolute bottom-1 right-2 text-[10px] font-mono text-gray-400">#${p.sku}</span>
                                    </div>

                                    <!-- Product Details -->
                                    <div class="mt-2">
                                        <p class="text-[11px] text-gray-400 uppercase font-mono">${p.brand} • ${p.series}</p>
                                        <h4 class="font-bold text-[15px] font-space text-brand-black leading-snug mt-0.5 group-hover:text-brand-orange transition-colors">
                                            <a href="${pageContext.request.contextPath}/products?action=detail&id=${p.id}">${p.name}</a>
                                        </h4>
                                        <p class="text-xs text-[#444748] mt-1.5 font-mono leading-relaxed line-clamp-2">
                                            ${p.cpu} • RAM ${p.ramGb}GB • SSD ${p.storageGb}GB • ${p.screenSize}" ${p.screenResolution}
                                        </p>
                                        <div class="flex items-center gap-1 text-[11px] text-emerald-700 mt-2 font-mono">
                                            <span class="material-symbols-outlined text-[13px]">verified_user</span>
                                            <span>${p.warranty}</span>
                                        </div>
                                    </div>
                                </div>

                                <!-- Price & Action CTA -->
                                <div class="pt-4 mt-3 border-t border-brand-border flex flex-col gap-2.5">
                                    <div>
                                        <c:if test="${not empty p.originalPrice && p.originalPrice > p.price}">
                                            <span class="text-[11px] text-gray-400 line-through font-mono"><fmt:formatNumber value="${p.originalPrice}" pattern="#,###"/> ₫</span>
                                        </c:if>
                                        <div class="text-lg font-bold font-space text-brand-black tracking-tight">
                                            <fmt:formatNumber value="${p.price}" pattern="#,###"/> ₫
                                        </div>
                                    </div>
                                    <div class="grid grid-cols-2 gap-2">
                                        <a href="${pageContext.request.contextPath}/products?action=detail&id=${p.id}" 
                                           class="border border-brand-black py-2 px-2 text-xs font-semibold hover:bg-brand-black hover:text-white transition text-center uppercase tracking-wider font-mono flex items-center justify-center">
                                            CHI TIẾT →
                                        </a>
                                        <form action="${pageContext.request.contextPath}/cart" method="POST" class="m-0 p-0 block w-full">
                                            <input type="hidden" name="action" value="add">
                                            <input type="hidden" name="productId" value="${p.id}">
                                            <input type="hidden" name="buyNow" value="true">
                                            <button type="submit" class="w-full h-full bg-brand-orange text-white py-2 px-2 text-xs font-semibold hover:bg-black transition text-center uppercase tracking-wider font-mono cursor-pointer border-0">
                                                MUA NGAY
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </article>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- 4. BOTTOM ASSURANCE BAR (CHÍNH HÃNG 3 KHỐI) -->
    <section class="w-full bg-white border-t border-brand-border py-8 mt-12">
        <div class="max-w-[1360px] mx-auto px-6 grid grid-cols-1 md:grid-cols-3 gap-0 border border-brand-border">
            <div class="p-6 border-b md:border-b-0 md:border-r border-brand-border flex flex-col justify-between">
                <div class="flex items-center gap-3 mb-3">
                    <div class="w-8 h-8 bg-brand-black text-white flex items-center justify-center font-mono text-xs font-bold">01</div>
                    <h3 class="font-bold text-sm font-space text-brand-black uppercase tracking-wide">100% NGUYÊN SEAL CHÍNH HÃNG</h3>
                </div>
                <p class="text-xs text-[#444748] leading-relaxed">
                    Đền tiền 200% nếu phát hiện sản phẩm đã mở hộp, refurbished hoặc không kích hoạt được gói bảo hành điện tử chính hãng từ nhà sản xuất.
                </p>
            </div>
            <div class="p-6 border-b md:border-b-0 md:border-r border-brand-border flex flex-col justify-between">
                <div class="flex items-center gap-3 mb-3">
                    <div class="w-8 h-8 bg-brand-black text-white flex items-center justify-center font-mono text-xs font-bold">02</div>
                    <h3 class="font-bold text-sm font-space text-brand-black uppercase tracking-wide">BẢO HÀNH CHÍNH HÃNG TẬN NƠI</h3>
                </div>
                <p class="text-xs text-[#444748] leading-relaxed">
                    Hỗ trợ kỹ thuật On-Site trực tiếp từ 12 - 24 tháng theo đúng tiêu chuẩn Dell ProSupport, Asus VIP, Lenovo Premier và Apple Authorized Service.
                </p>
            </div>
            <div class="p-6 flex flex-col justify-between">
                <div class="flex items-center gap-3 mb-3">
                    <div class="w-8 h-8 bg-brand-orange text-white flex items-center justify-center font-mono text-xs font-bold">03</div>
                    <h3 class="font-bold text-sm font-space text-brand-black uppercase tracking-wide">HỖ TRỢ TRẢ GÓP 0% LÃI SUẤT</h3>
                </div>
                <p class="text-xs text-[#444748] leading-relaxed">
                    Thanh toán linh hoạt qua thẻ tín dụng hơn 25 ngân hàng hoặc thẩm định hồ sơ công dân trực tuyến trong 5 phút, không giữ giấy tờ tùy thân.
                </p>
            </div>
        </div>
    </section>
</main>

<!-- MODAL AI RECOMMENDATION ENGINE (Chức năng 2 của Phước Anh) -->
<div id="recModal" class="fixed inset-0 z-50 bg-black/60 hidden flex items-center justify-center p-4">
    <div class="bg-white border-2 border-brand-black w-full max-w-lg p-6 relative shadow-2xl">
        <button onclick="toggleRecModal()" class="absolute top-4 right-4 text-gray-400 hover:text-black">
            <span class="material-symbols-outlined">close</span>
        </button>

        <div class="flex items-center gap-2 mb-4">
            <span class="w-3 h-3 bg-brand-orange inline-block"></span>
            <h3 class="font-space font-bold text-lg uppercase text-brand-black">THUẬT TOÁN GỢI Ý MÁY (MATCHING ENGINE)</h3>
        </div>
        <p class="text-xs text-[#444748] mb-4 font-mono leading-relaxed">
            Hệ thống sẽ tính điểm Matching Score theo ngân sách và cấu hình phần cứng tối ưu nhất cho nhu cầu của bạn.
        </p>

        <form action="${pageContext.request.contextPath}/products" method="GET" class="space-y-4">
            <input type="hidden" name="action" value="recommend">

            <div>
                <label class="block text-xs font-mono font-bold uppercase text-brand-black mb-1">1. Ngân sách tối đa của bạn (VND):</label>
                <input type="text" name="budget" required placeholder="Ví dụ: 35000000 hoặc 35.000.000"
                       class="w-full border border-brand-border px-3 py-2 text-sm font-mono focus:border-brand-black focus:outline-none">
            </div>

            <div>
                <label class="block text-xs font-mono font-bold uppercase text-brand-black mb-1">2. Nhu cầu sử dụng chính:</label>
                <select name="need" class="w-full border border-brand-border px-3 py-2 text-xs font-mono bg-white focus:border-brand-black focus:outline-none">
                    <option value="OFFICE">Dân văn phòng / Sinh viên (Mỏng nhẹ, pin trâu 18h)</option>
                    <option value="GAMING">Game thủ / Streamer (Card rời RTX, màn hình 165Hz+)</option>
                    <option value="GRAPHICS">Đồ họa / Kiến trúc / Video (Màn hình OLED chuẩn màu DCI-P3)</option>
                </select>
            </div>

            <div class="grid grid-cols-2 gap-3">
                <div>
                    <label class="block text-xs font-mono font-bold uppercase text-brand-black mb-1">3. Thương hiệu ưa thích:</label>
                    <select name="brand" class="w-full border border-brand-border px-3 py-2 text-xs font-mono bg-white focus:border-brand-black focus:outline-none">
                        <option value="ALL">Tất cả thương hiệu</option>
                        <option value="Apple">Apple (MacBook)</option>
                        <option value="Dell">Dell</option>
                        <option value="ASUS">ASUS</option>
                        <option value="Lenovo">Lenovo (ThinkPad/Legion)</option>
                        <option value="Acer">Acer</option>
                    </select>
                </div>
                <div>
                    <label class="block text-xs font-mono font-bold uppercase text-brand-black mb-1">4. RAM tối thiểu:</label>
                    <select name="minRam" class="w-full border border-brand-border px-3 py-2 text-xs font-mono bg-white focus:border-brand-black focus:outline-none">
                        <option value="8">Tối thiểu 8GB</option>
                        <option value="16" selected>Tối thiểu 16GB (Khuyên dùng)</option>
                        <option value="32">Tối thiểu 32GB</option>
                    </select>
                </div>
            </div>

            <div class="pt-2">
                <button type="submit" class="w-full py-3 bg-brand-orange text-white hover:bg-black font-space font-bold text-sm uppercase tracking-wider transition-colors">
                    TÍNH TOÁN VÀ GỢI Ý MÁY PHÙ HỢP →
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    function toggleRecModal() {
        const modal = document.getElementById('recModal');
        if (modal) {
            modal.classList.toggle('hidden');
        }
    }

    function setPriceRange(min, max) {
        document.getElementById('minPriceInput').value = min;
        document.getElementById('maxPriceInput').value = max;
        document.getElementById('filterForm').submit();
    }

    function setRam(ram) {
        document.getElementById('minRamInput').value = ram;
        document.getElementById('filterForm').submit();
    }

    function applySort(sortVal) {
        const url = new URL(window.location.href);
        url.searchParams.set('sort', sortVal);
        window.location.href = url.toString();
    }
</script>

<%@ include file="/WEB-INF/layout/footer.jspf" %>

