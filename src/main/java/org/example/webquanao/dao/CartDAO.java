package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.CartItem;
import org.example.webquanao.entity.Product;
import org.jdbi.v3.core.Jdbi;
import java.util.List;

public class CartDAO {
    private Jdbi jdbi;

    private Jdbi getJdbi() {
        if (jdbi == null) jdbi = DBConnect.get();
        return jdbi;
    }

    public List<CartItem> getCartItemsByUserId(int userId) {
        String sql = "SELECT ci.id AS item_id, ci.cart_id, ci.product_id, ci.quantity AS cart_qty, ci.is_selected, " +
                "p.productName, p.productPrice, p.productImage " +
                "FROM carts c " +
                "JOIN cart_items ci ON c.id = ci.cart_id " +
                "JOIN products p ON ci.product_id = p.productId " +
                "WHERE c.user_id = :userId";

        return getJdbi().withHandle(handle ->
                handle.createQuery(sql)
                        .bind("userId", userId)
                        .map((rs, ctx) -> {
                            Product product = new Product();
                            product.setProductId(rs.getInt("product_id"));
                            product.setProductName(rs.getString("productName"));
                            product.setProductPrice(rs.getDouble("productPrice"));
                            product.setProductImage(rs.getString("productImage"));

                            CartItem cartItem = new CartItem();
                            cartItem.setId(rs.getInt("item_id"));
                            cartItem.setCartId(rs.getInt("cart_id"));
                            cartItem.setProduct(product);
                            cartItem.setQuantity(rs.getInt("cart_qty"));
                            cartItem.setSelected(rs.getBoolean("is_selected"));
                            cartItem.calculateTotal();

                            return cartItem;
                        })
                        .list()
        );
    }

    public boolean deleteCartItem(int cartId, int productId) {
        int rows = getJdbi().withHandle(handle ->
                handle.createUpdate("DELETE FROM cart_items WHERE cart_id = :cartId AND product_id = :productId")
                        .bind("cartId", cartId)
                        .bind("productId", productId)
                        .execute()
        );
        return rows > 0;
    }

    public boolean updateItemSelection(int cartId, int productId, boolean isSelected) {
        int rows = getJdbi().withHandle(handle ->
                handle.createUpdate("UPDATE cart_items SET is_selected = :isSelected WHERE cart_id = :cartId AND product_id = :productId")
                        .bind("isSelected", isSelected ? 1 : 0)
                        .bind("cartId", cartId)
                        .bind("productId", productId)
                        .execute()
        );
        return rows > 0;
    }

    public CartItem checkUserCart(int userId, int productId) {
        String sql = "SELECT ci.id AS item_id, ci.cart_id, ci.product_id, ci.quantity AS cart_qty, ci.is_selected, " +
                "p.productName, p.productPrice, p.productImage " +
                "FROM carts c " +
                "JOIN cart_items ci ON c.id = ci.cart_id " +
                "JOIN products p ON ci.product_id = p.productId " +
                "WHERE c.user_id = :userId AND ci.product_id = :productId";

        return getJdbi().withHandle(handle ->
                handle.createQuery(sql)
                        .bind("userId", userId)
                        .bind("productId", productId)
                        .map((rs, ctx) -> {
                            Product product = new Product();
                            product.setProductId(rs.getInt("product_id"));
                            product.setProductName(rs.getString("productName"));
                            product.setProductPrice(rs.getDouble("productPrice"));
                            product.setProductImage(rs.getString("productImage"));

                            CartItem cartItem = new CartItem();
                            cartItem.setId(rs.getInt("item_id"));
                            cartItem.setCartId(rs.getInt("cart_id"));
                            cartItem.setProduct(product);
                            cartItem.setQuantity(rs.getInt("cart_qty"));
                            cartItem.setSelected(rs.getBoolean("is_selected"));
                            cartItem.calculateTotal();

                            return cartItem;
                        })
                        .findOne()
                        .orElse(null)
        );
    }

    public int getOrCreateCartId(int userId) {
        Integer cartId = getJdbi().withHandle(handle ->
                handle.createQuery("SELECT id FROM carts WHERE user_id = :userId")
                        .bind("userId", userId)
                        .mapTo(Integer.class)
                        .findOne()
                        .orElse(null)
        );

        if (cartId != null) {
            return cartId;
        }

        return getJdbi().withHandle(handle ->
                handle.createUpdate("INSERT INTO carts (user_id) VALUES (:userId)")
                        .bind("userId", userId)
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(Integer.class)
                        .one()
        );
    }

    public boolean insertCartItem(int cartId, int productId, int quantity) {
        int rows = getJdbi().withHandle(handle ->
                handle.createUpdate("INSERT INTO cart_items (cart_id, product_id, quantity, is_selected) VALUES (:cartId, :productId, :quantity, 1)")
                        .bind("cartId", cartId)
                        .bind("productId", productId)
                        .bind("quantity", quantity)
                        .execute()
        );
        return rows > 0;
    }

    public boolean updateCartItemQuantity(int cartId, int productId, int newQuantity) {
        int rows = getJdbi().withHandle(handle ->
                handle.createUpdate("UPDATE cart_items SET quantity = :newQuantity WHERE cart_id = :cartId AND product_id = :productId")
                        .bind("newQuantity", newQuantity)
                        .bind("cartId", cartId)
                        .bind("productId", productId)
                        .execute()
        );
        return rows > 0;
    }

    public int getTotalCartCount(int userId) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COALESCE(SUM(ci.quantity), 0) FROM carts c " +
                                "JOIN cart_items ci ON c.id = ci.cart_id " +
                                "WHERE c.user_id = :userId")
                        .bind("userId", userId)
                        .mapTo(Integer.class)
                        .one()
        );
    }
}