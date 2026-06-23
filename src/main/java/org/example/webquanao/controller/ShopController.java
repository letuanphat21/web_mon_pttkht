package org.example.webquanao.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.dao.CategoryDAO;
import org.example.webquanao.dao.ProductDAO;
import org.example.webquanao.entity.Category;
import org.example.webquanao.entity.Product;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/shop")
public class ShopController extends HttpServlet {
    private final ProductDAO productDAO   = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // --- Đọc params từ URL ---
            String keyword    = request.getParameter("keyword");    // UC-1.32: tìm kiếm
            String catParam   = request.getParameter("categoryId"); // UC-1.33: lọc danh mục
            String minParam   = request.getParameter("minPrice");   // UC-1.33: lọc giá
            String maxParam   = request.getParameter("maxPrice");

            int    categoryId = parseSafe(catParam, 0);
            double minPrice   = parseSafe(minParam, 0.0);
            double maxPrice   = parseSafe(maxParam, Double.MAX_VALUE);
            if (maxPrice <= 0) maxPrice = Double.MAX_VALUE;

            // --- Lấy tất cả sản phẩm đang active ---
            List<Product> products = productDAO.findAllActive();

            // --- UC-1.32: Lọc theo keyword (tên, thương hiệu) ---
            if (keyword != null && !keyword.trim().isEmpty()) {
                final String kw = keyword.trim().toLowerCase();
                products = products.stream()
                        .filter(p -> p.getProductName().toLowerCase().contains(kw)
                                || (p.getProductBrand() != null && p.getProductBrand().toLowerCase().contains(kw)))
                        .collect(Collectors.toList());
            }

            // --- UC-1.33: Lọc theo danh mục ---
            if (categoryId > 0) {
                final int cid = categoryId;
                products = products.stream()
                        .filter(p -> p.getCategoryId() == cid)
                        .collect(Collectors.toList());
            }

            // --- UC-1.33: Lọc theo khoảng giá ---
            final double finalMin = minPrice;
            final double finalMax = maxPrice;
            products = products.stream()
                    .filter(p -> p.getProductPrice() >= finalMin && p.getProductPrice() <= finalMax)
                    .collect(Collectors.toList());

            // --- Tính maxPrice thực tế trong DB để hiển thị slider ---
            List<Product> allActive = productDAO.findAllActive();
            double maxPriceInDb = allActive.stream()
                    .mapToDouble(Product::getProductPrice)
                    .max().orElse(10_000_000);

            // --- Lấy danh sách category cho sidebar filter ---
            List<Category> categories = categoryDAO.findAllActive();

            request.setAttribute("products",      products);
            request.setAttribute("categories",    categories);
            request.setAttribute("maxPriceInDb",  (long) maxPriceInDb);
            // Giữ lại giá trị filter để hiển thị lại trên form
            request.setAttribute("keyword",       keyword   != null ? keyword   : "");
            request.setAttribute("selectedCat",   categoryId);
            request.setAttribute("selectedMin",   (long) (minPrice  <= 0              ? 0            : minPrice));
            request.setAttribute("selectedMax",   (long) (maxPrice  == Double.MAX_VALUE ? maxPriceInDb : maxPrice));

            request.getRequestDispatcher("/WEB-INF/shop.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<h3>Lỗi tải trang sản phẩm: " + e.getMessage() + "</h3>");
        }
    }

    // --- Helper parse an toàn ---
    private int parseSafe(String val, int def) {
        try { return (val != null && !val.isEmpty()) ? Integer.parseInt(val.trim()) : def; }
        catch (NumberFormatException e) { return def; }
    }
    private double parseSafe(String val, double def) {
        try { return (val != null && !val.isEmpty()) ? Double.parseDouble(val.trim()) : def; }
        catch (NumberFormatException e) { return def; }
    }
}