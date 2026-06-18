package org.example.webquanao.dto.response;

import java.util.ArrayList;
import java.util.List;

public class CartPageResponse {
    private double totalAmount;
    private List<CartItemResponse> cartItems = new ArrayList<>();

    public CartPageResponse(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void addCartItem(CartItemResponse item) {
        this.cartItems.add(item);
    }

    public double getTotalAmount() { return totalAmount; }
    public List<CartItemResponse> getCartItems() { return cartItems; }

    public static class CartItemResponse {
        private int id;
        private String name;
        private double price;
        private int qty;
        private double subTotal;
        private boolean selected;

        public CartItemResponse(int id, String name, double price, int qty, double subTotal, boolean selected) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.qty = qty;
            this.subTotal = subTotal;
            this.selected = selected;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQty() { return qty; }
        public double getSubTotal() { return subTotal; }
        public boolean isSelected() { return selected; }
    }
}