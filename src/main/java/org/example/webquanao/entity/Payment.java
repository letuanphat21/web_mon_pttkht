package org.example.webquanao.entity;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.sql.Timestamp;

public class Payment {
    private int paymentId;

    @ColumnName("order_id")
    private String orderId;

    private String method;
    private double amount;

    @ColumnName("momo_order_id")
    private String momoOrderId;

    @ColumnName("request_id")
    private String requestId;

    @ColumnName("result_code")
    private Integer resultCode;

    @ColumnName("payment_status")
    private String paymentStatus;

    @ColumnName("paid_at")
    private Timestamp paidAt;

    @ColumnName("created_at")
    private Timestamp createdAt;

    public Payment() {}

    public Payment(String orderId, String method, double amount) {
        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
        this.paymentStatus = "Chưa thanh toán";
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getMomoOrderId() { return momoOrderId; }
    public void setMomoOrderId(String momoOrderId) { this.momoOrderId = momoOrderId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public Integer getResultCode() { return resultCode; }
    public void setResultCode(Integer resultCode) { this.resultCode = resultCode; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Timestamp getPaidAt() { return paidAt; }
    public void setPaidAt(Timestamp paidAt) { this.paidAt = paidAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /**
     * true nếu thanh toán đã hoàn tất (COD đã xác nhận hoặc MoMo resultCode = 0)
     */
    public boolean isSuccess() {
        return "Đã thanh toán".equals(this.paymentStatus);
    }
}