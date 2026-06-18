package org.example.webquanao.dto.response;

public class OrderDetailResponse {
    private int productId;
    private String productName;
    private double price;
    private int quantity;
    private double subTotal;
    public OrderDetailResponse() {}


    public OrderDetailResponse(CartPageResponse.CartItemResponse item) {
        this.productId = item.getId();
        this.productName = item.getName();
        this.price = item.getPrice();
        this.quantity = item.getQty();
        this.subTotal = item.getSubTotal();
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubTotal() { return subTotal; }
    public void setSubTotal(double subTotal) { this.subTotal = subTotal; }
}