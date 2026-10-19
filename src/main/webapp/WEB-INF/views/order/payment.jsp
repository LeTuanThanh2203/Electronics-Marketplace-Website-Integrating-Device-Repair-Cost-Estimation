<%--
    FR-PAY-01/02 - Man hinh thanh toan kenh NEW_DEVICE (SDD 13.8, BR-44).

    Gop hai man mockup PaymentUI + PaymentPendingUI lam mot, vi trong MVP
    Payment luon duoc sinh o PENDING va Admin xac nhan thu cong: man "chon
    phuong thuc" va man "cho xac nhan" la cung mot trang thai. Trang thai
    PAID / FAILED duoc re nhanh ngay duoi day.

    Khong tich hop payment gateway (BR-44) nen KHONG co QR dong: chi hien
    thong tin chuyen khoan va Payment.transactionCode lam noi dung chuyen khoan.

    Model: ${payment} (Payment), ${payment.order} (Order).
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />

<%-- ponytail: so tai khoan nhan tien la hang so tam thoi cua MVP, chua co
     entity/config nao giu. Chuyen sang config khi Admin co man hinh cau hinh. --%>
<c:set var="bankName" value="MB Bank — Chi nhánh Sài Gòn" />
<c:set var="bankAccountName" value="CÔNG TY CỔ PHẦN SỬA HAY BÁN" />
<c:set var="bankAccountNo" value="9999 8888 6868" />

<c:set var="pageTitle" value="Thanh toán đơn hàng — Sửa hay bán" />
<c:set var="order" value="${payment.order}" />
<c:set var="payBadge"
       value="${payment.status == 'PAID' ? 'badge-solid' : (payment.status == 'FAILED' ? 'badge-accent' : 'badge-outline')}" />

<%@ include file="_header.jspf" %>

<div class="page-head">
    <p class="eyebrow">Thanh toán</p>
    <h1>
        <c:choose>
            <c:when test="${payment.status == 'PENDING'}">Chờ xác nhận</c:when>
            <c:when test="${payment.status == 'PAID'}">Đã thanh toán</c:when>
            <c:otherwise>Thanh toán không thành công</c:otherwise>
        </c:choose>
    </h1>
    <p class="crumbs">
        <a href="${ctx}/order?action=history">Đơn hàng của tôi</a>
        <span>/</span>
        <span class="current">DH-<fmt:formatNumber value="${order.id}" pattern="00000" /></span>
    </p>
</div>

<c:if test="${not empty flashError}">
    <p class="flash flash-error">${flashError}</p>
</c:if>

