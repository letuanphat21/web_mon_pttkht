package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.webquanao.entity.Order;
import org.example.webquanao.entity.OrderDetail;
import org.example.webquanao.service.OrderService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/order-history")
public class HistoryController extends HttpServlet {
    private OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Object userIdObj = session.getAttribute("userId");

        if (userIdObj == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = Integer.parseInt(userIdObj.toString());

        String action = request.getParameter("action");
        String orderId = request.getParameter("id");

        if ("detail".equals(action) && orderId != null) {
            Order order = orderService.getOrderById(orderId);
            List<OrderDetail> details = orderService.getOrderDetails(orderId);


            if (order != null && order.getUserId() == userId) {
                request.setAttribute("order", order);
                request.setAttribute("details", details);
                request.getRequestDispatcher("/WEB-INF/order-detail.jsp").forward(request, response);
            } else {
                System.out.println("CẢNH BÁO: Sai UserId hoặc Order Null -> Trả về 403");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Object userIdObj = session.getAttribute("userId");
        String action = request.getParameter("action");
        String orderId = request.getParameter("orderId");

        PrintWriter out = response.getWriter();

        if (userIdObj == null) {
            out.write("{\"status\": \"error\", \"message\": \"Phiên làm việc hết hạn.\"}");
            return;
        }

        int userId = Integer.parseInt(userIdObj.toString());

        if ("cancel".equals(action) && orderId != null) {
            String result = orderService.cancelOrder(orderId, userId);

            if ("SUCCESS".equals(result)) {
                out.write("{\"status\": \"success\"}");
            } else {
                out.write("{\"status\": \"error\", \"message\": \"" + result + "\"}");
            }
        } else {
            out.write("{\"status\": \"error\", \"message\": \"Yêu cầu không hợp lệ.\"}");
        }

        out.flush(); // Đẩy dữ liệu đi và kết thúc response tại đây
    }
}