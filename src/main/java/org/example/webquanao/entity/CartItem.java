package org.example.webquanao.entity;

import java.sql.Timestamp;

public class CartItem {
    private int id;
    private Cart cart;
    private Product product;
    private int quantity;
    private double totalAmount;
    private boolean selected = true;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public CartItem() {}

    public CartItem(Cart cart, Product product, int quantity) {
        this.cart = cart;
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


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

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

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}