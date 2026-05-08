package org.example.webquanao.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;
import org.example.webquanao.entity.Category;
import org.example.webquanao.service.CategoryService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "ManagerCategoryController", value = "/admin/managerCategory")
public class CategoryController extends HttpServlet {
    private CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("roleId") == null || (int)session.getAttribute("roleId") != 2) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
<<<<<<< HEAD
=======

        Result result = categoryService.getAllCategory();

        if (result.isSuccess()) {
            List<Category> categories = (List<Category>) result.getData().get("categorys");
            request.setAttribute("categories", categories);
        } else {
            request.setAttribute("error", result.getMessage());
        }
>>>>>>> parent of b8b741b (revert)
        request.getRequestDispatcher("/WEB-INF/admin/managerCategory.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
<<<<<<< HEAD
      
=======
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        if ("toggle".equals(action) && idParam != null) {
            int id = Integer.parseInt(idParam);
            categoryService.toggleCategoryStatus(id);
        }
        response.sendRedirect(request.getContextPath() + "/admin/managerCategory");
>>>>>>> parent of b8b741b (revert)
    }
}