<%--
    FR-CART-01/02/03 - Gio hang kenh NEW_DEVICE.
    quantity luon = 1 (BR-45, SDD 13.8): moi Product la mot don vi duy nhat,
    nen man hinh chi hien "SL: 01" chu khong co stepper 1..5 nhu ban mockup.

    Model: ${cart} (request attribute) hoac ${sessionScope.cart}.
    Cart.getSubtotal() tinh SUM(product.price x quantity) - xem Cart.java.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />

<c:set var="pageTitle" value="Giỏ hàng — Sửa hay bán" />
<c:set var="cart" value="${empty cart ? sessionScope.cart : cart}" />

<%@ include file="../order/_header.jspf" %>

<div class="page-head">
    <p class="eyebrow">Giỏ hàng</p>
    <h1>Thiết bị bạn chọn</h1>
    <p class="crumbs">
        <a href="${ctx}/home">Trang chủ</a>
        <span>/</span>
        <span class="current">Giỏ hàng</span>
    </p>
</div>

<c:choose>
    <%-- ---------- Trang thai rong ---------- --%>
    <c:when test="${empty cart.items}">
        <div class="empty">
            <h2>Giỏ hàng đang trống</h2>
            <p>Bạn chưa thêm thiết bị mới nào. Ghé catalog để chọn máy đã kiểm định.</p>
            <a class="btn btn-primary" href="${ctx}/product?action=list">Xem danh mục máy mới</a>
        </div>
    </c:when>

    <%-- ---------- Co hang ---------- --%>
    <c:otherwise>
        <div class="split">
            <div>
                <div class="panel">
                    <div class="panel-head">
                        <h3>Danh sách thiết bị</h3>
                        <span class="mono">${fn:length(cart.items)} sản phẩm</span>
                    </div>

                    <div class="cart-head">
                        <span>Thiết bị</span>
                        <span class="center">Số lượng</span>
                        <span>Đơn giá</span>
                        <span class="center">Thao tác</span>
                    </div>

                    <c:forEach var="item" items="${cart.items}">
                        <div class="cart-item" data-item-id="${item.id}" data-price="${item.product.price}">
                            <div class="cart-thumb">
                                <%-- Product khong co truong anh trong SDD 13.5: hien ma thiet bi thay the. --%>
                                <span class="mono">#${item.product.id}</span>
                            </div>

                            <div class="cart-main">
                                <a class="cart-title" href="${ctx}/product?action=detail&amp;id=${item.product.id}">
                                    ${item.product.name}
                                </a>
                                <p class="cart-spec">${item.product.brand}</p>
                                <p class="cart-meta">
                                    <c:if test="${not empty item.product.storeName}">
                                        <span>${item.product.storeName}</span>
                                    </c:if>
                                    <span class="badge badge-outline">${item.product.status}</span>
                                </p>
                            </div>

                            <div class="cart-qty">
                                <span class="value">01</span>
                                <span class="mono">SL</span>
                            </div>

                            <div class="cart-price money">
                                <fmt:formatNumber value="${item.product.price}" type="currency" currencySymbol="₫" />
                            </div>

                            <div class="center">
                                <button type="button"
                                        class="link-danger js-remove"
                                        data-item-id="${item.id}"
                                        data-url="${ctx}/cart?action=remove&amp;itemId=${item.id}">
                                    Xoá
                                </button>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <p class="hint" style="margin-top: var(--space-md)">
                    Giỏ hàng không giữ máy. Thiết bị chỉ được xác nhận thuộc về bạn sau khi đơn hàng được tạo.
                </p>
            </div>

            <%-- ---------- Tom tat don ---------- --%>
            <aside>
                <div class="panel sticky">
                    <div class="panel-head">
                        <h3>Tóm tắt đơn</h3>
                    </div>

                    <div class="panel-body">
                        <div class="ledger">
                            <div class="ledger-row">
                                <span class="label">Tạm tính</span>
                                <span class="value js-subtotal">
                                    <fmt:formatNumber value="${cart.subtotal}" type="currency" currencySymbol="₫" />
                                </span>
                            </div>
                            <div class="ledger-row muted">
                                <span class="label">Phí vận chuyển</span>
                                <span class="value">Tính khi thanh toán</span>
                            </div>
                            <div class="ledger-total">
                                <span class="label">Tổng cộng</span>
                                <span class="value js-total">
                                    <fmt:formatNumber value="${cart.subtotal}" type="currency" currencySymbol="₫" />
                                </span>
                            </div>
                        </div>
                    </div>

                    <div class="panel-foot">
                        <a class="btn btn-primary btn-block" href="${ctx}/checkout">Tiến hành thanh toán</a>
                    </div>
                </div>
            </aside>
        </div>
    </c:otherwise>
</c:choose>

<%--
    Rule 4 - cap nhat mot phan bang Fetch API. Xoa mot dong la thao tac cuc bo
    nen khong reload ca trang. Nut van la <button> chu khong phai <a>: khong co
    JS thi khong xoa duoc, chap nhan vi day la hanh dong pha huy (khong nen
    fallback im lang).

    Hop dong JSON cua CartServlet?action=remove:
        { "ok": true, "subtotal": 26390000, "count": 2 }
--%>
<script>
(function () {
    var vnd = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
    var subtotalEl = document.querySelector('.js-subtotal');
    var totalEl = document.querySelector('.js-total');

    document.querySelectorAll('.js-remove').forEach(function (btn) {
        btn.addEventListener('click', function () {
            btn.disabled = true;
            fetch(btn.dataset.url, {method: 'POST', headers: {'Accept': 'application/json'}})
                .then(function (res) {
                    if (!res.ok) throw new Error('HTTP ' + res.status);
                    return res.json();
                })
                .then(function (data) {
                    var row = document.querySelector('.cart-item[data-item-id="' + btn.dataset.itemId + '"]');
                    if (row) row.remove();
                    if (subtotalEl) subtotalEl.textContent = vnd.format(data.subtotal);
                    if (totalEl) totalEl.textContent = vnd.format(data.subtotal);
                    if (data.count === 0) window.location.reload();
                })
                .catch(function () {
                    btn.disabled = false;
                    window.location.reload();
                });
        });
    });
})();
</script>

<%@ include file="../order/_footer.jsp" %>
