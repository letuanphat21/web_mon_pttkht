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

        // LUỒNG 5: Trả về HTML Fragment
        if ("detail".equals(action) && orderId != null) {
            var orderDetail = orderService.getOrderDetailsForHistory(orderId);

            if (orderDetail != null && orderDetail.getUserId() == userId) {
                request.setAttribute("orderDetail", orderDetail);
                request.getRequestDispatcher("/WEB-INF/order-detail.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        // LUỒNG 1-4: Hiển thị danh sách đơn hàng
        else {
            var orders = orderService.getOrderHistoryList(userId);
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
        String reason = request.getParameter("reason");

        if (userIdObj == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = Integer.parseInt(userIdObj.toString());

        // LUỒNG 6: Hủy đơn hàng
        if ("confirm-cancel".equals(action) && orderId != null) {
            String result = orderService.processCancelOrder(orderId, userId, reason);

            PrintWriter out = response.getWriter();
            if ("SUCCESS".equals(result)) {
                out.write("{\"status\": \"success\", \"msg\": \"Hủy đơn hàng thành công\"}");
            } else {
                out.write("{\"status\": \"error\", \"msg\": \"" + result + "\"}");
            }
        }
    }
}