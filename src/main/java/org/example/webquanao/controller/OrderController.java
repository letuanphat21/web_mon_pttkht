package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.webquanao.entity.Order;
import org.example.webquanao.entity.User;
import org.example.webquanao.entity.CartItem;
import org.example.webquanao.service.OrderService;
import org.example.webquanao.service.CartService;
import java.io.IOException;
import java.util.Map;

@WebServlet("/order-process")
public class OrderController extends HttpServlet {
    private OrderService orderService = new OrderService();
    private CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if ("checkout".equals(action)) {
            prepareCheckout(request, response);
        } else if ("review".equals(action)) {
            showReviewPage(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/shop");
        }
    }

    private void prepareCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            request.setAttribute("errorMessage", "Giỏ hàng của bạn đang trống!");
            request.getRequestDispatcher("/WEB-INF/home.jsp").forward(request, response);
            return;
        }

        User user = (User) session.getAttribute("auth");
        if (user != null) {
            request.setAttribute("defaultName", user.getFullName());
            request.setAttribute("defaultPhone", user.getPhone());
            request.setAttribute("defaultAddress", user.getAddress());
        }

        request.getRequestDispatcher("/WEB-INF/order-info.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        User user = (User) session.getAttribute("auth");

        // 7. Lấy dữ liệu từ Form nhập thông tin giao hàng
        String name = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        // 8, 9. Kiểm tra thông tin hợp lệ
        String validation = orderService.validateShippingInfo(phone, address);

        if (!validation.equals("VALID")) {
            request.setAttribute("errorDetail", validation);
            request.setAttribute("inputName", name);
            request.setAttribute("inputPhone", phone);
            request.setAttribute("inputAddress", address);
            request.getRequestDispatcher("/WEB-INF/order-info.jsp").forward(request, response);
            return;
        }

        Order pendingOrder = new Order();
        pendingOrder.setUserId(user != null ? user.getId() : 0);
        pendingOrder.setFullName(name);
        pendingOrder.setPhone(phone);
        pendingOrder.setAddress(address);
        pendingOrder.setTotalPrice(cartService.calculateTotalCart(cart));

        session.setAttribute("pendingOrder", pendingOrder);
        response.sendRedirect(request.getContextPath() + "/order-process?action=review");
    }

    private void showReviewPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        if (session.getAttribute("pendingOrder") == null) {
            response.sendRedirect(request.getContextPath() + "/order-process?action=checkout");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/order-review.jsp").forward(request, response);
    }
}