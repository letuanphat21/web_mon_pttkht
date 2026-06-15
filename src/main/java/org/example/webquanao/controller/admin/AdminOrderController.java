package org.example.webquanao.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.webquanao.dto.request.AdminUpdateStatusRequest;
import org.example.webquanao.service.OrderService;

import java.io.IOException;

@WebServlet("/admin/orders")
public class AdminOrderController extends HttpServlet {
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        String orderId = request.getParameter("orderId");

        if ("detail".equals(action)) {
            var orderDetail = orderService.getOrderDetailsForAdmin(orderId);
            request.setAttribute("orderDetail", orderDetail);
            request.getRequestDispatcher("/WEB-INF/admin/order-detail.jsp").forward(request, response);
        }

        else {
            var orders = orderService.getAllOrdersForAdmin();
            System.out.println("Số lượng đơn hàng lấy được: " + (orders != null ? orders.size() : "null"));
            request.setAttribute("orders", orders);
            request.getRequestDispatcher("/WEB-INF/admin/order-list.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        String orderId = request.getParameter("orderId");

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        String result = "ERROR_UNKNOWN";

        if ("update-status".equals(action)) {
            String newStatus = request.getParameter("newStatus");
            AdminUpdateStatusRequest updateReq = new AdminUpdateStatusRequest(orderId, newStatus, null);
            result = orderService.updateOrderStatusAdmin(updateReq);
        }
        else if ("cancel-order".equals(action)) {
            String reason = request.getParameter("reason");
            result = orderService.processCancelOrderForAdmin(orderId, reason);
        }

        response.getWriter().write(result);
    }

    private boolean isAdmin(HttpServletRequest request) {
        Object roleId = request.getSession().getAttribute("roleId");
        return roleId != null && roleId.equals(2);
    }
}