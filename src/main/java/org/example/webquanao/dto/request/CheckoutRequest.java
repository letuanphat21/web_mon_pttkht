package org.example.webquanao.dto.request;

/**
 * DTO nhận thông tin shipping từ form checkout
 * orderRef: lưu orderId thật (String) để PaymentService dùng sau khi tạo đơn
 */
public class CheckoutRequest {
    private String fullName;
    private String phone;
    private String address;
    private String orderRef; // VD: "ORD-ABCD1234" — set bởi OrderService.createOrder()

    public CheckoutRequest() {}

    public CheckoutRequest(String fullName, String phone, String address) {
        this.fullName = fullName;
        this.phone    = phone;
        this.address  = address;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOrderRef() { return orderRef; }
    public void setOrderRef(String orderRef) { this.orderRef = orderRef; }
}