<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="rpPage" value="request" scope="request"/>
<c:set var="rpCrumb" value="Yêu cầu sửa chữa" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yêu cầu sửa chữa | Sửa hay bán</title>
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
                <span>DỊCH VỤ PHÒNG LAB KỸ THUẬT // ĐẶT LỊCH KHÁM MÁY &amp; DỰ TOÁN</span>
            </div>
            <h1>Yêu cầu sửa chữa</h1>
            <p class="rp-pagehead__lead">
                Mô tả tình trạng laptop để nhận tư vấn và dự toán chi phí sửa chữa.
                Kỹ thuật viên đối soát lỗi linh kiện công khai trước khi tiến hành can thiệp phần cứng.
            </p>

            <div class="rp-note">
                <svg class="rp-note__icon" width="20" height="20" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="square">
                    <path d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
                </svg>
                <p>
                    <span class="rp-note__label">[Lưu ý minh bạch]</span>
                    Báo giá cuối cùng chỉ được xác lập sau khi kỹ thuật viên kiểm tra trực tiếp linh kiện và
                    đo đạc phần cứng trước mặt khách hàng tại bàn tiếp nhận. Mọi chi phí ước tính trực tuyến
                    chỉ mang tính định hướng quy trình.
                </p>
            </div>
        </div>

        <!-- ================= WORKBENCH ================= -->
        <div class="rp-workbench">
            <div class="rp-workbench__grid">

                <!-- ---------- FORM ---------- -->
                <form class="rp-form" id="repairForm"
                      action="${pageContext.request.contextPath}/repair/request" method="POST">

                    <c:if test="${not empty errorMessage}">
                        <div class="rp-section" style="padding-bottom:0; border-bottom:none;">
                            <div class="rp-alert rp-alert--error">
                                <strong>Lỗi:</strong> <c:out value="${errorMessage}" />
                            </div>
                        </div>
                    </c:if>

                    <!-- SECTION 01 -->
                    <div class="rp-section">
                        <div class="rp-section__head">
                            <div class="rp-section__title">
                                <span class="rp-section__num">01</span>
                                <h2>Thông tin laptop</h2>
                            </div>
                            <span class="rp-section__hint rp-section__hint--required">* Bắt buộc nhập</span>
                        </div>

                        <div class="rp-grid-2">
                            <div class="rp-field">
                                <label class="rp-label" for="deviceId">
                                    <span>Dòng máy / Thiết bị <span class="rp-req">*</span></span>
                                    <span class="rp-label__meta">MODEL</span>
                                </label>
                                <div class="rp-select-wrap">
                                    <select class="rp-select" name="deviceId" id="deviceId" required onchange="calculateEstimate()">
                                        <option value="">-- Chọn dòng thiết bị --</option>
                                        <c:forEach items="${devices}" var="device">
                                            <option value="${device.id}"
                                                    data-cpu="<c:out value='${device.cpu}' />"
                                                    data-ram="<c:out value='${device.ram}' />"
                                                    data-storage="<c:out value='${device.storage}' />"
                                                    data-year="<c:out value='${device.releaseYear}' />">
                                                <c:out value="${device.brand} ${device.series} ${device.model}" /><c:if test="${not empty device.releaseYear}"> (<c:out value="${device.releaseYear}" />)</c:if>
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>

                            <div class="rp-field">
                                <label class="rp-label" for="problemId">
                                    <span>Danh mục sự cố <span class="rp-req">*</span></span>
                                    <span class="rp-label__meta">FAULT TYPE</span>
                                </label>
                                <div class="rp-select-wrap">
                                    <select class="rp-select" name="problemId" id="problemId" required onchange="calculateEstimate()">
                                        <option value="">-- Chọn loại lỗi hỏng --</option>
                                        <c:forEach items="${problems}" var="prob">
                                            <option value="${prob.id}"><c:out value="${prob.name}" /></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <%-- Thông số dòng máy đang chọn: lấy từ danh mục Device --%>
                        <div class="rp-spec" id="deviceSpec"></div>
                    </div>

                    <!-- SECTION 02 -->
                    <div class="rp-section">
                        <div class="rp-section__head">
                            <div class="rp-section__title">
                                <span class="rp-section__num">02</span>
                                <h2>Tình trạng lỗi</h2>
                            </div>
                            <span class="rp-section__hint rp-section__hint--required">CHỌN LOẠI LỖI Ở TRÊN *</span>
                        </div>

                        <div class="rp-field">
                            <label class="rp-label" for="description">
                                <span>Mô tả chi tiết tình trạng máy <span class="rp-req">*</span></span>
                                <span class="rp-label__meta">Càng chi tiết - Báo giá càng chính xác</span>
                            </label>
                            <textarea class="rp-textarea" id="description" name="description" rows="4" required
                                      placeholder="Mô tả cụ thể biểu hiện lỗi: xảy ra từ khi nào, sau sự cố gì (rơi rớt, đổ nước), máy đã qua sửa chữa ở đâu chưa, các dữ liệu quan trọng cần lưu ý bảo vệ..."></textarea>
                        </div>

                        <div class="rp-field" style="margin-top:24px;">
                            <span class="rp-label">
                                <span>Ảnh mô tả lỗi</span>
                                <span class="rp-label__meta">URL ẢNH (TÙY CHỌN)</span>
                            </span>
                            <input class="rp-input" type="url" id="imageUrl" name="imageUrl"
                                   placeholder="https://example.com/images/hinh_anh_loi.jpg">
                            <div class="rp-label__meta" style="margin-top:6px;">
                                Hệ thống hiện lưu đường dẫn ảnh, chưa hỗ trợ tải tệp trực tiếp.
                            </div>
                        </div>
                    </div>

                    <!-- SECTION 03 -->
                    <div class="rp-section">
                        <div class="rp-section__head">
                            <div class="rp-section__title">
                                <span class="rp-section__num">03</span>
                                <h2>Thông tin người gửi &amp; Liên hệ</h2>
                            </div>
                            <span class="rp-section__hint">BẢO MẬT THÔNG TIN 100%</span>
                        </div>

                        <div class="rp-grid-2">
                            <div class="rp-field">
                                <label class="rp-label" for="customerName">
                                    <span>Họ và tên <span class="rp-req">*</span></span>
                                </label>
                                <input class="rp-input" type="text" id="customerName" name="customerName"
                                       placeholder="Ví dụ: Trần Văn An" required>
                            </div>

                            <div class="rp-field">
                                <label class="rp-label" for="customerPhone">
                                    <span>Số điện thoại liên hệ <span class="rp-req">*</span></span>
                                </label>
                                <input class="rp-input" type="tel" id="customerPhone" name="customerPhone"
                                       placeholder="Ví dụ: 0918 xxx xxx" required pattern="[0-9]{9,11}"
                                       title="9-11 chữ số">
                            </div>

                            <div class="rp-field rp-field--full">
                                <label class="rp-label" for="customerEmail">
                                    <span>Email nhận báo cáo kỹ thuật</span>
                                    <span class="rp-label__meta">TÙY CHỌN</span>
                                </label>
                                <input class="rp-input" type="email" id="customerEmail" name="customerEmail"
                                       placeholder="email@example.com">
                            </div>
                        </div>
                    </div>

                    <!-- SECTION 04 -->
                    <div class="rp-section rp-section--commit">
                        <div class="rp-section__head">
                            <div class="rp-section__title">
                                <span class="rp-section__num">04</span>
                                <h2>Xác nhận cam kết dịch vụ</h2>
                            </div>
                            <span class="rp-section__hint">BƯỚC HOÀN TẤT</span>
                        </div>

                        <label class="rp-check">
                            <input type="checkbox" id="confirmAccurate" required>
                            <span>
                                <strong>Tôi xác nhận thông tin cung cấp là chính xác</strong> và đồng ý để kỹ thuật
                                viên mở máy kiểm tra đối chứng trước sự chứng kiến của tôi.
                            </span>
                        </label>

                        <div class="rp-callout">
                            <span class="rp-callout__dot"></span>
                            <p>
                                <strong>Nguyên tắc chi phí:</strong> Không cam kết giá sửa chữa cố định trước khi
                                kiểm tra thực tế phần cứng tại trạm kỹ thuật. Chi phí hiển thị bên cạnh là khoảng
                                dự toán từ bảng giá chuẩn, có thể thay đổi sau khi kiểm tra trực tiếp.
                            </p>
                        </div>

                        <div class="rp-actions">
                            <a class="rp-btn rp-btn--ghost" href="${pageContext.request.contextPath}/repair">Làm mới biểu mẫu</a>
                            <button class="rp-btn rp-btn--primary" type="submit">
                                Gửi yêu cầu sửa chữa
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                     stroke-width="2.5" stroke-linecap="square">
                                    <path d="M14 5l7 7-7 7M21 12H3"/>
                                </svg>
                            </button>
                        </div>
                    </div>
                </form>

                <!-- ---------- SIDEBAR ---------- -->
                <aside class="rp-aside">

                    <!-- Dự toán -->
                    <div class="rp-panel">
                        <div class="rp-panel__head">
                            <span class="rp-panel__title">Dự toán chi phí</span>
                            <span class="rp-panel__status">
                                <span class="rp-dot"></span> TRỰC TUYẾN
                            </span>
                        </div>

                        <div class="rp-estimate" id="estimateCard">
                            <div class="rp-estimate__range" id="estRange">--</div>
                            <dl class="rp-estimate__rows">
                                <div class="rp-estimate__row">
                                    <dt>Linh kiện</dt>
                                    <dd id="estParts">--</dd>
                                </div>
                                <div class="rp-estimate__row">
                                    <dt>Tiền công</dt>
                                    <dd id="estLabor">--</dd>
                                </div>
                                <div class="rp-estimate__row">
                                    <dt>Thời gian xử lý</dt>
                                    <dd id="estTime">--</dd>
                                </div>
                            </dl>
                            <div class="rp-estimate__note" id="estNote">
                                Chi phí thực tế có thể thay đổi sau khi kỹ thuật viên kiểm tra trực tiếp.
                            </div>
                        </div>

                        <p class="rp-estimate__placeholder" id="estimatePlaceholder">
                            Chọn dòng máy và loại lỗi để xem khoảng chi phí dự kiến từ bảng giá chuẩn.
                        </p>
                    </div>

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
                                    <p>Khách hàng trực tiếp ký tên lên Mainboard, Pin, Ổ cứng SSD, Màn hình và RAM trước khi rời bàn kỹ thuật.</p>
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
                                    <p>Gửi bảng phân tích chi phí linh kiện thay thế so với sửa chữa mạch. Chỉ thực hiện khi khách hàng xác nhận giá.</p>
                                </div>
                            </div>
                            <div class="rp-step">
                                <span class="rp-step__num rp-step__num--last">4</span>
                                <div class="rp-step__body">
                                    <h3>Bàn giao &amp; Kích hoạt bảo hành</h3>
                                    <p>Khách kiểm tra lại chữ ký niêm phong. Cấp mã bảo hành điện tử trên hệ thống.</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Hỗ trợ -->
                    <div class="rp-panel">
                        <div class="rp-panel__head">
                            <span class="rp-panel__title">Trạm hỗ trợ kỹ thuật</span>
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
                                    <div class="rp-support__sub">Bấm phím 1 để gặp kỹ sư trưởng ca</div>
                                </div>
                            </div>
                            <div class="rp-support__item">
                                <span class="rp-support__icon">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="square">
                                        <circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>
                                    </svg>
                                </span>
                                <div>
                                    <div class="rp-support__label">Giờ làm việc tiếp nhận</div>
                                    <div class="rp-support__value">08:00 – 21:00 (Thứ 2 – Chủ Nhật)</div>
                                    <div class="rp-support__sub">Mở cửa xuyên suốt cả giờ trưa</div>
                                </div>
                            </div>
                        </div>
                    </div>

                </aside>
            </div>
        </div>
    </main>

    <%@ include file="/WEB-INF/views/repair/_footer.jspf" %>
