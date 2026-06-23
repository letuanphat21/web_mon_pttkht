package org.example.webquanao.dto.response;

public class ProductResponse {
    private int productId;
    private String productName;
    private String productBrand;
    private double productPrice;
    private int quantity;
    private String productDescription;
    private String productImage;
    private int categoryId;
    private String categoryName;
    private int productStatus;

    public ProductResponse() {}

    public ProductResponse(int productId, String productName, String productBrand,
                           double productPrice, int quantity, String productDescription,
                           String productImage, int categoryId, String categoryName, int productStatus) {
        this.productId = productId;
        this.productName = productName;
        this.productBrand = productBrand;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.productDescription = productDescription;
        this.productImage = productImage;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.productStatus = productStatus;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductBrand() { return productBrand; }
    public void setProductBrand(String productBrand) { this.productBrand = productBrand; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getProductStatus() { return productStatus; }
    public void setProductStatus(int productStatus) { this.productStatus = productStatus; }
}