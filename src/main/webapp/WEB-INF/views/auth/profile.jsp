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
            <div style="position: relative; width: 108px; height: 108px; margin: 0 auto 1.25rem;">
                <c:choose>
                    <c:when test="${not empty u.avatar}">
                        <img id="sidebarAvatarImg" class="profile-avatar" src="<c:url value='${u.avatar}'/>" alt="avatar" style="margin: 0; width: 108px; height: 108px;">
                        <div id="sidebarAvatarInitial" class="profile-avatar-initial" style="margin: 0; width: 108px; height: 108px; display: none;"><c:out value="${u.initial}"/></div>
                    </c:when>
                    <c:otherwise>
                        <img id="sidebarAvatarImg" class="profile-avatar" src="" alt="avatar" style="margin: 0; width: 108px; height: 108px; display: none;">
                        <div id="sidebarAvatarInitial" class="profile-avatar-initial" style="margin: 0; width: 108px; height: 108px;"><c:out value="${u.initial}"/></div>
                    </c:otherwise>
                </c:choose>

                <!-- Nút máy ảnh mở chọn file nhanh từ thư mục -->
                <button type="button" onclick="document.getElementById('avatarFileInput').click()" 
                        title="Bấm để chọn ảnh từ thư mục máy tính"
                        style="position: absolute; bottom: 0; right: 0; width: 34px; height: 34px; border-radius: 50%; background: #FF4A1C; color: #000000; border: 2px solid #FFFFFF; display: flex; align-items: center; justify-content: center; cursor: pointer; box-shadow: 0 4px 10px rgba(0,0,0,0.25); transition: transform 0.15s, background-color 0.15s;"
                        onmouseover="this.style.transform='scale(1.1)'; this.style.backgroundColor='#ff6438';" 
                        onmouseout="this.style.transform='scale(1)'; this.style.backgroundColor='#FF4A1C';">
                    <span class="material-symbols-outlined text-[18px]">photo_camera</span>
                </button>
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
            <!-- Update Info Form (Hỗ trợ multipart/form-data để upload ảnh trực tiếp) -->
            <div class="profile-card">
                <h3 class="profile-card-title">
                    <span class="material-symbols-outlined text-[#FF4A1C]">badge</span>
                    <span>Cập nhật thông tin tài khoản</span>
                </h3>
                <form method="post" action="${pageContext.request.contextPath}/profile" enctype="multipart/form-data" id="profileUpdateForm">
                    <input type="hidden" name="action" value="info">
                    <input type="hidden" id="clearAvatarInput" name="clearAvatar" value="false">
                    
                    <!-- AVATAR SELECTION BOX (Chọn từ folder máy tính & Xem trước trực tiếp) -->
                    <div style="background: #F4F4F2; border: 1px solid #DADAD8; padding: 1.25rem; margin-bottom: 1.5rem;">
                        <div style="display: flex; flex-direction: column; sm:flex-row; gap: 1.25rem; align-items: flex-start;">
                            <!-- Khung ảnh Preview -->
                            <div style="position: relative; width: 92px; height: 92px; flex-shrink: 0; margin: 0 auto sm:margin-0;">
                                <c:choose>
                                    <c:when test="${not empty u.avatar}">
                                        <img id="previewAvatarImg" src="<c:url value='${u.avatar}'/>" 
                                             alt="Ảnh xem trước" 
                                             style="width: 92px; height: 92px; border-radius: 50%; object-fit: cover; border: 3px solid #0B0B0B; box-shadow: 0 4px 12px rgba(0,0,0,0.12);">
                                        <div id="previewAvatarInitial" style="display: none; width: 92px; height: 92px; border-radius: 50%; background: #0B0B0B; color: #FFFFFF; font-family: var(--auth-font-heading); font-size: 2rem; font-weight: 800; display: flex; align-items: center; justify-content: center; border: 3px solid #FF4A1C;">
                                            <c:out value="${u.initial}"/>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <img id="previewAvatarImg" src="" 
                                             alt="Ảnh xem trước" 
                                             style="display: none; width: 92px; height: 92px; border-radius: 50%; object-fit: cover; border: 3px solid #FF4A1C; box-shadow: 0 4px 12px rgba(0,0,0,0.12);">
                                        <div id="previewAvatarInitial" style="width: 92px; height: 92px; border-radius: 50%; background: #0B0B0B; color: #FFFFFF; font-family: var(--auth-font-heading); font-size: 2rem; font-weight: 800; display: flex; align-items: center; justify-content: center; border: 3px solid #FF4A1C;">
                                            <c:out value="${u.initial}"/>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <!-- Huy hiệu thông báo trạng thái ảnh mới -->
                                <span id="previewNewBadge" style="display: none; position: absolute; top: -6px; right: -6px; background: #FF4A1C; color: #000000; font-family: var(--auth-font-mono); font-size: 9px; font-weight: 800; padding: 2px 6px; text-transform: uppercase; border-radius: 2px; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">
                                    MỚI
                                </span>
                            </div>

                            <!-- Bộ nút chọn file từ thư mục & thông tin -->
                            <div style="flex: 1; width: 100%;">
                                <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.35rem;">
                                    <label style="font-family: var(--auth-font-mono); font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--auth-black); letter-spacing: 0.05em;">
                                        ẢNH ĐẠI DIỆN TÀI KHOẢN
                                    </label>
                                </div>
                                <p style="font-size: 0.8125rem; color: #555555; margin: 0 0 0.85rem 0; line-height: 1.4;">
                                    Tải ảnh trực tiếp từ thư mục trên máy tính của bạn (hỗ trợ JPG, PNG, WEBP, GIF tối đa 5MB). Xem lại ảnh hiển thị ngay bên cạnh trước khi bấm lưu.
                                </p>

                                <!-- File Input ẩn, kích hoạt qua nút bấm bên dưới -->
                                <input type="file" id="avatarFileInput" name="avatarFile" 
                                       accept="image/png,image/jpeg,image/jpg,image/webp,image/gif" 
                                       style="display: none;" 
                                       onchange="handleAvatarFileSelect(this)">

                                <!-- Hàng nút điều khiển -->
                                <div style="display: flex; flex-wrap: wrap; gap: 0.625rem; align-items: center;">
                                    <!-- Nút mở Folder máy tính -->
                                    <button type="button" onclick="document.getElementById('avatarFileInput').click()" 
                                            style="display: inline-flex; align-items: center; gap: 0.5rem; height: 40px; padding: 0 1.25rem; background: #0B0B0B; color: #FFFFFF; border: 1px solid #0B0B0B; font-family: var(--auth-font-mono); font-size: 0.75rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; cursor: pointer; transition: all 0.2s;"
                                            onmouseover="this.style.background='#FF4A1C'; this.style.borderColor='#FF4A1C'; this.style.color='#000000';" 
                                            onmouseout="this.style.background='#0B0B0B'; this.style.borderColor='#0B0B0B'; this.style.color='#FFFFFF';">
                                        <span class="material-symbols-outlined text-[18px]">folder_open</span>
                                        <span>CHỌN ẢNH TỪ MÁY TÍNH...</span>
                                    </button>

                                    <!-- Nút Hủy chọn ảnh vừa tải lên -->
                                    <button type="button" id="btnCancelAvatarSelect" onclick="resetAvatarPreview()" 
                                            style="display: none; align-items: center; gap: 0.375rem; height: 40px; padding: 0 1rem; background: #FFFFFF; color: #333333; border: 1px solid #DADAD8; font-family: var(--auth-font-mono); font-size: 0.75rem; font-weight: 600; cursor: pointer; transition: all 0.2s;"
                                            onmouseover="this.style.borderColor='#0B0B0B'" onmouseout="this.style.borderColor='#DADAD8'">
                                        <span class="material-symbols-outlined text-[16px]">cancel</span>
                                        <span>Hủy chọn</span>
                                    </button>

                                    <!-- Nút Xóa ảnh hiện tại (về chữ cái mặc định) -->
                                    <c:if test="${not empty u.avatar}">
                                        <button type="button" id="btnRemoveAvatar" onclick="removeCurrentAvatar()" 
                                                style="display: inline-flex; align-items: center; gap: 0.375rem; height: 40px; padding: 0 1rem; background: #FFFFFF; color: #DC2626; border: 1px solid #FCA5A5; font-family: var(--auth-font-mono); font-size: 0.75rem; font-weight: 600; cursor: pointer; transition: all 0.2s;"
                                                onmouseover="this.style.background='#FEF2F2'" onmouseout="this.style.background='#FFFFFF'">
                                            <span class="material-symbols-outlined text-[16px]">delete</span>
                                            <span>Xóa ảnh đại diện</span>
                                        </button>
                                    </c:if>
                                </div>

                                <!-- Thanh trạng thái tên file -->
                                <div id="avatarFileNameDisplay" style="display: none; margin-top: 0.625rem; font-family: var(--auth-font-mono); font-size: 0.75rem; color: #047857; font-weight: 600;">
                                    <span class="material-symbols-outlined text-[16px]" style="vertical-align: middle;">task_alt</span>
                                    <span id="avatarFileNameText"></span>
                                </div>
                            </div>
                        </div>
                    </div>

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
    const originalAvatarUrl = "${not empty u.avatar ? u.avatar : ''}";
    const originalInitial = "${u.initial}";

    function handleAvatarFileSelect(input) {
        if (!input.files || !input.files[0]) return;
        const file = input.files[0];

        // Kiểm tra định dạng ảnh
        const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];
        if (!validTypes.includes(file.type)) {
            alert('Vui lòng chọn file hình ảnh hợp lệ (JPG, PNG, WEBP, GIF).');
            input.value = '';
            return;
        }

        // Kiểm tra dung lượng (tối đa 5MB)
        if (file.size > 5 * 1024 * 1024) {
            alert('Dung lượng ảnh tối đa là 5MB. Vui lòng chọn ảnh có dung lượng nhỏ hơn.');
            input.value = '';
            return;
        }

        const reader = new FileReader();
        reader.onload = function(e) {
            const dataUrl = e.target.result;

            // 1. Cập nhật xem trước ở khung Form
            const previewImg = document.getElementById('previewAvatarImg');
            const previewInitial = document.getElementById('previewAvatarInitial');
            if (previewImg) {
                previewImg.src = dataUrl;
                previewImg.style.display = 'block';
                previewImg.style.border = '3px solid #FF4A1C';
            }
            if (previewInitial) {
                previewInitial.style.display = 'none';
            }

            // 2. Cập nhật xem trước ngay lập tức ở cột Sidebar bên trái
            const sidebarImg = document.getElementById('sidebarAvatarImg');
            const sidebarInitial = document.getElementById('sidebarAvatarInitial');
            if (sidebarImg) {
                sidebarImg.src = dataUrl;
                sidebarImg.style.display = 'block';
                sidebarImg.style.border = '3px solid #FF4A1C';
            }
            if (sidebarInitial) {
                sidebarInitial.style.display = 'none';
            }

            // 3. Hiển thị huy hiệu ảnh mới & nút hủy chọn
            const badge = document.getElementById('previewNewBadge');
            if (badge) badge.style.display = 'inline-block';

            const btnCancel = document.getElementById('btnCancelAvatarSelect');
            if (btnCancel) btnCancel.style.display = 'inline-flex';

            document.getElementById('clearAvatarInput').value = 'false';

            // 4. Hiển thị tên file và dung lượng
            const nameDisplay = document.getElementById('avatarFileNameDisplay');
            const nameText = document.getElementById('avatarFileNameText');
            if (nameDisplay && nameText) {
                const sizeKb = (file.size / 1024).toFixed(1);
                nameText.textContent = "Đã chọn: " + file.name + " (" + sizeKb + " KB) — Xem lại ảnh và bấm 'LƯU THAY ĐỔI' để cập nhật.";
                nameDisplay.style.display = 'block';
            }
        };
        reader.readAsDataURL(file);
    }

    function resetAvatarPreview() {
        const fileInput = document.getElementById('avatarFileInput');
        fileInput.value = '';

        const previewImg = document.getElementById('previewAvatarImg');
        const previewInitial = document.getElementById('previewAvatarInitial');
        const sidebarImg = document.getElementById('sidebarAvatarImg');
        const sidebarInitial = document.getElementById('sidebarAvatarInitial');

        if (originalAvatarUrl && originalAvatarUrl.trim() !== '') {
            if (previewImg) {
                previewImg.src = originalAvatarUrl;
                previewImg.style.display = 'block';
                previewImg.style.border = '3px solid #0B0B0B';
            }
            if (previewInitial) previewInitial.style.display = 'none';

            if (sidebarImg) {
                sidebarImg.src = originalAvatarUrl;
                sidebarImg.style.display = 'block';
                sidebarImg.style.border = '3px solid var(--auth-orange)';
            }
            if (sidebarInitial) sidebarInitial.style.display = 'none';
        } else {
            if (previewImg) previewImg.style.display = 'none';
            if (previewInitial) previewInitial.style.display = 'flex';

            if (sidebarImg) sidebarImg.style.display = 'none';
            if (sidebarInitial) sidebarInitial.style.display = 'flex';
        }

        const badge = document.getElementById('previewNewBadge');
        if (badge) badge.style.display = 'none';

        const btnCancel = document.getElementById('btnCancelAvatarSelect');
        if (btnCancel) btnCancel.style.display = 'none';

        const nameDisplay = document.getElementById('avatarFileNameDisplay');
        if (nameDisplay) nameDisplay.style.display = 'none';

        document.getElementById('clearAvatarInput').value = 'false';
    }

    function removeCurrentAvatar() {
        if (!confirm('Bạn có chắc muốn xóa ảnh đại diện này và quay về chữ cái đại diện mặc định?')) return;
        document.getElementById('avatarFileInput').value = '';
        document.getElementById('clearAvatarInput').value = 'true';

        const previewImg = document.getElementById('previewAvatarImg');
        const previewInitial = document.getElementById('previewAvatarInitial');
        const sidebarImg = document.getElementById('sidebarAvatarImg');
        const sidebarInitial = document.getElementById('sidebarAvatarInitial');

        if (previewImg) previewImg.style.display = 'none';
        if (previewInitial) previewInitial.style.display = 'flex';
        if (sidebarImg) sidebarImg.style.display = 'none';
        if (sidebarInitial) sidebarInitial.style.display = 'flex';

        const btnRemove = document.getElementById('btnRemoveAvatar');
        if (btnRemove) btnRemove.style.display = 'none';

        const btnCancel = document.getElementById('btnCancelAvatarSelect');
        if (btnCancel) btnCancel.style.display = 'inline-flex';

        const nameDisplay = document.getElementById('avatarFileNameDisplay');
        const nameText = document.getElementById('avatarFileNameText');
        if (nameDisplay && nameText) {
            nameText.textContent = "Sẽ xóa ảnh khi bấm 'LƯU THAY ĐỔI THÔNG TIN'.";
            nameDisplay.style.display = 'block';
        }
    }
</script>

<%@ include file="/WEB-INF/layout/footer.jspf" %>