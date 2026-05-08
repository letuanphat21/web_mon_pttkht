package org.example.webquanao.service;

import org.example.webquanao.entity.CartItem;
import org.example.webquanao.entity.Product;
import java.util.Map;

public class CartService {

    public void addToCart(Map<Integer, CartItem> cart, Product product, int quantity) {
        // BR1.27-2: Kiểm tra số lượng nguyên dương
        if (quantity <= 0) return;
        int pId = product.getProductId();

        // 4a. Sản phẩm đã có trong giỏ hàng
        if (cart.containsKey(pId)) {

            // 4a1 & 4a2. Nhận diện và cộng dồn số lượng
            CartItem item = cart.get(pId);
            item.setQuantity(item.getQuantity() + quantity);

            // 4a3. Cập nhật lại thành tiền
            item.calculateTotal();
        }
        // 4b. Khởi tạo sản phẩm mới trong Session
        else {
            CartItem newItem = new CartItem(product, quantity);
            cart.put(pId, newItem);
        }
    }

    public void updateQuantity(Map<Integer, CartItem> cart, int productId, int quantity) {
        if (cart.containsKey(productId)) {
            // E5c1.1 & BR1.24-2. Nếu > 0 thì cập nhật
            if (quantity > 0) {
                CartItem item = cart.get(productId);
                item.setQuantity(quantity);
                item.calculateTotal(); // BR1.24-1
            } else {
                // E5c1.2. Nếu <= 0 thì xóa khỏi Session
                removeFromCart(cart, productId);
            }
        }
    }

    public void removeFromCart(Map<Integer, CartItem> cart, int productId) {
        if (cart != null) {
            cart.remove(productId); // Luồng 5b4
        }
    }

    public double calculateTotalCart(Map<Integer, CartItem> cart) {
        if (cart == null) return 0;
        return cart.values().stream().mapToDouble(CartItem::getTotalAmount).sum();
    }
}