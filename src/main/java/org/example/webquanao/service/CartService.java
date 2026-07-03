package org.example.webquanao.service;

import org.example.webquanao.dto.request.AddToCartRequest;
import org.example.webquanao.dto.response.CartPageResponse;
import org.example.webquanao.dto.response.CartResponse;
import org.example.webquanao.entity.Cart;
import org.example.webquanao.entity.CartItem;
import org.example.webquanao.entity.Product;
import org.example.webquanao.dao.CartDAO;
import jakarta.servlet.http.HttpSession;
import java.util.List;

public class CartService {

    private ProductService productService = new ProductService();
    private CartDAO cartDAO = new CartDAO();

    public CartPageResponse getCartPageDetails(Integer userId, HttpSession session) {
        CartPageResponse pageResponse;

        if (userId == null) {
            //Trạng thái người dùng là Guest -> Đọc dữ liệu giỏ hàng từ Session
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart == null) {
                cart = new Cart();
                session.setAttribute("cart", cart);
            }

            // Tính tổng tiền dự kiến ban đầu của toàn bộ giỏ hàng Session
            cart.recalculateTotal();
            pageResponse = new CartPageResponse(cart.getTotalAmount());

            for (CartItem item : cart.getItems().values()) {
                CartPageResponse.CartItemResponse itemDTO = new CartPageResponse.CartItemResponse(
                        item.getProduct().getProductId(),
                        item.getProduct().getProductName(),
                        item.getProduct().getProductPrice(),
                        item.getQuantity(),
                        item.getProduct().getProductPrice() * item.getQuantity(),
                        item.isSelected()
                );
                pageResponse.addCartItem(itemDTO);
            }
        } else {
            // Trạng thái người dùng là User -> Đọc dữ liệu giỏ hàng từ Database
            List<CartItem> dbItems = cartDAO.getCartItemsByUserId(userId);

            // Tính toán tổng tiền dựa trên các sản phẩm đang được tích chọn (Luồng 6e3)
            double totalSelectedAmount = dbItems.stream()
                    .filter(CartItem::isSelected)
                    .mapToDouble(item -> item.getProduct().getProductPrice() * item.getQuantity())
                    .sum();

            pageResponse = new CartPageResponse(totalSelectedAmount);

            // Duyệt danh sách từ database đổ lên List<CartItemResponse>
            for (CartItem item : dbItems) {
                CartPageResponse.CartItemResponse itemDTO = new CartPageResponse.CartItemResponse(
                        item.getProduct().getProductId(),
                        item.getProduct().getProductName(),
                        item.getProduct().getProductPrice(),
                        item.getQuantity(),
                        item.getProduct().getProductPrice() * item.getQuantity(),
                        item.isSelected()
                );
                pageResponse.addCartItem(itemDTO);
            }
        }
        return pageResponse;
    }

    // 2. LUỒNG PHỤ 6b: LOẠI SẢN PHẨM KHỎI BỘ LƯU TRỮ (XÓA SẢN PHẨM) - ĐÃ CẬP NHẬT
    public double removeItemFromCart(Integer userId, HttpSession session, int productId) throws Exception {
        if (userId == null) {
            // Guest: Xóa trên Session
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart != null) {
                cart.getItems().remove(productId);
                cart.recalculateTotal();

                session.setAttribute("cart", cart);
                return cart.getTotalAmount();
            }
            return 0;
        } else {
            // User: Xóa hẳn dưới Database thông qua CartID
            int cartId = cartDAO.getOrCreateCartId(userId);
            cartDAO.deleteCartItem(cartId, productId);

            // Tính lại tổng tiền mới của Database sau khi xóa
            List<CartItem> remainingItems = cartDAO.getCartItemsByUserId(userId);
            return remainingItems.stream()
                    .filter(CartItem::isSelected)
                    .mapToDouble(item -> item.getProduct().getProductPrice() * item.getQuantity())
                    .sum();
        }
    }

    // 3. LUỒNG PHỤ 6c: CẬP NHẬT SỐ LƯỢNG SẢN PHẨM TRONG GIỎ
    public CartComputation updateItemQuantity(Integer userId, HttpSession session, int productId, int newQty) throws Exception {
        double itemTotal = 0;
        double cartTotal = 0;

        if (userId == null) {
            // Guest: Cập nhật trực tiếp trên Model Session
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart != null && cart.getItems().containsKey(productId)) {
                CartItem item = cart.getItems().get(productId);
                item.setQuantity(newQty); // 6c2a. Cập nhật thành tiền của item

                itemTotal = item.getProduct().getProductPrice() * newQty;
                cart.recalculateTotal();

                session.setAttribute("cart", cart);
                cartTotal = cart.getTotalAmount();
            }
        } else {
            // User: Cập nhật trực tiếp xuống Database câu lệnh UPDATE
            int cartId = cartDAO.getOrCreateCartId(userId);
            cartDAO.updateCartItemQuantity(cartId, productId, newQty);

            // Đọc lại dữ liệu để tính toán phân phối chính xác dòng tiền hiển thị
            List<CartItem> dbItems = cartDAO.getCartItemsByUserId(userId);
            for (CartItem item : dbItems) {
                if (item.getProduct().getProductId() == productId) {
                    itemTotal = item.getProduct().getProductPrice() * newQty;
                }
            }
            cartTotal = dbItems.stream()
                    .filter(CartItem::isSelected)
                    .mapToDouble(item -> item.getProduct().getProductPrice() * item.getQuantity())
                    .sum();
        }

        return new CartComputation(itemTotal, cartTotal);
    }

    // 4. LUỒNG PHỤ 6e: TÍCH CHỌN / BỎ TÍCH CHỌN SẢN PHẨM (CHECKBOX)
    public double toggleItemSelection(Integer userId, HttpSession session, int productId, boolean isChecked) throws Exception {
        if (userId == null) {
            // Guest: Cập nhật cờ lựa chọn trên Session
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart != null && cart.getItems().containsKey(productId)) {
                cart.getItems().get(productId).setSelected(isChecked);

                session.setAttribute("cart", cart);

                // Luồng 6e3: Tính lại tiền các mục được tích chọn
                return cart.getItems().values().stream()
                        .filter(CartItem::isSelected)
                        .mapToDouble(item -> item.getProduct().getProductPrice() * item.getQuantity())
                        .sum();
            }
            return 0;
        } else {
            // User: Chạy câu lệnh UPDATE xuống SQL để lưu lại trạng thái Checkbox
            int cartId = cartDAO.getOrCreateCartId(userId);
            cartDAO.updateItemSelection(cartId, productId, isChecked);

            // 6e3. Hệ thống tính toán lại “Tổng tiền” dựa trên danh sách các sản phẩm đang được chọn
            List<CartItem> dbItems = cartDAO.getCartItemsByUserId(userId);
            return dbItems.stream()
                    .filter(CartItem::isSelected)
                    .mapToDouble(item -> item.getProduct().getProductPrice() * item.getQuantity())
                    .sum();
        }
    }

    // LUỒNG 6d: GỘP GIỎ HÀNG TỪ SESSION VÀO DATABASE KHI GUEST ĐĂNG NHẬP
    public void mergeCartOnLogin(int userId, HttpSession session) throws Exception {
        Cart sessionCart = (Cart) session.getAttribute("cart");
        System.out.println("[DEBUG] mergeCartOnLogin: Bắt đầu gộp cho User " + userId);

        if (sessionCart != null && !sessionCart.getItems().isEmpty()) {
            int cartId = cartDAO.getOrCreateCartId(userId);
            for (CartItem sessionItem : sessionCart.getItems().values()) {
                int pid = sessionItem.getProduct().getProductId();
                CartItem dbItem = cartDAO.checkUserCart(userId, pid);
                if (dbItem == null) {
                    cartDAO.insertCartItem(cartId, pid, sessionItem.getQuantity());
                } else {
                    cartDAO.updateCartItemQuantity(cartId, pid, dbItem.getQuantity() + sessionItem.getQuantity());
                }
            }
            session.removeAttribute("cart");
            System.out.println("[DEBUG] mergeCartOnLogin: Đã gộp và xóa Session Cart");
        } else {
            System.out.println("[DEBUG] mergeCartOnLogin: Không có giỏ hàng tạm để gộp");
        }
    }

    // BỔ TRỢ LUỒNG PHỤ: KIỂM TRA SỐ LƯỢNG TỒN KHO THỰC TẾ TRƯỚC KHI CHO PHÉP TĂNG
    public int checkProductStock(int productId) throws Exception {
        Product product = productService.findById(productId);
        return (product != null) ? product.getQuantity() : 0;
    }

    public Object addToCart(AddToCartRequest reqAdd, HttpSession session) throws Exception {
        int productId = reqAdd.getProductId();
        int qtyToAdd = reqAdd.getQuantity();
        Product product = productService.findById(productId);
        if (product == null) return "PRODUCT_NOT_FOUND";

        Integer userId = (Integer) session.getAttribute("userId");
        System.out.println("[DEBUG] addToCart: UserID = " + userId + ", ProductID = " + productId);

        if (userId == null) {
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart == null) cart = new Cart();
            int currentQty = cart.getItems().containsKey(productId) ? cart.getItems().get(productId).getQuantity() : 0;
            if (currentQty + qtyToAdd > product.getQuantity()) return "STOCK_EXCEEDED";

            if (currentQty == 0) cart.addCartItem(new CartItem(product, qtyToAdd));
            else cart.getItems().get(productId).setQuantity(currentQty + qtyToAdd);

            session.setAttribute("cart", cart);
        } else {
            int cartId = cartDAO.getOrCreateCartId(userId);
            CartItem dbItem = cartDAO.checkUserCart(userId, productId);
            int currentQty = (dbItem != null) ? dbItem.getQuantity() : 0;

            if (currentQty + qtyToAdd > product.getQuantity()) return "STOCK_EXCEEDED";

            boolean success;
            if (dbItem == null) {
                success = cartDAO.insertCartItem(cartId, productId, qtyToAdd);
                System.out.println("[DEBUG] addToCart: INSERT item mới: " + success);
            } else {
                success = cartDAO.updateCartItemQuantity(cartId, productId, currentQty + qtyToAdd);
                System.out.println("[DEBUG] addToCart: UPDATE cộng dồn item: " + success);
            }
        }
        return new CartResponse("Thêm thành công", getTotalCartCount(userId != null ? userId : 0));
    }

    public CartPageResponse getSelectedItemsCart(int userId) throws Exception {
        List<CartItem> dbItems = cartDAO.getCartItemsByUserId(userId);

        // Tính tổng tiền lọc riêng các sản phẩm có is_selected = true
        double totalSelectedAmount = dbItems.stream()
                .filter(CartItem::isSelected)
                .mapToDouble(item -> item.getProduct().getProductPrice() * item.getQuantity())
                .sum();

        CartPageResponse selectedCartResponse = new CartPageResponse(totalSelectedAmount);

        // Đổ dữ liệu đã lọc sang cấu trúc DTO gửi đi
        for (CartItem item : dbItems) {
            if (item.isSelected()) {
                CartPageResponse.CartItemResponse itemDTO = new CartPageResponse.CartItemResponse(
                        item.getProduct().getProductId(),
                        item.getProduct().getProductName(),
                        item.getProduct().getProductPrice(),
                        item.getQuantity(),
                        item.getProduct().getProductPrice() * item.getQuantity(),
                        item.isSelected()
                );
                selectedCartResponse.addCartItem(itemDTO);
            }
        }
        return selectedCartResponse;
    }

    /**
     * LUỒNG NGOẠI LỆ E6b: Kiểm tra trạng thái tồn kho thực tế của các sản phẩm khách muốn đặt mua
     */
    public boolean validateItemsStock(List<CartPageResponse.CartItemResponse> checkedItems) throws Exception {
        for (CartPageResponse.CartItemResponse item : checkedItems) {
            // Lấy thực thể sản phẩm hiện tại trong kho thông qua Service bổ trợ có sẵn của bạn
            Product currentProduct = productService.findById(item.getId());

            // Nếu sản phẩm không tồn tại, bị ẩn hoặc số lượng khách mua vượt quá lượng tồn kho thực tế
            if (currentProduct == null || item.getQty() > currentProduct.getQuantity()) {
                return true; // Xác nhận: Có sản phẩm không hợp lệ (hasInvalidItem = true)
            }
        }
        return false; // Toàn bộ sản phẩm đều đảm bảo số lượng tồn kho
    }

    /**
     * POST-CONDITION LUỒNG 16: Tiến hành dọn dẹp sạch các sản phẩm đã thanh toán thành công khỏi Giỏ hàng DB
     */
    public void clearPurchasedItems(int userId, CartPageResponse purchasedCart) throws Exception {
        int cartId = cartDAO.getOrCreateCartId(userId);

        // Duyệt qua danh sách các sản phẩm vừa chốt mua để xóa lệnh hàng loạt dưới SQL
        for (CartPageResponse.CartItemResponse purchasedItem : purchasedCart.getCartItems()) {
            cartDAO.deleteCartItem(cartId, purchasedItem.getId());
        }
    }

    public int getTotalCartCount(int userId) {
        if (userId == 0) return 0;
        return cartDAO.sumQuantityByUserId(userId);
    }

    public static class CartComputation {
        private final double itemTotal;
        private final double cartTotal;

        public CartComputation(double itemTotal, double cartTotal) {
            this.itemTotal = itemTotal;
            this.cartTotal = cartTotal;
        }

        public double getItemTotal() { return itemTotal; }
        public double getCartTotal() { return cartTotal; }
    }


}