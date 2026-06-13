package org.example.webquanao.entity;

import java.sql.Timestamp;

public class CartItem {
    private int id;
    private int cartId;
    private Product product;
    private int quantity;
    private double totalAmount;
    private boolean selected = true;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public CartItem() {}

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.calculateTotal();
    }

    public void calculateTotal() {
        if (this.product != null) {
            this.totalAmount = this.quantity * this.product.getProductPrice();
        } else {
            this.totalAmount = 0.0;
        }
    }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) {
        this.product = product;
        this.calculateTotal();
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.calculateTotal();
    }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}