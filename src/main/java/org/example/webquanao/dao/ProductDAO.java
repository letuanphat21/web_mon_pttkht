package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.Product;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Map;

public class ProductDAO {
    private Jdbi jdbi;

    private Jdbi getJdbi() {
        if (jdbi == null) jdbi = DBConnect.get();
        return jdbi;
    }

    // Lấy theo ID
    public Product findById(int id) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM products WHERE productId = :id")
                        .bind("id", id)
                        .mapToBean(Product.class)
                        .findOne()
                        .orElse(null)
        );
    }

    // Chỉ lấy sản phẩm đang active (cho trang shop)
    public List<Product> findAllActive() {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM products WHERE productStatus = 1")
                        .mapToBean(Product.class)
                        .list()
        );
    }

    // Lấy TẤT CẢ sản phẩm kèm tên danh mục (cho admin)
    public List<Map<String, Object>> findAllWithCategory() {
        return getJdbi().withHandle(handle ->
                handle.createQuery(
                                "SELECT p.*, c.name AS categoryName " +
                                        "FROM products p " +
                                        "LEFT JOIN categories c ON p.categoryId = c.id " +
                                        "ORDER BY p.productId DESC"
                        )
                        .mapToMap()
                        .list()
        );
    }

    // Thêm sản phẩm mới
    public int insert(Product p) {
        return getJdbi().withHandle(handle ->
                handle.createUpdate(
                                "INSERT INTO products (productName, productBrand, productPrice, quantity, " +
                                        "productDescription, productImage, categoryId, productStatus) " +
                                        "VALUES (:productName, :productBrand, :productPrice, :quantity, " +
                                        ":productDescription, :productImage, :categoryId, :productStatus)"
                        )
                        .bindBean(p)
                        .executeAndReturnGeneratedKeys("productId")
                        .mapTo(int.class)
                        .one()
        );
    }

    // Cập nhật sản phẩm
    public boolean update(Product p) {
        return getJdbi().withHandle(handle ->
                handle.createUpdate(
                                "UPDATE products SET productName = :productName, productBrand = :productBrand, " +
                                        "productPrice = :productPrice, quantity = :quantity, " +
                                        "productDescription = :productDescription, productImage = :productImage, " +
                                        "categoryId = :categoryId " +
                                        "WHERE productId = :productId"
                        )
                        .bindBean(p)
                        .execute() > 0
        );
    }

    // Toggle trạng thái (khóa / mở khóa) — soft delete
    public boolean toggleStatus(int productId) {
        return getJdbi().withHandle(handle ->
                handle.createUpdate(
                                "UPDATE products SET productStatus = CASE WHEN productStatus = 1 THEN 0 ELSE 1 END " +
                                        "WHERE productId = :id"
                        )
                        .bind("id", productId)
                        .execute() > 0
        );
    }
}