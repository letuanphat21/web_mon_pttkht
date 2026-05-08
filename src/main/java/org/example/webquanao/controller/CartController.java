package org.example.webquanao.controller;

import org.example.webquanao.entity.CartItem;
import org.example.webquanao.entity.Product;
import org.example.webquanao.service.CartService;
import org.example.webquanao.dao.ProductDAO;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/add-to-cart")
public class CartController extends HttpServlet {
    private CartService cartService = new CartService();
    private ProductDAO productDAO = new ProductDAO();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // 1 & 2. Lấy thông tin từ ProductPage (productId và quantity)
            int pId = Integer.parseInt(request.getParameter("productId"));
            int qty = Integer.parseInt(request.getParameter("quantity"));

            // Pre-Condition: Sản phẩm phải còn tồn tại trong hệ thống
            Product p = productDAO.findById(pId);

            if (p != null) {

                // 4. Cập nhật thông tin giỏ hàng tạm thời vào Session (Post-condition)
                HttpSession session = request.getSession();
                Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
                if (cart == null) cart = new HashMap<>();

                // Gọi service xử lý logic 4a (cộng dồn) hoặc 4b (tạo mới)
                cartService.addToCart(cart, p, qty);
                session.setAttribute("cart", cart);

                // NFR1.27-2: Tính toán tổng số loại sản phẩm trong giỏ để cập nhật Header
                int totalQty = cart.size();
                response.getWriter().print("{\"status\":\"success\", \"totalQty\":" + totalQty + "}");
            } else {
                response.setStatus(400);
                response.getWriter().print("{\"status\":\"error\", \"message\":\"Sản phẩm không tồn tại\"}");
            }
        } catch (Exception e) {

            // Exception Flow E: Lỗi hệ thống hoặc kết nối
            response.setStatus(500);
            response.getWriter().print("{\"status\":\"error\", \"message\":\"Lỗi hệ thống\"}");
        }
    }
}