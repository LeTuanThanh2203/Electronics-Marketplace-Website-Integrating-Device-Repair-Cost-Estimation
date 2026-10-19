<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Thông tin cá nhân" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>
<c:set var="u" value="${sessionScope.currentUser}"/>
<c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
<c:if test="${not empty success}"><div class="alert success"><c:out value="${success}"/></div></c:if>

<div class="grid">
    <div class="card profile-side">
        <c:choose>
            <c:when test="${not empty u.avatar}"><img class="avatar" src="<c:out value='${u.avatar}'/>" alt="avatar"></c:when>
            <c:otherwise><div class="avatar avatar-text"><c:out value="${u.initial}"/></div></c:otherwise>
        </c:choose>
        <h2><c:out value="${u.name}"/></h2>
        <span class="badge ${u.admin ? 'badge-admin' : 'badge-user'}">${u.role}</span>
        <span class="badge badge-${u.active ? 'ok' : 'bad'}">${u.status}</span>
        <ul class="info">
            <li>📧 <c:out value="${u.email}"/></li>
            <li>📞 <c:out value="${u.phone}"/></li>
            <li>📍 <c:out value="${empty u.location ? 'Chưa cập nhật' : u.location}"/></li>
            <li>🛒 Đã bán: <b>${u.completedSalesCount}</b> đơn</li>
            <li>🗓 Tham gia: <fmt:formatDate value="${u.createdAt}" pattern="dd/MM/yyyy"/></li>
        </ul>
    </div>

    <div>
        <div class="card">
            <h2>Cập nhật thông tin</h2>
            <form method="post" action="${pageContext.request.contextPath}/profile">
                <input type="hidden" name="action" value="info">
                <label>Họ và tên</label>
                <input type="text" name="name" value="<c:out value='${u.name}'/>" required>
                <label>Số điện thoại</label>
                <input type="tel" name="phone" value="<c:out value='${u.phone}'/>" required pattern="[0-9]{9,11}">
                <label>Địa chỉ / Khu vực</label>
                <input type="text" name="location" value="<c:out value='${u.location}'/>">
                <label>Ảnh đại diện (URL)</label>
                <input type="url" name="avatar" value="<c:out value='${u.avatar}'/>" placeholder="https://...">
                <button class="btn" type="submit">Lưu thay đổi</button>
            </form>
        </div>
        <div class="card">
            <h2>Đổi mật khẩu</h2>
            <form method="post" action="${pageContext.request.contextPath}/profile">
                <input type="hidden" name="action" value="password">
                <label>Mật khẩu hiện tại</label>
                <input type="password" name="oldPassword" required>
                <label>Mật khẩu mới</label>
                <input type="password" name="newPassword" required minlength="6">
                <label>Nhập lại mật khẩu mới</label>
                <input type="password" name="confirm" required minlength="6">
                <button class="btn" type="submit">Đổi mật khẩu</button>
            </form>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/layout/footer.jspf" %>