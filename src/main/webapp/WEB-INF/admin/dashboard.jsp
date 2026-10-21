<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Bảng điều khiển quản trị — ELECTRONICSMARKETPLACE" scope="request"/>
<c:set var="activeNav" value="admin" scope="request"/>
<%@ include file="/WEB-INF/layout/header.jspf" %>

<div class="admin-container">
    <div class="admin-header">
        <h1 class="admin-title">
            <span class="material-symbols-outlined text-[24px] text-[#FF4A1C]">shield_person</span>
            <span>BẢNG ĐIỀU KHIỂN QUẢN TRỊ</span>
        </h1>
        <span class="admin-badge-role">ADMINISTRATOR CONTROL</span>
    </div>

    <!-- Admin Sub-Navigation Tabs -->
    <div class="admin-tabs">
        <a class="admin-tab is-active" href="${pageContext.request.contextPath}/admin">
            <span class="material-symbols-outlined text-[16px]">group</span>
            <span>Người dùng & Duyệt tin</span>
        </a>
        <a class="admin-tab" href="${pageContext.request.contextPath}/admin/repairs">
            <span class="material-symbols-outlined text-[16px]">build</span>
            <span>Đơn sửa chữa</span>
        </a>
        <a class="admin-tab" href="${pageContext.request.contextPath}/admin/repair/devices">
            <span class="material-symbols-outlined text-[16px]">devices</span>
            <span>Dòng máy</span>
        </a>
        <a class="admin-tab" href="${pageContext.request.contextPath}/admin/repair/problems">
            <span class="material-symbols-outlined text-[16px]">report_problem</span>
            <span>Loại lỗi</span>
        </a>
        <a class="admin-tab" href="${pageContext.request.contextPath}/admin/repair/pricing">
            <span class="material-symbols-outlined text-[16px]">payments</span>
            <span>Bảng giá sửa</span>
        </a>
        <a class="admin-tab" href="${pageContext.request.contextPath}/admin/repair/reports">
            <span class="material-symbols-outlined text-[16px]">analytics</span>
            <span>Thống kê Lab</span>
        </a>
    </div>

    <div id="ajaxAlert" class="alert" style="display:none"></div>
    <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>

    <div class="stats">
        <div class="stat"><span id="stat-total">${totalUsers}</span>Tổng người dùng</div>
        <div class="stat"><span id="stat-active">${activeUsers}</span>Đang hoạt động</div>
        <div class="stat"><span id="stat-suspended">${suspendedUsers}</span>Đang bị khóa</div>
        <div class="stat"><span id="stat-pending-listings">${pendingListingsCount}</span>Tin chờ duyệt</div>
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
                <td>${x.id}</td>
                <td><c:out value="${x.name}"/></td>
                <td><c:out value="${x.email}"/></td>
                <td><c:out value="${x.phone}"/></td>
                <td><span class="badge ${x.admin ? 'badge-admin' : 'badge-user'}">${x.role}</span></td>
                <td><span id="status-${x.id}" class="badge badge-${x.active ? 'ok' : 'bad'}">${x.status}</span></td>
                <td>${x.completedSalesCount}</td>
                <td><fmt:formatDate value="${x.createdAt}" pattern="dd/MM/yyyy"/></td>
                <td id="actions-${x.id}">
                    <c:if test="${!x.admin && !x.deleted}">
                        <c:choose>
                            <c:when test="${x.suspended}">
                                <button class="btn btn-sm" data-action="unlock" data-id="${x.id}">Mở khóa</button>
                            </c:when>
                            <c:otherwise>
                                <button class="btn btn-sm btn-warn" data-action="lock" data-id="${x.id}">Khóa</button>
                            </c:otherwise>
                        </c:choose>
                        <button class="btn btn-sm btn-danger" data-action="delete" data-id="${x.id}">Xóa</button>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    </div>
</div>

