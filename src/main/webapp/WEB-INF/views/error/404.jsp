<%-- Trang 404. Khong lo thong tin he thong, chi mot loi khuyen cu the. --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Không tìm thấy trang — Sửa hay bán" />
<c:set var="activeNav" value="" />
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
        <p class="eyebrow">Lỗi 404</p>
        <h1>Không tìm thấy trang</h1>
        <p>Đường dẫn bạn mở không tồn tại hoặc đã bị gỡ.</p>
        <a class="btn btn-primary" href="${ctx}/cart">Về giỏ hàng</a>
    </div>
</main>
</body>
</html>
