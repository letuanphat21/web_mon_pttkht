package org.example.webquanao.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private User user;
    private String fullName;
    private String phone;
    private String address;
    private double totalPrice;
    private String status;
    private Timestamp createdAt;
    private String cancelReason;

    private List<OrderDetail> orderDetails = new ArrayList<>();

    public Order() {}

    public Order(String orderId, User user, String fullName, String phone, String address, double totalPrice) {
        this.orderId = orderId;
        this.user = user;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.totalPrice = totalPrice;
        this.status = "Chờ xác nhận";
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public int getUserId() {
        return (user != null) ? user.getId() : 0;
    }

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

    public List<OrderDetail> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetail> orderDetails) { this.orderDetails = orderDetails;}
}