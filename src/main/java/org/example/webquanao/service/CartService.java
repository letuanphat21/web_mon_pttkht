package org.example.webquanao.service;

import org.example.webquanao.dao.CartDAO;
import org.example.webquanao.dto.request.AddToCartRequest;
import org.example.webquanao.dto.response.CartPageResponse;
import org.example.webquanao.dto.response.CartResponse;
import org.example.webquanao.entity.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public class CartService {

    private ProductService productService = new ProductService();
    private CartDAO cartDAO = new CartDAO();

    private void attachCartToItems(List<CartItem> items, User user, int cartId) {
        Cart cart = new Cart(cartId, user);
        for (CartItem item : items) {
            item.setCart(cart);
        }
    }

    private CartPageResponse.CartItemResponse convertToDTO(CartItem item) {
        return new CartPageResponse.CartItemResponse(
                item.getProduct().getProductId(),
                item.getProduct().getProductName(),
                item.getProduct().getProductPrice(),
                item.getQuantity(),
                item.getProduct().getProductPrice() * item.getQuantity(),
                item.isSelected()
        );
    }

    public CartPageResponse getCartPageDetails(User user, HttpSession session) {
        CartPageResponse pageResponse;

        if (user == null) {
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart == null) {
                cart = new Cart();
                session.setAttribute("cart", cart);
            }
            cart.recalculateTotal();
            pageResponse = new CartPageResponse(cart.getTotalAmount());
            for (CartItem item : cart.getItems().values()) {
                pageResponse.addCartItem(convertToDTO(item));
            }
        } else {
            int cartId = cartDAO.getOrCreateCartId(user.getId());
            List<CartItem> dbItems = cartDAO.getCartItemsByUserId(user.getId());
            attachCartToItems(dbItems, user, cartId);

            double totalSelectedAmount = dbItems.stream()
                    .filter(CartItem::isSelected)
                    .mapToDouble(item -> item.getProduct().getProductPrice() * item.getQuantity())
                    .sum();
            pageResponse = new CartPageResponse(totalSelectedAmount);
            for (CartItem item : dbItems) {
                pageResponse.addCartItem(convertToDTO(item));
            }
        }
        return pageResponse;
    }

    public double removeItemFromCart(User user, HttpSession session, int productId) throws Exception {
        if (user == null) {
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart != null) {
                cart.getItems().remove(productId);
                cart.recalculateTotal();
                session.setAttribute("cart", cart);
                return cart.getTotalAmount();
            }
            return 0;
        } else {
            int cartId = cartDAO.getOrCreateCartId(user.getId());
            cartDAO.deleteCartItem(cartId, productId);
            List<CartItem> remainingItems = cartDAO.getCartItemsByUserId(user.getId());
            return remainingItems.stream().filter(CartItem::isSelected).mapToDouble(i -> i.getProduct().getProductPrice() * i.getQuantity()).sum();
        }
    }

    public CartComputation updateItemQuantity(User user, HttpSession session, int productId, int newQty) throws Exception {
        double itemTotal = 0;
        double cartTotal = 0;
        if (user == null) {
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart != null && cart.getItems().containsKey(productId)) {
                CartItem item = cart.getItems().get(productId);
                item.setQuantity(newQty);
                itemTotal = item.getProduct().getProductPrice() * newQty;
                cart.recalculateTotal();
                session.setAttribute("cart", cart);
                cartTotal = cart.getTotalAmount();
            }
        } else {
            int cartId = cartDAO.getOrCreateCartId(user.getId());
            cartDAO.updateCartItemQuantity(cartId, productId, newQty);
            List<CartItem> dbItems = cartDAO.getCartItemsByUserId(user.getId());
            itemTotal = dbItems.stream().filter(i -> i.getProduct().getProductId() == productId).mapToDouble(i -> i.getProduct().getProductPrice() * newQty).findFirst().orElse(0);
            cartTotal = dbItems.stream().filter(CartItem::isSelected).mapToDouble(i -> i.getProduct().getProductPrice() * i.getQuantity()).sum();
        }
        return new CartComputation(itemTotal, cartTotal);
    }

    public double toggleItemSelection(User user, HttpSession session, int productId, boolean isChecked) throws Exception {
        if (user == null) {
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart != null && cart.getItems().containsKey(productId)) {
                cart.getItems().get(productId).setSelected(isChecked);
                session.setAttribute("cart", cart);
                return cart.getItems().values().stream().filter(CartItem::isSelected).mapToDouble(CartItem::getTotalAmount).sum();
            }
            return 0;
        } else {
            int cartId = cartDAO.getOrCreateCartId(user.getId());
            cartDAO.updateItemSelection(cartId, productId, isChecked);
            List<CartItem> dbItems = cartDAO.getCartItemsByUserId(user.getId());
            return dbItems.stream().filter(CartItem::isSelected).mapToDouble(i -> i.getProduct().getProductPrice() * i.getQuantity()).sum();
        }
    }

    public void mergeCartOnLogin(User user, HttpSession session) throws Exception {
        Cart sessionCart = (Cart) session.getAttribute("cart");
        if (sessionCart != null && !sessionCart.getItems().isEmpty()) {
            int cartId = cartDAO.getOrCreateCartId(user.getId());
            for (CartItem sessionItem : sessionCart.getItems().values()) {
                int pid = sessionItem.getProduct().getProductId();
                CartItem dbItem = cartDAO.checkUserCart(user.getId(), pid);
                if (dbItem == null) cartDAO.insertCartItem(cartId, pid, sessionItem.getQuantity());
                else cartDAO.updateCartItemQuantity(cartId, pid, dbItem.getQuantity() + sessionItem.getQuantity());
            }
            session.removeAttribute("cart");
        }
    }

    public Object addToCart(AddToCartRequest reqAdd, HttpSession session) throws Exception {
        int productId = reqAdd.getProductId();
        int qtyToAdd = reqAdd.getQuantity();
        Product product = productService.findById(productId);
        if (product == null) return "PRODUCT_NOT_FOUND";

        User user = (User) session.getAttribute("user");
        if (user == null) {
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart == null) cart = new Cart();
            int currentQty = cart.getItems().containsKey(productId) ? cart.getItems().get(productId).getQuantity() : 0;
            if (currentQty + qtyToAdd > product.getQuantity()) return "STOCK_EXCEEDED";
            if (currentQty == 0) {
                CartItem newItem = new CartItem();
                newItem.setProduct(product);
                newItem.setQuantity(qtyToAdd);
                newItem.setSelected(true);
                newItem.calculateTotal();
                cart.addCartItem(newItem);
            } else {
                cart.getItems().get(productId).setQuantity(currentQty + qtyToAdd);
                cart.getItems().get(productId).calculateTotal();
            }
            session.setAttribute("cart", cart);
        } else {
            int cartId = cartDAO.getOrCreateCartId(user.getId());
            CartItem dbItem = cartDAO.checkUserCart(user.getId(), productId);
            int currentQty = (dbItem != null) ? dbItem.getQuantity() : 0;
            if (currentQty + qtyToAdd > product.getQuantity()) return "STOCK_EXCEEDED";
            if (dbItem == null) cartDAO.insertCartItem(cartId, productId, qtyToAdd);
            else cartDAO.updateCartItemQuantity(cartId, productId, currentQty + qtyToAdd);
        }
        return new CartResponse("Thêm thành công", getTotalCartCount(user != null ? user.getId() : 0));
    }

    public CartPageResponse getSelectedItemsCart(User user) throws Exception {
        List<CartItem> dbItems = cartDAO.getCartItemsByUserId(user.getId());
        double totalSelectedAmount = dbItems.stream().filter(CartItem::isSelected).mapToDouble(i -> i.getProduct().getProductPrice() * i.getQuantity()).sum();
        CartPageResponse selectedCartResponse = new CartPageResponse(totalSelectedAmount);
        for (CartItem item : dbItems) {
            if (item.isSelected()) selectedCartResponse.addCartItem(convertToDTO(item));
        }
        return selectedCartResponse;
    }

    public boolean validateItemsStock(List<CartPageResponse.CartItemResponse> checkedItems) throws Exception {
        for (CartPageResponse.CartItemResponse item : checkedItems) {
            Product currentProduct = productService.findById(item.getId());
            if (currentProduct == null || item.getQty() > currentProduct.getQuantity()) return true;
        }
        return false;
    }

    public void clearPurchasedItems(User user, CartPageResponse purchasedCart) throws Exception {
        int cartId = cartDAO.getOrCreateCartId(user.getId());
        for (CartPageResponse.CartItemResponse purchasedItem : purchasedCart.getCartItems()) {
            cartDAO.deleteCartItem(cartId, purchasedItem.getId());
        }
    }

    public int getTotalCartCount(int userId) {
        return (userId == 0) ? 0 : cartDAO.sumQuantityByUserId(userId);
    }

    public int checkProductStock(int productId) {
        Product product = productService.findById(productId);
        return (product != null) ? product.getQuantity() : 0;
    }

    public static class CartComputation {
        private final double itemTotal;
        private final double cartTotal;
        public CartComputation(double itemTotal, double cartTotal) { this.itemTotal = itemTotal; this.cartTotal = cartTotal; }
        public double getItemTotal() { return itemTotal; }
        public double getCartTotal() { return cartTotal; }
    }
}