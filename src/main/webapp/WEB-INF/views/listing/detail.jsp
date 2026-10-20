<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Chi Tiết Laptop Cũ — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="used" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-12 py-8">
        
        <!-- Breadcrumb Navigation -->
        <nav class="flex items-center gap-2 text-xs text-outline mb-6">
            <a class="hover:text-primary transition-colors flex items-center gap-1" href="${pageContext.request.contextPath}/home">
                <span class="material-symbols-outlined text-[16px]">home</span>
                <span>Trang chủ</span>
            </a>
            <span>/</span>
            <a class="hover:text-primary transition-colors" href="${pageContext.request.contextPath}/listing/marketplace">Chợ đồ cũ</a>
            <span>/</span>
            <span class="text-primary font-bold truncate">
                <c:out value="${not empty listing ? listing.device.name : 'Chi tiết thiết bị'}"/>
            </span>
        </nav>

        <!-- Notification Banner: Creation Success -->
        <c:if test="${param.created == '1'}">
            <div class="mb-6 p-4 bg-emerald-50 border-l-4 border-emerald-500 text-emerald-950 flex items-center justify-between gap-4 shadow-sm">
                <div class="flex items-center gap-3">
                    <span class="material-symbols-outlined text-emerald-600 text-[26px]">task_alt</span>
                    <div>
                        <div class="font-extrabold text-sm uppercase tracking-wide text-emerald-900">ĐĂNG BÁN MÁY THÀNH CÔNG!</div>
                        <div class="text-xs text-emerald-800 mt-0.5">Tin đăng #${listing.id} đã được khởi tạo và lưu vào hệ thống an toàn.</div>
                    </div>
                </div>
                <a href="${pageContext.request.contextPath}/listing/marketplace" class="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold uppercase tracking-wider transition-colors shrink-0">
                    Xem Chợ Đồ Cũ
                </a>
            </div>
        </c:if>

        <!-- Notification Banner: Pending Admin Approval -->
        <c:if test="${not empty listing && !listing.isApproved}">
            <div class="mb-6 p-4 bg-amber-50 border-l-4 border-amber-500 text-amber-950 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-sm">
                <div class="flex items-center gap-3">
                    <span class="material-symbols-outlined text-amber-600 text-[26px] shrink-0">pending_actions</span>
                    <div>
                        <div class="font-extrabold text-sm uppercase tracking-wide text-amber-900">TIN ĐĂNG ĐANG CHỜ DUYỆT (QUY CHUẨN BR-15)</div>
                        <div class="text-xs text-amber-800 mt-0.5">
                            Tin đăng của bạn hiện đang ở trạng thái chờ duyệt. Quản trị viên (Admin) sẽ kiểm duyệt trước khi tin hiển thị công khai trên Chợ Đồ Cũ.
                        </div>
                    </div>
                </div>
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="px-3.5 py-1.5 bg-amber-600 hover:bg-amber-700 text-white text-xs font-bold uppercase tracking-wider transition-colors shrink-0">
                    Trang Duyệt Tin (Admin)
                </a>
            </div>
        </c:if>

        <!-- Two-Column Product Detail Layout -->
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
            
            <!-- LEFT COLUMN: Gallery & Trust Signals (7 cols) -->
            <div class="lg:col-span-7 flex flex-col gap-4">
                
                <!-- Main High-Res Viewer -->
                <div class="relative bg-surface-container-lowest border border-surface-dim p-4 overflow-hidden flex flex-col">
                    <div class="absolute top-4 left-4 z-10 flex flex-wrap items-center gap-2">
                        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 bg-primary text-on-primary text-[11px] font-bold uppercase tracking-wider">
                            <span class="w-2 h-2 bg-[#FF4A1C] inline-block"></span>
                            KIỂM ĐỊNH 48 BƯỚC
                        </span>
                        <span class="inline-flex items-center gap-1 px-2.5 py-1 bg-surface-container border border-surface-dim text-primary text-[11px] font-bold uppercase tracking-wider">
                            <span class="material-symbols-outlined text-[14px] text-emerald-600">verified</span>
                            ĐẠT TIÊU CHUẨN A+
                        </span>
                    </div>
                    <div class="absolute top-4 right-4 z-10 flex items-center gap-1 px-2.5 py-1 bg-surface-container-lowest/90 border border-surface-dim text-on-surface-variant text-[11px] font-mono">
                        <span class="material-symbols-outlined text-[14px]">center_focus_strong</span>
                        <span>ẢNH THỰC TẾ 100%</span>
                    </div>
                    
                    <div class="w-full aspect-[4/3] bg-surface-container flex items-center justify-center overflow-hidden mt-8">
                        <img id="main-preview-img" 
                             class="w-full h-full object-contain p-4 transition-transform duration-300"
                             src="${not empty listing.images ? listing.images[0].imageUrl : listing.mainImageUrl}" 
                             onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&auto=format&fit=crop&q=80';"
                             alt="Product View">
                    </div>

                    <div class="pt-3 border-t border-surface-dim flex items-center justify-between text-outline text-xs font-mono">
                        <span>SN: 8HQ9X23 • MAINBOARD & LINH KIỆN NGUYÊN BẢN</span>
                        <span class="text-emerald-700 font-bold">Chụp tại trạm kiểm định</span>
                    </div>
                </div>

                <!-- Interactive Thumbnails Grid (Render đúng danh sách ảnh thực tế của bài đăng) -->
                <div class="grid grid-cols-4 sm:grid-cols-5 gap-3" id="listing-thumbnails-grid">
                    <c:choose>
                        <c:when test="${not empty listing.images && listing.images.size() > 0}">
                            <c:forEach items="${listing.images}" var="img" varStatus="status">
                                <button type="button" 
                                        onclick="selectThumbnail('${img.imageUrl}', this)" 
                                        class="thumb-btn ${status.first ? 'active border-2 border-[#FF4A1C]' : 'border border-surface-dim hover:border-primary'} bg-surface-container-lowest p-1 aspect-[4/3] flex items-center justify-center cursor-pointer transition-all relative group overflow-hidden">
                                    <img src="${img.imageUrl}" 
                                         onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&auto=format&fit=crop&q=80';" 
                                         class="w-full h-full object-cover">
                                    <span class="absolute bottom-1 left-1 px-1 py-0.2 bg-black/80 text-white text-[9px] font-mono">${status.first ? 'Bìa' : '#' += status.count}</span>
                                </button>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <button type="button" 
                                    onclick="selectThumbnail('${listing.mainImageUrl}', this)" 
                                    class="thumb-btn active border-2 border-[#FF4A1C] bg-surface-container-lowest p-1 aspect-[4/3] flex items-center justify-center cursor-pointer transition-colors relative">
                                <img src="${listing.mainImageUrl}" 
                                     onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&auto=format&fit=crop&q=80';" 
                                     class="w-full h-full object-cover">
                                <span class="absolute bottom-1 left-1 px-1 py-0.2 bg-black/80 text-white text-[9px] font-mono">Bìa</span>
                            </button>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Trust Signals Box -->
                <div class="bg-surface-container-lowest border border-surface-dim p-4 grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div class="flex items-start gap-2.5">
                        <span class="material-symbols-outlined text-[#FF4A1C] text-[20px] shrink-0">published_with_changes</span>
                        <div>
                            <div class="text-xs font-bold uppercase text-primary">Bao test 7 ngày</div>
                            <div class="text-[11px] text-outline mt-0.5">Hoàn tiền 100% nếu phát sinh lỗi không đúng mô tả</div>
                        </div>
                    </div>
                    <div class="flex items-start gap-2.5">
                        <span class="material-symbols-outlined text-[#FF4A1C] text-[20px] shrink-0">fact_check</span>
                        <div>
                            <div class="text-xs font-bold uppercase text-primary">Biên bản 48 bước</div>
                            <div class="text-[11px] text-outline mt-0.5">Kiểm tra chi tiết BIOS, CPU throttling, Pin & Màn hình</div>
                        </div>
                    </div>
                    <div class="flex items-start gap-2.5">
                        <span class="material-symbols-outlined text-[#FF4A1C] text-[20px] shrink-0">lock</span>
                        <div>
                            <div class="text-xs font-bold uppercase text-primary">Giao dịch an toàn</div>
                            <div class="text-[11px] text-outline mt-0.5">Gặp mặt trực tiếp tại trạm hoặc ship COD đồng kiểm</div>
                        </div>
                    </div>
                </div>

                <!-- Description & Notes -->
                <div class="bg-surface-container-lowest border border-surface-dim p-6">
                    <h3 class="text-sm font-bold uppercase tracking-wider text-primary pb-3 border-b border-surface-dim">
                        MÔ TẢ CHI TIẾT TỪ NGƯỜI BÁN
                    </h3>
                    <div class="text-sm text-on-surface-variant leading-relaxed mt-4 space-y-3">
                        <c:choose>
                            <c:when test="${not empty listing.description}">
                                <p><c:out value="${listing.description}"/></p>
                            </c:when>
                            <c:otherwise>
                                <p>Máy cá nhân dùng làm việc kỹ thuật văn phòng tại nhà, giữ gìn rất cẩn thận.</p>
                                <p>Mặt A dán skin carbon nhôm từ ngày đầu bóc ra không vết trầy. Màn hình IPS sáng đều, zero dead pixel, không hở sáng.</p>
                                <p>Bàn phím nguyên độ nảy đặc trưng ThinkPad, chưa bóng dầu, trackpoint hoạt động hoàn hảo. Keo tản nhiệt Thermal Grizzly vừa thay định kỳ.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- RIGHT COLUMN: Hardware Specs, Defect Matrix, Seller Contact & Action (5 cols) -->
            <div class="lg:col-span-5 flex flex-col gap-6">
                
                <!-- Main Header & Price Card -->
                <div class="bg-surface-container-lowest border border-surface-dim p-6">
                    <div class="flex items-center justify-between mb-2">
                        <c:choose>
                            <c:when test="${listing.condition == 'DAMAGED'}">
                                <span class="listing-badge-damaged">HƯ HỎNG LINH KIỆN</span>
                            </c:when>
                            <c:when test="${listing.condition == 'USED_FAIR'}">
                                <span class="listing-badge-fair">NGOẠI HÌNH TẠM (95%)</span>
                            </c:when>
                            <c:otherwise>
                                <span class="listing-badge-good">LIKE NEW (99%)</span>
                            </c:otherwise>
                        </c:choose>
                        <span class="text-xs font-mono text-outline">MÃ TIN: #${not empty listing ? listing.id : 8841}</span>
                    </div>

                    <h1 class="text-2xl font-black uppercase tracking-tight text-primary mt-2">
                        <c:out value="${not empty listing ? listing.device.name : 'ThinkPad T14 Gen 2 Carbon Edition'}"/>
                    </h1>
                    <p class="text-xs text-outline mt-1 font-mono">
                        Dòng máy: <c:out value="${not empty listing ? listing.device.model : 'T14 Gen 2'}"/> • Đăng bởi Seller uy tín
                    </p>

                    <div class="py-4 my-4 border-t border-b border-surface-dim flex items-baseline justify-between">
                        <div>
                            <span class="text-xs uppercase tracking-wider text-outline block font-semibold">GIÁ NIÊM YẾT</span>
                            <span class="text-3xl font-black tracking-tight text-primary">
                                <fmt:formatNumber value="${not empty listing ? listing.price : 12800000}" pattern="#,###"/> ₫
                            </span>
                        </div>
                        <span class="text-xs bg-emerald-50 text-emerald-700 font-bold px-2 py-1 border border-emerald-200 uppercase">
                            GIÁ CẠNH TRANH
                        </span>
                    </div>

                    <!-- Hardware Spec Table -->
                    <div class="space-y-2 text-xs">
                        <div class="flex justify-between spec-grid-item">
                            <span class="text-outline uppercase font-semibold">Bộ vi xử lý (CPU)</span>
                            <span class="font-bold text-primary text-right">${not empty listing.device ? listing.device.cpu : 'Intel Core i5-1135G7 (4.2GHz)'}</span>
                        </div>
                        <div class="flex justify-between spec-grid-item">
                            <span class="text-outline uppercase font-semibold">Bộ nhớ RAM</span>
                            <span class="font-bold text-primary text-right">${not empty listing.device ? listing.device.ram : '16GB DDR4 3200MHz'}</span>
                        </div>
                        <div class="flex justify-between spec-grid-item">
                            <span class="text-outline uppercase font-semibold">Ổ cứng lưu trữ</span>
                            <span class="font-bold text-primary text-right">${not empty listing.device ? listing.device.storage : '512GB NVMe PCIe SSD'}</span>
                        </div>
                        <div class="flex justify-between spec-grid-item">
                            <span class="text-outline uppercase font-semibold">Màn hình</span>
                            <span class="font-bold text-primary text-right">14.0" FHD IPS (1920x1080) 300 nits</span>
                        </div>
                        <div class="flex justify-between spec-grid-item">
                            <span class="text-outline uppercase font-semibold">Chu kỳ sạc pin</span>
                            <span class="font-bold text-primary text-right">142 lần (Pin 95.4%)</span>
                        </div>
                    </div>
                </div>

                <!-- Hardware Defect Matrix (ListingProblem) -->
                <div class="bg-surface-container-lowest border border-surface-dim p-6">
                    <div class="flex items-center justify-between pb-3 border-b border-surface-dim">
                        <span class="text-xs font-bold uppercase tracking-wider text-primary">DANH SÁCH LỖI KHAI BÁO (LISTING PROBLEMS)</span>
                        <span class="material-symbols-outlined text-outline text-[18px]">build</span>
                    </div>
                    <div class="mt-4">
                        <c:choose>
                            <c:when test="${not empty listing.problems}">
                                <div class="space-y-2">
                                    <c:forEach items="${listing.problems}" var="prob">
                                        <div class="p-3 bg-amber-50 border border-amber-300 text-amber-900 text-xs flex items-start gap-2">
                                            <span class="material-symbols-outlined text-amber-700 text-[18px]">warning</span>
                                            <div>
                                                <div class="font-bold uppercase">${prob.problem.name}</div>
                                                <div class="text-amber-800 mt-0.5">${prob.note}</div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="p-3 bg-emerald-50 border border-emerald-300 text-emerald-900 text-xs flex items-center gap-2">
                                    <span class="material-symbols-outlined text-emerald-700 text-[18px]">check_circle</span>
                                    <span class="font-bold uppercase">NGUYÊN BẢN 100% • KHÔNG CÓ LỖI PHẦN CỨNG ĐƯỢC GHI NHẬN</span>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Seller Contact & Purchase Section -->
                <div class="bg-surface-container-lowest border border-surface-dim p-6 flex flex-col gap-4">
                    <div class="flex items-center gap-3 pb-3 border-b border-surface-dim">
                        <div class="w-10 h-10 rounded-full bg-primary text-white flex items-center justify-center font-bold">
                            ${not empty listing.seller ? listing.seller.name.substring(0,1) : 'T'}
                        </div>
                        <div>
                            <div class="text-sm font-bold text-primary uppercase">
                                ${not empty listing.seller ? listing.seller.name : 'Trần Minh Đức'}
                            </div>
                            <div class="text-xs text-outline">
                                Đã bán thành công: <strong class="text-primary">${not empty listing.seller ? listing.seller.completedSalesCount : 12} thiết bị</strong>
                            </div>
                        </div>
                    </div>

                    <!-- Secure Phone Button (BR-33, BR-34, BR-35) -->
                    <div id="seller-phone-container">
                        <c:choose>
                            <c:when test="${not empty sessionScope.currentUser}">
                                <button id="reveal-phone-btn" 
                                        type="button" 
                                        onclick="revealSellerContact('${not empty listing ? listing.id : 1}', '${pageContext.request.contextPath}')" 
                                        data-phone="${not empty listing.seller.phone ? listing.seller.phone : '0912 888 999'}"
                                        class="w-full h-12 bg-zinc-900 text-white font-bold text-xs uppercase tracking-wider flex items-center justify-center gap-2 hover:bg-black transition-colors">
                                    <span class="material-symbols-outlined text-[18px]">call</span>
                                    <span>XEM SỐ ĐIỆN THOẠI NGƯỜI BÁN</span>
                                </button>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/auth/login" 
                                   class="w-full h-12 bg-zinc-900 text-white font-bold text-xs uppercase tracking-wider flex items-center justify-center gap-2 hover:bg-black transition-colors text-center px-4">
                                    <span class="material-symbols-outlined text-[18px]">lock</span>
                                    <span>ĐĂNG NHẬP ĐỂ XEM SĐT NGƯỜI BÁN (BR-33)</span>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Primary Action: Chốt mua (Giao điểm duy nhất với OrderServlet của Nam theo handoff-nam-thanh.md) -->
                    <form method="post" action="${pageContext.request.contextPath}/order?action=create" class="w-full">
                        <input type="hidden" name="listingId" value="${not empty listing ? listing.id : 1}">
                        <button type="submit" 
                                class="w-full h-14 bg-[#FF4A1C] text-black font-extrabold text-sm uppercase tracking-widest flex items-center justify-center gap-2 hover:brightness-110 active:scale-[0.99] transition-all shadow-md cursor-pointer border-0">
                            <span>CHỐT MUA MÁY (GIỮ CHỖ)</span>
                            <span class="text-[18px]">→</span>
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>

<script src="${pageContext.request.contextPath}/js/listing.js"></script>
<%@ include file="/WEB-INF/layout/footer.jspf" %>
