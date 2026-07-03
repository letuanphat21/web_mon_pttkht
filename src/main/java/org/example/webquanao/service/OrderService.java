package org.example.webquanao.service;

import org.example.webquanao.dao.OrderDAO;
import org.example.webquanao.dto.request.AdminUpdateStatusRequest;
import org.example.webquanao.dto.request.CheckoutRequest;
import org.example.webquanao.dto.response.*;
import org.example.webquanao.dto.response.CartPageResponse.CartItemResponse;
import org.example.webquanao.entity.Order;
import org.example.webquanao.entity.OrderDetail;
import org.example.webquanao.entity.Product;
import org.example.webquanao.entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductService productService = new ProductService();
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
    public OrderResponse createOrder(User user, CartPageResponse cartResponse, CheckoutRequest checkoutRequest) throws Exception {
        String uniqueOrderIdStr = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order();
        order.setOrderId(uniqueOrderIdStr);
        order.setUser(user);
        order.setFullName(checkoutRequest.getFullName());
        order.setPhone(checkoutRequest.getPhone());
        order.setAddress(checkoutRequest.getAddress());
        order.setTotalPrice(cartResponse.getTotalAmount());
        order.setStatus("Chờ xác nhận");

        List<OrderDetail> dbDetailsList = new ArrayList<>();
        List<OrderDetailResponse> dtoDetailsList = new ArrayList<>();

        for (CartItemResponse item : cartResponse.getCartItems()) {
            Product product = productService.findById(item.getId());
            OrderDetail detail = new OrderDetail(order, product, item.getQty(), item.getPrice());
            dbDetailsList.add(detail);
            dtoDetailsList.add(new OrderDetailResponse(item));
        }

        boolean isSuccess = orderDAO.insertOrder(order, dbDetailsList);
        if (!isSuccess) return null;

        checkoutRequest.setOrderRef(uniqueOrderIdStr);
        return new OrderResponse(0, dtoDetailsList, checkoutRequest);
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

    public String cancelOrder(String orderId, User user) {
        Order order = orderDAO.findById(orderId);
        if (order == null || order.getUserId() != user.getId()) {
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

    public String processCancelOrder(String orderId, User user, String reason) {
        Order order = orderDAO.findById(orderId);
        if (order == null || order.getUserId() != user.getId()) {
            return "ERROR_NOT_FOUND";
        }

        if (!"Chờ xác nhận".equals(order.getStatus())) {
            return "ERROR_INVALID_STATUS";
        }

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

        // Nếu trạng thái mới trùng với trạng thái cũ thì không cần xử lý, trả về thành công luôn
        if (currentStatus.equals(nextStatus)) {
            return "SUCCESS";
        }

        // 2. Logic kiểm soát vòng đời trạng thái (Finite State Machine)
        boolean isValidTransition = false;

        switch (currentStatus) {
            case "Chờ xác nhận":
                // Đơn COD hoặc đơn chưa xử lý: Được phép Xác nhận hoặc Hủy
                if ("Đã xác nhận".equals(nextStatus) || "Đã hủy".equals(nextStatus)) {
                    isValidTransition = true;
                }
                break;

            case "Đã xác nhận":
                // Đơn MoMo hoặc đơn COD đã duyệt: Chỉ được chuyển sang Đang giao hoặc Hủy
                if ("Đang giao".equals(nextStatus) || "Đã hủy".equals(nextStatus)) {
                    isValidTransition = true;
                }
                break;

            case "Đang giao":
                // Đơn đang đi đường: Chỉ được chuyển sang Đã giao (Hoàn thành) hoặc Đã hủy (Nếu ship thất bại)
                if ("Đã giao".equals(nextStatus) || "Đã hủy".equals(nextStatus)) {
                    isValidTransition = true;
                }
                break;

            case "Đã giao":
            case "Đã hủy":
                isValidTransition = false;
                break;
        }

        // 3. Thực thi cập nhật nếu luồng chuyển đổi hợp lệ
        if (isValidTransition) {
            if ("Đã hủy".equals(nextStatus)) {
                return processCancelOrderForAdmin(request.getOrderId(), "Admin cập nhật trạng thái");
            }

            boolean success = orderDAO.updateStatus(request.getOrderId(), nextStatus);
            return success ? "SUCCESS" : "ERROR_SYSTEM";
        }

        return "INVALID_TRANSITION";
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
        Order order = orderDAO.findById(orderId);
        if (order == null) {
            return "ERROR_NOT_FOUND";
        }

        if ("Đã hủy".equals(order.getStatus())) {
            return "ERROR_INVALID_STATUS";
        }

        List<OrderDetail> details = orderDAO.getDetailsByOrderId(orderId);
        boolean success = orderDAO.cancelOrderTransaction(orderId, reason, details);

        return success ? "SUCCESS" : "ERROR_SYSTEM";
    }
}