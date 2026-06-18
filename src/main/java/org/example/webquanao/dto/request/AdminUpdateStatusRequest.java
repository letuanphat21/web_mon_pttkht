package org.example.webquanao.dto.request;

public class AdminUpdateStatusRequest {
    private String orderId;
    private String newStatus;
    private String reason;

    public AdminUpdateStatusRequest() {}

    public AdminUpdateStatusRequest(String orderId, String newStatus, String reason) {
        this.orderId = orderId;
        this.newStatus = newStatus;
        this.reason = reason;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}