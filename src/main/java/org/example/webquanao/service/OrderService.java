package org.example.webquanao.service;

import org.example.webquanao.dao.OrderDAO;
import org.example.webquanao.dto.request.AdminUpdateStatusRequest;
import org.example.webquanao.dto.request.CheckoutRequest;
import org.example.webquanao.dto.response.*;
import org.example.webquanao.dto.response.CartPageResponse.CartItemResponse;
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
        String uniqueOrderIdStr = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order();
        order.setOrderId(uniqueOrderIdStr);
        order.setUserId(userId);
        order.setFullName(checkoutRequest.getFullName());
        order.setPhone(checkoutRequest.getPhone());
        order.setAddress(checkoutRequest.getAddress());
        order.setTotalPrice(cartResponse.getTotalAmount());
        order.setStatus("Chờ xác nhận");

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

    public List<OrderHistoryResponse> getOrderHistoryList(int userId) {
        List<Order> orders = orderDAO.getOrdersByUserId(userId);
        List<OrderHistoryResponse> responseList = new ArrayList<>();

        for (Order order : orders) {
            OrderHistoryResponse dto = new OrderHistoryResponse(order);
            responseList.add(dto);
        }
        return responseList;
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    public OrderHistoryResponse getOrderDetailsForHistory(String orderId) {
        Order order = orderDAO.findById(orderId);
        if (order == null) return null;

        OrderHistoryResponse response = new OrderHistoryResponse(order);
        List<OrderDetail> details = orderDAO.getDetailsByOrderId(orderId);
        List<OrderDetailHistoryResponse> detailDTOs = new ArrayList<>();

        // Map Entity Detail sang DTO Detail (kèm thông tin sản phẩm)
        for (OrderDetail detail : details) {
            OrderDetailHistoryResponse dto = new OrderDetailHistoryResponse(
                    detail.getProductId(), "Tên sản phẩm", "Ảnh", // Bạn cần truyền data thực từ DB
                    detail.getQuantity(), detail.getPrice()
            );
            detailDTOs.add(dto);
        }
        response.setOrderDetails(detailDTOs);
        return response;
    }

    /**
     * Xử lý hủy đơn hàng
     * Hỗ trợ logic kiểm tra điều kiện và Transaction hoàn trả kho
     */
    public String processCancelOrder(String orderId, int userId, String reason) {
        // 1. Kiểm tra đơn hàng tồn tại và quyền sở hữu
        Order order = orderDAO.findById(orderId);
        if (order == null || order.getUserId() != userId) {
            return "ERROR_NOT_FOUND";
        }

        // 2. Kiểm tra trạng thái (E6a1)
        if (!"Chờ xác nhận".equals(order.getStatus())) {
            return "ERROR_INVALID_STATUS";
        }

        // 3. Thực hiện hủy đơn và hoàn trả kho (6a3, 6a4)
        List<OrderDetail> details = orderDAO.getDetailsByOrderId(orderId);
        boolean success = orderDAO.cancelOrderTransaction(orderId, reason, details);

        return success ? "SUCCESS" : "ERROR_SYSTEM";
    }

    public List<AdminOrderListResponse> getAllOrdersForAdmin() {
        List<Order> orders = orderDAO.selectAllOrders();
        List<AdminOrderListResponse> responseList = new ArrayList<>();
        for (Order order : orders) {
            responseList.add(new AdminOrderListResponse(order));
        }
        return responseList;
    }

    public String updateOrderStatusAdmin(AdminUpdateStatusRequest request) {
        // 1. Lấy đơn hàng hiện tại từ DB
        Order order = orderDAO.findById(request.getOrderId());
        if (order == null) return "NOT_FOUND";

        String currentStatus = order.getStatus();
        String nextStatus = request.getNewStatus();

        // 2. Logic kiểm soát trạng thái (Finite State Machine)
        // Chỉ cho phép chuyển trạng thái nếu đơn hàng đang ở "Chờ xác nhận"
        if (!"Chờ xác nhận".equals(currentStatus)) {
            return "INVALID_TRANSITION"; // Trạng thái hiện tại không cho phép chuyển đổi
        }

        // 3. Chỉ cho phép chuyển sang "Đã xác nhận" hoặc "Đã hủy"
        if ("Đã xác nhận".equals(nextStatus) || "Đã hủy".equals(nextStatus)) {
            boolean success = orderDAO.updateStatus(request.getOrderId(), nextStatus);
            return success ? "SUCCESS" : "ERROR_SYSTEM";
        }

        return "INVALID_STATUS"; // Trạng thái đích không hợp lệ
    }

    public OrderHistoryResponse getOrderDetailsForAdmin(String orderId) {
        Order order = orderDAO.findById(orderId);
        if (order == null) return null;

        OrderHistoryResponse response = new OrderHistoryResponse(order);
        List<Map<String, Object>> details = orderDAO.getDetailsWithProductInfo(orderId);
        List<OrderDetailHistoryResponse> detailDTOs = new ArrayList<>();

        for (Map<String, Object> item : details) {
            Object rawName = item.get("productName") != null ? item.get("productName") : item.get("productname");
            String pName = (rawName != null) ? String.valueOf(rawName) : "Không xác định";

            Object rawImg = item.get("productImage") != null ? item.get("productImage") : item.get("productimage");
            String pImage = (rawImg != null) ? String.valueOf(rawImg) : "";

            int pId = ((Number) item.get("product_id")).intValue();
            int qty = ((Number) item.get("quantity")).intValue();
            double price = ((Number) item.get("price")).doubleValue();

            OrderDetailHistoryResponse dto = new OrderDetailHistoryResponse(pId, pName, pImage, qty, price);
            detailDTOs.add(dto);
        }
        response.setOrderDetails(detailDTOs);
        return response;
    }

    public String processCancelOrderForAdmin(String orderId, String reason) {
        // 1. Chỉ kiểm tra đơn hàng có tồn tại hay không
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            return "ERROR_NOT_FOUND";
        }

        // 2. Kiểm tra trạng thái
        if (!"Chờ xác nhận".equals(order.getStatus())) {
            return "ERROR_INVALID_STATUS";
        }

        // 3. Thực hiện hủy đơn và hoàn trả kho
        List<OrderDetail> details = orderDAO.getDetailsByOrderId(orderId);
        boolean success = orderDAO.cancelOrderTransaction(orderId, reason, details);

        return success ? "SUCCESS" : "ERROR_SYSTEM";
    }
}