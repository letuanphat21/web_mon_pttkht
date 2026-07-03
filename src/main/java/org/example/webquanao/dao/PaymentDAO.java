package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.Payment;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;

public class PaymentDAO {
    private Jdbi jdbi;

    private Jdbi getJdbi() {
        if (jdbi == null) jdbi = DBConnect.get();
        return jdbi;
    }

    /**
     * Tạo bản ghi thanh toán mới cho 1 đơn hàng (gọi khi bắt đầu quy trình COD hoặc trước khi redirect MoMo)
     */
    public boolean insertPayment(Payment payment) {
        return getJdbi().withHandle(handle ->
                handle.createUpdate("INSERT INTO payments (order_id, method, amount, momo_order_id, request_id, result_code, payment_status) " +
                                "VALUES (:orderId, :method, :amount, :momoOrderId, :requestId, :resultCode, :paymentStatus)")
                        .bindBean(payment)
                        .execute() > 0
        );
    }

    public Payment findByOrderId(String orderId) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM payments WHERE order_id = :orderId ORDER BY created_at DESC LIMIT 1")
                        .bind("orderId", orderId)
                        .map(BeanMapper.of(Payment.class))
                        .findOne()
                        .orElse(null)
        );
    }

    /**
     * Cập nhật trạng thái thanh toán (vd: "Đã thanh toán", "Thất bại", "Đã hủy") và set paid_at = NOW() nếu thành công
     */
    public boolean updatePaymentStatus(String orderId, String paymentStatus, boolean setPaidAt) {
        return getJdbi().withHandle(handle -> {
            String sql = "UPDATE payments SET payment_status = :status" +
                    (setPaidAt ? ", paid_at = CURRENT_TIMESTAMP" : "") +
                    " WHERE order_id = :orderId";
            return handle.createUpdate(sql)
                    .bind("status", paymentStatus)
                    .bind("orderId", orderId)
                    .execute() > 0;
        });
    }

    /**
     * Lưu lại thông tin MoMo trả về (momoOrderId, requestId, resultCode) sau khi có response từ cổng thanh toán
     */
    public boolean updateMomoInfo(String orderId, String momoOrderId, String requestId, int resultCode) {
        return getJdbi().withHandle(handle ->
                handle.createUpdate("UPDATE payments SET momo_order_id = :momoOrderId, request_id = :requestId, result_code = :resultCode " +
                                "WHERE order_id = :orderId")
                        .bind("momoOrderId", momoOrderId)
                        .bind("requestId", requestId)
                        .bind("resultCode", resultCode)
                        .bind("orderId", orderId)
                        .execute() > 0
        );
    }
}