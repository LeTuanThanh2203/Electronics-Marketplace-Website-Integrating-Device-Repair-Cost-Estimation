<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="rpPage" value="track" scope="request"/>
<c:set var="rpCrumb" value="Tra cứu tiến độ" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tra cứu tiến độ sửa chữa | Sửa hay bán</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&family=Space+Grotesk:wght@500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/repair.css">
</head>
<body class="rp-body">

<div class="rp-shell">
    <%@ include file="/WEB-INF/views/repair/_header.jspf" %>

    <main class="rp-main">

        <div class="rp-pagehead">
            <div class="rp-eyebrow">
                <span class="rp-eyebrow__dot"></span>
                <span>TRA CỨU PHIẾU // TIẾN ĐỘ &amp; BÁO GIÁ ĐÃ LƯU</span>
            </div>
            <h1>Tra cứu tiến độ</h1>
            <p class="rp-pagehead__lead">
                Nhập mã phiếu sửa chữa để xem trạng thái xử lý và báo giá dự toán đã lưu cho phiếu đó.
            </p>
        </div>

        <div class="rp-workbench">
            <div class="rp-workbench__grid" style="grid-template-columns: minmax(0, 1fr); max-width: 820px; margin: 0 auto;">

                <!-- Ô tra cứu -->
                <div class="rp-panel">
                    <div class="rp-panel__head">
                        <span class="rp-panel__title">Nhập mã phiếu</span>
                        <span class="rp-panel__status"><span class="rp-dot"></span> TRỰC TUYẾN</span>
                    </div>

                    <form action="${pageContext.request.contextPath}/repair/track" method="GET">
                        <label class="rp-label" for="reqId">
                            <span>Mã phiếu sửa chữa <span class="rp-req">*</span></span>
                            <span class="rp-label__meta">VÍ DỤ: 1</span>
                        </label>
                        <div style="display:flex; gap:12px; margin-top:8px;">
                            <%-- value dùng c:out để escape, tránh chèn HTML qua query string --%>
                            <input class="rp-input" type="number" id="reqId" name="id" min="1"
                                   placeholder="Nhập mã phiếu..." value="<c:out value='${param.id}' />" required
                                   style="font-family:var(--rp-font-mono);">
                            <button class="rp-btn rp-btn--primary" type="submit" style="flex-shrink:0;">
                                Tra cứu
                            </button>
                        </div>
                    </form>

                    <c:if test="${not empty errorMessage}">
                        <div class="rp-alert rp-alert--error" style="margin-top:16px; margin-bottom:0;">
                            <c:out value="${errorMessage}" />
                        </div>
                    </c:if>
                </div>

                <!-- Kết quả -->
                <c:if test="${not empty repairRequest}">
                    <div class="rp-panel">
                        <div class="rp-panel__head">
                            <span class="rp-panel__title">
                                Phiếu sửa chữa #<c:out value="${repairRequest.id}" />
                            </span>
                            <c:choose>
                                <c:when test="${repairRequest.status == 'ESTIMATED'}">
                                    <span class="rp-badge rp-badge--estimated">Đã báo giá</span>
                                </c:when>
                                <c:when test="${repairRequest.status == 'WAITING_PRICING'}">
                                    <span class="rp-badge rp-badge--waiting">Chờ báo giá</span>
                                </c:when>
                                <c:when test="${repairRequest.status == 'IN_PROGRESS'}">
                                    <span class="rp-badge rp-badge--progress">Đang sửa chữa</span>
                                </c:when>
                                <c:when test="${repairRequest.status == 'COMPLETED'}">
                                    <span class="rp-badge rp-badge--done">Hoàn thành</span>
                                </c:when>
                                <c:when test="${repairRequest.status == 'CANCELLED'}">
                                    <span class="rp-badge rp-badge--cancelled">Đã hủy</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="rp-badge rp-badge--pending"><c:out value="${repairRequest.status}" /></span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="rp-kv">
                            <div class="rp-kv__row">
                                <span class="rp-kv__key">Khách hàng</span>
                                <span class="rp-kv__val"><c:out value="${repairRequest.customerName}" /></span>
                            </div>
                            <div class="rp-kv__row">
                                <span class="rp-kv__key">Số điện thoại</span>
                                <span class="rp-kv__val rp-kv__val--mono"><c:out value="${repairRequest.customerPhone}" /></span>
                            </div>
                            <div class="rp-kv__row">
                                <span class="rp-kv__key">Thiết bị</span>
                                <span class="rp-kv__val"><c:out value="${repairRequest.device.displayName}" /></span>
                            </div>
                            <div class="rp-kv__row">
                                <span class="rp-kv__key">Loại lỗi</span>
                                <span class="rp-kv__val"><c:out value="${repairRequest.problem.name}" /></span>
                            </div>
                            <div class="rp-kv__row">
                                <span class="rp-kv__key">Ngày tạo</span>
                                <span class="rp-kv__val">
                                    <fmt:formatDate value="${repairRequest.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                </span>
                            </div>
                        </div>

                        <c:if test="${not empty repairRequest.description}">
                            <div class="rp-quote" style="margin-top:16px;">
                                <div class="rp-quote__label">Mô tả tình trạng từ khách hàng</div>
                                <p><c:out value="${repairRequest.description}" /></p>
                            </div>
                        </c:if>

                        <c:if test="${not empty repairRequest.imageUrl}">
                            <div class="rp-quote" style="margin-top:16px;">
                                <div class="rp-quote__label">Ảnh mô tả lỗi đính kèm</div>
                                <div style="margin-top:8px;">
                                    <a href="${repairRequest.imageUrl}" target="_blank" rel="noopener noreferrer">
                                        <img src="${repairRequest.imageUrl}" alt="Ảnh mô tả lỗi"
                                             style="max-width:100%; max-height:220px; object-fit:contain; border:1px solid var(--rp-hairline); display:block; background:#fff; padding:4px;">
                                    </a>
                                </div>
                            </div>
                        </c:if>

                        <c:choose>
                            <c:when test="${not empty repairEstimate}">
                                <div style="margin-top:20px;">
                                    <div style="font-family:var(--rp-font-mono); font-size:12px; font-weight:600;
                                                text-transform:uppercase; letter-spacing:.05em; margin-bottom:8px;">
                                        Chi phí dự toán ban đầu
                                    </div>
                                    <div class="rp-estimate__range">
                                        <fmt:formatNumber value="${repairEstimate.minCost}" pattern="#,###" /> ₫
                                        –
                                        <fmt:formatNumber value="${repairEstimate.maxCost}" pattern="#,###" /> ₫
                                    </div>
                                    <div class="rp-estimate__rows" style="margin-top:14px;">
                                        <div class="rp-estimate__row">
                                            <dt>Linh kiện</dt>
                                            <dd><fmt:formatNumber value="${repairEstimate.partsCostSnapshot}" pattern="#,###" /> ₫</dd>
                                        </div>
                                        <div class="rp-estimate__row">
                                            <dt>Nhân công</dt>
                                            <dd><fmt:formatNumber value="${repairEstimate.laborCostSnapshot}" pattern="#,###" /> ₫</dd>
                                        </div>
                                        <div class="rp-estimate__row">
                                            <dt>Phiên bản giá</dt>
                                            <dd>v<c:out value="${repairEstimate.pricingVersion}" /></dd>
                                        </div>
                                        <div class="rp-estimate__row">
                                            <dt>Tính lúc</dt>
                                            <dd><fmt:formatDate value="${repairEstimate.calculatedAt}" pattern="dd/MM/yyyy HH:mm" /></dd>
                                        </div>
                                    </div>
                                    <div class="rp-estimate__note" style="margin-top:14px;">
                                        Đây là báo giá dự kiến tại thời điểm tiếp nhận. Chi phí cuối cùng được
                                        xác lập sau khi kỹ thuật viên kiểm tra trực tiếp máy.
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="rp-empty" style="margin-top:20px;">
                                    <div class="rp-empty__title">Chưa có báo giá</div>
                                    <p>
                                        Cửa hàng chưa thiết lập bảng giá cho cấu hình này.
                                        Kỹ thuật viên sẽ liên hệ báo giá sau khi kiểm tra máy.
                                    </p>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <div class="rp-actions">
                            <a class="rp-btn rp-btn--ghost" href="${pageContext.request.contextPath}/repair">Tạo yêu cầu mới</a>
                        </div>
                    </div>
                </c:if>

            </div>
        </div>
    </main>

    <%@ include file="/WEB-INF/views/repair/_footer.jspf" %>
</div>

</body>
</html>
