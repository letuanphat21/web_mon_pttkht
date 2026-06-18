package org.example.webquanao.dto.response;

import org.example.webquanao.entity.Order;
import java.sql.Timestamp;

public class AdminOrderListResponse {
    private String orderId;
    private String customerName;
    private double totalPrice;
    private String status;
    private Timestamp createdAt;

    public AdminOrderListResponse(Order order) {
        this.orderId = order.getOrderId();
        this.customerName = order.getFullName();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
    }

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }
}