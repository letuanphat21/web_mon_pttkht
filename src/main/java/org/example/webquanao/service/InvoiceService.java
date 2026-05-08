package org.example.webquanao.service;

import org.example.webquanao.dao.InvoiceDAO;
import org.example.webquanao.dao.OrderDAO;
import org.example.webquanao.entity.Invoice;
import org.example.webquanao.entity.Order;

import java.util.List;
import java.util.UUID;

public class InvoiceService {
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private OrderDAO orderDAO = new OrderDAO();

    public List<Invoice> getAllInvoices() {
        return invoiceDAO.getAllInvoices();
    }

    // 4a. Lấy danh sách đơn hàng hợp lệ để lập hóa đơn
    public List<Order> getOrdersEligibleForInvoice() {
        return invoiceDAO.getOrdersEligibleForInvoice();
    }

    // 4f, 4g. Tạo hóa đơn mới từ đơn hàng
    public String createInvoiceFromOrder(String orderId, Integer adminId) {
        Order order = orderDAO.findById(orderId);
        if (order == null) return "E4d4: Đơn hàng không tồn tại.";

        // E4d. Kiểm tra trạng thái đơn hàng
        if ("Đang gửi".equals(order.getStatus())) {
            // E4d3. Chuyển sang xác nhận và tiếp tục
            orderDAO.updateOrderStatus(orderId, "Xác nhận");
        } else if (!"Chờ xác nhận".equals(order.getStatus()) && !"Xác nhận".equals(order.getStatus())) {
            return "E4d4: Trạng thái đơn hàng không hợp lệ để tạo hóa đơn.";
        }

        // 4f. Tạo mã hóa đơn duy nhất
        String invoiceId = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 4b. Trích xuất thông tin từ đơn hàng
        Invoice newInvoice = new Invoice();
        newInvoice.setInvoiceId(invoiceId);
        newInvoice.setOrderId(orderId);
        newInvoice.setAdminId(adminId);
        newInvoice.setCustomerName(order.getFullName());
        newInvoice.setTotalAmount(order.getTotalPrice()); // BR1.7-2. Lấy giá gốc
        newInvoice.setPaymentMethod("Tiền mặt");
        newInvoice.setPaymentStatus("Chưa thanh toán");

        boolean success = invoiceDAO.insertInvoice(newInvoice);
        return success ? "SUCCESS" : "Lỗi hệ thống khi lưu hóa đơn.";
    }

    public String updateInvoice(Invoice updatedInvoice) {
        // E5e1.1. Kiểm tra bỏ trống/định dạng
        if (updatedInvoice.getCustomerName() == null || updatedInvoice.getCustomerName().trim().isEmpty()) {
            return "E5e1.1: Tên khách hàng không được để trống.";
        }

        // BR1.7-2 & E5e1.2. Kiểm tra tổng tiền không được thay đổi so với đơn hàng gốc
        Order originalOrder = orderDAO.findById(updatedInvoice.getOrderId());
        if (originalOrder != null && updatedInvoice.getTotalAmount() != originalOrder.getTotalPrice()) {
            return "E5e1.2: Tổng tiền không được thay đổi so với đơn hàng gốc (" + originalOrder.getTotalPrice() + ")";
        }

        boolean success = invoiceDAO.updateInvoice(updatedInvoice);
        return success ? "SUCCESS" : "Cập nhật thất bại.";
    }

    public String cancelInvoice(String invoiceId) {
        boolean success = invoiceDAO.softDeleteInvoice(invoiceId);
        return success ? "SUCCESS" : "Không thể xóa hóa đơn này.";
    }
}