package org.example.webquanao.controller;

import com.google.gson.JsonObject;
import org.example.webquanao.dto.response.CartPageResponse;
import org.example.webquanao.entity.User;
import org.example.webquanao.service.CartService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/cart")
public class ManageCartController extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        try {
            // Bước 2: Gọi Service đọc dữ liệu phân nhánh Guest/User và đóng gói vào DTO
            CartPageResponse cartPageData = cartService.getCartPageDetails(user, session);

            if (user != null) {
                int totalCartCount = cartService.getTotalCartCount(user.getId());
                session.setAttribute("totalCartCount", totalCartCount);
            }

            // Đẩy dữ liệu DTO sang View
            request.setAttribute("cartData", cartPageData);
            request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        String action = request.getParameter("action");
        String queryType = request.getParameter("queryType");
        String productIdStr = request.getParameter("productId");

        JsonObject jsonResponse = new JsonObject();

        try {

            if ("checkAuth".equals(queryType)) {
                if (user == null) {
                    // Trạng thái Guest -> Trả về cờ báo bắt buộc chuyển hướng đăng nhập
                    jsonResponse.addProperty("isGuest", true);
                } else {
                    // Trạng thái User hợp lệ -> Cho đi tiếp
                    jsonResponse.addProperty("isGuest", false);
                }
                response.getWriter().print(jsonResponse.toString());
                return;
            }

            // Nhánh phụ E6c1b: Kiểm tra tồn kho trước khi cập nhật số lượng
            if ("checkStock".equals(queryType)) {
                int productId = Integer.parseInt(productIdStr);
                int newQty = Integer.parseInt(request.getParameter("newQty"));

                int maxAvailable = cartService.checkProductStock(productId);
                if (newQty > maxAvailable) {
                    jsonResponse.addProperty("status", "outOfStock");
                    jsonResponse.addProperty("maxAvailable", maxAvailable);
                } else {
                    jsonResponse.addProperty("status", "available");
                }
                response.getWriter().print(jsonResponse.toString());
                return;
            }

            if (action == null || productIdStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Tham số không hợp lệ.");
                response.getWriter().print(jsonResponse.toString());
                return;
            }

            int productId = Integer.parseInt(productIdStr);

            switch (action) {
                // Luồng 6b: Xóa sản phẩm khỏi bộ lưu trữ
                case "DELETE":
                    double totalAfterDelete = cartService.removeItemFromCart(user, session, productId);

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("newTotal", totalAfterDelete);
                    break;

                // Luồng 6c: Thay đổi số lượng, tính lại thành tiền & tổng tiền
                case "UPDATE":
                    int newQty = Integer.parseInt(request.getParameter("newQty"));

                    var calculation = cartService.updateItemQuantity(user, session, productId, newQty);

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("itemTotal", calculation.getItemTotal());
                    jsonResponse.addProperty("cartTotal", calculation.getCartTotal());
                    break;

                // Luồng 6e: Tích chọn / Bỏ tích chọn checkbox sản phẩm
                case "SELECT":
                    boolean isChecked = Boolean.parseBoolean(request.getParameter("isChecked"));

                    double selectedTotal = cartService.toggleItemSelection(user, session, productId, isChecked);

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("cartTotal", selectedTotal);
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Hành động (Action) không hợp lệ.");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Tham số định dạng số không chính xác.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "CONNECTION_ERROR");
        }

        response.getWriter().print(jsonResponse.toString());
        response.getWriter().flush();
    }
}