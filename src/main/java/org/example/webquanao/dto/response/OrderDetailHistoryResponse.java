package org.example.webquanao.dto.response;

public class OrderDetailHistoryResponse {
    private int productId;
    private String productName;
    private String productImage;
    private int quantity;
    private double price;
    private double subTotal;

    public OrderDetailHistoryResponse() {}

    public OrderDetailHistoryResponse(int productId, String productName, String productImage, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.quantity = quantity;
        this.price = price;
        this.subTotal = quantity * price;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getSubTotal() { return subTotal; }
    public void setSubTotal(double subTotal) { this.subTotal = subTotal; }
}