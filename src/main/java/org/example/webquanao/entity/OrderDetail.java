package org.example.webquanao.entity;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class OrderDetail {
    private int id;
    private Order order;
    private Product product;
    private int quantity;
    private double price;

    public OrderDetail() {}

    public OrderDetail(Order order, Product product, int quantity, double price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getOrderId() {
        return (order != null) ? order.getOrderId() : null;
    }

    public int getProductId() {
        return (product != null) ? product.getProductId() : 0;
    }
}