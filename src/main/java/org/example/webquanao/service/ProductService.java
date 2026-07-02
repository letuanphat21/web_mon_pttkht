package org.example.webquanao.service;

import org.example.webquanao.action.Result;
import org.example.webquanao.dao.CategoryDAO;
import org.example.webquanao.dao.ProductDAO;
import org.example.webquanao.dto.request.ProductRequest;
import org.example.webquanao.dto.response.ProductResponse;
import org.example.webquanao.entity.Category;
import org.example.webquanao.entity.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductService {
    private final ProductDAO productDAO   = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // ---- Dùng bởi ShopController ----
    public Product findById(int productId) {
        return productDAO.findById(productId);
    }

    // ---- Helper: đọc map an toàn, chịu được cả key gốc lẫn lowercase ----
    private String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) v = row.get(key.toLowerCase());
        return v != null ? v.toString() : "";
    }

    private long num(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) v = row.get(key.toLowerCase());
        if (v == null) return 0;
        return ((Number) v).longValue();
    }

    private double dbl(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) v = row.get(key.toLowerCase());
        if (v == null) return 0.0;
        return ((Number) v).doubleValue();
    }

    // ---- Dùng bởi admin API ----
    public Result getAllProductsForAdmin() {
        try {
            List<Map<String, Object>> rows = productDAO.findAllWithCategory();

            // Debug: in key thực tế của row đầu tiên ra log
            if (!rows.isEmpty()) {
                System.out.println("[ProductService] Map keys: " + rows.get(0).keySet());
            }

            List<ProductResponse> list = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                ProductResponse r = new ProductResponse();
                r.setProductId((int) num(row, "productId"));
                r.setProductName(str(row, "productName"));
                r.setProductBrand(str(row, "productBrand"));
                r.setProductPrice(dbl(row, "productPrice"));
                r.setQuantity((int) num(row, "quantity"));
                r.setProductDescription(str(row, "productDescription"));
                r.setProductImage(str(row, "productImage"));
                r.setCategoryId((int) num(row, "categoryId"));
                r.setCategoryName(str(row, "categoryName"));
                r.setProductStatus((int) num(row, "productStatus"));
                list.add(r);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("products", list);
            return Result.ok("Lấy danh sách thành công", data);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    public Result addProduct(ProductRequest dto) {
        try {
            if (dto.getProductName() == null || dto.getProductName().trim().isEmpty())
                return Result.fail("Tên sản phẩm không được để trống");
            if (dto.getProductPrice() <= 0)
                return Result.fail("Giá sản phẩm phải lớn hơn 0");
            if (dto.getQuantity() < 0)
                return Result.fail("Số lượng không hợp lệ");

            Product p = new Product();
            p.setProductName(dto.getProductName().trim());
            p.setProductBrand(dto.getProductBrand() != null ? dto.getProductBrand().trim() : "");
            p.setProductPrice(dto.getProductPrice());
            p.setQuantity(dto.getQuantity());
            p.setProductDescription(dto.getProductDescription() != null ? dto.getProductDescription().trim() : "");
            p.setProductImage(dto.getProductImage() != null && !dto.getProductImage().trim().isEmpty()
                    ? dto.getProductImage().trim() : "https://via.placeholder.com/200");
            p.setCategoryId(dto.getCategoryId());
            p.setProductStatus(1);

            productDAO.insert(p);
            return Result.ok("Thêm sản phẩm thành công", null);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Lỗi hệ thống khi thêm sản phẩm: " + e.getMessage());
        }
    }

    public Result updateProduct(ProductRequest dto) {
        try {
            Product existing = productDAO.findById(dto.getProductId());
            if (existing == null) return Result.fail("Không tìm thấy sản phẩm");
            if (dto.getProductName() == null || dto.getProductName().trim().isEmpty())
                return Result.fail("Tên sản phẩm không được để trống");
            if (dto.getProductPrice() <= 0)
                return Result.fail("Giá sản phẩm phải lớn hơn 0");

            existing.setProductName(dto.getProductName().trim());
            existing.setProductBrand(dto.getProductBrand() != null ? dto.getProductBrand().trim() : "");
            existing.setProductPrice(dto.getProductPrice());
            existing.setQuantity(dto.getQuantity());
            existing.setProductDescription(dto.getProductDescription() != null ? dto.getProductDescription().trim() : "");
            if (dto.getProductImage() != null && !dto.getProductImage().trim().isEmpty())
                existing.setProductImage(dto.getProductImage().trim());
            existing.setCategoryId(dto.getCategoryId());

            productDAO.update(existing);
            return Result.ok("Cập nhật sản phẩm thành công", null);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Lỗi hệ thống khi cập nhật: " + e.getMessage());
        }
    }

    public Result toggleProductStatus(int productId) {
        try {
            Product existing = productDAO.findById(productId);
            if (existing == null) return Result.fail("Không tìm thấy sản phẩm");
            productDAO.toggleStatus(productId);
            String msg = existing.getProductStatus() == 1 ? "Đã khóa sản phẩm" : "Đã mở khóa sản phẩm";
            return Result.ok(msg, null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Lỗi hệ thống khi thay đổi trạng thái: " + e.getMessage());
        }
    }

    public Result getCategoriesForDropdown() {
        try {
            List<Category> cats = categoryDAO.findAllActive();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Category c : cats) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("name", c.getName());
                list.add(m);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("categories", list);
            return Result.ok("OK", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Lỗi lấy danh mục: " + e.getMessage());
        }
    }
}