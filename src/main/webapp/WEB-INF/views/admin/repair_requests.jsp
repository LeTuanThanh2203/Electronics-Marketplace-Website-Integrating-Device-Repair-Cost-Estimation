<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="rpAdminPage" value="requests" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đơn sửa chữa | Sửa hay bán Admin</title>
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
            <h1>Đơn sửa chữa</h1>
            <p>Toàn bộ phiếu yêu cầu sửa chữa đã tiếp nhận.</p>
        </div>
        <div class="rp-adminhead__meta">
            Tổng số: <c:out value="${repairRequests.size()}" /> phiếu
        </div>
    </div>

    <div class="rp-adminbody">
        <div class="rp-table-wrap">
            <table class="rp-table">
                <thead>
                    <tr>
                        <th>Mã</th>
                        <th>Khách hàng</th>
                        <th>Liên hệ</th>
                        <th>Dòng máy</th>
                        <th>Lỗi</th>
                        <th class="rp-table__num">Dự toán</th>
                        <th>Trạng thái</th>
                        <th>Tạo lúc</th>
                        <th>Cập nhật</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${repairRequests}" var="req">
                        <tr>
                            <td class="rp-table__mono" style="font-weight:600;">#<c:out value="${req.id}" /></td>
                            <td><c:out value="${req.customerName}" /></td>
                            <td class="rp-table__mono rp-table__muted"><c:out value="${req.customerPhone}" /></td>
                            <td><c:out value="${req.device.displayName}" /></td>
                            <td>
                                <div><c:out value="${req.problem.name}" /></div>
                                <c:if test="${not empty req.imageUrl}">
                                    <div style="margin-top:4px;">
                                        <a href="${req.imageUrl}" target="_blank" rel="noopener noreferrer"
                                           style="display:inline-flex; align-items:center; gap:4px; font-size:11px; font-family:var(--rp-font-mono); color:var(--rp-orange); text-decoration:underline;">
                                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                                                <circle cx="8.5" cy="8.5" r="1.5"/>
                                                <polyline points="21 15 16 10 5 21"/>
                                            </svg>
                                            Xem ảnh lỗi
                                        </a>
                                    </div>
                                </c:if>
                            </td>
                            <td class="rp-table__num">
                                <c:choose>
                                    <c:when test="${not empty req.repairEstimate}">
                                        <span style="font-weight:600;">
                                            <fmt:formatNumber value="${req.repairEstimate.minCost}" pattern="#,###" />
                                            –
                                            <fmt:formatNumber value="${req.repairEstimate.maxCost}" pattern="#,###" /> ₫
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="rp-table__muted">Chưa có bảng giá</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${req.status == 'ESTIMATED'}">
                                        <span class="rp-badge rp-badge--estimated">Đã báo giá</span>
                                    </c:when>
                                    <c:when test="${req.status == 'WAITING_PRICING'}">
                                        <span class="rp-badge rp-badge--waiting">Chờ báo giá</span>
                                    </c:when>
                                    <c:when test="${req.status == 'IN_PROGRESS'}">
                                        <span class="rp-badge rp-badge--progress">Đang sửa</span>
                                    </c:when>
                                    <c:when test="${req.status == 'COMPLETED'}">
                                        <span class="rp-badge rp-badge--done">Hoàn thành</span>
                                    </c:when>
                                    <c:when test="${req.status == 'CANCELLED'}">
                                        <span class="rp-badge rp-badge--cancelled">Đã hủy</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="rp-badge rp-badge--pending"><c:out value="${req.status}" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="rp-table__muted">
                                <fmt:formatDate value="${req.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${req.status == 'WAITING_PRICING'}">
                                        <span class="rp-table__muted">Cần nhập bảng giá</span>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${pageContext.request.contextPath}/admin/repairs" method="POST"
                                              style="display:flex; gap:6px; align-items:center;">
                                            <input type="hidden" name="action" value="updateRequestStatus">
                                            <input type="hidden" name="requestId" value="${req.id}">
                                            <div class="rp-select-wrap">
                                                <select class="rp-select rp-select--sm" name="status" style="min-width:130px;">
                                                    <c:forEach items="${selectableStatuses}" var="st">
                                                        <option value="${st.name()}" ${req.status == st.name() ? 'selected' : ''}>
                                                            <c:out value="${st.label}" />
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <button class="rp-btn rp-btn--primary rp-btn--sm" type="submit">Lưu</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty repairRequests}">
                        <tr>
                            <td colspan="9" class="rp-table__empty">Chưa có yêu cầu sửa chữa nào.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

</body>
</html>