<div class="split">
    <div class="panel">
        <div class="panel-head">
            <h3>Thông tin giao dịch</h3>
            <span class="badge ${payBadge}">${payment.status}</span>
        </div>

        <div class="panel-body">
            <dl class="bank-list">
                <div class="bank-row">
                    <dt>Mã đơn hàng</dt>
                    <dd class="mono">DH-<fmt:formatNumber value="${order.id}" pattern="00000" /></dd>
                </div>
                <div class="bank-row">
                    <dt>Số tiền</dt>
                    <dd class="mono">
                        <fmt:formatNumber value="${payment.amount}" type="currency" currencySymbol="₫" />
                    </dd>
                </div>
                <div class="bank-row">
                    <dt>Phương thức</dt>
                    <dd>
                        <c:choose>
                            <c:when test="${empty payment.method}">Nhận hàng trả tiền (COD)</c:when>
                            <c:when test="${payment.method == 'QR'}">Chuyển khoản QR</c:when>
                            <c:when test="${payment.method == 'MOMO'}">Ví MoMo</c:when>
                            <c:when test="${payment.method == 'BANK'}">Thẻ ngân hàng</c:when>
                            <c:otherwise>${payment.method}</c:otherwise>
                        </c:choose>
                    </dd>
                </div>
                <div class="bank-row">
                    <dt>Nội dung chuyển khoản</dt>
                    <dd>
                        <button type="button" class="copy js-copy" data-copy="${payment.transactionCode}">
                            <span>${payment.transactionCode}</span>
                            <span class="mono">COPY</span>
                        </button>
                    </dd>
                </div>
                <c:if test="${not empty payment.paidAt}">
                    <div class="bank-row">
                        <dt>Thời điểm xác nhận</dt>
                        <dd class="mono">
                            <fmt:formatDate value="${payment.paidAt}" pattern="dd/MM/yyyy HH:mm" />
                        </dd>
                    </div>
                </c:if>
            </dl>
        </div>

        <c:choose>
            <%-- ---------- Cho Admin doi soat ---------- --%>
            <c:when test="${payment.status == 'PENDING'}">
                <div class="panel-body" style="border-top: 1px solid var(--hairline)">
                    <div class="notice">
                        <strong>Đã ghi nhận yêu cầu thanh toán.</strong>
                        Hệ thống chưa tích hợp cổng thanh toán trực tuyến. Đơn hàng chuyển sang
                        <span class="mono">CONFIRMED</span> sau khi bộ phận kế toán đối soát và xác nhận thủ công,
                        thường trong vòng 24 giờ làm việc.
                    </div>

                    <c:if test="${not empty payment.method}">
                        <h3 style="margin: var(--space-lg) 0 var(--space-md); font-size: 17px">
                            Thông tin chuyển khoản
                        </h3>
                        <dl class="bank-list">
                            <div class="bank-row">
                                <dt>Ngân hàng</dt>
                                <dd>${bankName}</dd>
                            </div>
                            <div class="bank-row">
                                <dt>Chủ tài khoản</dt>
                                <dd>${bankAccountName}</dd>
                            </div>
                            <div class="bank-row">
                                <dt>Số tài khoản</dt>
                                <dd class="mono">${bankAccountNo}</dd>
                            </div>
                        </dl>
                    </c:if>
                </div>
            </c:when>

            <%-- ---------- Da thanh toan ---------- --%>
            <c:when test="${payment.status == 'PAID'}">
                <div class="panel-body" style="border-top: 1px solid var(--hairline)">
                    <div class="notice">
                        <strong>Thanh toán đã được xác nhận.</strong>
                        Đơn hàng đang được chuẩn bị bàn giao.
                    </div>
                </div>
            </c:when>

            <%-- ---------- That bai / huy ---------- --%>
            <c:otherwise>
                <div class="panel-body" style="border-top: 1px solid var(--hairline)">
                    <div class="notice">
                        <strong>Giao dịch không hoàn tất.</strong>
                        Đơn hàng đã bị huỷ. Bạn có thể đặt lại từ giỏ hàng.
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <aside>
        <div class="panel sticky">
            <div class="panel-head">
                <h3>Tóm tắt</h3>
            </div>

            <div class="panel-body">
                <div class="ledger">
                    <div class="ledger-row">
                        <span class="label">Tạm tính</span>
                        <span class="value money">
                            <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫" />
                        </span>
                    </div>
                    <div class="ledger-row muted">
                        <span class="label">Phí vận chuyển</span>
                        <span class="value">Miễn phí</span>
                    </div>
                    <div class="ledger-total">
                        <span class="label">Tổng thanh toán</span>
                        <span class="value">
                            <fmt:formatNumber value="${payment.amount}" type="currency" currencySymbol="₫" />
                        </span>
                    </div>
                </div>
            </div>

            <div class="panel-foot">
                <a class="btn btn-ghost btn-block" href="${ctx}/order?action=history">Xem đơn hàng</a>
                <a class="btn btn-primary btn-block" style="margin-top: var(--space-sm)"
                   href="${ctx}/product?action=list">Tiếp tục mua sắm</a>
            </div>
        </div>
    </aside>
</div>

<script>
(function () {
    document.querySelectorAll('.js-copy').forEach(function (el) {
        el.addEventListener('click', function () {
            var text = el.dataset.copy || '';
            // ponytail: chi can cho trinh duyet hien dai; khong keo theo clipboard lib.
            if (navigator.clipboard) navigator.clipboard.writeText(text);
        });
    });
})();
</script>

<%@ include file="_footer.jsp" %>
