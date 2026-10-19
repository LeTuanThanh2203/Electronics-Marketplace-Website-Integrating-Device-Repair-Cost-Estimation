<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="So Sánh Cấu Hình Sản Phẩm — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="new" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

    <div class="flex-1 max-w-[1360px] mx-auto px-6 py-8 w-full">
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
                                                            <img src="${p.imageUrl}" alt="${p.name}" class="max-h-full max-w-full object-contain mix-blend-multiply">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="material-symbols-outlined text-4xl text-gray-300">laptop</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <span class="text-[10px] font-mono text-brand-gray uppercase">${p.brand} • ${p.series}</span>
                                                <h3 class="font-bold font-space text-sm text-brand-black mt-1 leading-snug">
                                                    <a href="${pageContext.request.contextPath}/products?action=detail&id=${p.id}" class="hover:text-brand-orange">
                                                        ${p.name}
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
                                        ${p.cpu}
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Dung lượng RAM</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 font-bold text-brand-black">
                                        ${p.ramGb} GB
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Ổ cứng SSD</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 font-bold text-brand-black">
                                        ${p.storageGb} GB
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Card đồ họa (GPU)</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 text-brand-black">
                                        ${p.gpu}
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Màn hình hiển thị</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 text-brand-black">
                                        ${p.screenSize}" • ${p.screenResolution}
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
                                            ${p.targetNeed}
                                        </span>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Chính sách bảo hành</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0 text-emerald-700 font-semibold">
                                        ${p.warranty}
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <td class="p-4 font-semibold text-brand-gray border-r border-brand-border bg-brand-card">Tình trạng máy</td>
                                <c:forEach var="p" items="${compareProducts}">
                                    <td class="p-4 border-r border-brand-border last:border-r-0">
                                        <span class="inline-block px-2 py-0.5 bg-emerald-100 text-emerald-800 text-[10px] font-bold">
                                            ${p.badge} (${p.status})
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
        </div>
    </div>

<%@ include file="/WEB-INF/layout/footer.jspf" %>

