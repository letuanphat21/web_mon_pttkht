package org.example.webquanao.service;

import com.google.gson.Gson;
import org.example.webquanao.action.Result;
import org.example.webquanao.dao.CategoryDAO;
import org.example.webquanao.dao.StatisticsDAO;
import org.example.webquanao.dto.response.StatisticRowResponse;

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
        List<StatisticRowResponse> dailyRevenue = statisticsDAO.getDailyRevenue(startAt, endAt, categoryId);
        List<StatisticRowResponse> topProducts = statisticsDAO.getTopProducts(startAt, endAt, categoryId);
        List<StatisticRowResponse> categoryRevenue = statisticsDAO.getRevenueByCategory(startAt, endAt, categoryId);
        List<StatisticRowResponse> orderStatuses = statisticsDAO.getOrderStatusCounts(startAt, endAt);

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
        List<StatisticRowResponse> dailyRevenue = (List<StatisticRowResponse>) reportData.get("dailyRevenue");
        List<StatisticRowResponse> topProducts = (List<StatisticRowResponse>) reportData.get("topProducts");
        List<StatisticRowResponse> categoryRevenue = (List<StatisticRowResponse>) reportData.get("categoryRevenue");

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
        for (StatisticRowResponse row : dailyRevenue) {
            csv.append(escapeCsv(row.getLabel())).append(',')
                    .append(row.getOrderCount()).append(',')
                    .append(row.getRevenue()).append('\n');
        }

        csv.append("\nSan pham ban chay\n");
        csv.append("Ma san pham,Ten san pham,Danh muc,So luong,Doanh thu\n");
        for (StatisticRowResponse row : topProducts) {
            csv.append(row.getProductId()).append(',')
                    .append(escapeCsv(row.getProductName())).append(',')
                    .append(escapeCsv(row.getCategoryName())).append(',')
                    .append(row.getSoldQuantity()).append(',')
                    .append(row.getRevenue()).append('\n');
        }

        csv.append("\nDoanh thu theo danh muc\n");
        csv.append("Danh muc,So luong,Doanh thu\n");
        for (StatisticRowResponse row : categoryRevenue) {
            csv.append(escapeCsv(row.getCategoryName())).append(',')
                    .append(row.getSoldQuantity()).append(',')
                    .append(row.getRevenue()).append('\n');
        }

        return csv.toString();
    }

    private String buildChartJson(List<StatisticRowResponse> dailyRevenue,
                                  List<StatisticRowResponse> topProducts,
                                  List<StatisticRowResponse> categoryRevenue) {
        Map<String, Object> charts = new LinkedHashMap<>();
        charts.put("dailyLabels", dailyRevenue.stream().map(StatisticRowResponse::getLabel).toList());
        charts.put("dailyRevenue", dailyRevenue.stream().map(StatisticRowResponse::getRevenue).toList());
        charts.put("productLabels", topProducts.stream().map(StatisticRowResponse::getProductName).toList());
        charts.put("productQuantity", topProducts.stream().map(StatisticRowResponse::getSoldQuantity).toList());
        charts.put("categoryLabels", categoryRevenue.stream().map(StatisticRowResponse::getCategoryName).toList());
        charts.put("categoryRevenue", categoryRevenue.stream().map(StatisticRowResponse::getRevenue).toList());
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
