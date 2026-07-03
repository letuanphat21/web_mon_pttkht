package org.example.webquanao.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private User user;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Map<Integer, CartItem> items;
    private double grandTotal;

    public Cart() {
        this.items = new HashMap<>();
        this.grandTotal = 0.0;
    }

    public Cart(int id, User user) {
        this.id = id;
        this.user = user;
        this.items = new HashMap<>();
        this.grandTotal = 0.0;
    }

    public void addCartItem(CartItem item) {
        if (item != null && item.getProduct() != null) {
            item.setCart(this);
            this.items.put(item.getProduct().getProductId(), item);
            this.recalculateTotal();
        }
    }

    public void recalculateTotal() {
        if (this.items != null) {
            this.grandTotal = this.items.values().stream()
                    .filter(CartItem::isSelected)
                    .mapToDouble(CartItem::getTotalAmount)
                    .sum();
        } else {
            this.grandTotal = 0.0;
        }
    }

    public double getTotalAmount() {
        this.recalculateTotal();
        return this.grandTotal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Map<Integer, CartItem> getItems() { return items; }
    public void setItems(Map<Integer, CartItem> items) {
        this.items = items;
        this.recalculateTotal();
    }

    public double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }
}