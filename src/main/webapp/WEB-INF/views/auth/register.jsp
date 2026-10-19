<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="pageTitle" value="Đăng ký tài khoản — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="auth" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

<div class="auth-screen">
    <div class="auth-card auth-card-wide">
        <!-- Top Badge & Header -->
        <div class="auth-badge">
            <span class="material-symbols-outlined text-[14px]">person_add</span>
            <span>MEMBERSHIP // ĐĂNG KÝ THÀNH VIÊN MỚI</span>
        </div>
        <h1 class="auth-title">Tạo tài khoản</h1>
        <p class="auth-subtitle">
            Tham gia cộng đồng kỹ thuật và sàn giao dịch thiết bị điện tử chính xác cao.
        </p>

        <!-- Flash alerts -->
        <c:if test="${not empty error}">
            <div class="auth-alert auth-alert-error">
                <span class="material-symbols-outlined text-[18px]">error</span>
                <span><c:out value="${error}"/></span>
            </div>
        </c:if>

        <!-- Register Form -->
        <form method="post" action="${pageContext.request.contextPath}/register" id="registerForm">
            <!-- Full name -->
            <div class="auth-field">
                <label class="auth-label" for="regName">
                    <span>HỌ VÀ TÊN</span>
                    <span class="text-zinc-400 font-normal">BẮT BUỘC</span>
                </label>
                <div class="auth-input-wrapper">
                    <span class="material-symbols-outlined auth-input-icon">person</span>
                    <input type="text" id="regName" name="name" value="<c:out value='${name}'/>" 
                           class="auth-input" placeholder="Nguyễn Văn A" required maxlength="100" autofocus>
                </div>
            </div>

            <!-- Email & Phone Grid -->
            <div class="auth-field-grid">
                <div>
                    <label class="auth-label" for="regEmail">
                        <span>ĐỊA CHỈ EMAIL</span>
                        <span class="text-zinc-400 font-normal">XÁC THỰC</span>
                    </label>
                    <div class="auth-input-wrapper">
                        <span class="material-symbols-outlined auth-input-icon">mail</span>
                        <input type="email" id="regEmail" name="email" value="<c:out value='${email}'/>" 
                               class="auth-input" placeholder="name@domain.com" required>
                    </div>
                </div>

                <div>
                    <label class="auth-label" for="regPhone">
                        <span>SỐ ĐIỆN THOẠI</span>
                        <span class="text-zinc-400 font-normal">9-11 SỐ</span>
                    </label>
                    <div class="auth-input-wrapper">
                        <span class="material-symbols-outlined auth-input-icon">call</span>
                        <input type="tel" id="regPhone" name="phone" value="<c:out value='${phone}'/>" 
                               class="auth-input" placeholder="0901234567" required pattern="[0-9]{9,11}" title="9-11 chữ số">
                    </div>
                </div>
            </div>

            <!-- Passwords Grid -->
            <div class="auth-field-grid">
                <div>
                    <label class="auth-label" for="regPassword">
                        <span>MẬT KHẨU</span>
                        <span class="text-zinc-400 font-normal">≥ 6 KÝ TỰ</span>
                    </label>
                    <div class="auth-input-wrapper">
                        <span class="material-symbols-outlined auth-input-icon">lock</span>
                        <input type="password" id="regPassword" name="password" 
                               class="auth-input" placeholder="••••••••" required minlength="6">
                        <button type="button" class="auth-toggle-pass" onclick="togglePass('regPassword', 'eyeIcon1')" title="Hiện/ẩn mật khẩu">
                            <span class="material-symbols-outlined text-[18px]" id="eyeIcon1">visibility</span>
                        </button>
                    </div>
                </div>

                <div>
                    <label class="auth-label" for="regConfirm">
                        <span>XÁC NHẬN</span>
                        <span class="text-zinc-400 font-normal">KHỚP MẬT KHẨU</span>
                    </label>
                    <div class="auth-input-wrapper">
                        <span class="material-symbols-outlined auth-input-icon">verified_user</span>
                        <input type="password" id="regConfirm" name="confirm" 
                               class="auth-input" placeholder="••••••••" required minlength="6">
                        <button type="button" class="auth-toggle-pass" onclick="togglePass('regConfirm', 'eyeIcon2')" title="Hiện/ẩn mật khẩu">
                            <span class="material-symbols-outlined text-[18px]" id="eyeIcon2">visibility</span>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Submit Button -->
            <button type="submit" class="auth-btn-submit" id="submitRegBtn">
                <span>TẠO TÀI KHOẢN NGAY</span>
                <span class="material-symbols-outlined text-[16px]">arrow_forward</span>
            </button>
        </form>

        <!-- Switch to Login -->
        <div class="auth-switch-link">
            <span>Đã có tài khoản giao dịch?</span>
            <a href="${pageContext.request.contextPath}/login">Đăng nhập ngay →</a>
        </div>

        <!-- Security Trust Stamps -->
        <div class="auth-trust-footer">
            <span>
                <span class="material-symbols-outlined text-[12px] text-[#FF4A1C]">shield</span>
                <span>Bảo mật dữ liệu</span>
            </span>
            <span>•</span>
            <span>
                <span class="material-symbols-outlined text-[12px] text-emerald-600">verified</span>
                <span>Tiêu chuẩn ISO</span>
            </span>
            <span>•</span>
            <span>
                <span class="material-symbols-outlined text-[12px] text-zinc-500">lock</span>
                <span>Zero-Spam</span>
            </span>
        </div>
    </div>
</div>

<script>
    function togglePass(inputId, iconId) {
        var input = document.getElementById(inputId);
        var icon = document.getElementById(iconId);
        if (input.type === 'password') {
            input.type = 'text';
            icon.textContent = 'visibility_off';
        } else {
            input.type = 'password';
            icon.textContent = 'visibility';
        }
    }
</script>

<%@ include file="/WEB-INF/layout/footer.jspf" %>