</div>

<%-- Thông số thiết bị đọc từ data-* của <option>, tránh nhúng dữ liệu vào JS --%>
<script>
    function formatCurrency(num) {
        if (num === null || num === undefined) return '--';
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
    }

    function renderDeviceSpec() {
        var select = document.getElementById('deviceId');
        var box = document.getElementById('deviceSpec');
        var opt = select.options[select.selectedIndex];

        if (!opt || !opt.value) {
            box.classList.remove('is-visible');
            box.textContent = '';
            return;
        }

        var parts = [];
        if (opt.dataset.cpu) parts.push('CPU: ' + opt.dataset.cpu);
        if (opt.dataset.ram) parts.push('RAM: ' + opt.dataset.ram);
        if (opt.dataset.storage) parts.push('LƯU TRỮ: ' + opt.dataset.storage);
        if (opt.dataset.year) parts.push('NĂM SX: ' + opt.dataset.year);

        if (parts.length === 0) {
            box.classList.remove('is-visible');
            box.textContent = '';
            return;
        }
        box.textContent = parts.join('  //  ');
        box.classList.add('is-visible');
    }

    function calculateEstimate() {
        renderDeviceSpec();

        var deviceId = document.getElementById('deviceId').value;
        var problemId = document.getElementById('problemId').value;
        var card = document.getElementById('estimateCard');
        var placeholder = document.getElementById('estimatePlaceholder');

        if (!deviceId || !problemId) {
            card.classList.remove('is-visible');
            placeholder.style.display = 'block';
            return;
        }

        var url = '${pageContext.request.contextPath}/repair/api/estimate?deviceId='
            + encodeURIComponent(deviceId) + '&problemId=' + encodeURIComponent(problemId);

        fetch(url, { headers: { 'Accept': 'application/json' } })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                var range = document.getElementById('estRange');
                if (data.available) {
                    range.classList.remove('rp-estimate__range--empty');
                    range.textContent = formatCurrency(data.minCost) + ' – ' + formatCurrency(data.maxCost);
                    document.getElementById('estParts').textContent = formatCurrency(data.partsCost);
                    document.getElementById('estLabor').textContent = formatCurrency(data.laborCost);
                    document.getElementById('estTime').textContent =
                        data.estimatedMinDays + ' – ' + data.estimatedMaxDays + ' ngày';
                    document.getElementById('estNote').textContent = data.message;
                } else {
                    range.classList.add('rp-estimate__range--empty');
                    range.textContent = 'Chưa có bảng giá';
                    document.getElementById('estParts').textContent = '--';
                    document.getElementById('estLabor').textContent = '--';
                    document.getElementById('estTime').textContent = '--';
                    document.getElementById('estNote').textContent = data.message;
                }
                card.classList.add('is-visible');
                placeholder.style.display = 'none';
            })
            .catch(function (err) {
                console.error('Lỗi tính giá dự toán:', err);
                card.classList.remove('is-visible');
                placeholder.style.display = 'block';
            });
    }
</script>

</body>
</html>
