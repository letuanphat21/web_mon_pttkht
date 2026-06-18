package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.Order;
import org.example.webquanao.entity.OrderDetail;
import org.jdbi.v3.core.Jdbi;
import java.util.List;
import java.util.Map;

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
                        .map(org.jdbi.v3.core.mapper.reflect.BeanMapper.of(Order.class))
                        .findOne()
                        .orElse(null)
        );
    }

    public List<Order> getOrdersByUserId(int userId) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM orders WHERE user_id = :userId ORDER BY created_at DESC")
                        .bind("userId", userId)
                        .map(org.jdbi.v3.core.mapper.reflect.BeanMapper.of(Order.class))
                        .list()
        );
    }

    public List<OrderDetail> getDetailsByOrderId(String orderId) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM order_details WHERE order_id = :orderId")
                        .bind("orderId", orderId)
                        .map(org.jdbi.v3.core.mapper.reflect.BeanMapper.of(OrderDetail.class))
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

    public boolean updateOrderStatusAndReason(String orderId, String status, String reason) {
        return getJdbi().withHandle(handle ->
                handle.createUpdate("UPDATE orders SET status = :status, cancel_reason = :reason WHERE order_id = :id")
                        .bind("status", status)
                        .bind("reason", reason)
                        .bind("id", orderId)
                        .execute() > 0
        );
    }

    public boolean cancelOrderTransaction(String orderId, String reason, List<OrderDetail> details) {
        try {
            getJdbi().useTransaction(handle -> {
                handle.createUpdate("UPDATE orders SET status = 'Đã hủy', cancel_reason = :reason WHERE order_id = :id")
                        .bind("reason", reason)
                        .bind("id", orderId)
                        .execute();

                for (OrderDetail item : details) {
                    handle.createUpdate("UPDATE products SET quantity = quantity + :qty WHERE productId = :pid")
                            .bind("qty", item.getQuantity())
                            .bind("pid", item.getProductId())
                            .execute();
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> selectAllOrders() {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM orders ORDER BY created_at DESC")
                        .map(org.jdbi.v3.core.mapper.reflect.BeanMapper.of(Order.class))
                        .list()
        );
    }

    public List<Map<String, Object>> getDetailsWithProductInfo(String orderId) {
        return getJdbi().withHandle(handle ->
                handle.createQuery(
                                "SELECT od.*, p.productName, p.productImage " +
                                        "FROM order_details od " +
                                        "JOIN products p ON od.product_id = p.productId " +
                                        "WHERE od.order_id = :orderId")
                        .bind("orderId", orderId)
                        .mapToMap()
                        .list()
        );
    }

    public boolean updateStatus(String orderId, String status) {
        return getJdbi().withHandle(handle ->
                handle.createUpdate("UPDATE orders SET status = :status WHERE order_id = :id")
                        .bind("status", status)
                        .bind("id", orderId)
                        .execute() > 0
        );
    }
}