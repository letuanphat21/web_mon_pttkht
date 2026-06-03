package org.example.webquanao.service;

import com.google.gson.Gson;
import org.example.webquanao.action.Result;
import org.example.webquanao.dao.CategoryDAO;
import org.example.webquanao.dao.StatisticsDAO;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsService {
    private final StatisticsDAO statisticsDAO = new StatisticsDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final Gson gson = new Gson();

    public Result getReport(String type, String startDateRaw, String endDateRaw, String categoryIdRaw) {
        String selectedType = normalizeType(type);
        LocalDate startDate = parseDate(startDateRaw);
        LocalDate endDate = parseDate(endDateRaw);

        if (startDate == null && endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        } else if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return Result.fail("Dữ liệu không hợp lệ");
        }

        Integer categoryId = parseCategoryId(categoryIdRaw);
        Timestamp startAt = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endAt = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());

        Map<String, Object> summary = statisticsDAO.getSummary(startAt, endAt, categoryId);
        List<Map<String, Object>> dailyRevenue = statisticsDAO.getDailyRevenue(startAt, endAt, categoryId);
        List<Map<String, Object>> topProducts = statisticsDAO.getTopProducts(startAt, endAt, categoryId);
        List<Map<String, Object>> categoryRevenue = statisticsDAO.getRevenueByCategory(startAt, endAt, categoryId);
        List<Map<String, Object>> orderStatuses = statisticsDAO.getOrderStatusCounts(startAt, endAt);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", selectedType);
        data.put("startDate", startDate.toString());
        data.put("endDate", endDate.toString());
        data.put("categoryId", categoryId == null ? "" : categoryId);
        data.put("categories", categoryDAO.findAll());
        data.put("summary", summary);
        data.put("dailyRevenue", dailyRevenue);
        data.put("topProducts", topProducts);
        data.put("categoryRevenue", categoryRevenue);
        data.put("orderStatuses", orderStatuses);
        data.put("chartJson", buildChartJson(dailyRevenue, topProducts, categoryRevenue));

        boolean hasData = ((Number) summary.get("orderCount")).intValue() > 0
                || !dailyRevenue.isEmpty()
                || !topProducts.isEmpty()
                || !categoryRevenue.isEmpty();
        data.put("hasData", hasData);

        return Result.ok(hasData ? "Lấy dữ liệu thống kê thành công" : "Không có dữ liệu", data);
    }

    public String toCsv(Map<String, Object> reportData) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        Map<String, Object> summary = (Map<String, Object>) reportData.get("summary");
        List<Map<String, Object>> dailyRevenue = (List<Map<String, Object>>) reportData.get("dailyRevenue");
        List<Map<String, Object>> topProducts = (List<Map<String, Object>>) reportData.get("topProducts");
        List<Map<String, Object>> categoryRevenue = (List<Map<String, Object>>) reportData.get("categoryRevenue");

        csv.append("Bao cao thong ke\n");
        csv.append("Tu ngay,").append(reportData.get("startDate")).append("\n");
        csv.append("Den ngay,").append(reportData.get("endDate")).append("\n\n");

        csv.append("Tong quan\n");
        csv.append("Doanh thu,So don hang,So san pham ban\n");
        csv.append(summary.get("revenue")).append(',')
                .append(summary.get("orderCount")).append(',')
                .append(summary.get("soldQuantity")).append("\n\n");

        csv.append("Doanh thu theo ngay\n");
        csv.append("Ngay,So don hang,Doanh thu\n");
        for (Map<String, Object> row : dailyRevenue) {
            csv.append(escapeCsv(row.get("label"))).append(',')
                    .append(row.get("orderCount")).append(',')
                    .append(row.get("revenue")).append('\n');
        }

        csv.append("\nSan pham ban chay\n");
        csv.append("Ma san pham,Ten san pham,Danh muc,So luong,Doanh thu\n");
        for (Map<String, Object> row : topProducts) {
            csv.append(row.get("productId")).append(',')
                    .append(escapeCsv(row.get("productName"))).append(',')
                    .append(escapeCsv(row.get("categoryName"))).append(',')
                    .append(row.get("soldQuantity")).append(',')
                    .append(row.get("revenue")).append('\n');
        }

        csv.append("\nDoanh thu theo danh muc\n");
        csv.append("Danh muc,So luong,Doanh thu\n");
        for (Map<String, Object> row : categoryRevenue) {
            csv.append(escapeCsv(row.get("categoryName"))).append(',')
                    .append(row.get("soldQuantity")).append(',')
                    .append(row.get("revenue")).append('\n');
        }

        return csv.toString();
    }

    private String buildChartJson(List<Map<String, Object>> dailyRevenue,
                                  List<Map<String, Object>> topProducts,
                                  List<Map<String, Object>> categoryRevenue) {
        Map<String, Object> charts = new LinkedHashMap<>();
        charts.put("dailyLabels", dailyRevenue.stream().map(row -> row.get("label")).toList());
        charts.put("dailyRevenue", dailyRevenue.stream().map(row -> row.get("revenue")).toList());
        charts.put("productLabels", topProducts.stream().map(row -> row.get("productName")).toList());
        charts.put("productQuantity", topProducts.stream().map(row -> row.get("soldQuantity")).toList());
        charts.put("categoryLabels", categoryRevenue.stream().map(row -> row.get("categoryName")).toList());
        charts.put("categoryRevenue", categoryRevenue.stream().map(row -> row.get("revenue")).toList());
        return gson.toJson(charts);
    }

    private String normalizeType(String type) {
        if ("revenue".equals(type) || "products".equals(type) || "categories".equals(type)) {
            return type;
        }
        return "overview";
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Integer parseCategoryId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int id = Integer.parseInt(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
