<%--
    Lich su don hang - ca hai kenh (BR-43):
      MARKETPLACE : mua tu Seller qua Listing, tra tien mat khi nhan hang, KHONG co Payment
      NEW_DEVICE  : mua may moi tu Admin, CO Payment (SDD 13.8)

    Nen moi the don phai re nhanh theo order.orderType truoc khi hien khoi
    thanh toan / nut thao tac, thay vi coi moi don deu co Payment.

    Model: ${orders} (List<Order>), ${currentStatus} (String, nullable),
           ${payments} (Map<Long orderId, Payment>), ${page}, ${totalPages}.

    LUU Y: Order KHONG co truong `payment`. Quan he 1-0..1 nam o phia Payment
    (Payment.orderId unique, SDD 13.8), nen servlet phai tra ve map
    ${payments} khoa theo orderId - xem OrderServlet?action=history.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />

<c:set var="pageTitle" value="Đơn hàng của tôi — Sửa hay bán" />

<%@ include file="_header.jspf" %>

<div class="page-head">
    <p class="eyebrow">Tài khoản</p>
    <h1>Đơn hàng của tôi</h1>
    <p class="crumbs">
        <a href="${ctx}/home">Trang chủ</a>
        <span>/</span>
        <span class="current">Đơn hàng</span>
    </p>
</div>

<c:if test="${not empty flashOk}">
    <p class="flash flash-ok">${flashOk}</p>
</c:if>
<c:if test="${not empty flashError}">
    <p class="flash flash-error">${flashError}</p>
</c:if>

