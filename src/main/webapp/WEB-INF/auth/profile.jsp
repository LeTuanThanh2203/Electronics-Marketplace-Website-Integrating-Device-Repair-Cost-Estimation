<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Hồ sơ cá nhân — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="auth" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

<div class="profile-screen">
    <c:set var="u" value="${sessionScope.currentUser}"/>

    <!-- Flash notifications -->
    <c:if test="${not empty error}">
        <div class="auth-alert auth-alert-error" style="margin-bottom: 1.5rem;">
            <span class="material-symbols-outlined text-[18px]">error</span>
            <span><c:out value="${error}"/></span>
        </div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="auth-alert auth-alert-success" style="margin-bottom: 1.5rem;">
            <span class="material-symbols-outlined text-[18px]">check_circle</span>
            <span><c:out value="${success}"/></span>
        </div>
    </c:if>

    <div class="profile-grid">
        <!-- Sidebar: User Overview -->
        <div class="profile-card profile-sidebar">
            <c:choose>
                <c:when test="${not empty u.avatar}">
                    <img class="profile-avatar" src="<c:url value='${u.avatar}'/>" alt="avatar">
                </c:when>
                <c:otherwise>
                    <div class="profile-avatar-initial"><c:out value="${u.initial}"/></div>
                </c:otherwise>
            </c:choose>

            <h2 class="profile-username"><c:out value="${u.name}"/></h2>
            
            <div style="margin-bottom: 1rem;">
                <span class="profile-badge ${u.admin ? 'profile-badge-admin' : 'profile-badge-user'}">${u.role}</span>
                <span class="profile-badge ${u.active ? 'profile-badge-active' : 'profile-badge-suspended'}">${u.status}</span>
            </div>

            <ul class="profile-info-list">
                <li>
                    <span class="material-symbols-outlined text-[18px] text-zinc-400">mail</span>
                    <span><c:out value="${u.email}"/></span>
                </li>
                <li>
                    <span class="material-symbols-outlined text-[18px] text-zinc-400">call</span>
                    <span><c:out value="${empty u.phone ? 'Chưa cập nhật' : u.phone}"/></span>
                </li>
                <li>
                    <span class="material-symbols-outlined text-[18px] text-zinc-400">location_on</span>
                    <span><c:out value="${empty u.location ? 'Chưa cập nhật' : u.location}"/></span>
                </li>
                <li>
                    <span class="material-symbols-outlined text-[18px] text-zinc-400">shopping_bag</span>
                    <span>Giao dịch thành công: <strong style="color: var(--auth-black);">${u.completedSalesCount}</strong> đơn</span>
                </li>
                <li>
                    <span class="material-symbols-outlined text-[18px] text-zinc-400">calendar_today</span>
                    <span>Tham gia: <fmt:formatDate value="${u.createdAt}" pattern="dd/MM/yyyy"/></span>
                </li>
            </ul>

            <div style="margin-top: 1.75rem; padding-top: 1.25rem; border-top: 1px solid var(--auth-border); display: flex; flex-direction: column; gap: 0.5rem;">
                <a href="${pageContext.request.contextPath}/order" class="profile-btn profile-btn-outline" style="width: 100%;">
                    <span class="material-symbols-outlined text-[16px]">receipt_long</span>
                    <span>QUẢN LÝ ĐƠN HÀNG</span>
                </a>
                <c:if test="${u.admin}">
                    <a href="${pageContext.request.contextPath}/admin" class="profile-btn profile-btn-admin" style="width: 100%;">
                        <span class="material-symbols-outlined text-[16px]">shield_person</span>
                        <span>VÀO BẢNG QUẢN TRỊ ADMIN</span>
                    </a>
                </c:if>
                <a href="${pageContext.request.contextPath}/logout" class="profile-btn profile-btn-outline" style="width: 100%; color: #DC2626; border-color: #FCA5A5;">
                    <span class="material-symbols-outlined text-[16px]">logout</span>
                    <span>ĐĂNG XUẤT</span>
                </a>
            </div>
        </div>

        <!-- Right Side: Edit Information & Security -->
        <div>
            <!-- Update Info Form -->
            <div class="profile-card">
                <h3 class="profile-card-title">
                    <span class="material-symbols-outlined text-[#FF4A1C]">badge</span>
                    <span>Cập nhật thông tin tài khoản</span>
                </h3>
                <form method="post" action="${pageContext.request.contextPath}/profile">
                    <input type="hidden" name="action" value="info">
                    
                    <div class="profile-field">
                        <label class="profile-label" for="profName">Họ và tên</label>
                        <input type="text" id="profName" name="name" value="<c:out value='${u.name}'/>" 
                               class="profile-input" required maxlength="100">
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                        <div class="profile-field">
                            <label class="profile-label" for="profPhone">Số điện thoại</label>
                            <input type="tel" id="profPhone" name="phone" value="<c:out value='${u.phone}'/>" 
                                   class="profile-input" pattern="[0-9]{9,11}" title="9-11 chữ số" placeholder="0901234567">
                        </div>

                        <div class="profile-field">
                            <label class="profile-label" for="profLocation">Khu vực / Tỉnh, Thành phố</label>
                            <input type="text" id="profLocation" name="location" value="<c:out value='${u.location}'/>" 
                                   class="profile-input" placeholder="Hà Nội, TP.HCM...">
                        </div>
                    </div>

                    <div class="profile-field">
                        <label class="profile-label" for="profAvatar">Đường dẫn ảnh đại diện (URL)</label>
                        <input type="url" id="profAvatar" name="avatar" value="<c:out value='${u.avatar}'/>" 
                               class="profile-input" placeholder="https://example.com/avatar.jpg">
                    </div>

                    <button class="profile-btn profile-btn-primary" type="submit" style="margin-top: 0.5rem;">
                        <span class="material-symbols-outlined text-[16px]">save</span>
                        <span>LƯU THAY ĐỔI THÔNG TIN</span>
                    </button>
                </form>
            </div>

            <!-- Change Password Form -->
            <div class="profile-card">
                <h3 class="profile-card-title">
                    <span class="material-symbols-outlined text-[#FF4A1C]">lock_reset</span>
                    <span>Đổi mật khẩu bảo mật</span>
                </h3>
                <form method="post" action="${pageContext.request.contextPath}/profile">
                    <input type="hidden" name="action" value="password">
                    
                    <div class="profile-field">
                        <label class="profile-label" for="profOldPassword">Mật khẩu hiện tại</label>
                        <input type="password" id="profOldPassword" name="oldPassword" 
                               class="profile-input" placeholder="••••••••" required>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                        <div class="profile-field">
                            <label class="profile-label" for="profNewPassword">Mật khẩu mới (≥ 6 ký tự)</label>
                            <input type="password" id="profNewPassword" name="newPassword" 
                                   class="profile-input" placeholder="••••••••" required minlength="6">
                        </div>

                        <div class="profile-field">
                            <label class="profile-label" for="profConfirmPassword">Xác nhận lại mật khẩu mới</label>
                            <input type="password" id="profConfirmPassword" name="confirm" 
                                   class="profile-input" placeholder="••••••••" required minlength="6">
                        </div>
                    </div>

                    <button class="profile-btn profile-btn-primary" type="submit" style="margin-top: 0.5rem;">
                        <span class="material-symbols-outlined text-[16px]">key</span>
                        <span>CẬP NHẬT MẬT KHẨU</span>
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/layout/footer.jspf" %>