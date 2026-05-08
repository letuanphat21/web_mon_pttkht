package org.example.webquanao.entity;

public class CartItem {
    // BR1.27-1: Mã duy nhất để định danh
    private int productId;
    private String productName;
    private String productImage;
    private int quantity;
    private double unitPrice;
    private double totalAmount; // Thành tiền

    public CartItem(Product p, int quantity) {
        this.productId = p.getProductId();
        this.productName = p.getProductName();
        this.productImage = p.getProductImage();
        this.unitPrice = p.getProductPrice();
        this.quantity = quantity;
        this.calculateTotal();
    }

    public void calculateTotal() {
        this.totalAmount = this.unitPrice * this.quantity;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}