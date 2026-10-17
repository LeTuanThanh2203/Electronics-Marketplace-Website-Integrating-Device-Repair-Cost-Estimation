<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chợ Laptop Cũ - ElectronicsMarketplace</title>
    
    <link href="https://fonts.googleapis.com" rel="preconnect">
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet">
    
    <!-- Custom Styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/listing.css">

    <!-- Tailwind Configuration & CDN for Stitch Source of Truth -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script id="tailwind-config">
    tailwind.config = {
      darkMode: "class",
      theme: {
        extend: {
          colors: {
            "surface": "#f9f9f7", "surface-dim": "#dadad8", "surface-container-lowest": "#ffffff",
            "surface-container-low": "#f4f4f2", "surface-container": "#eeeeec", "surface-container-high": "#e8e8e6",
            "on-surface": "#1a1c1b", "on-surface-variant": "#444748", "outline": "#747878",
            "primary": "#000000", "on-primary": "#ffffff", "primary-container": "#1c1b1b",
            "secondary-container": "#dc3200"
          },
          fontFamily: {
            "body": ["Be Vietnam Pro", "sans-serif"],
            "headline": ["Be Vietnam Pro", "sans-serif"]
          }
        }
      }
    };
    </script>
</head>
<body class="bg-surface font-body text-on-surface antialiased">

