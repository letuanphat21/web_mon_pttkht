package org.example.webquanao.api;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;
import org.example.webquanao.service.CategoryService;
import org.example.webquanao.entity.Category;

import java.io.IOException;

@WebServlet(name = "category_api", value = "/api/category")
public class CategoryController extends HttpServlet {
    private CategoryService categoryService;
    private Gson gson;

    @Override
    public void init() {
        categoryService = new CategoryService();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Result result = categoryService.getAllCategory();
        sendResponse(response, result);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Category category = gson.fromJson(request.getReader(), Category.class);
        Result result = categoryService.addCategory(category);
        sendResponse(response, result);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Category category = gson.fromJson(request.getReader(), Category.class);
        Result result = categoryService.updateCategory(category);
        sendResponse(response, result);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        // if (idParam == null) {
        //     sendResponse(response, Result.fail("Thiếu ID"));
        //     return;
        // }
        int id = Integer.parseInt(idParam);
        Result result = categoryService.toggleCategoryStatus(id);
        sendResponse(response, result);
    }

    private void sendResponse(HttpServletResponse response, Result result) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(result));
    }
}