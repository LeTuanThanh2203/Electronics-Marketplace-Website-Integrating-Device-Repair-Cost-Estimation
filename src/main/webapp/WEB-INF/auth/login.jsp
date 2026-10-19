<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="pageTitle" value="Đăng nhập — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="auth" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

<div class="auth-screen">
    <div class="auth-card">
        <!-- Top Metadata & Header -->
        <div class="auth-badge">
            <span class="material-symbols-outlined text-[14px]">verified_user</span>
            <span>SECURE ACCESS // CỔNG XÁC THỰC TIÊU CHUẨN</span>
        </div>
        <h1 class="auth-title">Đăng nhập</h1>
        <p class="auth-subtitle">
            Hệ thống chứng thực an toàn cho người mua, người bán và kỹ thuật viên phòng Lab.
        </p>

        <!-- Flash alerts -->
        <c:if test="${param.registered == '1'}">
            <div class="auth-alert auth-alert-success">
                <span class="material-symbols-outlined text-[18px]">check_circle</span>
                <span>Tài khoản của bạn đã được đăng ký thành công! Hãy đăng nhập ngay.</span>
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="auth-alert auth-alert-error">
                <span class="material-symbols-outlined text-[18px]">error</span>
                <span><c:out value="${error}"/></span>
            </div>
        </c:if>

        <!-- Quick Demo Credentials Box -->
        <div class="auth-demo-box">
            <div class="auth-demo-header">
                <span>NẠP NHANH TÀI KHOẢN MẪU:</span>
                <span class="material-symbols-outlined text-[13px]">smart_toy</span>
            </div>
            <div class="auth-demo-pills">
                <button type="button" class="auth-demo-pill" onclick="fillCreds('admin@marketplace.com', 'admin123')">
                    <span class="material-symbols-outlined text-[13px]">shield_person</span>
                    <span>Admin</span>
                </button>
                <button type="button" class="auth-demo-pill" onclick="fillCreds('user@marketplace.com', 'user123')">
                    <span class="material-symbols-outlined text-[13px]">person</span>
                    <span>Khách hàng</span>
                </button>
            </div>
        </div>

        <!-- Login Form -->
        <form method="post" action="${pageContext.request.contextPath}/login" id="loginForm">
            <!-- Email -->
            <div class="auth-field">
                <label class="auth-label" for="loginEmail">
                    <span>ĐỊA CHỈ EMAIL</span>
                    <span class="text-zinc-400 font-normal">BẮT BUỘC</span>
                </label>
                <div class="auth-input-wrapper">
                    <span class="material-symbols-outlined auth-input-icon">mail</span>
                    <input type="email" id="loginEmail" name="email" value="<c:out value='${email}'/>" 
                           class="auth-input" placeholder="name@domain.com" required autofocus>
                </div>
            </div>

            <!-- Password -->
            <div class="auth-field">
                <div class="auth-label">
                    <label for="loginPassword" style="margin: 0; cursor: pointer;">MẬT KHẨU BẢO MẬT</label>
                    <a href="${pageContext.request.contextPath}/register" class="text-[11px] text-[#FF4A1C] hover:underline font-normal font-sans lowercase">quên mật khẩu?</a>
                </div>
                <div class="auth-input-wrapper">
                    <span class="material-symbols-outlined auth-input-icon">lock</span>
                    <input type="password" id="loginPassword" name="password" 
                           class="auth-input" placeholder="••••••••" required>
                    <button type="button" class="auth-toggle-pass" onclick="togglePassVisibility()" title="Hiện/ẩn mật khẩu">
                        <span class="material-symbols-outlined text-[18px]" id="eyeIcon">visibility</span>
                    </button>
                </div>
            </div>

            <!-- Submit Button -->
            <button type="submit" class="auth-btn-submit" id="submitBtn">
                <span>ĐĂNG NHẬP HỆ THỐNG</span>
                <span class="material-symbols-outlined text-[16px]">arrow_forward</span>
            </button>
        </form>

        <!-- Switch to Register -->
        <div class="auth-switch-link">
            <span>Chưa có tài khoản giao dịch?</span>
            <a href="${pageContext.request.contextPath}/register">Đăng ký thành viên mới →</a>
        </div>

        <!-- Security Trust Stamps -->
        <div class="auth-trust-footer">
            <span>
                <span class="material-symbols-outlined text-[12px] text-[#FF4A1C]">lock</span>
                <span>256-Bit SSL</span>
            </span>
            <span>•</span>
            <span>
                <span class="material-symbols-outlined text-[12px] text-emerald-600">verified</span>
                <span>ISO 9001:2015</span>
            </span>
            <span>•</span>
            <span>
                <span class="material-symbols-outlined text-[12px] text-zinc-500">security</span>
                <span>Zero-Leak</span>
            </span>
        </div>
    </div>
</div>

<script>
    function togglePassVisibility() {
        var input = document.getElementById('loginPassword');
        var icon = document.getElementById('eyeIcon');
        if (input.type === 'password') {
            input.type = 'text';
            icon.textContent = 'visibility_off';
        } else {
            input.type = 'password';
            icon.textContent = 'visibility';
        }
    }

    function fillCreds(email, pass) {
        document.getElementById('loginEmail').value = email;
        document.getElementById('loginPassword').value = pass;
    }
</script>

<%@ include file="/WEB-INF/layout/footer.jspf" %>