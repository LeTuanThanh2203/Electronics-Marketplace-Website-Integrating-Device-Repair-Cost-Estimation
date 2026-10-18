<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="pageTitle" value="Đăng nhập" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>
<div class="card auth-card">
    <h1>Đăng nhập</h1>
    <c:if test="${param.registered == '1'}"><div class="alert success">Đăng ký thành công! Hãy đăng nhập.</div></c:if>
    <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
    <form method="post" action="${pageContext.request.contextPath}/login">
        <label>Email</label>
        <input type="email" name="email" value="<c:out value='${email}'/>" required autofocus>
        <label>Mật khẩu</label>
        <input type="password" name="password" required>
        <button class="btn btn-block" type="submit">Đăng nhập</button>
    </form>
    <p class="muted">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></p>
</div>
<%@ include file="/WEB-INF/layout/footer.jspf" %>