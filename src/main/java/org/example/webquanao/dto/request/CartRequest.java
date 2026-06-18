package org.example.webquanao.dto.request;

public class CartRequest {
    private int userId;
    private int productId;
    private int qty;
    private boolean selected;
    private String action;
    public CartRequest() {}

    public CartRequest(int userId, int productId, int qty, boolean selected) {
        this.userId = userId;
        this.productId = productId;
        this.qty = qty;
        this.selected = selected;
    }

    public CartRequest(int productId, String action) {
        this.productId = productId;
        this.action = action;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}