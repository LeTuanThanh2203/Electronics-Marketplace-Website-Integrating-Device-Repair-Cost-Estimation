<<<<<<< Updated upstream
=======
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${product.name} — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="new" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

    <!-- Breadcrumb -->
    <div class="w-full bg-brand-card border-b border-brand-border py-3">
        <div class="max-w-[1360px] mx-auto px-6 flex items-center gap-2 text-xs font-mono text-brand-gray">
            <a href="${pageContext.request.contextPath}/" class="hover:text-brand-black">TRANG CHỦ</a>
            <span>/</span>
            <a href="${pageContext.request.contextPath}/products" class="hover:text-brand-black">MUA MỚI</a>
            <span>/</span>
            <span class="text-brand-black font-semibold uppercase">${product.brand}</span>
            <span>/</span>
            <span class="text-brand-orange font-semibold">#${product.sku}</span>
        </div>
    </div>

    <!-- PRODUCT DETAIL HERO SECTION -->
    <div class="max-w-[1360px] mx-auto px-6 py-8">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-10">

            <!-- Left 5 cols: Product Showcase Gallery -->
            <div class="lg:col-span-5 flex flex-col gap-4">
                <div class="aspect-[4/3] bg-white border border-brand-border flex items-center justify-center p-6 relative">
                    <span class="absolute top-3 left-3 bg-brand-black text-white text-[11px] font-mono px-2 py-0.5 font-bold uppercase">
                        ${product.badge}
                    </span>
                    <c:if test="${not empty product.discountTag}">
                        <span class="absolute top-3 right-3 bg-brand-orange text-white text-[11px] font-mono px-2 py-0.5 font-bold uppercase">
                            ${product.discountTag}
                        </span>
                    </c:if>
                    
                    <c:choose>
                        <c:when test="${not empty product.imageUrl}">
                            <img class="max-h-full max-w-full object-contain" src="${product.imageUrl}" alt="${product.name}">
                        </c:when>
                        <c:otherwise>
                            <span class="material-symbols-outlined text-8xl text-gray-300">laptop</span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Inspection & Warranty Notes -->
                <div class="p-4 bg-white border border-brand-border text-xs font-mono space-y-2 text-[#444748]">
                    <div class="flex items-start gap-2">
                        <span class="material-symbols-outlined text-emerald-600 text-sm">verified</span>
                        <span><strong>100% NGUYÊN SEAL:</strong> Chưa qua sử dụng, đủ phụ kiện xuất xưởng.</span>
                    </div>
                    <div class="flex items-start gap-2">
                        <span class="material-symbols-outlined text-emerald-600 text-sm">home_repair_service</span>
                        <span><strong>BẢO HÀNH CHÍNH HÃNG:</strong> ${product.warranty}.</span>
                    </div>
                </div>
            </div>

            <!-- Right 7 cols: Specs, Pricing, and Purchase Actions -->
            <div class="lg:col-span-7 flex flex-col justify-between">
                <div>
                    <!-- Brand & SKU -->
                    <div class="flex items-center gap-2 text-xs font-mono text-brand-gray mb-1">
                        <span class="uppercase font-bold text-brand-black">${product.brand}</span>
                        <span>•</span>
                        <span>${product.series}</span>
                        <span>•</span>
                        <span>MÃ: #${product.sku}</span>
                    </div>

                    <!-- Title -->
                    <h1 class="text-2xl lg:text-3xl font-bold font-space text-brand-black tracking-tight leading-tight">
                        ${product.name}
                    </h1>

                    <!-- Pricing Banner -->
                    <div class="p-4 bg-brand-card border border-brand-border my-4 flex items-center justify-between">
                        <div>
                            <span class="text-xs font-mono text-gray-500 uppercase block">GIÁ BÁN NIÊM YẾT ĐÃ GỒM VAT</span>
                            <div class="text-2xl lg:text-3xl font-bold font-space text-brand-orange tracking-tight">
                                <fmt:formatNumber value="${product.price}" pattern="#,###"/> ₫
                            </div>
                        </div>
                        <c:if test="${not empty product.originalPrice && product.originalPrice > product.price}">
                            <div class="text-right">
                                <span class="text-xs font-mono text-gray-400 line-through">
                                    <fmt:formatNumber value="${product.originalPrice}" pattern="#,###"/> ₫
                                </span>
                                <span class="block text-xs font-mono text-emerald-700 font-bold">TIẾT KIỆM NGAY</span>
                            </div>
                        </c:if>
                    </div>

                    <!-- Quick Hardware Specification Matrix (Ma trận phần cứng tiêu chuẩn) -->
                    <div class="border border-brand-border bg-white mb-6">
                        <div class="px-4 py-2.5 bg-brand-card border-b border-brand-border flex items-center justify-between text-xs font-mono">
                            <span class="font-bold text-brand-black uppercase">CẤU HÌNH PHẦN CỨNG TIÊU CHUẨN</span>
                            <span class="text-brand-gray">XUẤT XƯỞNG NGUYÊN BẢN</span>
                        </div>
                        <div class="divide-y divide-brand-border text-xs">
                            <c:forEach var="spec" items="${specifications}">
                                <div class="px-4 py-2.5 grid grid-cols-12 gap-2">
                                    <span class="col-span-4 font-mono font-semibold text-brand-gray">${spec.specKey}</span>
                                    <span class="col-span-8 font-mono text-brand-black">
                                        ${spec.textValue} 
                                        <c:if test="${not empty spec.unit && spec.specKey != spec.textValue}">(${spec.unit})</c:if>
                                    </span>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>

                <!-- Action Controls -->
                <div class="pt-4 border-t border-brand-border flex flex-col gap-3">
                    <div class="flex items-center gap-3">
                        <!-- Qty selector -->
                        <div class="flex items-center border border-brand-border bg-white h-11 shrink-0">
                            <button type="button" onclick="changeQty(-1)" class="w-10 h-full flex items-center justify-center hover:bg-brand-card text-brand-black">
                                <span class="material-symbols-outlined text-sm">remove</span>
                            </button>
                            <input id="qtyInput" type="text" value="1" readonly class="w-10 text-center font-mono font-bold text-sm bg-transparent focus:outline-none">
                            <button type="button" onclick="changeQty(1)" class="w-10 h-full flex items-center justify-center hover:bg-brand-card text-brand-black">
                                <span class="material-symbols-outlined text-sm">add</span>
                            </button>
                        </div>

                        <!-- Form submitting to Nam's CartServlet -->
                        <form action="${pageContext.request.contextPath}/cart" method="POST" class="flex-1 flex items-center gap-3">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="productId" value="${product.id}">
                            
                            <!-- Buy Now CTA -->
                            <button type="submit" name="buyNow" value="true" class="flex-1 h-11 bg-brand-black hover:bg-brand-orange text-white font-mono font-bold text-xs uppercase tracking-wider transition-colors flex items-center justify-center gap-2 cursor-pointer">
                                <span class="material-symbols-outlined text-base">bolt</span>
                                <span>MUA NGAY (GIAO TẬN NƠI)</span>
                            </button>

                            <!-- Add to Cart CTA -->
                            <button type="submit" name="buyNow" value="false" class="h-11 px-5 border border-brand-black bg-white hover:bg-brand-card text-brand-black font-mono font-bold text-xs uppercase tracking-wider transition-colors flex items-center justify-center gap-1.5 cursor-pointer">
                                <span class="material-symbols-outlined text-base">shopping_cart</span>
                                <span>THÊM GIỎ HÀNG</span>
                            </button>
                        </form>
                    </div>

                    <div class="flex items-center justify-between text-xs font-mono text-brand-gray pt-1">
                        <a href="${pageContext.request.contextPath}/products?action=compare&ids=${product.id}" class="inline-flex items-center gap-1 hover:text-brand-orange underline">
                            <span class="material-symbols-outlined text-sm">compare_arrows</span>
                            <span>So sánh cấu hình máy này với các mẫu cùng phân khúc</span>
                        </a>
                        <span>Tình trạng: <strong class="text-emerald-700">${product.status}</strong> (${product.stockQuantity} máy có sẵn)</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
<script>
    function changeQty(delta) {
        const input = document.getElementById('qtyInput');
        let current = parseInt(input.value) || 1;
        current = Math.max(1, current + delta);
        input.value = current;
    }
</script>

<%@ include file="/WEB-INF/layout/footer.jspf" %>

>>>>>>> Stashed changes
