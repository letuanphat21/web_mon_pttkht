package org.example.webquanao;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "MapJsonServlet", value = "/api/map")
public class MapJsonServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Tạo Map
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Le Tuan Phat");
        data.put("age", 25);
        data.put("skills", new String[]{"Java", "JS", "Spring"});

        // Chuyển Map thành JSON
        Gson gson = new Gson();
        String json = gson.toJson(data);

        // Trả JSON về client
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}