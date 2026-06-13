package org.example.webquanao.service;

import org.example.webquanao.dao.ProductDAO;
import org.example.webquanao.entity.Product;

public class ProductService {
    private ProductDAO productDAO = new ProductDAO();

    public Product checkProductStock(int productId) {
        return productDAO.findById(productId);
    }

    public Product findById(int productId) {
        return productDAO.findById(productId);
    }
}