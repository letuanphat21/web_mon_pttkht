package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import org.example.webquanao.entity.CartItem;
import org.example.webquanao.service.CartService;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/cart")
public class ManageCartController extends HttpServlet {
    private CartService cartService = new CartService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");

        double totalAmount = cartService.calculateTotalCart(cart);
        request.setAttribute("totalAmount", totalAmount);
        request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            HttpSession session = request.getSession();
            Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");

            if (cart != null) {
                cartService.removeFromCart(cart, productId);
                session.setAttribute("cart", cart);
            }

            double cartTotal = cartService.calculateTotalCart(cart);
            boolean isEmpty = (cart == null || cart.isEmpty());

            response.setContentType("application/json");
            String json = String.format(java.util.Locale.US,
                    "{\"status\":\"success\", \"cartTotal\":%.2f, \"isEmpty\":%b}",
                    cartTotal, isEmpty
            );
            response.getWriter().print(json);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            HttpSession session = request.getSession();
            Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");

            if (cart != null && "update".equals(action)) {
                cartService.updateQuantity(cart, productId, quantity);
                session.setAttribute("cart", cart);

                double itemTotal = cart.containsKey(productId) ? cart.get(productId).getTotalAmount() : 0;
                double cartTotal = cartService.calculateTotalCart(cart);

                String json = String.format(java.util.Locale.US,
                        "{\"status\":\"success\", \"itemTotal\":%.0f, \"cartTotal\":%.0f, \"isEmpty\":%b}",
                        itemTotal, cartTotal, cart.isEmpty()
                );

                response.getWriter().print(json);
                response.getWriter().flush();
            }
        } catch (Exception e) {
            response.setStatus(500);
            e.printStackTrace();
            response.getWriter().print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}