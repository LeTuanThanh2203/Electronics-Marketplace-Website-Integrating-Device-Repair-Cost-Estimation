<%--
    Dat hang kenh NEW_DEVICE: tao Order(orderType = NEW_DEVICE) + Payment(PENDING).

    Da luoc bo so voi ban mockup, vi SDD khong co model cho chung:
      - voucher / ma giam gia  -> khong co entity Voucher
      - phi van chuyen         -> tich hop van chuyen ngoai MVP (SDD 12.x)
      - tinh/quan/phuong select -> khong co dia chi co cau truc; User chi co
                                   mot truong `location` dang chuoi tu do
    Dia chi giao hang duoi day la mot textarea bind vao `shippingAddress`:
    gia tri form-only, khong phai truong cua Order (SDD 13.7).

    Phuong thuc thanh toan map thang vao Payment.method ENUM(QR, MOMO, BANK),
    nullable (SDD 13.8):
      - COD (nhan hang tra tien) -> method = null
      - Chuyen khoan QR / Vi MoMo / The ngan hang -> method = QR / MOMO / BANK
    Du chon gi, Payment VAN duoc tao o PENDING va Admin xac nhan thu cong (BR-44).
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />

<c:set var="pageTitle" value="Thanh toán — Sửa hay bán" />
<c:set var="cart" value="${empty cart ? sessionScope.cart : cart}" />

<%@ include file="_header.jspf" %>

<div class="page-head">
    <p class="eyebrow">Bước cuối</p>
    <h1>Xác nhận đặt hàng</h1>
    <p class="crumbs">
        <a href="${ctx}/cart">Giỏ hàng</a>
        <span>/</span>
        <span class="current">Thanh toán</span>
    </p>
</div>

<c:if test="${not empty flashError}">
    <p class="flash flash-error">${flashError}</p>
</c:if>

