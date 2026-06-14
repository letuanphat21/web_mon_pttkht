package org.example.webquanao.dto.response;

public class CartItemResponse {
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