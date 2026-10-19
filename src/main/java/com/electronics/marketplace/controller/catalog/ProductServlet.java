package com.electronics.marketplace.controller.catalog;

import com.electronics.marketplace.dto.RecommendationRequest;
import com.electronics.marketplace.dto.RecommendedProduct;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller handling product catalog, filtering, detail inspection, and recommendation engine.
 * URL patterns: /products, /product
 * Follows Murach Controller architecture and Rule 1 (Action routing) & Rule 4 (AJAX/JSON support).
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/products", "/product"})
public class ProductServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ProductService productService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        this.productService = new ProductService();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "list";
        }

        String forwardUrl = null;

        switch (action.toLowerCase()) {
            case "detail":
                forwardUrl = handleDetail(request, response);
                break;
            case "compare":
                forwardUrl = handleCompare(request, response);
                break;
            case "recommend":
                forwardUrl = handleRecommend(request, response);
                break;
            case "search":
                forwardUrl = handleSearch(request, response);
                break;
            case "list":
            default:
                forwardUrl = handleList(request, response);
                break;
        }

        if (forwardUrl != null) {
            request.getRequestDispatcher(forwardUrl).forward(request, response);
        }
    }

    /**
     * Action: list (Displays and filters catalog items)
     */
    private String handleList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String[] brandsParam = request.getParameterValues("brand");
        List<String> brands = (brandsParam != null) ? Arrays.asList(brandsParam) : Collections.emptyList();

        String minPriceStr = request.getParameter("minPrice");
        String maxPriceStr = request.getParameter("maxPrice");
        String minRamStr = request.getParameter("minRam");
        String targetNeed = request.getParameter("need");
        String sortBy = request.getParameter("sort");

        BigDecimal minPrice = parseBigDecimal(minPriceStr);
        BigDecimal maxPrice = parseBigDecimal(maxPriceStr);
        Integer minRam = parseInteger(minRamStr);

        List<Product> products = productService.filterProducts(brands, minPrice, maxPrice, minRam, targetNeed, sortBy);

        // Retain filter state for JSP form inputs
        request.setAttribute("products", products);
        request.setAttribute("selectedBrands", brands);
        request.setAttribute("minPrice", minPriceStr);
        request.setAttribute("maxPrice", maxPriceStr);
        request.setAttribute("minRam", minRamStr);
        request.setAttribute("selectedNeed", targetNeed);
        request.setAttribute("selectedSort", sortBy);
        request.setAttribute("totalResults", products.size());

        if (isJsonRequest(request)) {
            sendJsonResponse(response, products);
            return null; // Response already handled
        }

        return "/WEB-INF/views/product/list.jsp";
    }

    /**
     * Action: detail (Views single product specs and matrix)
     */
    private String handleDetail(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        Long id = parseLong(idStr);

        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/products");
            return null;
        }

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            request.setAttribute("errorMessage", "Không tìm thấy sản phẩm yêu cầu.");
            return "/WEB-INF/views/product/list.jsp";
        }

        Product product = productOpt.get();
        request.setAttribute("product", product);
        request.setAttribute("specifications", product.getSpecifications());

        if (isJsonRequest(request)) {
            sendJsonResponse(response, product);
            return null;
        }

        return "/WEB-INF/views/product/detail.jsp";
    }

    /**
     * Action: compare (Compares 2 or more products by ID)
     */
    private String handleCompare(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idsStr = request.getParameter("ids");
        List<Long> ids = new ArrayList<>();

        if (idsStr != null && !idsStr.trim().isEmpty()) {
            for (String part : idsStr.split(",")) {
                Long id = parseLong(part.trim());
                if (id != null) ids.add(id);
            }
        }

        List<Product> compareList = productService.compareProducts(ids);
        request.setAttribute("compareProducts", compareList);

        if (isJsonRequest(request)) {
            sendJsonResponse(response, compareList);
            return null;
        }

        return "/WEB-INF/views/product/compare.jsp";
    }

    /**
     * Action: recommend (Calculates matching score based on user budget and needs)
     */
    private String handleRecommend(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String budgetStr = request.getParameter("budget");
        String need = request.getParameter("need");
        String brand = request.getParameter("brand");
        String minRamStr = request.getParameter("minRam");

        BigDecimal budget = parseBigDecimal(budgetStr);
        Integer minRam = parseInteger(minRamStr);

        RecommendationRequest recRequest = new RecommendationRequest(budget, need, minRam, brand);
        List<RecommendedProduct> recommendations = productService.recommend(recRequest);

        request.setAttribute("recommendations", recommendations);
        request.setAttribute("recBudget", budgetStr);
        request.setAttribute("recNeed", need);
        request.setAttribute("recBrand", brand);
        request.setAttribute("recMinRam", minRamStr);

        if (isJsonRequest(request)) {
            sendJsonResponse(response, recommendations);
            return null;
        }

        return "/WEB-INF/views/product/list.jsp";
    }

    /**
     * Action: search (Simple keyword search)
     */
    private String handleSearch(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String keyword = request.getParameter("q");
        List<Product> products = (keyword != null && !keyword.trim().isEmpty())
                ? productService.filterProducts(null, null, null, null, null, null)
                    .stream()
                    .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase().trim()) ||
                                 p.getBrand().toLowerCase().contains(keyword.toLowerCase().trim()))
                    .collect(Collectors.toList())
                : productService.getAllProducts();

        request.setAttribute("products", products);
        request.setAttribute("searchKeyword", keyword);
        request.setAttribute("totalResults", products.size());

        if (isJsonRequest(request)) {
            sendJsonResponse(response, products);
            return null;
        }

        return "/WEB-INF/views/product/list.jsp";
    }

    // --- Helpers ---

    private boolean isJsonRequest(HttpServletRequest request) {
        String format = request.getParameter("format");
        if ("json".equalsIgnoreCase(format)) return true;

        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) return true;

        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(data));
        out.flush();
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try {
            // Remove dots and commas (e.g. 15.000.000 or 15,000,000)
            String clean = val.replaceAll("[^0-9.]", "").trim();
            if (clean.isEmpty()) return null;
            return new BigDecimal(clean);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(val.replaceAll("[^0-9]", "").trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try {
            return Long.parseLong(val.replaceAll("[^0-9]", "").trim());
        } catch (Exception e) {
            return null;
        }
    }
}
