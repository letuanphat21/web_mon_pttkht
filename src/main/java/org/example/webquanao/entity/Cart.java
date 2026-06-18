package org.example.webquanao.entity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Cart {
    private int id;
    private int userId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Map<Integer, CartItem> items;
    private double grandTotal;

    public Cart() {
        this.items = new HashMap<>();
        this.grandTotal = 0.0;
    }

    public Cart(int id, int userId) {
        this.id = id;
        this.userId = userId;
        this.items = new HashMap<>();
        this.grandTotal = 0.0;
    }

    public void addCartItem(CartItem item) {
        if (item != null && item.getProduct() != null) {
            item.setCartId(this.id);
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

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

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