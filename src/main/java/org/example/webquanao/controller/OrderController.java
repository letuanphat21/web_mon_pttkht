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
import org.example.webquanao.entity.User;
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
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if ("editShipping".equals(action)) {
            CheckoutRequest shippingInfo = (CheckoutRequest) session.getAttribute("checkoutShipping");
            if (shippingInfo != null) {
                request.setAttribute("pendingOrder", shippingInfo);
            }

            request.setAttribute("cartData", cartService.getCartPageDetails(user, request.getSession()));
            request.setAttribute("openOrderForm", true);
            request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);
        } else if ("returnToCart".equals(action)) {
            // Luồng 14b2: Điều hướng nz`gười dùng quay về bước 3 (Giỏ hàng ban đầu)
            response.sendRedirect(request.getContextPath() + "/cart");

        } else if ("review".equals(action)) {
            try {
                showReviewPage(request, response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if ("backToCart".equals(action)) {
            CheckoutRequest shipping = (CheckoutRequest) session.getAttribute("checkoutShipping");
            if (shipping != null) {
                request.setAttribute("pendingOrder", shipping);
            }

            request.setAttribute("cartData", cartService.getCartPageDetails(user, session));
            request.setAttribute("openOrderForm", true);
            request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/cart");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        Integer userId = (Integer) session.getAttribute("userId");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        // LUỒNG 5 -> 8: KIỂM TRA & KHỞI TẠO ĐẶT HÀNG (Nhấn Đặt hàng tại cart.jsp)
        if ("proceedToCheckout".equals(action)) {
            try {
                CartPageResponse resCartCheck = cartService.getSelectedItemsCart(user);

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

                request.setAttribute("cartData", cartService.getCartPageDetails(user, session));
                request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);

            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/cart");
            }
        }

        // LUỒNG 9 -> 13: NHẬP LIỆU VÀ KIỂM TRA THÔNG TIN (Nhấn "Tiếp theo")
        else if ("validateShipping".equals(action)) {
            CartPageResponse cartResponse = (CartPageResponse) session.getAttribute("checkoutCart");
            if (cartResponse == null) {
                try {
                    cartResponse = cartService.getSelectedItemsCart(user);
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
                request.setAttribute("cartData", cartService.getCartPageDetails(user, session));
                request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);
                return;
            }

            session.setAttribute("checkoutShipping", reqCheckout);
            response.sendRedirect(request.getContextPath() + "/order-process?action=review");
        }

        // LUỒNG 15 -> 16: XÁC NHẬN ĐẶT HÀNG & CHUYỂN THANH TOÁN (Xác nhận đặt hàng)
        else if ("confirmOrder".equals(action)) {
            try {
                System.out.println("=== DEBUG THANH TOAN ===");
                CartPageResponse cartResponse = (CartPageResponse) session.getAttribute("checkoutCart");
                CheckoutRequest checkoutRequest = (CheckoutRequest) session.getAttribute("checkoutShipping");

                System.out.println("CartResponse tu session: " + (cartResponse != null ? "Co data" : "NULL"));
                System.out.println("CheckoutRequest tu session: " + (checkoutRequest != null ? "Co data" : "NULL"));

                if (cartResponse == null || checkoutRequest == null) {
                    System.out.println("-> LOI: Session checkout bi thieu, quay ve /cart");
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }

                OrderResponse orderResponse = orderService.createOrder(user, cartResponse, checkoutRequest);

                if (orderResponse == null) {
                    request.setAttribute("error_msg", "Hệ thống bận, không thể tạo đơn hàng. Vui lòng thử lại!");
                    request.getRequestDispatcher("/WEB-INF/cart.jsp").forward(request, response);
                    return;
                }

                session.setAttribute("pendingOrder", orderResponse);

                System.out.println("-> Đã tạo đơn hàng thành công. Chuyển hướng sang trang chọn PTTT...");
                response.sendRedirect(request.getContextPath() + "/payment");

            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/cart");
            }
        }
    }

    private void showReviewPage(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (session.getAttribute("checkoutShipping") == null) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        CartPageResponse freshCart = cartService.getSelectedItemsCart(user);

        if (freshCart == null || freshCart.getCartItems().isEmpty()) {
            session.removeAttribute("checkoutCart");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        session.setAttribute("checkoutCart", freshCart);
        request.getRequestDispatcher("/WEB-INF/order-review.jsp").forward(request, response);
    }
}