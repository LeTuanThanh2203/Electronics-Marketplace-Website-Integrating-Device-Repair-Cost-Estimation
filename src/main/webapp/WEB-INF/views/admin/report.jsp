<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="rpAdminPage" value="reports" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thống kê lỗi hỏng | Sửa hay bán Admin</title>
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
            <h1>Thống kê lỗi hỏng</h1>
            <p>Số lượt gặp của từng loại lỗi, tổng hợp bằng truy vấn GROUP BY &amp; COUNT.</p>
        </div>
        <div class="rp-adminhead__meta">DỮ LIỆU TỰ ĐỘNG</div>
    </div>

    <div class="rp-adminbody">

        <%-- Tổng lượt gặp: dùng <c:set> kiểu số để EL cộng Long chính xác --%>
        <c:set var="totalRequests" value="${0}" />
        <c:forEach items="${problemStats}" var="s">
            <c:set var="totalRequests" value="${totalRequests + s.requestCount}" />
        </c:forEach>

        <div class="rp-stats">
            <div class="rp-stat">
                <div class="rp-stat__label">Loại lỗi theo dõi</div>
                <div class="rp-stat__value"><c:out value="${problemStats.size()}" /></div>
            </div>
            <div class="rp-stat">
                <div class="rp-stat__label">Tổng lượt gặp</div>
                <div class="rp-stat__value rp-stat__value--accent"><c:out value="${totalRequests}" /></div>
            </div>
            <div class="rp-stat">
                <div class="rp-stat__label">Lỗi phổ biến nhất</div>
                <div class="rp-stat__value" style="font-size:16px; line-height:1.4; margin-top:10px;">
                    <c:choose>
                        <c:when test="${not empty problemStats}">
                            <c:out value="${problemStats[0].problemName}" />
                        </c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <div class="rp-table-wrap">
            <table class="rp-table">
                <thead>
                    <tr>
                        <th style="width:60px;">Hạng</th>
                        <th style="width:80px;">Mã</th>
                        <th>Tên loại lỗi</th>
                        <th>Thời gian dự kiến</th>
                        <th class="rp-table__num">Số lượt</th>
                        <th style="min-width:200px;">Tỷ trọng</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty problemStats}">
                            <c:forEach items="${problemStats}" var="stat" varStatus="loop">
                                <tr>
                                    <td>
                                        <c:choose>
                                            <c:when test="${loop.index == 0}">
                                                <span class="rp-rank rp-rank--1">1</span>
                                            </c:when>
                                            <c:when test="${loop.index == 1}">
                                                <span class="rp-rank rp-rank--2">2</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="rp-rank">${loop.index + 1}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="rp-table__mono rp-table__muted">#<c:out value="${stat.problemId}" /></td>
                                    <td style="font-weight:600;"><c:out value="${stat.problemName}" /></td>
                                    <td class="rp-table__mono">
                                        <c:out value="${stat.estimatedMinDays}" /> – <c:out value="${stat.estimatedMaxDays}" /> ngày
                                    </td>
                                    <td class="rp-table__num">
                                        <span style="font-weight:600;"><c:out value="${stat.requestCount}" /></span>
                                    </td>
                                    <td>
                                        <div class="rp-bar">
                                            <div class="rp-bar__track">
                                                <div class="rp-bar__fill"
                                                     style="width:${totalRequests > 0 ? (stat.requestCount * 100 / totalRequests) : 0}%;"></div>
                                            </div>
                                            <span class="rp-bar__value">
                                                <c:choose>
                                                    <c:when test="${totalRequests > 0}">
                                                        <fmt:formatNumber value="${stat.requestCount * 100 / totalRequests}"
                                                                          pattern="0.#" />%
                                                    </c:when>
                                                    <c:otherwise>0%</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="6" class="rp-table__empty">
                                    Chưa có dữ liệu yêu cầu sửa chữa nào trong hệ thống.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

</body>
</html>
