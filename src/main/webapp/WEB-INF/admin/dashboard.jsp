<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Bảng điều khiển" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

<h1>Bảng điều khiển quản trị</h1>

<div id="ajaxAlert" class="alert" style="display:none"></div>
<c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>

<div class="stats">
    <div class="stat"><span id="stat-total">${totalUsers}</span>Tổng người dùng</div>
    <div class="stat"><span id="stat-active">${activeUsers}</span>Đang hoạt động</div>
    <div class="stat"><span id="stat-suspended">${suspendedUsers}</span>Đang bị khóa</div>
</div>

<div class="card">
    <h2>Quản lý người dùng</h2>
    <div class="table-wrap">
    <table id="userTable">
        <thead>
            <tr><th>ID</th><th>Họ tên</th><th>Email</th><th>SĐT</th><th>Vai trò</th><th>Trạng thái</th><th>Đã bán</th><th>Ngày tạo</th><th>Thao tác</th></tr>
        </thead>
        <tbody>
        <c:forEach var="x" items="${users}">
            <tr>
                <td>${x.userID}</td>
                <td><c:out value="${x.name}"/></td>
                <td><c:out value="${x.email}"/></td>
                <td><c:out value="${x.phone}"/></td>
                <td><span class="badge ${x.admin ? 'badge-admin' : 'badge-user'}">${x.role}</span></td>
                <td><span id="status-${x.userID}" class="badge badge-${x.active ? 'ok' : 'bad'}">${x.status}</span></td>
                <td>${x.completedSalesCount}</td>
                <td><fmt:formatDate value="${x.createdAt}" pattern="dd/MM/yyyy"/></td>
                <td id="actions-${x.userID}">
                    <c:if test="${!x.admin && !x.deleted}">
                        <c:choose>
                            <c:when test="${x.suspended}">
                                <button class="btn btn-sm" data-action="unlock" data-id="${x.userID}">Mở khóa</button>
                            </c:when>
                            <c:otherwise>
                                <button class="btn btn-sm btn-warn" data-action="lock" data-id="${x.userID}">Khóa</button>
                            </c:otherwise>
                        </c:choose>
                        <button class="btn btn-sm btn-danger" data-action="delete" data-id="${x.userID}">Xóa</button>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    </div>
</div>

<div class="card">
    <h2>Duyệt tin đăng &amp; quản lý thiết bị (Product)</h2>
    <p class="muted">Phần này sẽ nối vào khi module Product của nhóm hoàn thành.</p>
</div>

<script>
(function () {
    var ctx = '${pageContext.request.contextPath}';
    var table = document.getElementById('userTable');
    var alertBox = document.getElementById('ajaxAlert');

    function showAlert(ok, msg) {
        alertBox.className = 'alert ' + (ok ? 'success' : 'error');
        alertBox.textContent = msg;
        alertBox.style.display = 'block';
    }

    function actionsHtml(id, status) {
        if (status === 'DELETED') return '';
        var first = status === 'SUSPENDED'
            ? '<button class="btn btn-sm" data-action="unlock" data-id="' + id + '">Mở khóa</button>'
            : '<button class="btn btn-sm btn-warn" data-action="lock" data-id="' + id + '">Khóa</button>';
        return first + ' <button class="btn btn-sm btn-danger" data-action="delete" data-id="' + id + '">Xóa</button>';
    }

    function updateRow(id, d) {
        var badge = document.getElementById('status-' + id);
        badge.textContent = d.status;
        badge.className = 'badge badge-' + (d.status === 'ACTIVE' ? 'ok' : 'bad');
        document.getElementById('actions-' + id).innerHTML = actionsHtml(id, d.status);
        document.getElementById('stat-total').textContent = d.total;
        document.getElementById('stat-active').textContent = d.active;
        document.getElementById('stat-suspended').textContent = d.suspended;
    }

    table.addEventListener('click', function (e) {
        var btn = e.target.closest('button[data-action]');
        if (!btn) return;
        var action = btn.dataset.action;
        var id = btn.dataset.id;
        if (action === 'delete' && !confirm('Xóa tài khoản này?')) return;

        btn.disabled = true;
        fetch(ctx + '/admin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json'
            },
            body: 'action=' + encodeURIComponent(action) + '&userId=' + encodeURIComponent(id)
        })
        .then(function (r) { return r.json(); })
        .then(function (d) {
            showAlert(d.ok, d.message);
            if (d.ok) updateRow(id, d);
            else btn.disabled = false;
        })
        .catch(function () {
            showAlert(false, 'Lỗi kết nối hoặc phiên đăng nhập đã hết hạn. Hãy tải lại trang.');
            btn.disabled = false;
        });
    });
})();
</script>
<%@ include file="/WEB-INF/layout/footer.jspf" %>