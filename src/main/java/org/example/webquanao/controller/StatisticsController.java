package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.webquanao.action.Result;
import org.example.webquanao.dto.request.StatisticRequest;
import org.example.webquanao.service.StatisticsService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@WebServlet(name = "StatisticsController", value = "/admin/statistics")
public class StatisticsController extends HttpServlet {
    private StatisticsService statisticsService;

    @Override
    public void init() {
        statisticsService = new StatisticsService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        StatisticRequest statisticRequest = buildStatisticRequest(request);
        Result result = statisticsService.getReport(statisticRequest);

        if (!result.isSuccess()) {
            request.setAttribute("error", result.getMessage());
            Result defaultResult = statisticsService.getReport(new StatisticRequest());
            applyReportData(request, defaultResult.getData());
            request.getRequestDispatcher("/WEB-INF/admin/statistics.jsp").forward(request, response);
            return;
        }

        Map<String, Object> data = result.getData();
        if (Boolean.FALSE.equals(data.get("hasData"))) {
            request.setAttribute("message", result.getMessage());
        }
        applyReportData(request, data);

        if ("export".equals(request.getParameter("action"))) {
            exportCsv(response, data);
            return;
        }

        request.getRequestDispatcher("/WEB-INF/admin/statistics.jsp").forward(request, response);
    }

    private StatisticRequest buildStatisticRequest(HttpServletRequest request) {
        return new StatisticRequest(
                request.getParameter("type"),
                request.getParameter("startDate"),
                request.getParameter("endDate"),
                request.getParameter("categoryId")
        );
    }

    private void applyReportData(HttpServletRequest request, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    private void exportCsv(HttpServletResponse response, Map<String, Object> data) throws IOException {
        String csv = statisticsService.toCsv(data);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"bao-cao-thong-ke.csv\"");
        response.getWriter().write(csv);
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object roles = session.getAttribute("roles");
        return roles instanceof List<?> roleList && roleList.stream().anyMatch(role -> "ADMIN".equals(role));
    }
}
