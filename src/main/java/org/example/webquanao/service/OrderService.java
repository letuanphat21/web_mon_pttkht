package org.example.webquanao.service;

import org.example.webquanao.dao.OrderDAO;
import org.example.webquanao.entity.Order;
import org.example.webquanao.entity.OrderDetail;

import java.util.List;
import java.util.UUID;

public class OrderService {
    private OrderDAO orderDAO = new OrderDAO();

    public String validateShippingInfo(String phone, String address) {
        if (phone == null || !phone.matches("^0\\d{9}$")) {
            return "Số điện thoại không hợp lệ (Phải bắt đầu bằng 0 và gồm 10 chữ số).";
        }
        if (address == null || address.trim().isEmpty()) {
            return "Địa chỉ giao hàng không được để trống.";
        }
        return "VALID";
    }

    public boolean processCheckout(Order order, List<OrderDetail> details) {
        String uniqueOrderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        order.setOrderId(uniqueOrderId);
        order.setStatus("Chờ xác nhận");
        for (OrderDetail item : details) {
            item.setOrderId(uniqueOrderId);
        }
        return orderDAO.insertOrder(order, details);
    }

    public Order getOrderById(String orderId) {
        return orderDAO.findById(orderId);
    }

    public List<OrderDetail> getOrderDetails(String orderId) {
        return orderDAO.getDetailsByOrderId(orderId);
    }

    public List<Order> getUserOrderHistory(int userId) {
        return orderDAO.getOrdersByUserId(userId);
    }

    public String cancelOrder(String orderId, int userId) {
        Order order = orderDAO.findById(orderId);

        // NFR1.29-1. Kiểm tra quyền sở hữu đơn hàng
        if (order == null || order.getUserId() != userId) {
            return "Bạn không có quyền thực hiện thao tác này.";
        }

        // BR1.29-1 & E7a1. Chỉ cho phép hủy khi ở trạng thái "Chờ xác nhận"
        if (!"Chờ xác nhận".equals(order.getStatus())) {
            return "Đơn hàng đang trong quá trình xử lý hoặc vận chuyển, không thể hủy.";
        }

        boolean success = orderDAO.updateOrderStatus(orderId, "Đã hủy");
        return success ? "SUCCESS" : "Lỗi hệ thống khi hủy đơn hàng.";
    }
}