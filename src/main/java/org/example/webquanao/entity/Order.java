package org.example.webquanao.entity;

import java.sql.Timestamp;

public class Order {
    private String orderId;
    private int userId;
    private String fullName;
    private String phone;
    private String address;
    private double totalPrice;
    private String status;
    private Timestamp createdAt;

    public Order() {}

    public Order(String orderId, int userId, String fullName, String phone, String address, double totalPrice) {
        this.orderId = orderId;
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.totalPrice = totalPrice;
        this.status = "Chờ xác nhận";
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

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
}