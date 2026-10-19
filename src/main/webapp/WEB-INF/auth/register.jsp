<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="pageTitle" value="Đăng ký tài khoản — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="auth" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

<div class="auth-screen">
    <div class="auth-card auth-card-wide">
        <h1 class="auth-title">Đăng ký tài khoản</h1>

        <!-- Flash alerts -->
        <c:if test="${not empty error}">
            <div class="auth-alert auth-alert-error">
                <span><c:out value="${error}"/></span>
            </div>
        </c:if>

        <!-- Register Form -->
        <form method="post" action="${pageContext.request.contextPath}/register" id="registerForm">
            <!-- Full name -->
            <div class="auth-field">
                <label class="auth-label" for="regName">Họ và tên</label>
                <input type="text" id="regName" name="name" value="<c:out value='${name}'/>" 
                       class="auth-input" placeholder="Nguyễn Văn A" required maxlength="100" autofocus>
            </div>

            <!-- Email & Phone Grid -->
            <div class="auth-field-grid">
                <div>
                    <label class="auth-label" for="regEmail">Email</label>
                    <input type="email" id="regEmail" name="email" value="<c:out value='${email}'/>" 
                           class="auth-input" placeholder="name@domain.com" required>
                </div>

                <div>
                    <label class="auth-label" for="regPhone">Số điện thoại</label>
                    <input type="tel" id="regPhone" name="phone" value="<c:out value='${phone}'/>" 
                           class="auth-input" placeholder="0901234567" required pattern="[0-9]{9,11}" title="9-11 chữ số">
                </div>
            </div>

            <!-- Passwords Grid -->
            <div class="auth-field-grid">
                <div>
                    <label class="auth-label" for="regPassword">Mật khẩu</label>
                    <div class="auth-input-wrapper">
                        <input type="password" id="regPassword" name="password" 
                               class="auth-input auth-input-pass" placeholder="Tối thiểu 6 ký tự" required minlength="6">
                        <button type="button" class="auth-toggle-pass" onclick="togglePass('regPassword', 'eyeIcon1')" title="Hiện/ẩn mật khẩu">
                            <span class="material-symbols-outlined text-[18px]" id="eyeIcon1">visibility</span>
                        </button>
                    </div>
                </div>

                <div>
                    <label class="auth-label" for="regConfirm">Xác nhận mật khẩu</label>
                    <div class="auth-input-wrapper">
                        <input type="password" id="regConfirm" name="confirm" 
                               class="auth-input auth-input-pass" placeholder="Nhập lại mật khẩu" required minlength="6">
                        <button type="button" class="auth-toggle-pass" onclick="togglePass('regConfirm', 'eyeIcon2')" title="Hiện/ẩn mật khẩu">
                            <span class="material-symbols-outlined text-[18px]" id="eyeIcon2">visibility</span>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Submit Button -->
            <button type="submit" class="auth-btn-submit" id="submitRegBtn">
                TẠO TÀI KHOẢN
            </button>
        </form>

        <!-- Switch to Login -->
        <div class="auth-switch-link">
            <span>Đã có tài khoản?</span>
            <a href="${pageContext.request.contextPath}/login">Đăng nhập ngay</a>
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
