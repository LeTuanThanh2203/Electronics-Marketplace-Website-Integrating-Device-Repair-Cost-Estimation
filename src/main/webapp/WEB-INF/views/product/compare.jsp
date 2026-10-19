<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi" class="h-full">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>So Sánh Sản Phẩm | SỬA HAY BÁN</title>
    
    <!-- Google Fonts: Space Grotesk & Space Mono -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Space+Mono:ital,wght@0,400;0,700;1,400&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- Material Symbols -->
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />
    
    <!-- Tailwind CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        brand: {
                            orange: '#F05A28',
                            black: '#111315',
                            gray: '#5A5D5E',
                            border: '#2A2C2E',
                            card: '#F6F6F4',
                            bg: '#FAF9F5'
                        }
                    },
                    fontFamily: {
                        space: ['"Space Grotesk"', 'sans-serif'],
                        mono: ['"Space Mono"', 'monospace'],
                        inter: ['Inter', 'sans-serif']
                    }
                }
            }
        }
    </script>
</head>
<body class="bg-brand-bg text-brand-black font-inter antialiased min-h-screen flex flex-col selection:bg-brand-orange selection:text-white">

    <!-- 1. TOP ANNOUNCEMENT TICKER -->
    <div class="bg-brand-black text-white py-1 px-4 text-[11px] font-mono border-b border-brand-border flex justify-between items-center tracking-wider">
        <div class="flex items-center gap-3">
            <span class="inline-block w-2 h-2 bg-brand-orange animate-pulse"></span>
            <span>HỆ THỐNG SO SÁNH MA TRẬN PHẦN CỨNG B2C CHUẨN XUẤT XƯỞNG</span>
        </div>
        <div class="hidden md:flex items-center gap-4 text-gray-400">
            <span>HOTLINE: 1800 6868</span>
            <span>CHÍNH HÃNG 100% NGUYÊN SEAL</span>
        </div>
    </div>

    <!-- 2. MAIN HEADER -->
    <header class="border-b border-brand-border bg-white sticky top-0 z-40">
        <div class="max-w-[1360px] mx-auto px-6 h-16 flex items-center justify-between">
            <div class="flex items-center gap-8">
                <a href="${pageContext.request.contextPath}/products" class="flex items-center gap-2">
                    <span class="w-8 h-8 bg-brand-orange text-white font-bold font-space flex items-center justify-center text-lg border border-brand-black shadow-[2px_2px_0px_#111315]">S</span>
                    <span class="font-bold text-xl tracking-tight font-space text-brand-black">SỬA HAY BÁN</span>
                </a>
                <nav class="hidden md:flex items-center gap-6 text-xs font-mono font-semibold uppercase">
                    <a href="${pageContext.request.contextPath}/products" class="text-brand-orange">CỬA HÀNG MÁY MỚI</a>
                    <a href="${pageContext.request.contextPath}/" class="text-brand-gray hover:text-brand-black transition">TRANG CHỦ</a>
                </nav>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/products" class="text-xs font-mono font-semibold border border-brand-border px-3 py-2 hover:bg-brand-black hover:text-white transition flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-sm">arrow_back</span>
                    <span>QUAY LẠI CỬA HÀNG</span>
                </a>
            </div>
        </div>
    </header>

    <!-- 3. MAIN CONTENT -->
    <main class="flex-1 max-w-[1360px] mx-auto px-6 py-8 w-full">
        <!-- Title & Breadcrumbs -->
        <div class="mb-6 border-b border-brand-border pb-4 flex flex-col md:flex-row md:items-end justify-between gap-4">
            <div>
                <div class="flex items-center gap-2 text-xs font-mono text-brand-gray mb-1">
                    <a href="${pageContext.request.contextPath}/products" class="hover:underline">CỬA HÀNG MÁY MỚI</a>
                    <span>/</span>
                    <span class="text-brand-black font-bold">SO SÁNH CẤU HÌNH</span>
                </div>
                <h1 class="text-2xl md:text-3xl font-bold font-space text-brand-black uppercase tracking-tight">
                    MA TRẬN SO SÁNH ĐỐI ĐẦU PHẦN CỨNG
                </h1>
            </div>
            <p class="text-xs font-mono text-brand-gray">
                Đang so sánh <strong class="text-brand-black">${compareProducts.size()}</strong> sản phẩm
            </p>
        </div>

        <c:choose>
            <c:when test="${empty compareProducts}">
                <div class="border border-brand-border bg-white p-12 text-center my-8">
                    <span class="material-symbols-outlined text-5xl text-brand-gray mb-3">compare</span>
                    <h3 class="font-bold font-space text-lg text-brand-black uppercase">CHƯA CÓ SẢN PHẨM NÀO ĐỂ SO SÁNH</h3>
                    <p class="text-xs font-mono text-brand-gray mt-2 mb-6">Vui lòng quay lại cửa hàng và chọn các thiết bị bạn muốn đối chiếu thông số.</p>
                    <a href="${pageContext.request.contextPath}/products" class="inline-block bg-brand-orange text-white px-6 py-3 font-mono font-bold text-xs uppercase tracking-wider hover:bg-black transition">
                        DUYỆT DANH SÁCH MÁY MỚI →
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <!-- Comparison Table -->
                <div class="border border-brand-border bg-white overflow-x-auto shadow-[4px_4px_0px_#111315]">
                    <table class="w-full border-collapse text-left">
                        <!-- Head: Products Preview -->
                        <thead>
                            <tr class="border-b-2 border-brand-border bg-brand-card">
                                <th class="p-4 w-52 min-w-[200px] font-mono text-xs font-bold text-brand-gray uppercase border-r border-brand-border">
                                    TIÊU CHÍ KỸ THUẬT
                                </th>
                                <c:forEach var="p" items="${compareProducts}">
                                    <th class="p-4 min-w-[260px] max-w-[320px] border-r border-brand-border last:border-r-0 align-top">
                                        <div class="flex flex-col h-full justify-between">
                                            <div>
                                                <div class="aspect-[4/3] bg-white border border-brand-border p-3 flex items-center justify-center mb-3">
                                                    <c:choose>
                                                        <c:when test="${not empty p.imageUrl}">
                                                            <img src="<c:out value='${p.imageUrl}'/>" alt="<c:out value='${p.name}'/>" class="max-h-full max-w-full object-contain mix-blend-multiply">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="material-symbols-outlined text-4xl text-gray-300">laptop</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <span class="text-[10px] font-mono text-brand-gray uppercase"><c:out value="${p.brand}"/> • <c:out value="${p.series}"/></span>
                                                <h3 class="font-bold font-space text-sm text-brand-black mt-1 leading-snug">
                                                    <a href="${pageContext.request.contextPath}/products?action=detail&id=${p.id}" class="hover:text-brand-orange">
                                                        <c:out value="${p.name}"/>
                                                    </a>
                                                </h3>
                                                <div class="mt-2 text-base font-bold font-space text-brand-orange">
                                                    <fmt:formatNumber value="${p.price}" pattern="#,###"/> ₫
                                                </div>
                                            </div>
                                            <div class="mt-4 pt-3 border-t border-brand-border flex gap-2">
                                                <a href="${pageContext.request.contextPath}/products?action=detail&id=${p.id}" 
                                                   class="flex-1 bg-brand-black text-white text-center py-2 text-[11px] font-mono font-bold uppercase hover:bg-brand-orange transition">
                                                    XEM CHI TIẾT
                                                </a>
                                            </div>
                                        </div>
                                    </th>
                                </c:forEach>
                            </tr>
                        </thead>

                        <!-- Body: Specs Matrix -->
                        <tbody class="divide-y divide-brand-border text-xs font-mono">
                            <!-- Section: Hardware -->
                            <tr class="bg-gray-100 font-bold">
                                <td colspan="${compareProducts.size() + 1}" class="px-4 py-2 uppercase text-brand-black tracking-wider text-[11px]">
                                    PHẦN CỨNG TIÊU CHUẨN
                                </td>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">CPU / Bộ vi xử lý</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 font-medium text-brand-black">
                                        <c:out value="${p.cpu}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Dung lượng RAM</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 font-bold text-brand-black">
                                        <c:if test="${not empty p.ramGb}">${p.ramGb} GB</c:if>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Ổ cứng SSD</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 font-bold text-brand-black">
                                        <c:if test="${not empty p.storageGb}">${p.storageGb} GB</c:if>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Card đồ họa (GPU)</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 text-brand-black">
                                        <c:out value="${p.gpu}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Màn hình hiển thị</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 text-brand-black">
                                        <c:if test="${not empty p.screenSize}">${p.screenSize}"</c:if>
                                        <c:if test="${not empty p.screenSize && not empty p.screenResolution}"> • </c:if>
                                        <c:out value="${p.screenResolution}"/>
                                    </td>
                                </c:forEach>
                            </tr>

                            <!-- Section: Service & Commerce -->
                            <tr class="bg-gray-100 font-bold">
                                <td colspan="${compareProducts.size() + 1}" class="px-4 py-2 uppercase text-brand-black tracking-wider text-[11px]">
                                    THƯƠNG MẠI & CHÍNH SÁCH BẢO HÀNH
                                </td>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Nhu cầu tối ưu</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0">
                                        <span class="inline-block px-2 py-0.5 border border-brand-border bg-white text-[10px] font-bold uppercase">
                                            <c:out value="${p.targetNeed}"/>
                                        </span>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Chính sách bảo hành</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 text-emerald-700 font-semibold">
                                        <c:out value="${p.warranty}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Tình trạng máy</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0">
                                        <span class="inline-block px-2 py-0.5 bg-emerald-100 text-emerald-800 text-[10px] font-bold">
                                            <c:out value="${p.badge}"/> (${p.status})
                                        </span>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Hành động</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0">
                                        <a href="${pageContext.request.contextPath}/products?action=detail&id=${p.id}" 
                                           class="inline-block w-full text-center bg-brand-orange text-white py-2 text-xs font-bold uppercase tracking-wider hover:bg-black transition">
                                            MUA MÁY NÀY →
                                        </a>
                                    </td>
                                </c:forEach>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </main>

    <!-- FOOTER -->
    <footer class="border-t border-brand-border bg-white py-6 mt-12">
        <div class="max-w-[1360px] mx-auto px-6 flex flex-col md:flex-row justify-between items-center text-xs font-mono text-brand-gray gap-4">
            <div>
                © 2026 SỬA HAY BÁN - HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ &amp; ĐỊNH GIÁ SỬA CHỮA THIẾT BỊ.
            </div>
            <div class="flex gap-4">
                <span>PHẠM PHƯỚC ANH - MODULE CỬA HÀNG MÁY MỚI</span>
            </div>
        </div>
    </footer>

</body>
</html>
