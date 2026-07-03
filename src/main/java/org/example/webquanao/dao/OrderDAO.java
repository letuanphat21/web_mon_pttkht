package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.example.webquanao.entity.Order;
import org.example.webquanao.entity.OrderDetail;
import org.example.webquanao.entity.Product;
import org.example.webquanao.entity.User;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrderDAO {
    private Jdbi jdbi;

    private Jdbi getJdbi() {
        if (jdbi == null) jdbi = DBConnect.get();
        return jdbi;
    }

    private static class OrderMapper implements RowMapper<Order> {
        @Override
        public Order map(ResultSet rs, StatementContext ctx) throws SQLException {
            Order order = new Order();
            order.setOrderId(rs.getString("order_id"));
            order.setFullName(rs.getString("full_name"));
            order.setPhone(rs.getString("phone"));
            order.setAddress(rs.getString("address"));
            order.setTotalPrice(rs.getDouble("total_price"));
            order.setStatus(rs.getString("status"));

            User user = new User();
            user.setId(rs.getInt("user_id"));
            order.setUser(user);
            return order;
        }
    }

    private static class OrderDetailMapper implements RowMapper<OrderDetail> {
        @Override
        public OrderDetail map(ResultSet rs, StatementContext ctx) throws SQLException {
            OrderDetail detail = new OrderDetail();
            detail.setId(rs.getInt("id"));
            detail.setQuantity(rs.getInt("quantity"));
            detail.setPrice(rs.getDouble("price"));

            Order order = new Order();
            order.setOrderId(rs.getString("order_id"));
            detail.setOrder(order);

            Product product = new Product();
            product.setProductId(rs.getInt("product_id"));
            detail.setProduct(product);

            return detail;
        }
    }

    public boolean insertOrder(Order order, List<OrderDetail> details) {
        try {
            getJdbi().useTransaction(handle -> {
                handle.createUpdate("INSERT INTO orders (order_id, user_id, full_name, phone, address, total_price, status) " +
                                "VALUES (:orderId, :userId, :fullName, :phone, :address, :totalPrice, :status)")
                        .bind("orderId", order.getOrderId())
                        .bind("userId", order.getUser().getId())
                        .bind("fullName", order.getFullName())
                        .bind("phone", order.getPhone())
                        .bind("address", order.getAddress())
                        .bind("totalPrice", order.getTotalPrice())
                        .bind("status", order.getStatus())
                        .execute();

                var preparedBatch = handle.prepareBatch("INSERT INTO order_details (order_id, product_id, quantity, price) " +
                        "VALUES (:orderId, :productId, :quantity, :price)");

                for (OrderDetail item : details) {
                    preparedBatch.bind("orderId", item.getOrder().getOrderId())
                            .bind("productId", item.getProduct().getProductId())
                            .bind("quantity", item.getQuantity())
                            .bind("price", item.getPrice())
                            .add();
                }
                preparedBatch.execute();

                for (OrderDetail item : details) {
                    int updatedRows = handle.createUpdate("UPDATE products SET quantity = quantity - :qty WHERE productId = :pid AND quantity >= :qty")
                            .bind("qty", item.getQuantity())
                            .bind("pid", item.getProduct().getProductId())
                            .execute();
                    if (updatedRows == 0) {
                        throw new RuntimeException("Sản phẩm " + item.getProduct().getProductName() + " không đủ tồn kho!");
                    }
                }
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
                        .map(new OrderMapper())
                        .findOne()
                        .orElse(null)
        );
    }

    public List<Order> getOrdersByUserId(int userId) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM orders WHERE user_id = :userId ORDER BY created_at DESC")
                        .bind("userId", userId)
                        .map(new OrderMapper())
                        .list()
        );
    }

    public List<OrderDetail> getDetailsByOrderId(String orderId) {
        return getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM order_details WHERE order_id = :orderId")
                        .bind("orderId", orderId)
                        .map(new OrderDetailMapper())
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
                            .bind("pid", item.getProduct().getProductId())
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
                        .map(new OrderMapper())
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