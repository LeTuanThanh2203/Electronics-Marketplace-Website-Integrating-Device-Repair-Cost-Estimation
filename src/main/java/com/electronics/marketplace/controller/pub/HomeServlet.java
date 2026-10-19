package com.electronics.marketplace.controller.pub;

import com.electronics.marketplace.entity.Listing;
import com.electronics.marketplace.entity.Product;
import com.electronics.marketplace.service.MarketplaceService;
import com.electronics.marketplace.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final MarketplaceService marketplaceService = new MarketplaceService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Product> products = productService.getAllProducts();
            if (products != null && products.size() > 4) {
                products = products.subList(0, 4);
            }
            request.setAttribute("featuredProducts", products);

            List<Listing> listings = marketplaceService.getAvailableMarketplaceListings(null, null, null, null, null);
            if (listings != null && listings.size() > 4) {
                listings = listings.subList(0, 4);
            }
            request.setAttribute("featuredListings", listings);
        } catch (Exception e) {
            // Keep going if DB is not ready, JSP will fallback to default preview cards
        }

        // Forward the request to the home.jsp view
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}

