package org.example.webquanao.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.dao.ProductDAO;
import org.example.webquanao.entity.Product;

import java.io.IOException;
import java.util.List;

@WebServlet("/shop")
public class ShopController extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Product> products = productDAO.findAllActive();
            request.setAttribute("products", products);
            request.setAttribute("msg", "Danh sách sản phẩm mới nhất");
            request.getRequestDispatcher("/WEB-INF/home.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<h3>Hệ thống đang gặp sự cố khi tải sản phẩm!</h3>");
            response.getWriter().println("<p>Lỗi: " + e.getMessage() + "</p>");
        }
    }
}