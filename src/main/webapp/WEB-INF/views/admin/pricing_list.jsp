<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="rpAdminPage" value="pricing" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng giá | Sửa hay bán Admin</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&family=Space+Grotesk:wght@500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/repair.css">
</head>
<body class="rp-body">

<div class="rp-shell">
    <%@ include file="/WEB-INF/views/admin/_header.jspf" %>

    <div class="rp-adminhead">
        <div>
            <h1>Bảng giá chuẩn</h1>
            <p>Chi phí linh kiện và nhân công dùng để tính dự toán sửa chữa.</p>
        </div>
        <div class="rp-adminhead__meta">
            Tổng số: <c:out value="${pricings.size()}" /> bản giá
        </div>
    </div>

    <div class="rp-adminbody">

        <!-- Thiết lập bảng giá -->
        <div class="rp-adminform">
            <div class="rp-adminform__title">
                <span class="rp-section__num">+</span> Thiết lập / cập nhật bảng giá
            </div>
            <p style="margin:0 0 16px; font-size:13px; line-height:1.6; color:var(--rp-gray);">
                Lưu cùng một cặp (dòng máy, loại lỗi) sẽ tạo <strong style="color:var(--rp-black);">phiên bản mới</strong>.
                Các phiếu đang "Chờ báo giá" sẽ được tự động cập nhật báo giá.
            </p>
            <form action="${pageContext.request.contextPath}/admin/repair/pricing" method="POST">
                <input type="hidden" name="action" value="savePricing">
                <div class="rp-adminform__grid">
                    <div class="rp-field">
                        <label class="rp-label" for="deviceId"><span>Dòng thiết bị <span class="rp-req">*</span></span></label>
                        <div class="rp-select-wrap">
                            <select class="rp-select rp-select--sm" id="deviceId" name="deviceId" required>
                                <c:forEach items="${devices}" var="d">
                                    <option value="${d.id}"><c:out value="${d.displayName}" /></option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="problemId"><span>Loại lỗi <span class="rp-req">*</span></span></label>
                        <div class="rp-select-wrap">
                            <select class="rp-select rp-select--sm" id="problemId" name="problemId" required>
                                <c:forEach items="${problems}" var="p">
                                    <option value="${p.id}"><c:out value="${p.name}" /></option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="partsCost"><span>Tiền linh kiện (₫) <span class="rp-req">*</span></span></label>
                        <input class="rp-input rp-input--sm" type="number" id="partsCost" name="partsCost"
                               min="0" step="1000" placeholder="2500000" required>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="laborCost"><span>Tiền công (₫) <span class="rp-req">*</span></span></label>
                        <input class="rp-input rp-input--sm" type="number" id="laborCost" name="laborCost"
                               min="0" step="1000" placeholder="300000" required>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="estimationMargin">
                            <span>Biên độ <span class="rp-req">*</span></span>
                            <span class="rp-label__meta">0.10 = 10%</span>
                        </label>
                        <input class="rp-input rp-input--sm" type="number" id="estimationMargin" name="estimationMargin"
                               min="0" max="0.99" step="0.01" value="0.10" required>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="isActive"><span>Trạng thái</span></label>
                        <div class="rp-select-wrap">
                            <select class="rp-select rp-select--sm" id="isActive" name="isActive">
                                <option value="true">Áp dụng</option>
                                <option value="false">Tạm dừng</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="rp-adminform__actions">
                    <button class="rp-btn rp-btn--primary" type="submit">Lưu bảng giá</button>
                </div>
            </form>
        </div>

        <!-- Danh sách bảng giá -->
        <div class="rp-table-wrap">
            <table class="rp-table">
                <thead>
                    <tr>
                        <th>Thiết bị</th>
                        <th>Loại lỗi</th>
                        <th class="rp-table__num">Linh kiện</th>
                        <th class="rp-table__num">Tiền công</th>
                        <th class="rp-table__num">Khoảng dự toán</th>
                        <th>Phiên bản</th>
                        <th>Trạng thái</th>
                        <th>Cập nhật</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${pricings}" var="pr">
                        <tr>
                            <td style="font-weight:600;"><c:out value="${pr.device.displayName}" /></td>
                            <td><c:out value="${pr.problem.name}" /></td>
                            <td class="rp-table__num"><fmt:formatNumber value="${pr.partsCost}" pattern="#,###" /> ₫</td>
                            <td class="rp-table__num"><fmt:formatNumber value="${pr.laborCost}" pattern="#,###" /> ₫</td>
                            <td class="rp-table__num" style="font-weight:600; color:var(--rp-orange);">
                                <fmt:formatNumber value="${pr.calculateMinCost()}" pattern="#,###" /> –
                                <fmt:formatNumber value="${pr.calculateMaxCost()}" pattern="#,###" /> ₫
                            </td>
                            <td class="rp-table__mono">v<c:out value="${pr.version}" /></td>
                            <td>
                                <c:choose>
                                    <c:when test="${pr.active}">
                                        <span class="rp-badge rp-badge--done">Áp dụng</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="rp-badge rp-badge--cancelled">Tạm dừng</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="rp-table__muted">
                                <fmt:formatDate value="${pr.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pricings}">
                        <tr>
                            <td colspan="8" class="rp-table__empty">
                                Chưa có bảng giá nào. Hãy thiết lập ở biểu mẫu phía trên.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

</body>
</html>
