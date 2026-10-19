<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="rpPage" value="estimate" scope="request"/>
<c:set var="rpCrumb" value="Dự toán chi phí" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dự toán chi phí sửa chữa | Sửa hay bán</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&family=Space+Grotesk:wght@500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/repair.css">
</head>
<body class="rp-body">

<div class="rp-shell">

    <%@ include file="/WEB-INF/views/repair/_header.jspf" %>

    <main class="rp-main">

        <!-- ================= PAGE HEADER ================= -->
        <div class="rp-pagehead">
            <div class="rp-eyebrow">
                <span class="rp-eyebrow__dot"></span>
                <span>MODULE #14 // CHI PHÍ VẬT TƯ &amp; GIỜ CÔNG</span>
            </div>
            <h1>Dự toán chi phí sửa chữa</h1>
            <p class="rp-pagehead__lead">
                Chi phí dự kiến dựa trên bảng giá linh kiện và giờ công tiêu chuẩn tại phòng kỹ thuật.
                Đây là khoảng giá tham khảo, không phải báo giá cố định cuối cùng.
            </p>
        </div>

        <!-- ================= WORKBENCH ================= -->
        <div class="rp-workbench">
            <div class="rp-workbench__grid rp-workbench__grid--split">

                <!-- ---------- CỘT TRÁI: THÔNG TIN PHIẾU ---------- -->
                <section class="rp-form">

                    <div class="rp-section">
                        <div class="rp-success">
                            <span class="rp-success__icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                     stroke-width="3" stroke-linecap="square">
                                    <path d="M20 6L9 17l-5-5"/>
                                </svg>
                            </span>
                            <div>
                                <h2>Yêu cầu sửa chữa đã được tiếp nhận</h2>
                                <p>
                                    Mã phiếu của bạn là <strong>#<c:out value="${repairRequest.id}" /></strong>.
                                    Vui lòng lưu lại mã này để tra cứu tiến độ.
                                </p>
                            </div>
                        </div>
                    </div>

                    <div class="rp-section">
                        <div class="rp-section__head">
                            <div class="rp-section__title">
                                <span class="rp-section__num">01</span>
                                <h2>Chi tiết phiếu tiếp nhận</h2>
                            </div>
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
                                <span class="rp-kv__key">Mã phiếu</span>
                                <span class="rp-kv__val rp-kv__val--mono">#<c:out value="${repairRequest.id}" /></span>
                            </div>
                            <div class="rp-kv__row">
                                <span class="rp-kv__key">Thời gian tạo</span>
                                <span class="rp-kv__val">
                                    <fmt:formatDate value="${repairRequest.createdAt}" pattern="dd/MM/yyyy HH:mm:ss" />
                                </span>
                            </div>
                            <div class="rp-kv__row">
                                <span class="rp-kv__key">Khách hàng</span>
                                <span class="rp-kv__val"><c:out value="${repairRequest.customerName}" /></span>
                            </div>
                            <div class="rp-kv__row">
                                <span class="rp-kv__key">Số điện thoại</span>
                                <span class="rp-kv__val rp-kv__val--mono"><c:out value="${repairRequest.customerPhone}" /></span>
                            </div>
                            <div class="rp-kv__row">
                                <span class="rp-kv__key">Dòng thiết bị</span>
                                <span class="rp-kv__val"><c:out value="${repairRequest.device.displayName}" /></span>
                            </div>
                            <div class="rp-kv__row">
                                <span class="rp-kv__key">Loại hư hỏng</span>
                                <span class="rp-kv__val"><c:out value="${repairRequest.problem.name}" /></span>
                            </div>
                            <c:if test="${not empty repairRequest.customerEmail}">
                                <div class="rp-kv__row">
                                    <span class="rp-kv__key">Email</span>
                                    <span class="rp-kv__val"><c:out value="${repairRequest.customerEmail}" /></span>
                                </div>
                            </c:if>
                        </div>

                        <c:if test="${not empty repairRequest.description}">
                            <div class="rp-quote">
                                <div class="rp-quote__label">Mô tả tình trạng từ khách hàng</div>
                                <p><c:out value="${repairRequest.description}" /></p>
                            </div>
                        </c:if>
                    </div>

                    <div class="rp-section rp-section--commit">
                        <div class="rp-section__head">
                            <div class="rp-section__title">
                                <span class="rp-section__num">02</span>
                                <h2>Bước tiếp theo</h2>
                            </div>
                        </div>
                        <p style="margin:0 0 20px; font-size:14px; line-height:1.65; color:#71717A;">
                            Kỹ thuật viên sẽ liên hệ theo số điện thoại bạn cung cấp để xác nhận lịch tiếp nhận.
                            Chi phí chính xác chỉ được xác lập sau khi kiểm tra trực tiếp linh kiện tại trạm.
                        </p>
                        <div class="rp-actions" style="margin-top:0;">
                            <a class="rp-btn rp-btn--ghost" href="${pageContext.request.contextPath}/repair">Tạo yêu cầu khác</a>
                            <a class="rp-btn rp-btn--primary" href="${pageContext.request.contextPath}/repair/track?id=${repairRequest.id}">
                                Tra cứu phiếu này
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                     stroke-width="2.5" stroke-linecap="square">
                                    <path d="M14 5l7 7-7 7M21 12H3"/>
                                </svg>
                            </a>
                        </div>
                    </div>
                </section>

                <!-- ---------- CỘT PHẢI: KẾT QUẢ DỰ TOÁN ---------- -->
                <section class="rp-aside" style="position:static;">

                    <c:choose>
                        <%-- ===== CÓ BÁO GIÁ ===== --%>
                        <c:when test="${not empty repairEstimate}">
                            <div class="rp-panel">
                                <div class="rp-panel__head">
                                    <span class="rp-panel__title">Kết quả dự toán tham khảo</span>
                                    <span class="rp-dot"></span>
                                </div>

                                <div style="font-family:var(--rp-font-mono); font-size:12px; text-transform:uppercase;
                                            letter-spacing:.03em; color:var(--rp-gray); margin-bottom:6px;">
                                    Mức chi phí dự kiến (khoảng giá tham khảo)
                                </div>
                                <div class="rp-estimate__range">
                                    <fmt:formatNumber value="${repairEstimate.minCost}" pattern="#,###" /> ₫
                                    –
                                    <fmt:formatNumber value="${repairEstimate.maxCost}" pattern="#,###" /> ₫
                                </div>
                                <p style="margin:8px 0 0; font-size:13px; line-height:1.55; color:var(--rp-gray);">
                                    Khoảng giá theo bảng giá chuẩn, chưa bao gồm phát sinh ngoài hạng mục báo giá.
                                </p>

                                <div style="margin-top:20px;">
                                    <div style="font-family:var(--rp-font-mono); font-size:12px; font-weight:600;
                                                text-transform:uppercase; letter-spacing:.05em; margin-bottom:8px;">
                                        Phân bổ chi tiết
                                    </div>
                                    <div class="rp-breakdown">
                                        <div class="rp-breakdown__row">
                                            <span class="rp-breakdown__label">Chi phí linh kiện thay thế</span>
                                            <span class="rp-breakdown__value">
                                                <fmt:formatNumber value="${repairEstimate.partsCostSnapshot}" pattern="#,###" /> ₫
                                            </span>
                                        </div>
                                        <div class="rp-breakdown__row">
                                            <span class="rp-breakdown__label">Chi phí nhân công tháo lắp &amp; kiểm thử</span>
                                            <span class="rp-breakdown__value">
                                                <fmt:formatNumber value="${repairEstimate.laborCostSnapshot}" pattern="#,###" /> ₫
                                            </span>
                                        </div>
                                        <c:if test="${not empty repairEstimate.estimationMarginSnapshot}">
                                            <div class="rp-breakdown__row">
                                                <span class="rp-breakdown__label">Biên độ dự toán</span>
                                                <span class="rp-breakdown__value">
                                                    <fmt:formatNumber value="${repairEstimate.estimationMarginSnapshot * 100}"
                                                                      pattern="0.##" />%
                                                </span>
                                            </div>
                                        </c:if>
                                        <div class="rp-breakdown__row rp-breakdown__row--total">
                                            <span class="rp-breakdown__label" style="font-weight:700;">Tổng dự toán</span>
                                            <span class="rp-breakdown__value rp-breakdown__value--accent">
                                                <fmt:formatNumber value="${repairEstimate.minCost}" pattern="#,###" /> –
                                                <fmt:formatNumber value="${repairEstimate.maxCost}" pattern="#,###" /> ₫
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="rp-estimate__rows" style="margin-top:16px;">
                                    <div class="rp-estimate__row">
                                        <dt>Phiên bản giá</dt>
                                        <dd>v<c:out value="${repairEstimate.pricingVersion}" /></dd>
                                    </div>
                                    <div class="rp-estimate__row">
                                        <dt>Thời gian dự kiến</dt>
                                        <dd>
                                            <c:out value="${repairRequest.problem.estimatedMinDays}" /> –
                                            <c:out value="${repairRequest.problem.estimatedMaxDays}" /> ngày
                                        </dd>
                                    </div>
                                    <div class="rp-estimate__row">
                                        <dt>Tính lúc</dt>
                                        <dd><fmt:formatDate value="${repairEstimate.calculatedAt}" pattern="dd/MM/yyyy HH:mm" /></dd>
                                    </div>
                                </div>

                                <div class="rp-estimate__note" style="margin-top:16px;">
                                    Đây là mức chi phí ước tính dựa trên cấu hình đã chọn, <strong>không phải</strong>
                                    báo giá cố định. Chi phí chính xác được kỹ thuật viên đo đạc trực tiếp và thông báo
                                    bằng văn bản trước khi thực hiện. Khách hàng có quyền từ chối sửa chữa mà không mất
                                    phí kiểm định.
                                </div>
                            </div>
                        </c:when>

                        <%-- ===== CHƯA CÓ BẢNG GIÁ ===== --%>
                        <c:otherwise>
                            <div class="rp-panel">
                                <div class="rp-panel__head">
                                    <span class="rp-panel__title">Kết quả dự toán tham khảo</span>
                                    <span class="rp-panel__status">
                                        <span class="rp-dot" style="background:#D97706;"></span> CHỜ BÁO GIÁ
                                    </span>
                                </div>
                                <div class="rp-empty">
                                    <div class="rp-empty__title">Chưa có bảng giá cho cấu hình này</div>
                                    <p>
                                        Cửa hàng chưa thiết lập bảng giá chuẩn cho
                                        <strong><c:out value="${repairRequest.device.displayName}" /></strong>
                                        với lỗi <strong><c:out value="${repairRequest.problem.name}" /></strong>.
                                        Phiếu <strong>#<c:out value="${repairRequest.id}" /></strong> đã được ghi nhận;
                                        kỹ thuật viên sẽ liên hệ báo giá sau khi kiểm tra máy.
                                    </p>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <!-- Quy trình -->
                    <div class="rp-panel">
                        <div class="rp-panel__head">
                            <span class="rp-panel__title">Quy trình kiểm định minh bạch</span>
                            <span class="rp-dot"></span>
                        </div>
                        <div class="rp-steps">
                            <div class="rp-step">
                                <span class="rp-step__num">1</span>
                                <div class="rp-step__body">
                                    <h3>Tiếp nhận &amp; Ký tên linh kiện</h3>
                                    <p>Khách ký tên lên Mainboard, Pin, SSD, Màn hình và RAM trước khi rời bàn kỹ thuật.</p>
                                </div>
                            </div>
                            <div class="rp-step">
                                <span class="rp-step__num">2</span>
                                <div class="rp-step__body">
                                    <h3>Kiểm tra đo đạc thông số</h3>
                                    <p>Đo nguồn xung, kiểm tra mạch chạm chập. Xác định chuẩn xác linh kiện suy hao.</p>
                                </div>
                            </div>
                            <div class="rp-step">
                                <span class="rp-step__num">3</span>
                                <div class="rp-step__body">
                                    <h3>Báo giá chi tiết &amp; Phương án</h3>
                                    <p>Chỉ thực hiện khi khách hàng xác nhận giá bằng biên bản.</p>
                                </div>
                            </div>
                            <div class="rp-step">
                                <span class="rp-step__num rp-step__num--last">4</span>
                                <div class="rp-step__body">
                                    <h3>Bàn giao &amp; Kích hoạt bảo hành</h3>
                                    <p>Khách kiểm tra chữ ký niêm phong. Cấp mã bảo hành điện tử.</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="rp-panel">
                        <div class="rp-panel__head">
                            <span class="rp-panel__title">Hỗ trợ kỹ thuật</span>
                            <span class="rp-panel__status">TRỰC TIẾP</span>
                        </div>
                        <div class="rp-support">
                            <div class="rp-support__item">
                                <span class="rp-support__icon">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="square">
                                        <path d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
                                    </svg>
                                </span>
                                <div>
                                    <div class="rp-support__label">Đường dây nóng kỹ thuật</div>
                                    <div class="rp-support__value rp-support__value--hotline">1900 6868</div>
                                    <div class="rp-support__sub">Nhánh 1 – gặp kỹ sư trưởng ca</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </div>

        <!-- ================= CAM KẾT ================= -->
        <section class="rp-features">
            <div class="rp-features__title">CAM KẾT QUY TRÌNH KỸ THUẬT MINH BẠCH</div>
            <div class="rp-features__grid">
                <div class="rp-feature">
                    <div class="rp-feature__code">01 // QUY TRÌNH KÝ DUYỆT</div>
                    <h3>Không chi phí ngầm</h3>
                    <p>Chỉ tiến hành khi khách hàng ký duyệt báo giá chi tiết bằng biên bản xác nhận.</p>
                </div>
                <div class="rp-feature">
                    <div class="rp-feature__code">02 // XÁC MINH NGUỒN GỐC</div>
                    <h3>Linh kiện kiểm định</h3>
                    <p>Linh kiện rõ xuất xứ. Bàn giao lại 100% linh kiện cũ hỏng đã thay thế cho khách hàng.</p>
                </div>
                <div class="rp-feature">
                    <div class="rp-feature__code">03 // CHẾ ĐỘ BẢO HÀNH</div>
                    <h3>Bảo hành 6 – 12 tháng</h3>
                    <p>Lỗi 1 đổi 1 nếu tái diễn, dán tem niêm phong chống tĩnh điện chuẩn phòng Lab.</p>
                </div>
                <div class="rp-feature">
                    <div class="rp-feature__code">04 // BẢO MẬT PHẦN CỨNG</div>
                    <h3>Ký trực tiếp linh kiện</h3>
                    <p>Khách được ký bút không xóa lên RAM, SSD, Pin, Màn hình trước khi bàn giao máy.</p>
                </div>
            </div>
        </section>
    </main>

    <%@ include file="/WEB-INF/views/repair/_footer.jspf" %>
</div>

</body>
</html>
