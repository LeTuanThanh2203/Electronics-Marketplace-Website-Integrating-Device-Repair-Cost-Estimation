<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="rpAdminPage" value="products" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Sản phẩm B2C | Sửa hay bán Admin</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&family=Space+Grotesk:wght@500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/repair.css">
    <style>
        .rp-badge-avail { background: #e8f5e9; color: #2e7d32; border: 1px solid #c8e6c9; font-weight: 600; padding: 2px 8px; font-size: 11px; }
        .rp-badge-out { background: #ffebee; color: #c62828; border: 1px solid #ffcdd2; font-weight: 600; padding: 2px 8px; font-size: 11px; }
        .rp-badge-need { background: #f3e5f5; color: #6a1b9a; border: 1px solid #e1bee7; font-weight: 600; padding: 2px 8px; font-size: 11px; }
        .rp-prod-img { width: 52px; height: 52px; object-fit: contain; background: #fff; border: 1px solid var(--rp-gray-200); padding: 2px; }
        .rp-prod-img-ph { width: 52px; height: 52px; background: var(--rp-gray-100); border: 1px dashed var(--rp-gray-300); display: flex; align-items: center; justify-content: center; color: #999; font-size: 10px; text-transform: uppercase; }
        .rp-adminform__grid-3 { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 14px; }
    </style>
</head>
<body class="rp-body">

<div class="rp-shell">
    <%@ include file="/WEB-INF/views/admin/_header.jspf" %>

    <div class="rp-adminhead">
        <div>
            <h1>Sản phẩm B2C (Catalog Máy Mới)</h1>
            <p>Quản lý danh mục & đăng bán sản phẩm chính hãng do Quản trị viên (Admin) điều phối.</p>
        </div>
        <div class="rp-adminhead__meta">
            Tổng số: <c:out value="${totalProducts}" /> sản phẩm
        </div>
    </div>

    <div class="rp-adminbody">

        <!-- Form Đăng sản phẩm mới -->
        <div class="rp-adminform">
            <div class="rp-adminform__title">
                <span class="rp-section__num">+</span> Đăng sản phẩm máy mới (B2C)
            </div>
            <form action="${pageContext.request.contextPath}/admin/products" method="POST">
                <input type="hidden" name="action" value="addProduct">
                
                <div class="rp-adminform__grid-3" style="margin-bottom: 14px;">
                    <div class="rp-field">
                        <label class="rp-label" for="name"><span>Tên sản phẩm <span class="rp-req">*</span></span></label>
                        <input class="rp-input rp-input--sm" type="text" id="name" name="name"
                               placeholder="VD: Dell XPS 13 Plus 9320, MacBook Air M2..." required>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="brand"><span>Hãng sản xuất <span class="rp-req">*</span></span></label>
                        <input class="rp-input rp-input--sm" type="text" id="brand" name="brand" list="brand-suggestions"
                               placeholder="Apple, Dell, Asus, Lenovo, HP, Acer, MSI..." required>
                        <datalist id="brand-suggestions">
                            <option value="Apple">
                            <option value="Dell">
                            <option value="Asus">
                            <option value="Lenovo">
                            <option value="HP">
                            <option value="Acer">
                            <option value="MSI">
                        </datalist>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="series"><span>Series / Dòng máy</span></label>
                        <input class="rp-input rp-input--sm" type="text" id="series" name="series"
                               placeholder="VD: XPS, MacBook, ROG, ThinkPad...">
                    </div>
                </div>

                <div class="rp-adminform__grid-3" style="margin-bottom: 14px;">
                    <div class="rp-field">
                        <label class="rp-label" for="price"><span>Giá bán (₫) <span class="rp-req">*</span></span></label>
                        <input class="rp-input rp-input--sm" type="number" id="price" name="price" min="0" step="10000"
                               placeholder="35990000" required>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="originalPrice"><span>Giá niêm yết gốc (₫)</span></label>
                        <input class="rp-input rp-input--sm" type="number" id="originalPrice" name="originalPrice" min="0" step="10000"
                               placeholder="38990000">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="stockQuantity"><span>Số lượng tồn kho</span></label>
                        <input class="rp-input rp-input--sm" type="number" id="stockQuantity" name="stockQuantity" min="0"
                               value="10">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="sku"><span>Mã SKU / Model</span></label>
                        <input class="rp-input rp-input--sm" type="text" id="sku" name="sku"
                               placeholder="Tự sinh nếu để trống">
                    </div>
                </div>

                <div class="rp-adminform__grid-3" style="margin-bottom: 14px;">
                    <div class="rp-field">
                        <label class="rp-label" for="targetNeed"><span>Phân khúc / Nhu cầu gợi ý AI</span></label>
                        <select class="rp-input rp-input--sm" id="targetNeed" name="targetNeed">
                            <option value="OFFICE">Văn phòng / Học tập (OFFICE)</option>
                            <option value="GAMING">Gaming / Hiệu năng cao (GAMING)</option>
                            <option value="GRAPHICS">Đồ họa / Sáng tạo (GRAPHICS)</option>
                        </select>
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="badge"><span>Huy hiệu nổi bật</span></label>
                        <input class="rp-input rp-input--sm" type="text" id="badge" name="badge"
                               value="SEAL 100%" placeholder="SEAL 100%, HOT DEAL...">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="discountTag"><span>Tag ưu đãi / Khuyến mãi</span></label>
                        <input class="rp-input rp-input--sm" type="text" id="discountTag" name="discountTag"
                               placeholder="VD: Giảm 2.000.000₫, Tặng balo...">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="warranty"><span>Chính sách bảo hành</span></label>
                        <input class="rp-input rp-input--sm" type="text" id="warranty" name="warranty"
                               value="Bảo hành 24 tháng chính hãng">
                    </div>
                </div>

                <div class="rp-adminform__grid-3" style="margin-bottom: 14px;">
                    <div class="rp-field" style="grid-column: span 2;">
                        <label class="rp-label" for="imageUrl"><span>URL hình ảnh sản phẩm</span></label>
                        <input class="rp-input rp-input--sm" type="url" id="imageUrl" name="imageUrl"
                               placeholder="https://images.unsplash.com/... hoặc đường dẫn ảnh">
                    </div>
                    <div class="rp-field">
                        <label class="rp-label" for="deviceId"><span>Thiết bị phần cứng (Tùy chọn)</span></label>
                        <select class="rp-input rp-input--sm" id="deviceId" name="deviceId">
                            <option value="">-- Không liên kết --</option>
                            <c:forEach items="${devices}" var="dev">
                                <option value="${dev.id}"><c:out value="${dev.brand}"/> <c:out value="${dev.displayName}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <!-- Cấu hình phần cứng chi tiết -->
                <div style="background: var(--rp-gray-50); border: 1px solid var(--rp-gray-200); padding: 12px; margin-bottom: 14px;">
                    <div style="font-weight: 600; font-size: 13px; margin-bottom: 10px; color: var(--rp-black);">
                        ⚙️ Thông số kỹ thuật chi tiết (Specifications):
                    </div>
                    <div class="rp-adminform__grid-3">
                        <div class="rp-field">
                            <label class="rp-label" for="cpu"><span>Vi xử lý CPU</span></label>
                            <input class="rp-input rp-input--sm" type="text" id="cpu" name="cpu" placeholder="Intel Core i7-1360P / Apple M2...">
                        </div>
                        <div class="rp-field">
                            <label class="rp-label" for="ramGb"><span>RAM (GB)</span></label>
                            <input class="rp-input rp-input--sm" type="number" id="ramGb" name="ramGb" min="4" max="128" placeholder="16">
                        </div>
                        <div class="rp-field">
                            <label class="rp-label" for="storageGb"><span>SSD / Ổ cứng (GB)</span></label>
                            <input class="rp-input rp-input--sm" type="number" id="storageGb" name="storageGb" min="128" max="4096" placeholder="512">
                        </div>
                        <div class="rp-field">
                            <label class="rp-label" for="screenSize"><span>Kích thước màn hình (inch)</span></label>
                            <input class="rp-input rp-input--sm" type="number" id="screenSize" name="screenSize" step="0.1" min="10" max="20" placeholder="14.0">
                        </div>
                        <div class="rp-field">
                            <label class="rp-label" for="screenDetail"><span>Độ phân giải / Tần số quét</span></label>
                            <input class="rp-input rp-input--sm" type="text" id="screenDetail" name="screenDetail" placeholder="3.5K OLED 60Hz / FHD IPS 144Hz...">
                        </div>
                        <div class="rp-field">
                            <label class="rp-label" for="gpu"><span>Card đồ họa GPU</span></label>
                            <input class="rp-input rp-input--sm" type="text" id="gpu" name="gpu" placeholder="Intel Iris Xe / RTX 4060 8GB...">
                        </div>
                    </div>
                </div>

                <div class="rp-field" style="margin-bottom: 14px;">
                    <label class="rp-label" for="description"><span>Mô tả sản phẩm</span></label>
                    <textarea class="rp-input rp-input--sm" id="description" name="description" rows="3"
                              placeholder="Thông tin giới thiệu nổi bật về thiết kế, tính năng, đối tượng người dùng..."></textarea>
                </div>

                <div class="rp-adminform__actions">
                    <button class="rp-btn rp-btn--primary" type="submit">
                        <span class="material-symbols-outlined" style="font-size:18px; vertical-align:middle;">publish</span>
                        ĐĂNG BÀI BÁN SẢN PHẨM
                    </button>
                </div>
            </form>
        </div>

        <!-- Bảng danh sách sản phẩm -->
        <div class="rp-table-wrap">
            <table class="rp-table">
                <thead>
                    <tr>
                        <th style="width: 50px;">Ảnh</th>
                        <th>Sản phẩm & Model</th>
                        <th>Hãng</th>
                        <th>Giá bán</th>
                        <th>Cấu hình tóm tắt</th>
                        <th>Nhu cầu</th>
                        <th>Tồn kho</th>
                        <th>Trạng thái</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty products}">
                            <tr><td colspan="9" style="text-align: center; color: #888; padding: 24px;">Chưa có sản phẩm máy mới nào trong catalog.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${products}" var="p">
                                <tr>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty p.imageUrl}">
                                                <img class="rp-prod-img" src="${p.imageUrl}" alt="${p.name}" onerror="this.src='https://placehold.co/100x100?text=Laptop';">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="rp-prod-img-ph">No IMG</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div style="font-weight: 700; color: var(--rp-black);">
                                            <c:out value="${p.name}" />
                                        </div>
                                        <small style="color: #666;">
                                            SKU: <span class="rp-table__mono"><c:out value="${p.sku}" /></span>
                                            <c:if test="${not empty p.series}"> · Series: <c:out value="${p.series}" /></c:if>
                                        </small>
                                    </td>
                                    <td>
                                        <strong><c:out value="${p.brand}" /></strong>
                                    </td>
                                    <td>
                                        <div style="font-weight: 700; color: #e65100;">
                                            <fmt:formatNumber value="${p.price}" pattern="#,###"/> ₫
                                        </div>
                                        <c:if test="${not empty p.originalPrice && p.originalPrice > p.price}">
                                            <small style="text-decoration: line-through; color: #999;">
                                                <fmt:formatNumber value="${p.originalPrice}" pattern="#,###"/> ₫
                                            </small>
                                        </c:if>
                                    </td>
                                    <td class="rp-table__muted" style="font-size: 12px;">
                                        <div><c:out value="${p.cpu}" /></div>
                                        <div>RAM <c:out value="${p.ramGb}" />GB · SSD <c:out value="${p.storageGb}" />GB</div>
                                    </td>
                                    <td>
                                        <span class="rp-badge-need"><c:out value="${p.targetNeed}" /></span>
                                    </td>
                                    <td class="rp-table__mono" style="font-weight: 600;">
                                        <c:out value="${p.stockQuantity}" />
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.status == 'AVAILABLE'}">
                                                <span class="rp-badge-avail">SẴN HÀNG</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="rp-badge-out">HẾT HÀNG</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="white-space: nowrap;">
                                        <a href="${pageContext.request.contextPath}/product/detail?id=${p.id}" target="_blank"
                                           class="rp-btn rp-btn--sm" style="background:#eee; color:#333; text-decoration:none; margin-right:4px;">
                                            Xem
                                        </a>

                                        <form action="${pageContext.request.contextPath}/admin/products" method="POST" style="display:inline;">
                                            <input type="hidden" name="action" value="toggleStatus">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <button class="rp-btn rp-btn--sm" type="submit" style="background:#f5f5f5; color:#555; margin-right:4px;" title="Chuyển trạng thái Còn/Hết">
                                                ⇄ Trạng thái
                                            </button>
                                        </form>

                                        <form action="${pageContext.request.contextPath}/admin/products" method="POST"
                                              style="display:inline;"
                                              onsubmit="return confirm('Bạn có chắc muốn xóa sản phẩm #${p.id} - ${p.name}?');">
                                            <input type="hidden" name="action" value="deleteProduct">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <button class="rp-btn rp-btn--danger rp-btn--sm" type="submit">Xóa</button>
                                        </form>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="9" style="padding-top:0;">
                                        <details class="rp-details">
                                            <summary>Chỉnh sửa nhanh sản phẩm #<c:out value="${p.id}" /> - <c:out value="${p.name}" /></summary>
                                            <div class="rp-details__body">
                                                <form action="${pageContext.request.contextPath}/admin/products" method="POST">
                                                    <input type="hidden" name="action" value="updateProduct">
                                                    <input type="hidden" name="id" value="${p.id}">
                                                    
                                                    <div class="rp-adminform__grid-3" style="margin-bottom: 10px;">
                                                        <div class="rp-field">
                                                            <label class="rp-label"><span>Tên sản phẩm</span></label>
                                                            <input class="rp-input rp-input--sm" type="text" name="name" value="<c:out value='${p.name}'/>" required>
                                                        </div>
                                                        <div class="rp-field">
                                                            <label class="rp-label"><span>Hãng</span></label>
                                                            <input class="rp-input rp-input--sm" type="text" name="brand" value="<c:out value='${p.brand}'/>" required>
                                                        </div>
                                                        <div class="rp-field">
                                                            <label class="rp-label"><span>Giá bán (₫)</span></label>
                                                            <input class="rp-input rp-input--sm" type="number" name="price" value="${p.price}" required>
                                                        </div>
                                                    </div>

                                                    <div class="rp-adminform__grid-3" style="margin-bottom: 10px;">
                                                        <div class="rp-field">
                                                            <label class="rp-label"><span>Tồn kho</span></label>
                                                            <input class="rp-input rp-input--sm" type="number" name="stockQuantity" value="${p.stockQuantity}">
                                                        </div>
                                                        <div class="rp-field">
                                                            <label class="rp-label"><span>Nhu cầu</span></label>
                                                            <select class="rp-input rp-input--sm" name="targetNeed">
                                                                <option value="OFFICE" ${p.targetNeed == 'OFFICE' ? 'selected' : ''}>OFFICE</option>
                                                                <option value="GAMING" ${p.targetNeed == 'GAMING' ? 'selected' : ''}>GAMING</option>
                                                                <option value="GRAPHICS" ${p.targetNeed == 'GRAPHICS' ? 'selected' : ''}>GRAPHICS</option>
                                                            </select>
                                                        </div>
                                                        <div class="rp-field">
                                                            <label class="rp-label"><span>Trạng thái</span></label>
                                                            <select class="rp-input rp-input--sm" name="status">
                                                                <option value="AVAILABLE" ${p.status == 'AVAILABLE' ? 'selected' : ''}>AVAILABLE (Sẵn hàng)</option>
                                                                <option value="OUT_OF_STOCK" ${p.status == 'OUT_OF_STOCK' ? 'selected' : ''}>OUT_OF_STOCK (Hết hàng)</option>
                                                                <option value="DISCONTINUED" ${p.status == 'DISCONTINUED' ? 'selected' : ''}>DISCONTINUED (Ngừng kinh doanh)</option>
                                                            </select>
                                                        </div>
                                                    </div>

                                                    <div class="rp-adminform__grid-3" style="margin-bottom: 10px;">
                                                        <div class="rp-field">
                                                            <label class="rp-label"><span>CPU</span></label>
                                                            <input class="rp-input rp-input--sm" type="text" name="cpu" value="<c:out value='${p.cpu}'/>">
                                                        </div>
                                                        <div class="rp-field">
                                                            <label class="rp-label"><span>RAM (GB)</span></label>
                                                            <input class="rp-input rp-input--sm" type="number" name="ramGb" value="${p.ramGb}">
                                                        </div>
                                                        <div class="rp-field">
                                                            <label class="rp-label"><span>SSD (GB)</span></label>
                                                            <input class="rp-input rp-input--sm" type="number" name="storageGb" value="${p.storageGb}">
                                                        </div>
                                                    </div>

                                                    <div class="rp-field" style="margin-bottom: 10px;">
                                                        <label class="rp-label"><span>Ảnh URL</span></label>
                                                        <input class="rp-input rp-input--sm" type="url" name="imageUrl" value="<c:out value='${p.imageUrl}'/>">
                                                    </div>

                                                    <div class="rp-adminform__actions">
                                                        <button class="rp-btn rp-btn--primary rp-btn--sm" type="submit">Lưu thay đổi</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </details>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

    </div>
</div>

</body>
</html>
