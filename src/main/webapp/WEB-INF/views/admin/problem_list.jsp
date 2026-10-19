<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="rpAdminPage" value="problems" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Loại lỗi | Sửa hay bán Admin</title>
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
            <h1>Loại lỗi</h1>
            <p>Danh mục lỗi hỏng tiếp nhận và thời gian xử lý dự kiến.</p>
        </div>
        <div class="rp-adminhead__meta">
            Tổng số: <c:out value="${problems.size()}" /> loại lỗi
        </div>
    </div>

    <div class="rp-adminbody">

        <!-- Thêm loại lỗi -->
        <div class="rp-adminform">
            <div class="rp-adminform__title">
                <span class="rp-section__num">+</span> Thêm loại lỗi mới
            </div>
            <form action="${pageContext.request.contextPath}/admin/repair/problems" method="POST">
                <input type="hidden" name="action" value="addProblem">
                <div class="rp-adminform__grid">
                    <div class="rp-field">
                        <label class="rp-label" for="name"><span>Tên loại lỗi <span class="rp-req">*</span></span></label>
                        <input class="rp-input rp-input--sm" type="text" id="name" name="name"
                               placeholder="Vỡ màn hình, Chai pin..." required>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="minDays"><span>Ngày tối thiểu</span></label>
                        <input class="rp-input rp-input--sm" type="number" id="minDays" name="estimatedMinDays"
                               value="1" min="0">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="maxDays"><span>Ngày tối đa</span></label>
                        <input class="rp-input rp-input--sm" type="number" id="maxDays" name="estimatedMaxDays"
                               value="3" min="0">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="isActive"><span>Trạng thái</span></label>
                        <div class="rp-select-wrap">
                            <select class="rp-select rp-select--sm" id="isActive" name="isActive">
                                <option value="true">Đang kích hoạt</option>
                                <option value="false">Tạm ngưng</option>
                            </select>
                        </div>
                    </div>
                    <div class="rp-field" style="grid-column:1 / -1;">
                        <label class="rp-label" for="description"><span>Mô tả</span></label>
                        <input class="rp-input rp-input--sm" type="text" id="description" name="description"
                               placeholder="Dấu hiệu nhận biết, phương án khắc phục tiêu chuẩn...">
                    </div>
                </div>
                <div class="rp-adminform__actions">
                    <button class="rp-btn rp-btn--primary" type="submit">Thêm loại lỗi</button>
                </div>
            </form>
        </div>

        <!-- Danh sách -->
        <div class="rp-table-wrap">
            <table class="rp-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên loại lỗi</th>
                        <th>Mô tả</th>
                        <th>Thời gian dự kiến</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${problems}" var="p">
                        <tr>
                            <td class="rp-table__mono">#<c:out value="${p.id}" /></td>
                            <td style="font-weight:600;"><c:out value="${p.name}" /></td>
                            <td class="rp-table__muted"><c:out value="${p.description}" /></td>
                            <td class="rp-table__mono">
                                <c:out value="${p.estimatedMinDays}" /> – <c:out value="${p.estimatedMaxDays}" /> ngày
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.active}">
                                        <span class="rp-badge rp-badge--done">Hoạt động</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="rp-badge rp-badge--cancelled">Tạm ngưng</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="5" style="padding-top:0;">
                                <details class="rp-details">
                                    <summary>Sửa loại lỗi #<c:out value="${p.id}" /></summary>
                                    <div class="rp-details__body">
                                        <form action="${pageContext.request.contextPath}/admin/repair/problems" method="POST">
                                            <input type="hidden" name="action" value="updateProblem">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <div class="rp-adminform__grid">
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>Tên loại lỗi <span class="rp-req">*</span></span></label>
                                                    <input class="rp-input rp-input--sm" type="text" name="name"
                                                           value="<c:out value='${p.name}' />" required>
                                                </div>
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>Ngày tối thiểu</span></label>
                                                    <input class="rp-input rp-input--sm" type="number" name="estimatedMinDays"
                                                           min="0" value="${p.estimatedMinDays}">
                                                </div>
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>Ngày tối đa</span></label>
                                                    <input class="rp-input rp-input--sm" type="number" name="estimatedMaxDays"
                                                           min="0" value="${p.estimatedMaxDays}">
                                                </div>
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>Trạng thái</span></label>
                                                    <div class="rp-select-wrap">
                                                        <select class="rp-select rp-select--sm" name="isActive">
                                                            <option value="true" ${p.active ? 'selected' : ''}>Đang kích hoạt</option>
                                                            <option value="false" ${p.active ? '' : 'selected'}>Tạm ngưng</option>
                                                        </select>
                                                    </div>
                                                </div>
                                                <div class="rp-field" style="grid-column:1 / -1;">
                                                    <label class="rp-label"><span>Mô tả</span></label>
                                                    <input class="rp-input rp-input--sm" type="text" name="description"
                                                           value="<c:out value='${p.description}' />">
                                                </div>
                                            </div>
                                            <div class="rp-adminform__actions">
                                                <button class="rp-btn rp-btn--primary rp-btn--sm" type="submit">Lưu thay đổi</button>
                                            </div>
                                        </form>
                                    </div>
                                </details>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty problems}">
                        <tr>
                            <td colspan="5" class="rp-table__empty">Chưa có loại lỗi nào.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

</body>
</html>
