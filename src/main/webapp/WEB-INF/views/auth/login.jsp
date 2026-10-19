<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="pageTitle" value="Đăng nhập — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="auth" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

<div class="auth-page-wrapper">
    <div class="card auth-card">
        <div class="auth-header">
            <span class="auth-badge">
                <span class="material-symbols-outlined text-[14px]">lock</span>
                <span>HỆ THỐNG XÁC THỰC</span>
            </span>
            <h1>Đăng nhập</h1>
            <p class="auth-sub">Truy cập tài khoản để quản lý đơn hàng và đăng tin bán máy</p>
        </div>

        <c:if test="${param.registered == '1'}">
            <div class="alert success">
                <span class="material-symbols-outlined text-base">check_circle</span>
                <span>Đăng ký tài khoản thành công! Hãy đăng nhập.</span>
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="alert error">
                <span class="material-symbols-outlined text-base">error</span>
                <span><c:out value="${error}"/></span>
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/login">
            <label>Email đăng nhập</label>
            <input type="email" name="email" value="<c:out value='${email}'/>" placeholder="name@domain.com" required autofocus>

            <label>Mật khẩu</label>
            <input type="password" name="password" placeholder="••••••••" required>

            <button class="btn btn-block" type="submit">ĐĂNG NHẬP NGAY →</button>
        </form>

        <p class="muted">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></p>
    </div>
</div>

<%@ include file="/WEB-INF/layout/footer.jspf" %>