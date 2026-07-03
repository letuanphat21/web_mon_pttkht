package org.example.webquanao.controller;

import org.example.webquanao.dto.request.AddToCartRequest;
import org.example.webquanao.dto.response.CartResponse;
import org.example.webquanao.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/cart/add")
public class CartController extends HttpServlet {

    private CartService cartService = new CartService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            AddToCartRequest reqAdd = new AddToCartRequest(productId, quantity);
            HttpSession session = request.getSession();

            Object serviceResult = cartService.addToCart(reqAdd, session);

            if (serviceResult instanceof String) {
                String status = (String) serviceResult;
                response.setStatus(400);

                switch (status) {
                    case "INVALID_QUANTITY":
                        // Bước 7) Trả về mã lỗi số lượng <= 0
                        response.getWriter().print("{\"status\":\"INVALID_QUANTITY\", \"message\":\"Số lượng mua phải là số dương\"}");
                        break;
                    case "STOCK_EXCEEDED":
                        // Bước 11) Trả về mã lỗi vượt quá số lượng kho hiện tại của hệ thống
                        response.getWriter().print("{\"status\":\"STOCK_EXCEEDED\", \"message\":\"Số lượng vượt quá tồn kho hệ thống\"}");
                        break;
                    default:
                        // Luồng E: Trục trặc không rõ nguyên nhân
                        response.setStatus(500);
                        response.getWriter().print("{\"status\":\"CONNECTION_ERROR\", \"message\":\"Lỗi hệ thống hoặc kết nối dữ liệu thất bại\"}");
                        break;
                }
            } else {
                // Ép kiểu về DTO phản hồi thành công nhận được từ Service
                CartResponse resCart = (CartResponse) serviceResult;

                // ĐỒNG BỘ: Nếu là thành viên (User) đã đăng nhập, lưu số lượng tổng vào Session
                Integer userId = (Integer) session.getAttribute("userId");
                if (userId != null) {
                    session.setAttribute("totalCartCount", resCart.getTotalCount());
                }

                // Bước 35) Trả về chuỗi JSON thành công kèm tổng số lượng mặt hàng mới
                response.setStatus(200);
                response.getWriter().print("{"
                        + "\"status\":\"success\","
                        + "\"message\":\"" + resCart.getMessage() + "\","
                        + "\"totalCount\":" + resCart.getTotalCount()
                        + "}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(400);
            response.getWriter().print("{\"status\":\"INVALID_DATA\", \"message\":\"Dữ liệu đầu vào không hợp lệ\"}");
        } catch (Exception e) {
            // Luồng ngoại lệ E: Hệ thống xảy ra lỗi Crash ngoài ý muốn
            response.setStatus(500);
            response.getWriter().print("{\"status\":\"CONNECTION_ERROR\", \"message\":\"Hệ thống không phản hồi, vui lòng thử lại sau\"}");
        }
    }
}