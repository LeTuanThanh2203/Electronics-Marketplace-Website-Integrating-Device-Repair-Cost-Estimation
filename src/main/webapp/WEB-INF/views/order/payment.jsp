<%--
    FR-PAY-01/02 - Man hinh thanh toan kenh NEW_DEVICE (SDD 13.8).

    Hai phuong thuc (theo yeu cau: bo chuyen khoan QR va the ngan hang):
      - COD  (Payment.method = null): PENDING, Admin xac nhan thu cong (BR-44)
      - MOMO (Payment.method = MOMO): nut mo cong MoMo, cong tu xac nhan PAID
        qua return URL + IPN (xem PaymentServlet)

    Model: ${payment} (Payment), ${payment.order} (Order).
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />

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
<c:if test="${not empty flashOk}">
    <div style="background: #ecfdf5; color: #065f46; border: 1px solid #a7f3d0; padding: 12px 16px; border-radius: 6px; margin-bottom: 16px; font-weight: 500; font-family: monospace;">
        ✓ ${flashOk}
    </div>
</c:if>
<c:if test="${not empty flashNotice}">
    <div style="background: #eff6ff; color: #1e40af; border: 1px solid #bfdbfe; padding: 12px 16px; border-radius: 6px; margin-bottom: 16px; font-size: 13px; line-height: 1.5;">
        ℹ <strong>Thông báo MoMo Sandbox:</strong> ${flashNotice}
    </div>
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
                            <c:when test="${payment.method == 'MOMO'}">Ví MoMo (Sandbox)</c:when>
                            <c:otherwise>${payment.method}</c:otherwise>
                        </c:choose>
                    </dd>
                </div>
                <div class="bank-row">
                    <dt>Mã giao dịch</dt>
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
            <%-- ---------- Cho thanh toan / doi soat ---------- --%>
            <c:when test="${payment.status == 'PENDING'}">
                <div class="panel-body" style="border-top: 1px solid var(--hairline)">
                    <c:choose>
                        <c:when test="${payment.method == 'MOMO'}">
                            <div class="notice" style="margin-bottom: 16px;">
                                <strong>Cổng Thanh Toán MoMo (Môi trường Sandbox Thử Nghiệm)</strong><br>
                                Bạn có thể quét mã QR MoMo bên dưới bằng app MoMo test hoặc bấm nút xác nhận mô phỏng để tự động chuyển đơn hàng sang trạng thái <span class="mono">CONFIRMED</span>.
                            </div>

                            <!-- MoMo QR Code & Instant Demo Confirmation -->
                            <div style="background: #fff5f7; border: 1.5px dashed #f43f5e; border-radius: 8px; padding: 20px; text-align: center; margin-bottom: 16px;">
                                <div style="display: inline-flex; align-items: center; gap: 8px; margin-bottom: 12px;">
                                    <span style="background: #a50064; color: #fff; font-weight: 800; font-size: 13px; padding: 3px 8px; border-radius: 4px; font-family: monospace;">MoMo</span>
                                    <span style="color: #9f1239; font-weight: 700; font-size: 14px;">Quét Mã QR MoMo</span>
                                </div>
                                <div style="background: #fff; padding: 10px; display: inline-block; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 12px;">
                                    <img src="https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=2|99|0912888999|||0|0|${payment.amount}|DH-${order.id}|transfer_myqr"
                                         alt="Mã QR MoMo"
                                         style="width: 180px; height: 180px; display: block;" />
                                </div>
                                <div style="font-size: 13px; color: #475569; line-height: 1.6;">
                                    <div>Số tiền thanh toán: <strong style="color: #e11d48; font-size: 16px; font-family: monospace;"><fmt:formatNumber value="${payment.amount}" pattern="#,###"/> ₫</strong></div>
                                    <div>Nội dung chuyển khoản: <strong class="mono" style="color: #0f172a;">DH-<fmt:formatNumber value="${order.id}" pattern="00000" /></strong></div>
                                </div>
                            </div>

                            <div style="display: flex; flex-direction: column; gap: 10px;">
                                <a class="btn btn-primary btn-block"
                                   style="background: #059669; border-color: #059669; color: #fff; text-align: center; text-decoration: none; padding: 12px; font-weight: 700;"
                                   href="${ctx}/payment?action=simulate&orderId=${order.id}">
                                    ⚡ XÁC NHẬN ĐÃ THANH TOÁN (MÔ PHỎNG TEST SANDBOX)
                                </a>
                                <a class="btn btn-secondary btn-block"
                                   style="text-align: center; text-decoration: none; font-size: 13px;"
                                   href="${ctx}/payment?action=create&orderId=${order.id}">
                                    Mở Cổng Trực Tiếp test-payment.momo.vn →
                                </a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="notice">
                                <strong>Đã ghi nhận đơn hàng.</strong>
                                Bạn thanh toán khi nhận hàng. Đơn chuyển sang
                                <span class="mono">CONFIRMED</span> sau khi bộ phận bán hàng
                                xác nhận, thường trong vòng 24 giờ làm việc.
                            </div>
                        </c:otherwise>
                    </c:choose>
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
