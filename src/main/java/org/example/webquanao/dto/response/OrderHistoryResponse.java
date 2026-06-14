package org.example.webquanao.dto.response;

import org.example.webquanao.entity.Order;
import java.sql.Timestamp;
import java.util.List;

public class OrderHistoryResponse {
    private String orderId;
    private int userId;
    private String fullName;
    private String phone;
    private String address;
    private double totalPrice;
    private String status;
    private Timestamp createdAt;
    private String cancelReason;

    private List<OrderDetailHistoryResponse> orderDetails;

    public OrderHistoryResponse() {}

    public OrderHistoryResponse(Order order) {
        this.orderId = order.getOrderId();
        this.userId = order.getUserId();
        this.fullName = order.getFullName();
        this.phone = order.getPhone();
        this.address = order.getAddress();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.cancelReason = order.getCancelReason();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public List<OrderDetailHistoryResponse> getOrderDetails() { return orderDetails; }

    public void setOrderDetails(List<OrderDetailHistoryResponse> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}