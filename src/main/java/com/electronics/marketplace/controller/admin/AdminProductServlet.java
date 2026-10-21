package com.electronics.marketplace.controller.admin;

import com.electronics.marketplace.entity.Device;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.entity.ProductSpecification;
import com.electronics.marketplace.entity.User;
import com.electronics.marketplace.entity.enums.ProductStatus;
import com.electronics.marketplace.filter.AuthenticationFilter;
import com.electronics.marketplace.service.ProductService;
import com.electronics.marketplace.service.RepairService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý quản trị và đăng sản phẩm máy mới (B2C Catalog).
 * Thuộc phân hệ của Phước Anh.
 */
@WebServlet(name = "AdminProductServlet", urlPatterns = {
        "/admin/products",
        "/admin/product/new",
        "/admin/product/edit",
        "/admin/product/delete",
        "/admin/product/toggle-status"
})
public class AdminProductServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final RepairService repairService = new RepairService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = checkAdmin(req, resp);
        if (user == null) return;

        List<Product> products = productService.getAllProducts();
        List<Device> devices = repairService.getAllDevices();

        req.setAttribute("products", products);
        req.setAttribute("devices", devices);
        req.setAttribute("totalProducts", products.size());

        req.getRequestDispatcher("/WEB-INF/views/admin/product_list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = checkAdmin(req, resp);
        if (user == null) return;

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "addProduct";
        }

        try {
            switch (action) {
                case "addProduct":
                    handleAddProduct(req, resp, user);
                    break;
                case "updateProduct":
                    handleUpdateProduct(req, resp);
                    break;
                case "deleteProduct":
                    handleDeleteProduct(req, resp);
                    break;
                case "toggleStatus":
                    handleToggleStatus(req, resp);
                    break;
                default:
                    resp.sendRedirect(req.getContextPath() + "/admin/products?error=" +
                            URLEncoder.encode("Thao tác không hợp lệ.", StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log("Lỗi trong AdminProductServlet", e);
            resp.sendRedirect(req.getContextPath() + "/admin/products?error=" +
                    URLEncoder.encode("Lỗi: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private void handleAddProduct(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String name = trimToNull(req.getParameter("name"));
        String brand = trimToNull(req.getParameter("brand"));
        String priceStr = trimToNull(req.getParameter("price"));

        if (name == null || brand == null || priceStr == null) {
            redirectWithError(req, resp, "Tên sản phẩm, hãng và giá bán không được để trống.");
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceStr.replaceAll("[^0-9.]", ""));
        } catch (Exception ex) {
            redirectWithError(req, resp, "Giá bán không hợp lệ.");
            return;
        }

        Product p = new Product();
        p.setAdminId(user.getId());
        p.setName(name);
        p.setBrand(brand);
        p.setPrice(price);
        p.setSeries(trimToNull(req.getParameter("series")));

        String origPriceStr = trimToNull(req.getParameter("originalPrice"));
        if (origPriceStr != null) {
            try {
                p.setOriginalPrice(new BigDecimal(origPriceStr.replaceAll("[^0-9.]", "")));
            } catch (Exception ignored) {}
        }

        String stockStr = trimToNull(req.getParameter("stockQuantity"));
        if (stockStr != null) {
            try {
                p.setStockQuantity(Integer.parseInt(stockStr));
            } catch (Exception ignored) {
                p.setStockQuantity(10);
            }
        } else {
            p.setStockQuantity(10);
        }

        String sku = trimToNull(req.getParameter("sku"));
        if (sku == null) {
            sku = (brand.substring(0, Math.min(3, brand.length())).toUpperCase()) + "-" + System.currentTimeMillis() % 100000;
        }
        p.setSku(sku);

        p.setImageUrl(trimToNull(req.getParameter("imageUrl")));
        p.setWarranty(trimToDefault(req.getParameter("warranty"), "Bảo hành 24 tháng chính hãng"));
        p.setTargetNeed(trimToDefault(req.getParameter("targetNeed"), "OFFICE"));
        p.setBadge(trimToDefault(req.getParameter("badge"), "SEAL 100%"));
        p.setDiscountTag(trimToNull(req.getParameter("discountTag")));
        p.setDescription(trimToNull(req.getParameter("description")));
        p.setStatus(ProductStatus.AVAILABLE);

        // Liên kết thiết bị phần cứng nếu có chọn
        String deviceIdStr = trimToNull(req.getParameter("deviceId"));
        if (deviceIdStr != null) {
            try {
                Long deviceId = Long.parseLong(deviceIdStr);
                Optional<Device> devOpt = repairService.getDeviceById(deviceId);
                devOpt.ifPresent(p::setDevice);
            } catch (Exception ignored) {}
        }

        // Bổ sung các thông số kỹ thuật (Specifications)
        addOrUpdateSpec(p, "CPU", trimToNull(req.getParameter("cpu")), null);
        addOrUpdateNumericSpec(p, "RAM", req.getParameter("ramGb"), "GB");
        addOrUpdateNumericSpec(p, "STORAGE", req.getParameter("storageGb"), "GB");
        addOrUpdateSpec(p, "GPU", trimToNull(req.getParameter("gpu")), null);
        addOrUpdateSpec(p, "SCREEN_DETAIL", trimToNull(req.getParameter("screenDetail")), null);

        String screenSizeStr = trimToNull(req.getParameter("screenSize"));
        if (screenSizeStr != null) {
            try {
                double val = Double.parseDouble(screenSizeStr);
                p.addSpecification(ProductSpecification.numberSpec("SCREEN_SIZE", (int) Math.round(val), "inch"));
            } catch (Exception ignored) {}
        }

        productService.saveProduct(p);

        redirectWithMessage(req, resp, "Đã đăng sản phẩm '" + p.getName() + "' thành công vào danh mục máy mới!");
    }

    private void handleUpdateProduct(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = trimToNull(req.getParameter("id"));
        if (idStr == null) {
            redirectWithError(req, resp, "Mã sản phẩm không hợp lệ.");
            return;
        }

        Long id;
        try {
            id = Long.parseLong(idStr);
        } catch (Exception e) {
            redirectWithError(req, resp, "Mã sản phẩm không đúng định dạng.");
            return;
        }

        Optional<Product> opt = productService.getProductById(id);
        if (!opt.isPresent()) {
            redirectWithError(req, resp, "Không tìm thấy sản phẩm có ID: " + id);
            return;
        }

        Product p = opt.get();
        String name = trimToNull(req.getParameter("name"));
        String brand = trimToNull(req.getParameter("brand"));
        String priceStr = trimToNull(req.getParameter("price"));

        if (name != null) p.setName(name);
        if (brand != null) p.setBrand(brand);
        if (priceStr != null) {
            try {
                p.setPrice(new BigDecimal(priceStr.replaceAll("[^0-9.]", "")));
            } catch (Exception ignored) {}
        }

        p.setSeries(trimToNull(req.getParameter("series")));

        String origPriceStr = trimToNull(req.getParameter("originalPrice"));
        if (origPriceStr != null) {
            try {
                p.setOriginalPrice(new BigDecimal(origPriceStr.replaceAll("[^0-9.]", "")));
            } catch (Exception ignored) {}
        }

        String stockStr = trimToNull(req.getParameter("stockQuantity"));
        if (stockStr != null) {
            try {
                p.setStockQuantity(Integer.parseInt(stockStr));
            } catch (Exception ignored) {}
        }

        String sku = trimToNull(req.getParameter("sku"));
        if (sku != null) p.setSku(sku);

        p.setImageUrl(trimToNull(req.getParameter("imageUrl")));
        p.setWarranty(trimToDefault(req.getParameter("warranty"), p.getWarranty()));
        p.setTargetNeed(trimToDefault(req.getParameter("targetNeed"), p.getTargetNeed()));
        p.setBadge(trimToDefault(req.getParameter("badge"), p.getBadge()));
        p.setDiscountTag(trimToNull(req.getParameter("discountTag")));
        p.setDescription(trimToNull(req.getParameter("description")));

        String statusStr = trimToNull(req.getParameter("status"));
        if (statusStr != null) {
            try {
                p.setStatus(ProductStatus.valueOf(statusStr));
            } catch (Exception ignored) {}
        }

        // Cập nhật thông số kỹ thuật
        addOrUpdateSpec(p, "CPU", trimToNull(req.getParameter("cpu")), null);
        addOrUpdateNumericSpec(p, "RAM", req.getParameter("ramGb"), "GB");
        addOrUpdateNumericSpec(p, "STORAGE", req.getParameter("storageGb"), "GB");
        addOrUpdateSpec(p, "GPU", trimToNull(req.getParameter("gpu")), null);
        addOrUpdateSpec(p, "SCREEN_DETAIL", trimToNull(req.getParameter("screenDetail")), null);

        productService.saveProduct(p);

        redirectWithMessage(req, resp, "Đã cập nhật sản phẩm #" + p.getId() + " thành công!");
    }

    private void handleDeleteProduct(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = trimToNull(req.getParameter("id"));
        if (idStr != null) {
            try {
                Long id = Long.parseLong(idStr);
                productService.deleteProduct(id);
                redirectWithMessage(req, resp, "Đã xóa sản phẩm #" + id + " thành công.");
                return;
            } catch (Exception e) {
                redirectWithError(req, resp, "Không thể xóa sản phẩm: " + e.getMessage());
                return;
            }
        }
        redirectWithError(req, resp, "Thiếu mã sản phẩm.");
    }

    private void handleToggleStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = trimToNull(req.getParameter("id"));
        if (idStr != null) {
            try {
                Long id = Long.parseLong(idStr);
                Optional<Product> opt = productService.getProductById(id);
                if (opt.isPresent()) {
                    Product p = opt.get();
                    if (p.getStatus() == ProductStatus.AVAILABLE) {
                        p.setStatus(ProductStatus.OUT_OF_STOCK);
                    } else {
                        p.setStatus(ProductStatus.AVAILABLE);
                    }
                    productService.saveProduct(p);
                    redirectWithMessage(req, resp, "Đã đổi trạng thái sản phẩm #" + id + " thành " + p.getStatus());
                    return;
                }
            } catch (Exception e) {
                redirectWithError(req, resp, "Không thể đổi trạng thái: " + e.getMessage());
                return;
            }
        }
        redirectWithError(req, resp, "Thiếu mã sản phẩm.");
    }

    private void addOrUpdateSpec(Product p, String key, String textVal, String unit) {
        if (textVal == null || textVal.trim().isEmpty()) return;
        ProductSpecification spec = p.getSpecification(key);
        if (spec != null) {
            spec.setTextValue(textVal.trim());
            if (unit != null) spec.setUnit(unit);
        } else {
            p.addSpecification(ProductSpecification.textSpec(key, textVal.trim()));
        }
    }

    private void addOrUpdateNumericSpec(Product p, String key, String numStr, String unit) {
        if (numStr == null || numStr.trim().isEmpty()) return;
        try {
            int val = Integer.parseInt(numStr.replaceAll("[^0-9]", ""));
            ProductSpecification spec = p.getSpecification(key);
            if (spec != null) {
                spec.setNumericValue(val);
                spec.setTextValue(String.valueOf(val));
                if (unit != null) spec.setUnit(unit);
            } else {
                p.addSpecification(ProductSpecification.numberSpec(key, val, unit));
            }
        } catch (Exception ignored) {}
    }

    private User checkAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute(AuthenticationFilter.SESSION_KEY);
        if (user == null || !user.isAdmin()) {
            resp.sendRedirect(req.getContextPath() + "/login?error=" +
                    URLEncoder.encode("Vui lòng đăng nhập bằng tài khoản Quản trị viên (Admin) để thao tác.", StandardCharsets.UTF_8));
            return null;
        }
        return user;
    }

    private void redirectWithMessage(HttpServletRequest req, HttpServletResponse resp, String msg) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/products?message=" +
                URLEncoder.encode(msg, StandardCharsets.UTF_8));
    }

    private void redirectWithError(HttpServletRequest req, HttpServletResponse resp, String error) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/products?errorMessage=" +
                URLEncoder.encode(error, StandardCharsets.UTF_8));
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToDefault(String s, String def) {
        String val = trimToNull(s);
        return val != null ? val : def;
    }
}
