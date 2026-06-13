package org.example.webquanao.api;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;
import org.example.webquanao.dto.request.CategoryRequest;
import org.example.webquanao.service.CategoryService;

import java.io.IOException;

@WebServlet(name = "category_api", value = "/api/category")
public class category_api extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CategoryService categoryService = new CategoryService();
        Result result = categoryService.getAllCategory();
        sendResponse(response, result);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        CategoryRequest categoryRequest = gson.fromJson(request.getReader(), CategoryRequest.class);
        CategoryService categoryService = new CategoryService();
        Result result = categoryService.addCategory(categoryRequest);
        sendResponse(response, result);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        CategoryRequest categoryRequest = gson.fromJson(request.getReader(), CategoryRequest.class);
        CategoryService categoryService = new CategoryService();
        Result result = categoryService.updateCategory(categoryRequest);
        sendResponse(response, result);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) {
            sendResponse(response, Result.fail("Thiếu ID"));
            return;
        }
        int id = Integer.parseInt(idParam);
        CategoryService categoryService = new CategoryService();
        Result result = categoryService.toggleCategoryStatus(id);
        sendResponse(response, result);
    }

    private void sendResponse(HttpServletResponse response, Result result) throws IOException {
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(result));
    }
}