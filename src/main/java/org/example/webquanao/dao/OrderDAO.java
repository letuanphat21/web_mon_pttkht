package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.Order;
import org.example.webquanao.entity.OrderDetail;
import org.jdbi.v3.core.Jdbi;
import java.util.List;

public class OrderDAO {
    private Jdbi jdbi;

    private Jdbi getJdbi() {
        if (jdbi == null) jdbi = DBConnect.get();
        return jdbi;
    }

    public boolean insertOrder(Order order, List<OrderDetail> details) {
        try {
            getJdbi().useTransaction(handle -> {
                handle.createUpdate("INSERT INTO orders (order_id, user_id, full_name, phone, address, total_price, status) " +
                                "VALUES (:orderId, :userId, :fullName, :phone, :address, :totalPrice, :status)")
                        .bindBean(order)
                        .execute();

                var preparedBatch = handle.prepareBatch("INSERT INTO order_details (order_id, product_id, quantity, price) " +
                        "VALUES (:orderId, :productId, :quantity, :price)");

                for (OrderDetail item : details) {
                    preparedBatch.bindBean(item).add();
                }

                preparedBatch.execute();
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Order findById(String orderId) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM orders WHERE order_id = :id")
                        .bind("id", orderId)
                        .mapToBean(Order.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public List<Order> getOrdersByUserId(int userId) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM orders WHERE user_id = :userId ORDER BY created_at DESC")
                        .bind("userId", userId)
                        .mapToBean(Order.class)
                        .list()
        );
    }

    public List<OrderDetail> getDetailsByOrderId(String orderId) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM order_details WHERE order_id = :orderId")
                        .bind("orderId", orderId)
                        .mapToBean(OrderDetail.class)
                        .list()
        );
    }

    public boolean updateOrderStatus(String orderId, String status) {
        return getJdbi().withHandle(handle ->
                handle.createUpdate("UPDATE orders SET status = :status WHERE order_id = :id")
                        .bind("status", status)
                        .bind("id", orderId)
                        .execute() > 0
        );
    }
}