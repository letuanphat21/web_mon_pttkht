package org.example.webquanao.service;

import org.example.webquanao.dao.OrderDAO;
import org.example.webquanao.dto.request.CheckoutRequest;
import org.example.webquanao.dto.response.CartPageResponse;
import org.example.webquanao.dto.response.CartPageResponse.CartItemResponse;
import org.example.webquanao.dto.response.OrderDetailResponse;
import org.example.webquanao.dto.response.OrderResponse;
import org.example.webquanao.entity.Order;
import org.example.webquanao.entity.OrderDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderService {
    private final OrderDAO orderDAO = new OrderDAO();

    /**
     * LUỒNG 9 -> 13: Xác thực thông tin giao hàng dựa trên DTO đầu vào (BR1.28-1)
     */
    public Map<String, String> validateShippingInfo(CheckoutRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            errors.put("fullName", "Họ và tên người nhận không được để trống.");
        }

        if (request.getPhone() == null || !request.getPhone().matches("^0\\d{9}$")) {
            errors.put("phone", "Số điện thoại không hợp lệ (Phải bắt đầu bằng 0 và gồm 10 chữ số).");
        }

        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            errors.put("address", "Địa chỉ giao hàng không được để trống.");
        }

        return errors;
    }

    /**
     * LUỒNG 15: Tạo lập đơn hàng hoàn chỉnh vào hệ thống Cơ sở dữ liệu
     */
    public OrderResponse createOrder(int userId, CartPageResponse cartResponse, CheckoutRequest checkoutRequest) throws Exception {
        // 1. Tạo chuỗi mã đơn hàng ngẫu nhiên duy nhất
        String uniqueOrderIdStr = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 2. Thiết lập thực thể Entity Order gốc để lưu xuống DB qua DAO cũ
        Order order = new Order();
        order.setOrderId(uniqueOrderIdStr);
        order.setUserId(userId);
        order.setFullName(checkoutRequest.getFullName());
        order.setPhone(checkoutRequest.getPhone());
        order.setAddress(checkoutRequest.getAddress());
        order.setTotalPrice(cartResponse.getTotalAmount());
        order.setStatus("Chờ xác nhận");

        // 3. Duyệt chuyển đổi danh sách từ CartItemResponse sang OrderDetail thực thể
        List<OrderDetail> dbDetailsList = new ArrayList<>();
        List<OrderDetailResponse> dtoDetailsList = new ArrayList<>();

        for (CartItemResponse item : cartResponse.getCartItems()) {
            // Đối tượng thực thể ghi xuống DB
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(uniqueOrderIdStr);
            detail.setProductId(item.getId());
            detail.setQuantity(item.getQty());
            detail.setPrice(item.getPrice());
            dbDetailsList.add(detail);

            // Đối tượng DTO đóng gói trả về phía Controller hiển thị
            OrderDetailResponse dtoDetail = new OrderDetailResponse(item);
            dtoDetailsList.add(dtoDetail);
        }

        // 4. Gọi tầng DAO thực thi lưu trữ xuống Database bằng Transaction liên hoàn
        boolean isSuccess = orderDAO.insertOrder(order, dbDetailsList);

        if (!isSuccess) {
            // Trả về null ứng với Ngoại lệ E16c (Hệ thống bận / Lỗi kết nối DB)
            return null;
        }

        // 5. Khởi tạo đối tượng DTO OrderResponse hoàn chỉnh truyền ra cho Controller (Bước 15)
        // Vì DAO cũ của bạn thiết lập Id là chuỗi String (UUID), ta bóc tách mã băm thành số int
        // hoặc chuyển đổi phù hợp để tương thích cấu trúc OrderResponse(int, list, checkoutRequest)
        int numericOrderId = Math.abs(uniqueOrderIdStr.hashCode());

        return new OrderResponse(numericOrderId, dtoDetailsList, checkoutRequest);
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC QUẢN LÝ ĐƠN HÀNG CŨ (Giữ nguyên tính năng)
    // =========================================================================
    public Order getOrderById(String orderId) {
        return orderDAO.findById(orderId);
    }

    public List<OrderDetail> getOrderDetails(String orderId) {
        return orderDAO.getDetailsByOrderId(orderId);
    }

    public List<Order> getUserOrderHistory(int userId) {
        return orderDAO.getOrdersByUserId(userId);
    }

    public String cancelOrder(String orderId, int userId) {
        Order order = orderDAO.findById(orderId);

        if (order == null || order.getUserId() != userId) {
            return "Bạn không có quyền thực hiện thao tác này.";
        }

        if (!"Chờ xác nhận".equals(order.getStatus())) {
            return "Đơn hàng đang trong quá trình xử lý hoặc vận chuyển, không thể hủy.";
        }

        boolean success = orderDAO.updateOrderStatus(orderId, "Đã hủy");
        return success ? "SUCCESS" : "Lỗi hệ thống khi hủy đơn hàng.";
    }
}