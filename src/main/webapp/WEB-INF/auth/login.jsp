<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="pageTitle" value="Đăng nhập — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="auth" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

<div class="auth-screen">
    <div class="auth-card">
        <h1 class="auth-title">Đăng nhập</h1>

        <!-- Flash alerts -->
        <c:if test="${param.registered == '1'}">
            <div class="auth-alert auth-alert-success">
                <span>Tài khoản của bạn đã được đăng ký thành công! Hãy đăng nhập ngay.</span>
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="auth-alert auth-alert-error">
                <span><c:out value="${error}"/></span>
            </div>
        </c:if>

        <!-- Quick Demo Credentials Box -->
        <div class="auth-demo-box">
            <div class="auth-demo-header">
                <span>TÀI KHOẢN MẪU:</span>
            </div>
            <div class="auth-demo-pills">
                <button type="button" class="auth-demo-pill" onclick="fillCreds('admin@marketplace.com', 'admin123')">
                    Admin
                </button>
                <button type="button" class="auth-demo-pill" onclick="fillCreds('user@marketplace.com', 'user123')">
                    Khách hàng
                </button>
            </div>
        </div>

        <!-- Login Form -->
        <form method="post" action="${pageContext.request.contextPath}/login" id="loginForm">
            <c:if test="${not empty param.redirect}">
                <input type="hidden" name="redirect" value="<c:out value='${param.redirect}'/>">
            </c:if>
            <!-- Email -->
            <div class="auth-field">
                <label class="auth-label" for="loginEmail">Email</label>
                <input type="email" id="loginEmail" name="email" value="<c:out value='${email}'/>" 
                       class="auth-input" placeholder="name@domain.com" required autofocus>
            </div>

            <!-- Password -->
            <div class="auth-field">
                <div class="auth-label">
                    <label for="loginPassword" style="margin: 0; cursor: pointer;">Mật khẩu</label>
                </div>
                <div class="auth-input-wrapper">
                    <input type="password" id="loginPassword" name="password" 
                           class="auth-input auth-input-pass" placeholder="Nhập mật khẩu" required>
                    <button type="button" class="auth-toggle-pass" onclick="togglePassVisibility()" title="Hiện/ẩn mật khẩu">
                        <span class="material-symbols-outlined text-[18px]" id="eyeIcon">visibility</span>
                    </button>
                </div>
            </div>

            <!-- Submit Button -->
            <button type="submit" class="auth-btn-submit" id="submitBtn">
                ĐĂNG NHẬP
            </button>
        </form>

        <!-- Switch to Register -->
        <div class="auth-switch-link">
            <span>Chưa có tài khoản?</span>
            <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a>
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