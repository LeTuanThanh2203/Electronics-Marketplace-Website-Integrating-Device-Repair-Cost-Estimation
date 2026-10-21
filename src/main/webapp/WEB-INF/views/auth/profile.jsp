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
            <div id="sidebarAvatarContainer">
                <c:choose>
                    <c:when test="${not empty u.avatar}">
                        <img id="sidebarAvatarImg" class="profile-avatar" src="<c:url value='${u.avatar}'/>" alt="avatar">
                        <div id="sidebarAvatarInitial" class="profile-avatar-initial" style="display: none;"><c:out value="${u.initial}"/></div>
                    </c:when>
                    <c:otherwise>
                        <img id="sidebarAvatarImg" class="profile-avatar" src="" alt="avatar" style="display: none;">
                        <div id="sidebarAvatarInitial" class="profile-avatar-initial"><c:out value="${u.initial}"/></div>
                    </c:otherwise>
                </c:choose>
            </div>

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
                <form method="post" action="${pageContext.request.contextPath}/profile" enctype="multipart/form-data">
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

                    <!-- Chọn ảnh đại diện từ máy tính & Xem trước (Live Preview) -->
                    <div class="profile-field">
                        <label class="profile-label">Ảnh đại diện</label>
                        
                        <div class="profile-avatar-upload-box">
                            <!-- Khung xem trước ảnh đại diện -->
                            <div class="profile-avatar-preview-wrap">
                                <c:choose>
                                    <c:when test="${not empty u.avatar}">
                                        <img id="avatarPreviewImg" class="profile-avatar-preview" src="<c:url value='${u.avatar}'/>" alt="Avatar preview">
                                        <div id="avatarPreviewInitial" class="profile-avatar-preview-initial" style="display: none;"><c:out value="${u.initial}"/></div>
                                    </c:when>
                                    <c:otherwise>
                                        <img id="avatarPreviewImg" class="profile-avatar-preview" src="" alt="Avatar preview" style="display: none;">
                                        <div id="avatarPreviewInitial" class="profile-avatar-preview-initial"><c:out value="${u.initial}"/></div>
                                    </c:otherwise>
                                </c:choose>
                                <span id="previewBadge" class="profile-preview-badge" style="display: none;">Xem trước</span>
                            </div>

                            <!-- Nút chọn ảnh và thao tác -->
                            <div class="profile-avatar-upload-content">
                                <div class="profile-avatar-btn-row">
                                    <label for="avatarFileInput" class="profile-upload-btn">
                                        <span class="material-symbols-outlined text-[18px]">photo_camera</span>
                                        <span>CHỌN ẢNH TỪ MÁY</span>
                                    </label>
                                    <input type="file" id="avatarFileInput" name="avatarFile" 
                                           accept="image/png, image/jpeg, image/jpg, image/webp, image/gif" style="display: none;">

                                    <c:if test="${not empty u.avatar}">
                                        <button type="button" id="btnRemoveAvatar" class="profile-btn-remove-avatar" title="Gỡ bỏ ảnh đại diện">
                                            <span class="material-symbols-outlined text-[16px]">delete</span>
                                            <span>Xóa ảnh</span>
                                        </button>
                                    </c:if>
                                    <button type="button" id="btnCancelPreview" class="profile-btn-remove-avatar" style="display: none;" title="Hủy ảnh vừa chọn">
                                        <span class="material-symbols-outlined text-[16px]">close</span>
                                        <span>Hủy chọn</span>
                                    </button>
                                </div>

                                <input type="hidden" id="removeAvatarInput" name="removeAvatar" value="false">

                                <div id="avatarFileName" class="profile-file-info" style="display: none;"></div>
                                <div class="profile-upload-hint">
                                    Hỗ trợ tệp định dạng JPG, PNG, WEBP, GIF (tối đa 5MB). Xem trước tức thì ngay khi chọn.
                                </div>
                            </div>
                        </div>
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

