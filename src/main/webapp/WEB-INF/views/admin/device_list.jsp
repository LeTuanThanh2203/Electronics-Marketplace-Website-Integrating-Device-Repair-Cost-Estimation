<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="rpAdminPage" value="devices" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dòng máy | Sửa hay bán Admin</title>
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
            <h1>Dòng máy</h1>
            <p>Danh mục thiết bị dùng để tra bảng giá sửa chữa.</p>
        </div>
        <div class="rp-adminhead__meta">
            Tổng số: <c:out value="${devices.size()}" /> dòng máy
        </div>
    </div>

    <div class="rp-adminbody">

        <!-- Thêm dòng máy -->
        <div class="rp-adminform">
            <div class="rp-adminform__title">
                <span class="rp-section__num">+</span> Thêm dòng thiết bị mới
            </div>
            <form action="${pageContext.request.contextPath}/admin/repair/devices" method="POST">
                <input type="hidden" name="action" value="addDevice">
                <div class="rp-adminform__grid">
                    <div class="rp-field">
                        <label class="rp-label" for="brand"><span>Hãng <span class="rp-req">*</span></span></label>
                        <input class="rp-input rp-input--sm" type="text" id="brand" name="brand"
                               placeholder="Apple, Dell, Asus..." required>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="series"><span>Series</span></label>
                        <input class="rp-input rp-input--sm" type="text" id="series" name="series"
                               placeholder="MacBook Pro, XPS, ROG...">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="model"><span>Model <span class="rp-req">*</span></span></label>
                        <input class="rp-input rp-input--sm" type="text" id="model" name="model"
                               placeholder="M1 Pro 14 inch, 15 9520..." required>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="releaseYear"><span>Năm SX</span></label>
                        <input class="rp-input rp-input--sm" type="number" id="releaseYear" name="releaseYear"
                               min="1990" max="2100" placeholder="2023">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="cpu"><span>CPU</span></label>
                        <input class="rp-input rp-input--sm" type="text" id="cpu" name="cpu" placeholder="Apple M1 Pro">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="ram"><span>RAM</span></label>
                        <input class="rp-input rp-input--sm" type="text" id="ram" name="ram" placeholder="16GB">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="storage"><span>Lưu trữ</span></label>
                        <input class="rp-input rp-input--sm" type="text" id="storage" name="storage" placeholder="512GB SSD">
                    </div>
                </div>
                <div class="rp-adminform__actions">
                    <button class="rp-btn rp-btn--primary" type="submit">Thêm thiết bị</button>
                </div>
            </form>
        </div>

        <!-- Danh sách -->
        <div class="rp-table-wrap">
            <table class="rp-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Hãng</th>
                        <th>Dòng / Model</th>
                        <th>Năm SX</th>
                        <th>Cấu hình</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${devices}" var="d">
                        <tr>
                            <td class="rp-table__mono">#<c:out value="${d.id}" /></td>
                            <td style="font-weight:600;"><c:out value="${d.brand}" /></td>
                            <td><c:out value="${d.displayName}" /></td>
                            <td class="rp-table__mono"><c:out value="${d.releaseYear}" /></td>
                            <td class="rp-table__muted">
                                <c:out value="${d.cpu}" /> · <c:out value="${d.ram}" /> · <c:out value="${d.storage}" />
                            </td>
                            <td>
                                <form action="${pageContext.request.contextPath}/admin/repair/devices" method="POST"
                                      style="display:inline;"
                                      onsubmit="return confirm('Bạn có chắc muốn xóa thiết bị này?');">
                                    <input type="hidden" name="action" value="deleteDevice">
                                    <input type="hidden" name="id" value="${d.id}">
                                    <button class="rp-btn rp-btn--danger rp-btn--sm" type="submit">Xóa</button>
                                </form>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="6" style="padding-top:0;">
                                <details class="rp-details">
                                    <summary>Sửa dòng máy #<c:out value="${d.id}" /></summary>
                                    <div class="rp-details__body">
                                        <form action="${pageContext.request.contextPath}/admin/repair/devices" method="POST">
                                            <input type="hidden" name="action" value="updateDevice">
                                            <input type="hidden" name="id" value="${d.id}">
                                            <div class="rp-adminform__grid">
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>Hãng <span class="rp-req">*</span></span></label>
                                                    <input class="rp-input rp-input--sm" type="text" name="brand"
                                                           value="<c:out value='${d.brand}' />" required>
                                                </div>
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>Series</span></label>
                                                    <input class="rp-input rp-input--sm" type="text" name="series"
                                                           value="<c:out value='${d.series}' />">
                                                </div>
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>Model <span class="rp-req">*</span></span></label>
                                                    <input class="rp-input rp-input--sm" type="text" name="model"
                                                           value="<c:out value='${d.model}' />" required>
                                                </div>
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>Năm SX</span></label>
                                                    <input class="rp-input rp-input--sm" type="number" name="releaseYear"
                                                           min="1990" max="2100" value="${d.releaseYear}">
                                                </div>
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>CPU</span></label>
                                                    <input class="rp-input rp-input--sm" type="text" name="cpu"
                                                           value="<c:out value='${d.cpu}' />">
                                                </div>
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>RAM</span></label>
                                                    <input class="rp-input rp-input--sm" type="text" name="ram"
                                                           value="<c:out value='${d.ram}' />">
                                                </div>
                                                <div class="rp-field">
                                                    <label class="rp-label"><span>Lưu trữ</span></label>
                                                    <input class="rp-input rp-input--sm" type="text" name="storage"
                                                           value="<c:out value='${d.storage}' />">
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
                    <c:if test="${empty devices}">
                        <tr>
                            <td colspan="6" class="rp-table__empty">
                                Chưa có dòng máy nào. Hãy thêm ở biểu mẫu phía trên.
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
