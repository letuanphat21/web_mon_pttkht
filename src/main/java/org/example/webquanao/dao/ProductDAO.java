package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.Product;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class ProductDAO {
    // Sử dụng lazy initialization để tránh lỗi nạp chồng Class khi khởi tạo Servlet
    private Jdbi jdbi;

    private Jdbi getJdbi() {
        if (jdbi == null) jdbi = DBConnect.get();
        return jdbi;
    }

    public Product findById(int id) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM products WHERE productId = :id")
                        .bind("id", id)
                        .mapToBean(Product.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public List<Product> findAllActive() {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM products WHERE productStatus = 1")
                        .mapToBean(Product.class)
                        .list()
        );
    }
}