<div class="split">
    <%-- ---------- Cot trai: bo loc + danh sach ---------- --%>
    <div>
        <nav class="tabs">
            <a class="tab ${empty currentStatus ? 'active' : ''}" href="${ctx}/order?action=history">Tất cả</a>
            <a class="tab ${currentStatus == 'PENDING' ? 'active' : ''}"
               href="${ctx}/order?action=history&amp;status=PENDING">Chờ xử lý</a>
            <a class="tab ${currentStatus == 'CONFIRMED' ? 'active' : ''}"
               href="${ctx}/order?action=history&amp;status=CONFIRMED">Đã xác nhận</a>
            <a class="tab ${currentStatus == 'COMPLETED' ? 'active' : ''}"
               href="${ctx}/order?action=history&amp;status=COMPLETED">Hoàn thành</a>
            <a class="tab ${currentStatus == 'CANCELLED' ? 'active' : ''}"
               href="${ctx}/order?action=history&amp;status=CANCELLED">Đã huỷ</a>
        </nav>

        <c:choose>
            <c:when test="${empty orders}">
                <div class="empty">
                    <h2>Chưa có đơn hàng nào</h2>
                    <p>
                        <c:choose>
                            <c:when test="${empty currentStatus}">
                                Bạn chưa đặt đơn nào trên Sửa hay bán.
                            </c:when>
                            <c:otherwise>Không có đơn hàng ở trạng thái này.</c:otherwise>
                        </c:choose>
                    </p>
                    <a class="btn btn-primary" href="${ctx}/product?action=list">Xem danh mục máy mới</a>
                </div>
            </c:when>

            <c:otherwise>
                <c:forEach var="order" items="${orders}">
                    <%-- Nhan trang thai: enum OrderStatus chi co 4 gia tri. --%>
                    <c:set var="statusLabel">
                        <c:choose>
                            <c:when test="${order.status == 'PENDING'}">Chờ xử lý</c:when>
                            <c:when test="${order.status == 'CONFIRMED'}">Đã xác nhận</c:when>
                            <c:when test="${order.status == 'COMPLETED'}">Hoàn thành</c:when>
                            <c:when test="${order.status == 'CANCELLED'}">Đã huỷ</c:when>
                            <c:otherwise>${order.status}</c:otherwise>
                        </c:choose>
                    </c:set>
                    <c:set var="statusBadge">
                        <c:choose>
                            <c:when test="${order.status == 'COMPLETED'}">badge-solid</c:when>
                            <c:when test="${order.status == 'CANCELLED'}">badge-muted</c:when>
                            <c:when test="${order.status == 'CONFIRMED'}">badge-accent</c:when>
                            <c:otherwise>badge-outline</c:otherwise>
                        </c:choose>
                    </c:set>

                    <article class="order-card">
                        <div class="order-card-head">
                            <span>
                                DH-<fmt:formatNumber value="${order.id}" pattern="00000" />
                                ·
                                <c:choose>
                                    <c:when test="${order.orderType == 'NEW_DEVICE'}">Máy mới</c:when>
                                    <c:otherwise>Chợ đồ cũ</c:otherwise>
                                </c:choose>
                            </span>
                            <span class="mono">
                                <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                            </span>
                            <span class="badge ${statusBadge}">${statusLabel}</span>
                        </div>

                        <div class="order-card-body">
                            <c:forEach var="item" items="${order.items}">
                                <div class="order-line">
                                    <div class="cart-thumb">
                                        <%-- Khong co truong anh trong model: hien ma dinh danh. --%>
                                        <span class="mono">
                                            <c:choose>
                                                <c:when test="${order.orderType == 'NEW_DEVICE'}">#${item.product.id}</c:when>
                                                <c:otherwise>#${item.listing.id}</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>

                                    <div class="order-line-main">
                                        <c:choose>
                                            <c:when test="${order.orderType == 'NEW_DEVICE'}">
                                                <p class="cart-title">${item.product.name}</p>
                                                <p class="cart-spec">${item.product.brand}</p>
                                            </c:when>
                                            <c:otherwise>
                                                <%-- Listing khong co truong `title` (SDD 13.6):
                                                     ten thiet bi nam o Device.name qua Listing.device. --%>
                                                <p class="cart-title">${item.listing.device.name}</p>
                                                <p class="cart-spec">
                                                    Người bán: ${item.listing.seller.name} · Tình trạng: ${item.listing.condition}
                                                </p>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="cart-price money">
                                        <fmt:formatNumber value="${item.price}" type="currency" currencySymbol="₫" />
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="order-card-foot">
                            <span>
                                <span class="mono" style="color: var(--gray); font-size: 13px">TỔNG CỘNG</span>
                                <span class="cart-total" style="margin-left: var(--space-sm)">
                                    <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫" />
                                </span>
                            </span>

                            <c:set var="payment" value="${payments[order.id]}" />

                            <div class="actions">
                                <c:if test="${not empty payment}">
                                    <a class="btn btn-ghost btn-sm" href="${ctx}/payment?action=view&amp;orderId=${order.id}">
                                        <c:choose>
                                            <c:when test="${payment.status == 'PENDING'}">Thanh toán ngay</c:when>
                                            <c:otherwise>Chi tiết thanh toán</c:otherwise>
                                        </c:choose>
                                    </a>
                                </c:if>

                                <%-- Huy don: Customer duoc huy truoc khi COMPLETED (BR-46).
                                     Voi don NEW_DEVICE, huy don keo theo Payment PENDING -> FAILED. --%>
                                <c:if test="${order.status == 'PENDING' or order.status == 'CONFIRMED'}">
                                    <form method="post" action="${ctx}/order?action=cancel" class="js-confirm"
                                          data-confirm="Huỷ đơn DH-${order.id}? Thao tác này không thể hoàn tác.">
                                        <input type="hidden" name="orderId" value="${order.id}">
                                        <button type="submit" class="btn btn-danger btn-sm">Huỷ đơn</button>
                                    </form>
                                </c:if>

                                <%-- BR-46: NEW_DEVICE chi COMPLETED khi Payment PAID. --%>
                                <c:if test="${order.status == 'CONFIRMED' and payment.status == 'PAID'}">
                                    <form method="post" action="${ctx}/order?action=complete" class="js-confirm"
                                          data-confirm="Xác nhận đã nhận được máy cho đơn DH-${order.id}?">
                                        <input type="hidden" name="orderId" value="${order.id}">
                                        <button type="submit" class="btn btn-primary btn-sm">Đã nhận hàng</button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </article>
                </c:forEach>

                <%-- Phan trang: servlet truyen ${page}, ${totalPages}. --%>
                <c:if test="${totalPages > 1}">
                    <nav class="tabs" style="border-bottom: 0; border-top: 1px solid var(--hairline); padding-top: var(--space-md)">
                        <c:forEach var="p" begin="1" end="${totalPages}">
                            <a class="tab ${p == page ? 'active' : ''}"
                               href="${ctx}/order?action=history&amp;page=${p}<c:if test="${not empty currentStatus}">&amp;status=${currentStatus}</c:if>">
                                ${p}
                            </a>
                        </c:forEach>
                    </nav>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- ---------- Cot phai: ho so ---------- --%>
    <aside>
        <div class="panel sticky">
            <div class="panel-head">
                <h3>Tài khoản</h3>
            </div>

            <div class="panel-body">
                <dl class="bank-list">
                    <div class="bank-row">
                        <dt>Họ tên</dt>
                        <dd>${currentUser.name}</dd>
                    </div>
                    <div class="bank-row">
                        <dt>Email</dt>
                        <dd>${currentUser.email}</dd>
                    </div>
                    <div class="bank-row">
                        <dt>Điện thoại</dt>
                        <dd class="mono">${currentUser.phone}</dd>
                    </div>
                    <div class="bank-row">
                        <dt>Đã mua thành công</dt>
                        <dd class="mono">${currentUser.completedSalesCount}</dd>
                    </div>
                </dl>
            </div>

            <div class="panel-foot">
                <a class="btn btn-ghost btn-block" href="${ctx}/cart">Giỏ hàng</a>
            </div>
        </div>
    </aside>
</div>

<script>
// Rule 4: xac nhan truoc khi huy don - hanh dong khong the hoan tac.
document.querySelectorAll('form.js-confirm').forEach(function (form) {
    form.addEventListener('submit', function (e) {
        if (!window.confirm(form.dataset.confirm)) e.preventDefault();
    });
});
</script>

<%@ include file="_footer.jsp" %>
