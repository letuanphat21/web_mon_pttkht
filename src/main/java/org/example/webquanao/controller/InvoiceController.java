package org.example.webquanao.controller;

import org.example.webquanao.entity.Invoice;
import org.example.webquanao.entity.Order; // Đảm bảo đã import Order
import org.example.webquanao.service.InvoiceService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/invoice")
public class InvoiceController extends HttpServlet {
    private final InvoiceService invoiceService = new InvoiceService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "list":
                List<Invoice> list = invoiceService.getAllInvoices();
                List<Order> eligibleOrders = invoiceService.getOrdersEligibleForInvoice();

                request.setAttribute("invoices", list);
                request.setAttribute("eligibleOrders", eligibleOrders);

                request.getRequestDispatcher("/WEB-INF/admin/invoice-list.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        Object adminObj = session.getAttribute("userId");
        Integer adminId = (adminObj instanceof Integer) ? (Integer) adminObj : null;

        String result = "";

        if ("add".equals(action)) {
            String orderId = request.getParameter("orderId");
            if (adminId != null && orderId != null && !orderId.isEmpty()) {
                result = invoiceService.createInvoiceFromOrder(orderId, adminId);
            } else if (orderId == null || orderId.isEmpty()) {
                result = "Lỗi: Vui lòng chọn một đơn hàng.";
            } else {
                result = "Lỗi: Phiên làm việc hết hạn, vui lòng đăng nhập lại.";
            }

        } else if ("update".equals(action)) {
            Invoice invoice = new Invoice();
            invoice.setInvoiceId(request.getParameter("invoiceId"));
            invoice.setOrderId(request.getParameter("orderId"));
            invoice.setCustomerName(request.getParameter("customerName"));
            invoice.setPaymentMethod(request.getParameter("paymentMethod"));
            invoice.setPaymentStatus(request.getParameter("paymentStatus"));

            try {
                String amountStr = request.getParameter("totalAmount");
                if (amountStr != null && !amountStr.isEmpty()) {
                    invoice.setTotalAmount(Double.parseDouble(amountStr));
                }
            } catch (NumberFormatException e) {
                result = "Lỗi: Định dạng số tiền không hợp lệ.";
            }

            if (result.isEmpty()) {
                result = invoiceService.updateInvoice(invoice);
            }

        } else if ("cancel".equals(action)) {
            String invoiceId = request.getParameter("invoiceId");

            System.out.println("--- POST CANCEL ACTION ---");
            System.out.println("Invoice ID to cancel: [" + invoiceId + "]");

            result = invoiceService.cancelInvoice(invoiceId);
        }

        if ("SUCCESS".equals(result)) {
            session.setAttribute("message", "Thực hiện thành công!");
        } else {
            session.setAttribute("error", result);
        }

        response.sendRedirect(request.getContextPath() + "/admin/invoice?action=list");
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        List<String> roles = (List<String>) session.getAttribute("roles");
        if (roles != null && roles.contains("ADMIN")) return true;

        Object roleId = session.getAttribute("roleId");
        return roleId != null && (roleId.toString().equals("2") || roleId.toString().equals("1"));
    }
}