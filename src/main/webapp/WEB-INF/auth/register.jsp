<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="pageTitle" value="Đăng ký tài khoản — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="auth" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

<div class="auth-page-wrapper">
    <div class="card auth-card">
        <div class="auth-header">
            <span class="auth-badge">
                <span class="material-symbols-outlined text-[14px]">person_add</span>
                <span>ĐĂNG KÝ THÀNH VIÊN</span>
            </span>
            <h1>Tạo tài khoản mới</h1>
            <p class="auth-sub">Tham gia nền tảng giao dịch thiết bị & kiểm định phần cứng tiêu chuẩn</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert error">
                <span class="material-symbols-outlined text-base">error</span>
                <span><c:out value="${error}"/></span>
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/register">
            <label>Họ và tên</label>
            <input type="text" name="name" value="<c:out value='${name}'/>" placeholder="Nguyễn Văn A" required maxlength="100">

            <label>Địa chỉ Email</label>
            <input type="email" name="email" value="<c:out value='${email}'/>" placeholder="name@domain.com" required>

            <label>Số điện thoại liên hệ</label>
            <input type="tel" name="phone" value="<c:out value='${phone}'/>" placeholder="0901234567" required pattern="[0-9]{9,11}" title="9-11 chữ số">

            <label>Mật khẩu (Tối thiểu 6 ký tự)</label>
            <input type="password" name="password" placeholder="••••••••" required minlength="6">

            <label>Xác nhận lại mật khẩu</label>
            <input type="password" name="confirm" placeholder="••••••••" required minlength="6">

            <button class="btn btn-block" type="submit">TẠO TÀI KHOẢN NGAY →</button>
        </form>

        <p class="muted">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập ngay</a></p>
    </div>
</div>

<%@ include file="/WEB-INF/layout/footer.jspf" %>
