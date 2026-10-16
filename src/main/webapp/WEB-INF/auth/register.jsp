<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="pageTitle" value="Đăng ký" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>
<div class="card auth-card">
    <h1>Tạo tài khoản</h1>
    <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
    <form method="post" action="${pageContext.request.contextPath}/register">
        <label>Họ và tên</label>
        <input type="text" name="name" value="<c:out value='${name}'/>" required maxlength="100">
        <label>Email</label>
        <input type="email" name="email" value="<c:out value='${email}'/>" required>
        <label>Số điện thoại</label>
        <input type="tel" name="phone" value="<c:out value='${phone}'/>" required pattern="[0-9]{9,11}" title="9-11 chữ số">
        <label>Mật khẩu</label>
        <input type="password" name="password" required minlength="6">
        <label>Nhập lại mật khẩu</label>
        <input type="password" name="confirm" required minlength="6">
        <button class="btn btn-block" type="submit">Đăng ký</button>
    </form>
    <p class="muted">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></p>
</div>
<%@ include file="/WEB-INF/layout/footer.jspf" %>