<!-- Unified Fixed Header (100% Đồng bộ home.jsp) -->
<header class="fixed top-0 left-0 right-0 z-50 bg-surface-container-lowest border-b border-surface-dim">
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-12 h-16 flex items-center justify-between">
        <div class="flex items-center shrink-0">
            <a class="text-[18px] text-primary tracking-wider uppercase font-extrabold flex items-center gap-2" href="${pageContext.request.contextPath}/home">
                <span class="w-3.5 h-3.5 bg-[#FF4A1C] inline-block"></span>
                ELECTRONICSMARKETPLACE
            </a>
        </div>
        <nav class="hidden md:flex items-center h-full gap-6">
            <a class="text-[13px] uppercase tracking-wider text-on-surface-variant hover:text-primary transition-colors py-2 border-b-2 border-transparent hover:border-primary font-medium" href="${pageContext.request.contextPath}/product/list">LAPTOP MỚI</a>
            <a class="text-[13px] uppercase tracking-wider text-primary border-b-2 border-primary font-bold transition-colors py-2" href="${pageContext.request.contextPath}/listing/marketplace">CHỢ LAPTOP CŨ</a>
            <a class="text-[13px] uppercase tracking-wider text-on-surface-variant hover:text-primary transition-colors py-2 border-b-2 border-transparent hover:border-primary font-medium" href="${pageContext.request.contextPath}/repair/request">SỬA CHỮA</a>
        </nav>
        <div class="flex items-center gap-3">
            <a class="inline-flex items-center gap-1.5 h-9 px-4 bg-[#FF4A1C] text-black font-bold text-xs uppercase tracking-wider hover:brightness-110 transition-all shadow-sm" href="${pageContext.request.contextPath}/listing/form">
                <span class="material-symbols-outlined text-[16px]">add_circle</span>
                <span>ĐĂNG BÁN MÁY</span>
            </a>
            <a class="flex items-center justify-center w-9 h-9 border border-surface-dim text-on-surface-variant hover:text-primary hover:border-primary transition-colors" href="${pageContext.request.contextPath}/cart" title="Giỏ hàng">
                <span class="material-symbols-outlined text-[18px]">shopping_bag</span>
            </a>
            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <a class="flex items-center gap-1.5 text-xs font-semibold text-primary uppercase ml-1" href="${pageContext.request.contextPath}/auth/profile">
                        <span class="material-symbols-outlined text-[20px]">account_circle</span>
                        <span class="hidden lg:inline"><c:out value="${sessionScope.currentUser.name}"/></span>
                    </a>
                </c:when>
                <c:otherwise>
                    <a class="flex items-center gap-1 text-xs uppercase text-on-surface-variant hover:text-primary transition-colors font-medium ml-1" href="${pageContext.request.contextPath}/auth/login">
                        <span class="material-symbols-outlined text-[18px]">person</span>
                        <span class="hidden lg:inline">Tài khoản</span>
                    </a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<main class="w-full pt-16 bg-surface min-h-[calc(100vh-64px)]">
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-12 py-8">
        
        <!-- Breadcrumb & Header Section -->
        <div class="pb-6 border-b border-surface-dim">
            <nav class="flex items-center gap-2 text-xs text-outline mb-3">
                <a class="hover:text-primary transition-colors" href="${pageContext.request.contextPath}/home">Trang chủ</a>
                <span>/</span>
                <span class="text-primary font-bold">Chợ Laptop Cũ</span>
            </nav>
            <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
                <div>
                    <h1 class="text-3xl lg:text-4xl font-extrabold uppercase tracking-tight text-primary flex items-center gap-3">
                        <span class="w-2.5 h-8 bg-[#FF4A1C] inline-block"></span> CHỢ LAPTOP CŨ
                    </h1>
                    <p class="text-sm text-outline mt-2 max-w-2xl leading-relaxed">
                        Sàn giao dịch thiết bị đã qua sử dụng với chứng nhận kiểm định độc lập 48 bước. Minh bạch lịch sử phần cứng, ảnh thực tế 100%, bảo đảm không tráo đổi linh kiện.
                    </p>
                </div>
                <div class="flex items-center gap-2 bg-surface-container border border-surface-dim px-3 py-1.5 self-start md:self-auto font-mono text-xs uppercase font-semibold">
                    <span class="w-2 h-2 rounded-full bg-emerald-600 animate-pulse"></span>
                    <span>TRẠM KIỂM ĐỊNH: ONLINE</span>
                </div>
            </div>
        </div>

        <!-- Filter & Search Bar with EL Parameters -->
        <section class="mt-6 bg-surface-container-lowest border border-surface-dim p-4">
            <form id="marketplace-filter-form" method="GET" action="${pageContext.request.contextPath}/listing/marketplace" class="flex flex-col xl:flex-row items-stretch xl:items-center gap-3">
                <div class="relative flex-1 min-w-[260px]">
                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">search</span>
                    <input id="live-search-input" name="keyword" value="${param.keyword}" class="w-full h-11 pl-10 pr-10 bg-surface-container-low border border-surface-dim text-sm text-primary placeholder:text-outline focus:border-primary focus:outline-none transition-colors" placeholder="Tìm theo tên máy, cấu hình hoặc người bán..." type="text" autocomplete="off">
                    <span id="search-spinner" class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-[#FF4A1C] text-[18px] animate-spin hidden pointer-events-none">progress_activity</span>
                    <button type="button" id="search-clear-btn" class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-primary ${empty param.keyword ? 'hidden' : ''}" title="Xóa tìm kiếm">
                        <span class="material-symbols-outlined text-[18px]">close</span>
                    </button>
                </div>
                <div class="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                    <div class="relative">
                        <select name="brand" class="w-full h-11 px-3 pr-8 bg-surface-container-low border border-surface-dim text-xs uppercase font-medium text-primary appearance-none focus:outline-none focus:border-primary cursor-pointer">
                            <option value="">Thương hiệu: Tất cả</option>
                            <option value="thinkpad" ${param.brand == 'thinkpad' || param.brand == 'lenovo' ? 'selected' : ''}>Lenovo / ThinkPad</option>
                            <option value="dell" ${param.brand == 'dell' ? 'selected' : ''}>Dell</option>
                            <option value="apple" ${param.brand == 'apple' ? 'selected' : ''}>Apple</option>
                            <option value="asus" ${param.brand == 'asus' ? 'selected' : ''}>Asus</option>
                            <option value="acer" ${param.brand == 'acer' ? 'selected' : ''}>Acer</option>
                            <option value="hp" ${param.brand == 'hp' ? 'selected' : ''}>HP</option>
                        </select>
                        <span class="material-symbols-outlined absolute right-2.5 top-1/2 -translate-y-1/2 text-outline pointer-events-none text-[16px]">expand_more</span>
                    </div>
                    <div class="relative">
                        <select name="priceRange" class="w-full h-11 px-3 pr-8 bg-surface-container-low border border-surface-dim text-xs uppercase font-medium text-primary appearance-none focus:outline-none focus:border-primary cursor-pointer">
                            <option value="">Mức giá: Tất cả</option>
                            <option value="u10" ${param.priceRange == 'u10' ? 'selected' : ''}>Dưới 10 triệu</option>
                            <option value="10-15" ${param.priceRange == '10-15' ? 'selected' : ''}>10 - 15 triệu</option>
                            <option value="15-20" ${param.priceRange == '15-20' ? 'selected' : ''}>15 - 20 triệu</option>
                            <option value="o20" ${param.priceRange == 'o20' ? 'selected' : ''}>Trên 20 triệu</option>
                        </select>
                        <span class="material-symbols-outlined absolute right-2.5 top-1/2 -translate-y-1/2 text-outline pointer-events-none text-[16px]">expand_more</span>
                    </div>
                    <div class="relative">
                        <select name="condition" class="w-full h-11 px-3 pr-8 bg-surface-container-low border border-surface-dim text-xs uppercase font-medium text-primary appearance-none focus:outline-none focus:border-primary cursor-pointer">
                            <option value="">Tình trạng: Tất cả</option>
                            <option value="USED_GOOD" ${param.condition == 'USED_GOOD' ? 'selected' : ''}>Like New (99%)</option>
                            <option value="USED_FAIR" ${param.condition == 'USED_FAIR' ? 'selected' : ''}>Ngoại hình tạm (95%)</option>
                            <option value="DAMAGED" ${param.condition == 'DAMAGED' ? 'selected' : ''}>Cần sửa chữa</option>
                        </select>
                        <span class="material-symbols-outlined absolute right-2.5 top-1/2 -translate-y-1/2 text-outline pointer-events-none text-[16px]">expand_more</span>
                    </div>
                    <div class="relative">
                        <select name="ram" class="w-full h-11 px-3 pr-8 bg-surface-container-low border border-surface-dim text-xs uppercase font-medium text-primary appearance-none focus:outline-none focus:border-primary cursor-pointer">
                            <option value="">RAM: Tất cả</option>
                            <option value="8" ${param.ram == '8' ? 'selected' : ''}>8GB</option>
                            <option value="16" ${param.ram == '16' ? 'selected' : ''}>16GB</option>
                            <option value="32" ${param.ram == '32' ? 'selected' : ''}>32GB</option>
                        </select>
                        <span class="material-symbols-outlined absolute right-2.5 top-1/2 -translate-y-1/2 text-outline pointer-events-none text-[16px]">expand_more</span>
                    </div>
                </div>
                <div class="flex items-center gap-2">
                    <button type="submit" class="h-11 px-6 bg-primary text-on-primary text-xs uppercase tracking-widest font-bold hover:bg-[#FF4A1C] hover:text-black transition-all">
                        LỌC
                    </button>
                    <a href="${pageContext.request.contextPath}/listing/marketplace" class="h-11 px-3 flex items-center justify-center text-xs text-outline hover:text-primary transition-colors uppercase font-medium">
                        XÓA
                    </a>
                </div>
            </form>
        </section>

        <!-- Product Grid & Main Content -->
        <section class="mt-8">
            <div class="flex items-center justify-between pb-4 border-b border-surface-dim mb-6">
                <span id="listings-count" class="text-xs uppercase tracking-widest text-outline font-semibold">
                    <c:choose>
                        <c:when test="${not empty listings}">HIỂN THỊ ${listings.size()} SẢN PHẨM KHẢ DỤNG</c:when>
                        <c:otherwise>DANH SÁCH MÁY CŨ ĐÃ KIỂM ĐỊNH (TIÊU CHUẨN 48 BƯỚC)</c:otherwise>
                    </c:choose>
                </span>
                <span class="text-xs uppercase tracking-widest text-primary font-mono font-bold">100% ẢNH THỰC TẾ</span>
            </div>

            <!-- Dynamic Listings Loop with JSTL -->
            <div id="listings-grid" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                <c:choose>
                    <c:when test="${not empty listings}">
                        <c:forEach items="${listings}" var="item">
                            <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-primary transition-all hover:shadow-xl">
                                <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                                    <c:choose>
                                        <c:when test="${item.condition == 'USED_GOOD'}">
                                            <span class="text-[11px] bg-emerald-600 text-white font-bold px-2.5 py-1 uppercase tracking-wider">TÌNH TRẠNG: TỐT (99%)</span>
                                        </c:when>
                                        <c:when test="${item.condition == 'USED_FAIR'}">
                                            <span class="text-[11px] bg-amber-500 text-white font-bold px-2.5 py-1 uppercase tracking-wider">TÌNH TRẠNG: TẠM (95%)</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-[11px] bg-[#FF4A1C] text-black font-bold px-2.5 py-1 uppercase tracking-wider">HƯ HỎNG LINH KIỆN</span>
                                        </c:otherwise>
                                    </c:choose>
                                    <span class="font-mono text-[11px] text-zinc-500 font-semibold uppercase">ID: #${item.id}</span>
                                </div>
                                <div class="w-full aspect-[4/3] bg-surface-container flex items-center justify-center overflow-hidden">
                                    <img class="w-full h-full object-cover grayscale contrast-110 group-hover:scale-105 transition-transform duration-300" 
                                         src="${not empty item.images ? item.images[0].imageUrl : 'https://lh3.googleusercontent.com/aida-public/AB6AXuDfW4DtavcquK1SUWDVWq4B-j-lm9TuqguAQN4Xa6MQg4U4aiGZnrk7v3ldHxLGKSeCe8M5zgisMD067mW34tC5KAdqFcN_v98c15IBo7SF0WlHF0EA-qXE2BsIcR-_VzT0LHni0Dr-Tlk-bNzyvpNTtHWVtuTOIxw8cQTE2qfcWCPYRDdie9vqz-Is7Wax6QMcB-0Odv_pp0ewcZPHPH8VU788RLkdImm_NIV1pXw'}" 
                                         alt="Laptop Image">
                                </div>
                                <div class="p-4 flex flex-col gap-2">
                                    <h3 class="text-[18px] font-extrabold uppercase tracking-tight text-primary transition-colors line-clamp-1">
                                        <c:out value="${item.device != null ? item.device.name : 'Laptop Đã Qua Sử Dụng'}"/>
                                    </h3>
                                    <p class="text-xs text-outline line-clamp-2 leading-relaxed">
                                        <c:out value="${item.description}"/>
                                    </p>
                                    <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                                        <span class="text-[20px] font-black text-primary tracking-tight">
                                            <fmt:formatNumber value="${item.price}" pattern="#,###"/> ₫
                                        </span>
                                        <a class="h-9 px-4 bg-primary text-white text-xs font-bold uppercase tracking-wider flex items-center justify-center hover:bg-[#FF4A1C] hover:text-black transition-colors" 
                                           href="${pageContext.request.contextPath}/listing/detail?id=${item.id}">
                                            CHI TIẾT →
                                        </a>
                                    </div>
                                </div>
                            </article>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <!-- Trạng thái không tìm thấy khi lọc kết quả -->
                        <div class="col-span-full py-16 flex flex-col items-center justify-center text-center bg-surface-container-lowest border border-surface-dim p-8">
                            <span class="material-symbols-outlined text-[48px] text-outline mb-3">search_off</span>
                            <h3 class="text-lg font-bold uppercase text-primary">Không tìm thấy máy cũ phù hợp</h3>
                            <p class="text-xs text-outline mt-1 max-w-md leading-relaxed">
                                Không có sản phẩm nào khớp với tiêu chí tìm kiếm hoặc bộ lọc hiện tại. Vui lòng thử xóa từ khóa hoặc chọn mức giá khác.
                            </p>
                            <a href="${pageContext.request.contextPath}/listing/marketplace" class="mt-4 px-6 py-2.5 bg-primary text-white text-xs font-bold uppercase tracking-wider hover:bg-[#FF4A1C] hover:text-black transition-colors">
                                XÓA TẤT CẢ BỘ LỌC
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>
</main>

<!-- Unified Architectural Footer (100% Đồng bộ home.jsp) -->
<footer class="w-full bg-[#050505] text-on-primary border-t border-zinc-800 mt-20">
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-12 py-12">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6 border-b border-zinc-800 pb-8 mb-8">
            <div class="flex flex-col gap-2">
                <a class="text-[18px] text-white tracking-wider uppercase font-extrabold flex items-center gap-2" href="${pageContext.request.contextPath}/home">
                    <span class="w-3 h-3 bg-[#FF4A1C]"></span>
                    ELECTRONICSMARKETPLACE
                </a>
                <p class="text-[12px] text-zinc-400 mt-2">Sàn giao dịch laptop cũ & mới minh bạch 100% lịch sử phần cứng và tiêu chuẩn kiểm định 48 bước.</p>
            </div>
            <div class="flex flex-col gap-2">
                <span class="text-[11px] uppercase tracking-widest text-zinc-500 mb-1 font-semibold">DANH MỤC</span>
                <a href="${pageContext.request.contextPath}/product/list" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Laptop Mới</a>
                <a href="${pageContext.request.contextPath}/listing/marketplace" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Chợ Laptop Cũ</a>
                <a href="${pageContext.request.contextPath}/repair/request" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Dịch Vụ Sửa Chữa</a>
            </div>
            <div class="flex flex-col gap-2">
                <span class="text-[11px] uppercase tracking-widest text-zinc-500 mb-1 font-semibold">DÀNH CHO NGƯỜI BÁN</span>
                <a href="${pageContext.request.contextPath}/listing/form" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Đăng Tin Bán Máy</a>
                <a href="${pageContext.request.contextPath}/listing/my" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Quản Lý Tin Của Tôi</a>
                <a href="#" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Quy Chuẩn Kiểm Định</a>
            </div>
            <div class="flex flex-col gap-2">
                <span class="text-[11px] uppercase tracking-widest text-zinc-500 mb-1 font-semibold">HỖ TRỢ & BẢO HÀNH</span>
                <span class="text-[13px] text-zinc-300 uppercase font-medium">Hotline: 1900 8888</span>
                <span class="text-[13px] text-zinc-300 uppercase font-medium">Email: support@electronics.vn</span>
                <span class="text-[11px] text-zinc-500 mt-2">Bao test 7 ngày • Đổi trả minh bạch</span>
            </div>
        </div>
        <div class="flex flex-col md:flex-row items-center justify-between font-mono text-[10px] text-zinc-600 uppercase tracking-widest">
            <span>© 2026 ELECTRONICSMARKETPLACE. ALL RIGHTS RESERVED.</span>
            <span>TASK CHỢ ĐỒ CŨ (THÀNH) • STITCH DESIGN ARCHITECTURE</span>
        </div>
    </div>
</footer>

<!-- Scripts -->
<script src="${pageContext.request.contextPath}/js/main.js"></script>
<script src="${pageContext.request.contextPath}/js/listing.js"></script>
</body>
</html>
