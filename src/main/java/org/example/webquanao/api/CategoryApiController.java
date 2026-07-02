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
public class CategoryApiController extends HttpServlet {
    private Gson gson;
    private CategoryService categoryService;

    @Override
    public void init() throws ServletException {
        gson = new Gson();
        categoryService = new CategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Result result = categoryService.getAllCategory();
        sendResponse(response, result);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        CategoryRequest categoryRequest = gson.fromJson(request.getReader(), CategoryRequest.class);
        Result result = categoryService.addCategory(categoryRequest);
        sendResponse(response, result);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        CategoryRequest categoryRequest = gson.fromJson(request.getReader(), CategoryRequest.class);
        Result result = categoryService.updateCategory(categoryRequest);
        sendResponse(response, result);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String idParam = request.getParameter("id");
        if (idParam == null) {
            sendResponse(response, Result.fail("Thiếu ID"));
            return;
        }
        int id = Integer.parseInt(idParam);
        Result result = categoryService.toggleCategoryStatus(id);
        sendResponse(response, result);
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return session.getAttribute("roleId") != null && (int) session.getAttribute("roleId") == 2;
    }

    private void sendResponse(HttpServletResponse response, Result result) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(result));
    }
}