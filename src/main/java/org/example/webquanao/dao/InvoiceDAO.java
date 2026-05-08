package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.Invoice;
import org.example.webquanao.entity.Order;
import org.jdbi.v3.core.Jdbi;
import java.util.List;

public class InvoiceDAO {
    private Jdbi jdbi = DBConnect.get();

    public List<Invoice> getAllInvoices() {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM invoices ORDER BY created_at DESC")
                        .map((rs, ctx) -> {
                            Invoice inv = new Invoice();
                            inv.setInvoiceId(rs.getString("invoice_id"));
                            inv.setOrderId(rs.getString("order_id"));
                            inv.setAdminId(rs.getInt("admin_id"));
                            inv.setCustomerName(rs.getString("customer_name"));
                            inv.setTotalAmount(rs.getDouble("total_amount"));
                            inv.setPaymentMethod(rs.getString("payment_method"));
                            inv.setPaymentStatus(rs.getString("payment_status"));
                            inv.setCreatedAt(rs.getTimestamp("created_at"));
                            return inv;
                        })
                        .list()
        );
    }

    // 4a. Lấy danh sách các đơn hàng chưa có hóa đơn (để Admin chọn)
    public List<Order> getOrdersEligibleForInvoice() {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM orders o WHERE NOT EXISTS " +
                                "(SELECT 1 FROM invoices i WHERE i.order_id = o.order_id) " +
                                "AND o.status != 'Đã hủy'")
                        .mapToBean(Order.class)
                        .list()
        );
    }

    // 4g. Lưu hóa đơn vào cơ sở dữ liệu
    public boolean insertInvoice(Invoice invoice) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("INSERT INTO invoices (invoice_id, order_id, admin_id, customer_name, total_amount, payment_method, payment_status) " +
                                "VALUES (:invoiceId, :orderId, :adminId, :customerName, :totalAmount, :paymentMethod, :paymentStatus)")
                        .bindBean(invoice)
                        .execute() > 0
        );
    }

    public Invoice findById(String invoiceId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM invoices WHERE invoice_id = :id")
                        .bind("id", invoiceId)
                        .map((rs, ctx) -> {
                            Invoice inv = new Invoice();
                            inv.setInvoiceId(rs.getString("invoice_id"));
                            inv.setOrderId(rs.getString("order_id"));
                            inv.setCustomerName(rs.getString("customer_name"));
                            inv.setTotalAmount(rs.getDouble("total_amount"));
                            inv.setPaymentStatus(rs.getString("payment_status"));
                            return inv;
                        })
                        .findOne()
                        .orElse(null)
        );
    }

    // 5g. Cập nhật thông tin vào cơ sở dữ liệu
    public boolean updateInvoice(Invoice invoice) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("UPDATE invoices SET customer_name = :customerName, " +
                                "payment_method = :paymentMethod, payment_status = :paymentStatus " +
                                "WHERE invoice_id = :invoiceId")
                        .bindBean(invoice)
                        .execute() > 0
        );
    }

    // 6f & 6g. Đánh dấu trạng thái hóa đơn là "Đã hủy"
    public boolean softDeleteInvoice(String invoiceId) {
        try {
            return jdbi.withHandle(handle -> {
                int rows = handle.createUpdate("UPDATE invoices SET payment_status = 'Đã hủy' WHERE TRIM(invoice_id) = TRIM(:id)")
                        .bind("id", invoiceId)
                        .execute();
                return rows > 0;
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isInvoiceIdExists(String invoiceId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM invoices WHERE invoice_id = :id")
                        .bind("id", invoiceId)
                        .mapTo(Integer.class)
                        .one() > 0
        );
    }
}