<script>
(function() {
    var fileInput = document.getElementById('avatarFileInput');
    var previewImg = document.getElementById('avatarPreviewImg');
    var previewInitial = document.getElementById('avatarPreviewInitial');
    var previewBadge = document.getElementById('previewBadge');
    var fileNameDisplay = document.getElementById('avatarFileName');
    var btnCancelPreview = document.getElementById('btnCancelPreview');
    var btnRemoveAvatar = document.getElementById('btnRemoveAvatar');
    var removeAvatarInput = document.getElementById('removeAvatarInput');
    
    var sidebarImg = document.getElementById('sidebarAvatarImg');
    var sidebarInitial = document.getElementById('sidebarAvatarInitial');

    var originalAvatarSrc = previewImg ? previewImg.getAttribute('src') : '';
    var hasOriginalAvatar = originalAvatarSrc && originalAvatarSrc.trim() !== '';

    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            var file = e.target.files && e.target.files[0];
            if (!file) return;

            if (file.size > 5 * 1024 * 1024) {
                alert('Tệp ảnh quá lớn. Vui lòng chọn tệp nhỏ hơn 5MB.');
                fileInput.value = '';
                return;
            }

            var objectUrl = URL.createObjectURL(file);

            if (previewImg) {
                previewImg.src = objectUrl;
                previewImg.style.display = 'block';
            }
            if (previewInitial) {
                previewInitial.style.display = 'none';
            }
            if (previewBadge) {
                previewBadge.textContent = 'Xem trước';
                previewBadge.style.background = 'var(--auth-orange)';
                previewBadge.style.color = '#000000';
                previewBadge.style.display = 'inline-block';
            }

            if (sidebarImg) {
                sidebarImg.src = objectUrl;
                sidebarImg.style.display = 'block';
            }
            if (sidebarInitial) {
                sidebarInitial.style.display = 'none';
            }

            var sizeKb = Math.round(file.size / 1024);
            var sizeStr = sizeKb >= 1024 ? (sizeKb / 1024).toFixed(1) + ' MB' : sizeKb + ' KB';
            if (fileNameDisplay) {
                fileNameDisplay.textContent = 'Đã chọn: ' + file.name + ' (' + sizeStr + ')';
                fileNameDisplay.style.display = 'block';
            }
            if (btnCancelPreview) {
                btnCancelPreview.style.display = 'inline-flex';
            }
            if (btnRemoveAvatar) {
                btnRemoveAvatar.style.display = 'none';
            }
            if (removeAvatarInput) {
                removeAvatarInput.value = 'false';
            }
        });
    }

    if (btnCancelPreview) {
        btnCancelPreview.addEventListener('click', function() {
            if (fileInput) fileInput.value = '';
            if (hasOriginalAvatar && (!removeAvatarInput || removeAvatarInput.value !== 'true')) {
                if (previewImg) {
                    previewImg.src = originalAvatarSrc;
                    previewImg.style.display = 'block';
                }
                if (previewInitial) previewInitial.style.display = 'none';
                if (sidebarImg) {
                    sidebarImg.src = originalAvatarSrc;
                    sidebarImg.style.display = 'block';
                }
                if (sidebarInitial) sidebarInitial.style.display = 'none';
            } else {
                if (previewImg) previewImg.style.display = 'none';
                if (previewInitial) previewInitial.style.display = 'flex';
                if (sidebarImg) sidebarImg.style.display = 'none';
                if (sidebarInitial) sidebarInitial.style.display = 'flex';
            }
            if (previewBadge) previewBadge.style.display = 'none';
            if (fileNameDisplay) fileNameDisplay.style.display = 'none';
            btnCancelPreview.style.display = 'none';
            if (btnRemoveAvatar && hasOriginalAvatar) {
                btnRemoveAvatar.style.display = 'inline-flex';
            }
            if (removeAvatarInput) {
                removeAvatarInput.value = 'false';
            }
        });
    }

    if (btnRemoveAvatar) {
        btnRemoveAvatar.addEventListener('click', function() {
            if (confirm('Bạn có chắc muốn xóa ảnh đại diện và quay về ký tự viết tắt mặc định?')) {
                if (fileInput) fileInput.value = '';
                if (removeAvatarInput) removeAvatarInput.value = 'true';
                if (previewImg) previewImg.style.display = 'none';
                if (previewInitial) previewInitial.style.display = 'flex';
                if (sidebarImg) sidebarImg.style.display = 'none';
                if (sidebarInitial) sidebarInitial.style.display = 'flex';
                if (previewBadge) {
                    previewBadge.textContent = 'Sẽ xóa ảnh';
                    previewBadge.style.display = 'inline-block';
                    previewBadge.style.background = '#DC2626';
                    previewBadge.style.color = '#FFFFFF';
                }
                if (fileNameDisplay) {
                    fileNameDisplay.textContent = 'Ảnh đại diện sẽ bị xóa sau khi bạn bấm Lưu.';
                    fileNameDisplay.style.display = 'block';
                }
                btnRemoveAvatar.style.display = 'none';
                if (btnCancelPreview) btnCancelPreview.style.display = 'inline-flex';
            }
        });
    }
})();
</script>

<%@ include file="/WEB-INF/layout/footer.jspf" %>