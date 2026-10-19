<%--
    Trang 500. KHONG hien stacktrace / exception message ra ngoai (Rule 5):
    chi mot cau xin loi. Chi tiet da nam trong log cua Tomcat.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lỗi hệ thống — Sửa hay bán</title>
    <link rel="stylesheet" href="${ctx}/css/repair.css">
</head>
<body class="rp-body">
<main style="max-width:640px;margin:0 auto;padding:64px 24px;text-align:center;">
    <p style="font-size:12px;letter-spacing:.12em;text-transform:uppercase;color:#6b7280;">Lỗi 500</p>
    <h1 style="margin:8px 0 12px;">Có lỗi xảy ra</h1>
    <p style="color:#4b5563;">Hệ thống không xử lý được yêu cầu này. Vui lòng thử lại, nếu vẫn lỗi hãy báo bộ phận kỹ thuật.</p>
    <p style="margin-top:24px;">
        <a class="rp-btn rp-btn--primary" href="${ctx}/repair">Về trang sửa chữa</a>
    </p>
</main>
</body>
</html>
