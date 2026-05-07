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
}