<c:choose>
    <c:when test="${empty cart.items}">
        <div class="empty">
            <h2>Không có gì để đặt</h2>
            <p>Giỏ hàng của bạn đang trống.</p>
            <a class="btn btn-primary" href="${ctx}/product?action=list">Xem danh mục máy mới</a>
        </div>
    </c:when>

    <c:otherwise>
        <form method="post" action="${ctx}/checkout" novalidate>
            <div class="split">
                <div class="panel">
                    <%-- 1. Nguoi nhan --%>
                    <div class="panel-body">
                        <div class="step">
                            <span class="step-no">1</span>
                            <div class="step-body">
                                <div class="step-title">
                                    <h3>Người nhận</h3>
                                    <span class="step-note">Bắt buộc</span>
                                </div>

                                <div class="grid-2">
                                    <div class="field">
                                        <label for="recipientName">Họ và tên</label>
                                        <input type="text" id="recipientName" name="recipientName"
                                               value="${currentUser.name}" required>
                                    </div>
                                    <div class="field">
                                        <label for="recipientPhone">Số điện thoại</label>
                                        <input type="tel" id="recipientPhone" name="recipientPhone"
                                               value="${currentUser.phone}" required>
                                    </div>
                                </div>

                                <div class="field">
                                    <label for="recipientEmail">Email</label>
                                    <input type="email" id="recipientEmail" name="recipientEmail"
                                           value="${currentUser.email}" required>
                                    <p class="hint">Dùng để gửi xác nhận đơn hàng.</p>
                                </div>
                            </div>
                        </div>

                        <%-- 2. Dia chi giao --%>
                        <div class="step">
                            <span class="step-no">2</span>
                            <div class="step-body">
                                <div class="step-title">
                                    <h3>Địa chỉ giao hàng</h3>
                                    <span class="step-note">Bắt buộc</span>
                                </div>

                                <div class="field">
                                    <label for="shippingAddress">Địa chỉ chi tiết</label>
                                    <textarea id="shippingAddress" name="shippingAddress"
                                              required placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố">${currentUser.location}</textarea>
                                </div>
                            </div>
                        </div>

                        <%-- 3. Phuong thuc thanh toan --%>
                        <div class="step">
                            <span class="step-no">3</span>
                            <div class="step-body">
                                <div class="step-title">
                                    <h3>Phương thức thanh toán</h3>
                                    <span class="step-note">Chọn một</span>
                                </div>

                                <div class="field">
                                    <label class="choice">
                                        <input type="radio" name="paymentMethod" value="" checked>
                                        <span>
                                            <strong>Nhận hàng trả tiền (COD)</strong>
                                            <span>Kiểm tra máy trước khi trả. Không phát sinh giao dịch trực tuyến.</span>
                                        </span>
                                    </label>
                                </div>

                                <div class="field">
                                    <label class="choice">
                                        <input type="radio" name="paymentMethod" value="QR">
                                        <span>
                                            <strong>Chuyển khoản QR</strong>
                                            <span>Quét mã bằng ứng dụng ngân hàng. Đơn được xác nhận sau khi đối soát.</span>
                                        </span>
                                    </label>
                                </div>

                                <div class="field">
                                    <label class="choice">
                                        <input type="radio" name="paymentMethod" value="MOMO">
                                        <span>
                                            <strong>Ví MoMo</strong>
                                            <span>Thanh toán qua ví điện tử MoMo.</span>
                                        </span>
                                    </label>
                                </div>

                                <div class="field">
                                    <label class="choice">
                                        <input type="radio" name="paymentMethod" value="BANK">
                                        <span>
                                            <strong>Thẻ ngân hàng</strong>
                                            <span>Thanh toán bằng thẻ nội địa hoặc quốc tế.</span>
                                        </span>
                                    </label>
                                </div>
                            </div>
                        </div>

                        <%-- 4. Ghi chu --%>
                        <div class="step">
                            <span class="step-no">4</span>
                            <div class="step-body">
                                <div class="step-title">
                                    <h3>Ghi chú</h3>
                                    <span class="step-note">Không bắt buộc</span>
                                </div>

                                <div class="field">
                                    <label for="note">Lời nhắn cho trạm kỹ thuật</label>
                                    <textarea id="note" name="note" maxlength="300"
                                              placeholder="Ví dụ: gọi trước khi giao, kiểm tra bàn phím giúp em..."></textarea>
                                    <p class="hint">Tối đa 300 ký tự.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <%-- Tom tat --%>
                <aside>
                    <div class="panel sticky">
                        <div class="panel-head">
                            <h3>Đơn của bạn</h3>
                            <span class="mono">${fn:length(cart.items)} món</span>
                        </div>

                        <div class="panel-body">
                            <c:forEach var="item" items="${cart.items}">
                                <div class="ledger-row" style="margin-bottom: 10px">
                                    <span class="label">${item.product.name}</span>
                                    <span class="value money">
                                        <fmt:formatNumber value="${item.product.price}" type="currency" currencySymbol="₫" />
                                    </span>
                                </div>
                            </c:forEach>

                            <div class="ledger" style="margin-top: var(--space-md)">
                                <div class="ledger-row muted">
                                    <span class="label">Phí vận chuyển</span>
                                    <span class="value">Miễn phí</span>
                                </div>
                                <div class="ledger-total">
                                    <span class="label">Tổng cộng</span>
                                    <span class="value">
                                        <fmt:formatNumber value="${cart.subtotal}" type="currency" currencySymbol="₫" />
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div class="panel-foot">
                            <button type="submit" class="btn btn-accent btn-block">ĐẶT HÀNG NGAY</button>
                        </div>
                    </div>

                    <p class="hint" style="margin-top: var(--space-md)">
                        Bằng việc đặt hàng, bạn đồng ý để trạm kỹ thuật liên hệ xác nhận thời gian giao máy.
                    </p>
                </aside>
            </div>
        </form>
    </c:otherwise>
</c:choose>

<%@ include file="_footer.jsp" %>
