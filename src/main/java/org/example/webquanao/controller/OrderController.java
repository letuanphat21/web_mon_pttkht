package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.webquanao.dto.request.CheckoutRequest;
import org.example.webquanao.dto.response.CartPageResponse;
import org.example.webquanao.dto.response.OrderResponse;
import org.example.webquanao.service.OrderService;
import org.example.webquanao.service.CartService;
import org.example.webquanao.service.UserService;

import java.io.IOException;
import java.util.Map;

@WebServlet("/order-process")
public class OrderController extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartService();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("editShipping".equals(action)) {
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            request.setAttribute("cartData", cartService.getCartPageDetails(userId, request.getSession()));
            request.setAttribute("openOrderForm", true);

            request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);

        } else if ("returnToCart".equals(action)) {
            // Luồng 14b2: Điều hướng nz`gười dùng quay về bước 3 (Giỏ hàng ban đầu)
            response.sendRedirect(request.getContextPath() + "/cart");

        } else if ("review".equals(action)) {
            // Hiển thị giao diện kiểm tra thông tin Review đơn hàng
            try {
                showReviewPage(request, response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {
            response.sendRedirect(request.getContextPath() + "/cart");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        System.out.println("DEBUG - checkoutCart: " + session.getAttribute("checkoutCart"));
        System.out.println("DEBUG - checkoutShipping: " + session.getAttribute("checkoutShipping"));
        Integer userId = (Integer) session.getAttribute("userId");

        // Đảm bảo PRE-CONDITION: User đã đăng nhập thành công
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");

        // LUỒNG 5 -> 8: KIỂM TRA & KHỞI TẠO ĐẶT HÀNG (Nhấn Đặt hàng tại cart.jsp)
        if ("proceedToCheckout".equals(action)) {
            try {
                // LUÔN LẤY DỮ LIỆU MỚI TỪ DB
                CartPageResponse resCartCheck = cartService.getSelectedItemsCart(userId);

                if (resCartCheck == null || resCartCheck.getCartItems().isEmpty()) {
                    request.setAttribute("error_msg", "Giỏ hàng trống!");
                    request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);
                    return;
                }

                session.setAttribute("checkoutCart", resCartCheck);

                // Ngoại lệ E6b: Trạng thái sản phẩm bị thay đổi (Hết hàng / Vượt quá tồn kho)
                boolean hasInvalidItem = cartService.validateItemsStock(resCartCheck.getCartItems());
                if (hasInvalidItem) {
                    request.setAttribute("error_msg", "Sản phẩm trong giỏ hàng đã hết hàng hoặc không đủ tồn kho!");
                    request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);
                    return;
                }


                // Bước 7 -> 8: Lấy hồ sơ thông tin mặc định của User
                var resProfile = userService.getUserProfile(userId);

                if (resProfile == null || resProfile.getAddress() == null || resProfile.getAddress().isEmpty()) {
                    // Luồng 8a: Hồ sơ trống -> Forward form trống để tự nhập
                    request.setAttribute("openOrderForm", true);
                } else {
                    // Luồng 8: Có thông tin mặc định -> Đẩy thông tin Profile có sẵn ra điền vào form
                    request.setAttribute("userProfile", resProfile);
                    request.setAttribute("openOrderForm", true);
                }

                request.setAttribute("cartData", cartService.getCartPageDetails(userId, session));
                request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);

            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/cart");
            }
        }

        // LUỒNG 9 -> 13: NHẬP LIỆU VÀ KIỂM TRA THÔNG TIN (Nhấn "Tiếp theo")
        else if ("validateShipping".equals(action)) {
            // CƠ CHẾ TỰ HỒI PHỤC: Nếu Session mất checkoutCart, nạp lại từ DB
            CartPageResponse cartResponse = (CartPageResponse) session.getAttribute("checkoutCart");
            if (cartResponse == null) {
                try {
                    cartResponse = cartService.getSelectedItemsCart(userId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                session.setAttribute("checkoutCart", cartResponse);
            }

            String name = request.getParameter("fullName");
            String phone = request.getParameter("phone");
            String address = request.getParameter("address");

            CheckoutRequest reqCheckout = new CheckoutRequest(name, phone, address);
            Map<String, String> errors = orderService.validateShippingInfo(reqCheckout);

            if (!errors.isEmpty()) {
                request.setAttribute("validationErrors", errors);
                request.setAttribute("pendingOrder", reqCheckout);
                request.setAttribute("openOrderForm", true);
                request.setAttribute("cartData", cartService.getCartPageDetails(userId, session));
                request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);
                return;
            }

            session.setAttribute("checkoutShipping", reqCheckout);
            response.sendRedirect(request.getContextPath() + "/order-process?action=review");
        }

        // LUỒNG 15 -> 16: XÁC NHẬN ĐẶT HÀNG & CHUYỂN THANH TOÁN (Xác nhận đặt hàng)
        else if ("confirmOrder".equals(action)) {
            try {
                // Lấy lại dữ liệu CartResponse và CheckoutRequest từ Session ra xử lý
                CartPageResponse cartResponse = (CartPageResponse) session.getAttribute("checkoutCart");
                CheckoutRequest checkoutRequest = (CheckoutRequest) session.getAttribute("checkoutShipping");

                if (cartResponse == null || checkoutRequest == null) {
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }

                // Gọi OrderService tiến hành tạo đơn hàng trong Database (Orders & OrderDetails)
                OrderResponse orderResponse = orderService.createOrder(userId, cartResponse, checkoutRequest);

                // Khối Ngoại lệ E16c: Lỗi kết nối đến cổng thanh toán
                if (orderResponse == null) {
                    request.setAttribute("error_msg", "Hệ thống thanh toán đang bận, vui lòng thử lại sau!");
                    request.getRequestDispatcher("/WEB-INF/order-review.jsp").forward(request, response);
                    return;
                }

                // POST-CONDITION: Kết nối thành công -> Xóa các sản phẩm đã mua khỏi giỏ hàng DB
                cartService.clearPurchasedItems(userId, cartResponse);

                // Xóa toàn bộ Session checkout tạm thời
                session.removeAttribute("checkoutCart");
                session.removeAttribute("checkoutShipping");

                // Bước 16: Lưu OrderResponse và chuyển hướng sang Use Case 1.29 Thanh toán
                request.setAttribute("orderResponse", orderResponse);
                request.getRequestDispatcher("/WEB-INF/payment-selection.jsp").forward(request, response);

            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("error_msg", "Hệ thống gặp sự cố trong quá trình xử lý đơn hàng!");
                request.getRequestDispatcher("/WEB-INF/order-review.jsp").forward(request, response);
            }
        }
        else if ("prepareOrder".equals(action)) {
            response.sendRedirect(request.getContextPath() + "/payment-selection.jsp");
        }/*
        else if ("processPayment".equals(action)) {
            // 1. Lấy dữ liệu từ session
            CartPageResponse cart = (CartPageResponse) session.getAttribute("checkoutCart");
            CheckoutRequest shipping = (CheckoutRequest) session.getAttribute("checkoutShipping");
            String paymentMethod = request.getParameter("paymentMethod");

            // 2. Gọi service thanh toán (do bạn của bạn làm)
            boolean isPaid = paymentService.process(paymentMethod, cart.getTotalAmount());

            if (isPaid) {
                // 3. THANH TOÁN THÀNH CÔNG -> MỚI TẠO ĐƠN HÀNG
                OrderResponse order = orderService.createOrder(userId, cart, shipping);
                cartService.clearPurchasedItems(userId, cart);
                session.removeAttribute("checkoutCart");
                session.removeAttribute("checkoutShipping");
                // Chuyển sang trang đặt hàng thành công
            } else {
                // Báo lỗi thanh toán
            }
        }*/
    }

    private void showReviewPage(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        CartPageResponse freshCart = cartService.getSelectedItemsCart(userId);

        if (freshCart == null || freshCart.getCartItems().isEmpty()) {
            session.removeAttribute("checkoutCart");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        session.setAttribute("checkoutCart", freshCart);
        request.getRequestDispatcher("/WEB-INF/order-review.jsp").forward(request, response);
    }
}