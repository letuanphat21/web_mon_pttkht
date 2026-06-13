package org.example.webquanao.dto.response;

import org.example.webquanao.dto.request.CheckoutRequest;
import java.util.List;

public class OrderResponse {
    private int orderId;
    private List<OrderDetailResponse> orderDetails;
    private CheckoutRequest shippingInfo;

    public OrderResponse() {}

    public OrderResponse(int orderId, List<OrderDetailResponse> orderDetails, CheckoutRequest shippingInfo) {
        this.orderId = orderId;
        this.orderDetails = orderDetails;
        this.shippingInfo = shippingInfo;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public List<OrderDetailResponse> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetailResponse> orderDetails) { this.orderDetails = orderDetails; }

    public CheckoutRequest getShippingInfo() { return shippingInfo; }
    public void setShippingInfo(CheckoutRequest shippingInfo) { this.shippingInfo = shippingInfo; }
}