package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.webquanao.entity.Order;
import org.example.webquanao.service.OrderService;

import java.io.IOException;
import java.util.List;

@WebServlet("/order-history")
public class HistoryController extends HttpServlet {
    private OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        String orderId = request.getParameter("id");

        if ("detail".equals(action) && orderId != null) {
            Order order = orderService.getOrderById(orderId);

            // NFR1.29-1.  So sánh id chủ đơn hàng với id trong session
            if (order != null && order.getUserId() == userId) {
                request.setAttribute("order", order);
                request.setAttribute("details", orderService.getOrderDetails(orderId));
                request.getRequestDispatcher("/WEB-INF/order-detail.jsp").forward(request, response);
            } else {
                response.sendRedirect("order-history");
            }
        } else {
            List<Order> orders = orderService.getUserOrderHistory(userId);
            request.setAttribute("orders", orders);
            request.getRequestDispatcher("/WEB-INF/order-history.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");
        String orderId = request.getParameter("orderId");

        if ("cancel".equals(action) && userId != null && orderId != null) {
            String result = orderService.cancelOrder(orderId, userId);
            if ("SUCCESS".equals(result)) {
                session.setAttribute("message", "Hủy đơn hàng " + orderId + " thành công!");
            } else {
                session.setAttribute("error", result);
            }
        } else {
            session.setAttribute("error", "Không thể xác định danh tính hoặc đơn hàng.");
        }
        response.sendRedirect(request.getContextPath() + "/order-history");
    }
}
