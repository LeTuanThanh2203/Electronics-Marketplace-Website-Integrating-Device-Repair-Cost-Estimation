<%--
    Trang 500. KHONG hien stacktrace / exception message ra ngoai (Rule 5):
    chi mot cau xin loi. Chi tiet da nam trong log cua Tomcat.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Lỗi hệ thống — Sửa hay bán" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="${ctx}/css/order.css">
</head>
<body>
<main class="container" style="padding: var(--space-2xl) 0">
    <div class="empty">
        <p class="eyebrow">Lỗi 500</p>
        <h1>Có lỗi xảy ra</h1>
        <p>Hệ thống không xử lý được yêu cầu này. Vui lòng thử lại, nếu vẫn lỗi hãy báo bộ phận kỹ thuật.</p>
        <a class="btn btn-primary" href="${ctx}/cart">Về giỏ hàng</a>
    </div>
</main>
</body>
</html>
