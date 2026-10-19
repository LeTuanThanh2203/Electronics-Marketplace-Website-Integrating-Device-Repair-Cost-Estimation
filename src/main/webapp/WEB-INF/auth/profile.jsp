<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Hồ sơ cá nhân — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="auth" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

<div class="profile-page-wrapper">
    <c:set var="u" value="${sessionScope.currentUser}"/>

    <c:if test="${not empty error}">
        <div class="alert error">
            <span class="material-symbols-outlined text-base">error</span>
            <span><c:out value="${error}"/></span>
        </div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert success">
            <span class="material-symbols-outlined text-base">check_circle</span>
            <span><c:out value="${success}"/></span>
        </div>
    </c:if>

    <div class="grid">
        <!-- Cột trái: Thông tin tổng quan tài khoản -->
        <div class="card profile-side">
            <c:choose>
                <c:when test="${not empty u.avatar}">
                    <img class="avatar" src="<c:url value='${u.avatar}'/>" alt="avatar">
                </c:when>
                <c:otherwise>
                    <div class="avatar avatar-text"><c:out value="${u.initial}"/></div>
                </c:otherwise>
            </c:choose>

            <h2><c:out value="${u.name}"/></h2>
            
            <div style="margin-bottom: 0.75rem;">
                <span class="badge ${u.admin ? 'badge-admin' : 'badge-user'}">${u.role}</span>
                <span class="badge badge-${u.active ? 'ok' : 'bad'}">${u.status}</span>
            </div>

            <ul class="info">
                <li>
                    <span class="material-symbols-outlined text-base text-zinc-400">mail</span>
                    <span><c:out value="${u.email}"/></span>
                </li>
                <li>
                    <span class="material-symbols-outlined text-base text-zinc-400">call</span>
                    <span><c:out value="${empty u.phone ? 'Chưa cập nhật' : u.phone}"/></span>
                </li>
                <li>
                    <span class="material-symbols-outlined text-base text-zinc-400">location_on</span>
                    <span><c:out value="${empty u.location ? 'Chưa cập nhật' : u.location}"/></span>
                </li>
                <li>
                    <span class="material-symbols-outlined text-base text-zinc-400">shopping_bag</span>
                    <span>Đã bán: <strong style="color: var(--brand-black);">${u.completedSalesCount}</strong> đơn</span>
                </li>
                <li>
                    <span class="material-symbols-outlined text-base text-zinc-400">calendar_today</span>
                    <span>Tham gia: <fmt:formatDate value="${u.createdAt}" pattern="dd/MM/yyyy"/></span>
                </li>
            </ul>

            <div style="margin-top: 1.5rem; pt-4; border-top: 1px solid var(--brand-border);">
                <a href="${pageContext.request.contextPath}/order" class="btn" style="width: 100%; margin-bottom: 0.5rem; font-size: 11px;">
                    <span class="material-symbols-outlined text-sm" style="margin-right: 4px;">receipt_long</span>
                    TRA CỨU ĐƠN HÀNG
                </a>
                <c:if test="${u.admin}">
                    <a href="${pageContext.request.contextPath}/admin" class="btn" style="width: 100%; background: #92400E; border-color: #92400E; color: #FFF; font-size: 11px;">
                        <span class="material-symbols-outlined text-sm" style="margin-right: 4px;">shield_person</span>
                        VÀO BẢNG QUẢN TRỊ
                    </a>
                </c:if>
            </div>
        </div>

        <!-- Cột phải: Form cập nhật thông tin & đổi mật khẩu -->
        <div>
            <div class="card">
                <h2>
                    <span class="material-symbols-outlined text-lg text-[#FF4A1C]">badge</span>
                    <span>Cập nhật thông tin tài khoản</span>
                </h2>
                <form method="post" action="${pageContext.request.contextPath}/profile" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="info">
                    
                    <label>Họ và tên</label>
                    <input type="text" name="name" value="<c:out value='${u.name}'/>" required>

                    <label>Số điện thoại</label>
                    <input type="tel" name="phone" value="<c:out value='${u.phone}'/>" pattern="[0-9]{9,11}" title="9-11 chữ số">

                    <label>Địa chỉ / Khu vực giao hàng</label>
                    <input type="text" name="location" value="<c:out value='${u.location}'/>" placeholder="Quận/Huyện, Tỉnh/TP...">

                    <label>Ảnh đại diện (JPG, PNG, GIF, WEBP - tối đa 2 MB)</label>
                    <input type="file" name="avatarFile" accept="image/jpeg,image/png,image/gif,image/webp">
                    
                    <c:if test="${not empty u.avatar}">
                        <label class="check">
                            <input type="checkbox" name="removeAvatar"> Xóa ảnh đại diện hiện tại
                        </label>
                    </c:if>

                    <button class="btn" type="submit" style="margin-top: 1.25rem;">LƯU THAY ĐỔI</button>
                </form>
            </div>

            <div class="card">
                <h2>
                    <span class="material-symbols-outlined text-lg text-[#FF4A1C]">key</span>
                    <span>Đổi mật khẩu bảo mật</span>
                </h2>
                <form method="post" action="${pageContext.request.contextPath}/profile">
                    <input type="hidden" name="action" value="password">
                    
                    <label>Mật khẩu hiện tại</label>
                    <input type="password" name="oldPassword" placeholder="••••••••" required>

                    <label>Mật khẩu mới (Tối thiểu 6 ký tự)</label>
                    <input type="password" name="newPassword" placeholder="••••••••" required minlength="6">

                    <label>Nhập lại mật khẩu mới</label>
                    <input type="password" name="confirm" placeholder="••••••••" required minlength="6">

                    <button class="btn" type="submit" style="margin-top: 1.25rem;">CẬP NHẬT MẬT KHẨU</button>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/layout/footer.jspf" %>