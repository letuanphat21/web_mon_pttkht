package org.example.webquanao.dao;

import org.example.webquanao.db.DBConnect;
import org.jdbi.v3.core.Jdbi;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsDAO {
    private final Jdbi jdbi = DBConnect.get();

    private static final String SUCCESS_CONDITION = """
            (
                o.status IN ('Đã hoàn thành', 'Đã thanh toán thành công', 'Đã giao')
                OR i.payment_status IN ('Đã thanh toán', 'Đã thanh toán thành công')
            )
            AND o.status NOT IN ('Đã hủy', 'Hủy', 'Hoàn trả')
            AND (i.payment_status IS NULL OR i.payment_status NOT IN ('Đã hủy', 'Hoàn trả'))
            """;

    public Map<String, Object> getSummary(Timestamp startAt, Timestamp endAt, Integer categoryId) {
        String amountExpression = categoryId == null ? "o.total_price" : "od.quantity * od.price";
        String detailJoin = categoryId == null ? "" : " JOIN order_details od ON od.order_id = o.order_id JOIN products p ON p.productId = od.product_id ";
        String categoryFilter = categoryId == null ? "" : " AND p.categoryId = :categoryId ";

        return jdbi.withHandle(handle -> {
            var query = handle.createQuery("""
                    SELECT COUNT(DISTINCT o.order_id) AS order_count,
                           COALESCE(SUM(%s), 0) AS revenue,
                           COALESCE(SUM(%s), 0) AS sold_quantity
                    FROM orders o
                    LEFT JOIN invoices i ON i.order_id = o.order_id
                    %s
                    WHERE o.created_at >= :startAt
                      AND o.created_at < :endAt
                      AND %s
                      %s
                    """.formatted(
                    amountExpression,
                    categoryId == null ? "0" : "od.quantity",
                    detailJoin,
                    SUCCESS_CONDITION,
                    categoryFilter
            ))
                    .bind("startAt", startAt)
                    .bind("endAt", endAt);

            if (categoryId != null) {
                query.bind("categoryId", categoryId);
            }

            return query.map((rs, ctx) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("orderCount", rs.getInt("order_count"));
                row.put("revenue", rs.getDouble("revenue"));
                row.put("soldQuantity", rs.getInt("sold_quantity"));
                return row;
            }).one();
        });
    }

    public List<Map<String, Object>> getDailyRevenue(Timestamp startAt, Timestamp endAt, Integer categoryId) {
        String amountExpression = categoryId == null ? "o.total_price" : "od.quantity * od.price";
        String detailJoin = categoryId == null ? "" : " JOIN order_details od ON od.order_id = o.order_id JOIN products p ON p.productId = od.product_id ";
        String categoryFilter = categoryId == null ? "" : " AND p.categoryId = :categoryId ";

        return jdbi.withHandle(handle -> {
            var query = handle.createQuery("""
                    SELECT DATE(o.created_at) AS report_date,
                           COUNT(DISTINCT o.order_id) AS order_count,
                           COALESCE(SUM(%s), 0) AS revenue
                    FROM orders o
                    LEFT JOIN invoices i ON i.order_id = o.order_id
                    %s
                    WHERE o.created_at >= :startAt
                      AND o.created_at < :endAt
                      AND %s
                      %s
                    GROUP BY DATE(o.created_at)
                    ORDER BY report_date
                    """.formatted(amountExpression, detailJoin, SUCCESS_CONDITION, categoryFilter))
                    .bind("startAt", startAt)
                    .bind("endAt", endAt);

            if (categoryId != null) {
                query.bind("categoryId", categoryId);
            }

            return query.map((rs, ctx) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("label", rs.getString("report_date"));
                row.put("orderCount", rs.getInt("order_count"));
                row.put("revenue", rs.getDouble("revenue"));
                return row;
            }).list();
        });
    }

    public List<Map<String, Object>> getTopProducts(Timestamp startAt, Timestamp endAt, Integer categoryId) {
        String categoryFilter = categoryId == null ? "" : " AND p.categoryId = :categoryId ";

        return jdbi.withHandle(handle -> {
            var query = handle.createQuery("""
                    SELECT p.productId AS product_id,
                           p.productName AS product_name,
                           COALESCE(c.name, 'Chưa phân loại') AS category_name,
                           SUM(od.quantity) AS sold_quantity,
                           SUM(od.quantity * od.price) AS revenue
                    FROM order_details od
                    JOIN orders o ON o.order_id = od.order_id
                    LEFT JOIN invoices i ON i.order_id = o.order_id
                    JOIN products p ON p.productId = od.product_id
                    LEFT JOIN categories c ON c.id = p.categoryId
                    WHERE o.created_at >= :startAt
                      AND o.created_at < :endAt
                      AND %s
                      %s
                    GROUP BY p.productId, p.productName, c.name
                    ORDER BY sold_quantity DESC, revenue DESC
                    LIMIT 10
                    """.formatted(SUCCESS_CONDITION, categoryFilter))
                    .bind("startAt", startAt)
                    .bind("endAt", endAt);

            if (categoryId != null) {
                query.bind("categoryId", categoryId);
            }

            return query.map((rs, ctx) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productId", rs.getInt("product_id"));
                row.put("productName", rs.getString("product_name"));
                row.put("categoryName", rs.getString("category_name"));
                row.put("soldQuantity", rs.getInt("sold_quantity"));
                row.put("revenue", rs.getDouble("revenue"));
                return row;
            }).list();
        });
    }

    public List<Map<String, Object>> getRevenueByCategory(Timestamp startAt, Timestamp endAt, Integer categoryId) {
        String categoryFilter = categoryId == null ? "" : " AND p.categoryId = :categoryId ";

        return jdbi.withHandle(handle -> {
            var query = handle.createQuery("""
                    SELECT COALESCE(c.name, 'Chưa phân loại') AS category_name,
                           SUM(od.quantity) AS sold_quantity,
                           SUM(od.quantity * od.price) AS revenue
                    FROM order_details od
                    JOIN orders o ON o.order_id = od.order_id
                    LEFT JOIN invoices i ON i.order_id = o.order_id
                    JOIN products p ON p.productId = od.product_id
                    LEFT JOIN categories c ON c.id = p.categoryId
                    WHERE o.created_at >= :startAt
                      AND o.created_at < :endAt
                      AND %s
                      %s
                    GROUP BY c.id, c.name
                    ORDER BY revenue DESC
                    """.formatted(SUCCESS_CONDITION, categoryFilter))
                    .bind("startAt", startAt)
                    .bind("endAt", endAt);

            if (categoryId != null) {
                query.bind("categoryId", categoryId);
            }

            return query.map((rs, ctx) -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("categoryName", rs.getString("category_name"));
                row.put("soldQuantity", rs.getInt("sold_quantity"));
                row.put("revenue", rs.getDouble("revenue"));
                return row;
            }).list();
        });
    }

    public List<Map<String, Object>> getOrderStatusCounts(Timestamp startAt, Timestamp endAt) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT status, COUNT(*) AS order_count
                        FROM orders
                        WHERE created_at >= :startAt
                          AND created_at < :endAt
                        GROUP BY status
                        ORDER BY order_count DESC
                        """)
                        .bind("startAt", startAt)
                        .bind("endAt", endAt)
                        .map((rs, ctx) -> {
                            Map<String, Object> row = new LinkedHashMap<>();
                            row.put("status", rs.getString("status"));
                            row.put("orderCount", rs.getInt("order_count"));
                            return row;
                        })
                        .list()
        );
    }
}
