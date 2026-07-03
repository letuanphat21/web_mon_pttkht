package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.webquanao.dto.response.CartPageResponse;
import org.example.webquanao.dto.response.OrderResponse;
import org.example.webquanao.entity.User;
import org.example.webquanao.service.CartService;
import org.example.webquanao.service.PaymentService;

import java.io.IOException;

/**
 * Controller tiếp nhận và điều phối các yêu cầu thanh toán (COD hoặc MoMo).
 */
@WebServlet("/payment")
public class PaymentController extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();
    private final CartService cartService = new CartService(); // Dùng để dọn dẹp giỏ hàng sau khi chốt đơn
    private static final String BASE_RETURN_URL = "http://localhost:8080";

    /**
     * LUỒNG GET: Hiển thị giao diện chọn phương thức thanh toán
     * hoặc tiếp nhận kết quả trả về từ cổng thanh toán MoMo.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // 1. Nhánh xử lý MoMo trả kết quả về (MoMo Callback)
        if ("momo-return".equals(action)) {
            handleMomoReturn(request, response);
            return;
        }

        // 2. Nhánh hiển thị giao diện chọn PTTT
        HttpSession session = request.getSession();
        OrderResponse orderResponse = (OrderResponse) session.getAttribute("pendingOrder");

        // Nếu không tồn tại đơn hàng chờ thanh toán trong session, điều hướng về giỏ hàng
        if (orderResponse == null) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        request.setAttribute("orderResponse", orderResponse);
        request.getRequestDispatcher("/WEB-INF/payment-selection.jsp").forward(request, response);
    }

    /**
     * LUỒNG POST: Xử lý khi người dùng bấm xác nhận thanh toán (COD hoặc chuyển hướng MoMo).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String method = request.getParameter("paymentMethod");

        OrderResponse orderResponse = (OrderResponse) session.getAttribute("pendingOrder");
        if (orderResponse == null) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // --- NHÁNH 1: THANH TOÁN KHI NHẬN HÀNG (COD) ---
        if ("COD".equals(method)) {
            String dbOrderId = orderResponse.getShippingInfo() != null
                    && orderResponse.getShippingInfo().getOrderRef() != null
                    ? orderResponse.getShippingInfo().getOrderRef()
                    : String.valueOf(orderResponse.getOrderId());

            // XỬ LÝ NGHIỆP VỤ COD: Cập nhật đơn hàng sang trạng thái "Chờ xác nhận" dưới DB
            boolean orderExists = paymentService.processCODPayment(dbOrderId);

            if (orderExists) {
                // POST-CONDITION: Dọn dẹp giỏ hàng (DB/Session) và reset số lượng trên Badge (Navbar)
                try {
                    User user = (User) session.getAttribute("user");
                    CartPageResponse purchasedCart = (CartPageResponse) session.getAttribute("checkoutCart");

                    if (user != null) {
                        if (purchasedCart != null) {
                            cartService.clearPurchasedItems(user, purchasedCart);
                        }
                        session.setAttribute("totalCartCount", 0); // Reset badge thành viên về 0
                    } else {
                        session.removeAttribute("cart");           // Xóa giỏ hàng tạm của khách vãng lai
                        session.removeAttribute("totalCartCount");
                    }
                } catch (Exception e) {
                    System.err.println("LOG_ERROR: Không thể dọn dẹp giỏ hàng sau khi đặt COD! Chi tiết: " + e.getMessage());
                    e.printStackTrace();
                }

                // Dọn dẹp các session checkout tạm thời
                session.removeAttribute("checkoutCart");
                session.removeAttribute("checkoutShipping");
                session.removeAttribute("pendingOrder");

                request.setAttribute("message", "Đặt hàng thành công! Đơn hàng của bạn đang ở trạng thái Chờ xác nhận.");
                request.setAttribute("orderId", dbOrderId);
                request.getRequestDispatcher("/WEB-INF/payment-success.jsp").forward(request, response);
            } else {
                request.setAttribute("error", "Không tìm thấy đơn hàng. Vui lòng thử lại.");
                request.setAttribute("orderResponse", orderResponse);
                request.getRequestDispatcher("/WEB-INF/payment-selection.jsp").forward(request, response);
            }
        }
        // --- NHÁNH 2: THANH TOÁN ĐIỆN TỬ (MOMO) ---
        else if ("MOMO".equals(method)) {
            try {
                String momoDbOrderId = orderResponse.getShippingInfo() != null
                        && orderResponse.getShippingInfo().getOrderRef() != null
                        ? orderResponse.getShippingInfo().getOrderRef()
                        : String.valueOf(orderResponse.getOrderId());
                session.setAttribute("momoMappedOrderId", momoDbOrderId);

                // Tạo URL thanh toán MoMo và chuyển hướng người dùng
                String payUrl = paymentService.createMomoUrl(orderResponse, request.getContextPath(), BASE_RETURN_URL);
                response.sendRedirect(payUrl);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("error", "Lỗi kết nối MoMo: " + e.getMessage());
                request.setAttribute("orderResponse", orderResponse);
                request.getRequestDispatcher("/WEB-INF/payment-selection.jsp").forward(request, response);
            }
        }
    }

    /**
     * Xử lý kết quả trả về từ MoMo (Callback/Redirect URL).
     */
    private void handleMomoReturn(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String resultCode = request.getParameter("resultCode");
        HttpSession session = request.getSession();
        String dbOrderId = (String) session.getAttribute("momoMappedOrderId");

        // resultCode = "0" nghĩa là giao dịch thanh toán MoMo thành công
        if ("0".equals(resultCode) && dbOrderId != null) {

            // XỬ LÝ NGHIỆP VỤ MOMO THÀNH CÔNG: Cập nhật đơn hàng sang trạng thái "Đã xác nhận" dưới DB
            paymentService.processMomoSuccess(dbOrderId);

            // POST-CONDITION: Dọn dẹp giỏ hàng và đồng bộ Badge số lượng
            try {
                User user = (User) session.getAttribute("user");
                CartPageResponse purchasedCart = (CartPageResponse) session.getAttribute("checkoutCart");

                if (user != null) {
                    if (purchasedCart != null) {
                        cartService.clearPurchasedItems(user, purchasedCart);
                    }
                    session.setAttribute("totalCartCount", 0);
                } else {
                    session.removeAttribute("cart");
                    session.removeAttribute("totalCartCount");
                }
            } catch (Exception e) {
                System.err.println("LOG_ERROR: Không thể dọn dẹp giỏ hàng sau thanh toán MoMo! Chi tiết: " + e.getMessage());
                e.printStackTrace();
            }

            // Dọn dẹp toàn bộ session checkout
            session.removeAttribute("checkoutCart");
            session.removeAttribute("checkoutShipping");
            session.removeAttribute("pendingOrder");
            session.removeAttribute("momoMappedOrderId");

            request.setAttribute("message", "Thanh toán MoMo thành công! Đơn hàng đã tự động xác nhận.");
            request.setAttribute("orderId", dbOrderId);
            request.getRequestDispatcher("/WEB-INF/payment-success.jsp").forward(request, response);
        } else {
            // Thanh toán thất bại / người dùng hủy -> Chuyển trạng thái đơn về "Đã hủy" để giải phóng tồn kho
            if (dbOrderId != null) {
                try { paymentService.cancelPendingOrder(dbOrderId); }
                catch (Exception ignored) {}
            }
            session.removeAttribute("momoMappedOrderId");

            OrderResponse orderResponse = (OrderResponse) session.getAttribute("pendingOrder");
            String errMsg = "0".equals(resultCode)
                    ? "Thanh toán MoMo thất bại."
                    : "Thanh toán MoMo đã bị huỷ bỏ.";
            request.setAttribute("error", errMsg);
            request.setAttribute("orderResponse", orderResponse);
            request.getRequestDispatcher("/WEB-INF/payment-selection.jsp").forward(request, response);
        }
    }
}