<div class="card">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
        <h2 style="margin:0;">Tin đăng Chợ đồ cũ chờ duyệt (<span id="pending-count">${pendingListingsCount}</span>)</h2>
    </div>
    <div class="table-wrap">
    <table id="listingTable">
        <thead>
            <tr>
                <th>ID</th>
                <th>Thiết bị</th>
                <th>Người bán</th>
                <th>Tình trạng</th>
                <th>Giá bán</th>
                <th>Thời điểm đăng</th>
                <th>Thao tác</th>
            </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${empty pendingListings}">
                <tr id="empty-listings-row"><td colspan="7" style="text-align:center; color:#888;">Hiện không có tin đăng nào cần duyệt.</td></tr>
            </c:when>
            <c:otherwise>
                <c:forEach var="item" items="${pendingListings}">
                    <tr id="listing-row-${item.id}">
                        <td>#${item.id}</td>
                        <td>
                            <strong><c:out value="${item.device != null ? item.device.name : 'Thiết bị'}"/></strong>
                            <c:if test="${not empty item.description}">
                                <br><small style="color:#666;"><c:out value="${item.description}"/></small>
                            </c:if>
                        </td>
                        <td>
                            <c:out value="${item.seller.name}"/><br>
                            <small style="color:#666;"><c:out value="${item.seller.email}"/></small>
                        </td>
                        <td><span class="badge badge-admin"><c:out value="${item.condition != null ? item.condition.displayName : ''}"/></span></td>
                        <td><strong style="color:#e65100;"><fmt:formatNumber value="${item.price}" pattern="#,###"/> ₫</strong></td>
                        <td><fmt:formatDate value="${item.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td id="listing-actions-${item.id}">
                            <button class="btn btn-sm" style="background:#2e7d32; color:#fff;" data-listing-action="approveListing" data-id="${item.id}">Duyệt</button>
                            <button class="btn btn-sm btn-danger" data-listing-action="rejectListing" data-id="${item.id}">Từ chối</button>
                            <a href="${pageContext.request.contextPath}/listing?action=detail&id=${item.id}" target="_blank" class="btn btn-sm" style="background:#eee; color:#333; text-decoration:none;">Xem</a>
                        </td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>
    </div>
</div>

<script>
(function () {
    var ctx = '${pageContext.request.contextPath}';
    var table = document.getElementById('userTable');
    var alertBox = document.getElementById('ajaxAlert');

    function showAlert(ok, msg) {
        alertBox.className = 'alert ' + (ok ? 'success' : 'error');
        alertBox.textContent = msg || (ok ? 'Thành công.' : 'Thao tác không thành công.');
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

    var listingTable = document.getElementById('listingTable');
    if (listingTable) {
        listingTable.addEventListener('click', function (e) {
            var btn = e.target.closest('button[data-listing-action]');
            if (!btn) return;
            var action = btn.dataset.listingAction;
            var id = btn.dataset.id;
            var isApprove = (action === 'approveListing');

            var reason = '';
            if (!isApprove) {
                reason = prompt('Nhập lý do từ chối duyệt bài đăng này:', 'Không đạt tiêu chuẩn kiểm duyệt');
                if (reason === null) return;
            } else {
                if (!confirm('Bạn có chắc chắn muốn duyệt tin đăng #' + id + '?')) return;
            }

            btn.disabled = true;
            fetch(ctx + '/admin', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Accept': 'application/json'
                },
                body: 'action=' + encodeURIComponent(action) + '&listingId=' + encodeURIComponent(id) + '&reason=' + encodeURIComponent(reason)
            })
            .then(function (r) { return r.json(); })
            .then(function (d) {
                showAlert(d.ok, d.message);
                if (d.ok) {
                    var row = document.getElementById('listing-row-' + id);
                    if (row) row.remove();
                    if (typeof d.pendingCount !== 'undefined') {
                        var pendingCountEl = document.getElementById('pending-count');
                        var statPendingEl = document.getElementById('stat-pending-listings');
                        if (pendingCountEl) pendingCountEl.textContent = d.pendingCount;
                        if (statPendingEl) statPendingEl.textContent = d.pendingCount;
                        if (d.pendingCount === 0) {
                            var tbody = listingTable.querySelector('tbody');
                            if (tbody) {
                                tbody.innerHTML = '<tr id="empty-listings-row"><td colspan="7" style="text-align:center; color:#888;">Hiện không có tin đăng nào cần duyệt.</td></tr>';
                            }
                        }
                    }
                } else {
                    btn.disabled = false;
                }
            })
            .catch(function () {
                showAlert(false, 'Lỗi kết nối hoặc phiên đăng nhập đã hết hạn. Hãy tải lại trang.');
                btn.disabled = false;
            });
        });
    }
})();
</script>
</div>
<%@ include file="/WEB-INF/layout/footer.jspf" %>