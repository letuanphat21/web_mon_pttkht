package org.example.webquanao.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;
import org.example.webquanao.entity.Role;
import org.example.webquanao.entity.User;
import org.example.webquanao.service.AuthService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "LoginController", value = "/login")
public class LoginController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. lấy data từ form
        String username = request.getParameter("email");
        String password = request.getParameter("password");

        // 3. gọi service
        AuthService authService = new AuthService();
        Result result = authService.login(username, password);

        if (result.isSuccess()) {

            // lấy user từ data
            int userId = (int) result.getData().get("id");
            String username1 = (String) result.getData().get("username");
            String email = (String) result.getData().get("email");
            List<String> roles = (List<String>) result.getData().get("roles");

            // tạo session
            HttpSession session = request.getSession();
            session.setAttribute("userId", userId);
            session.setAttribute("username", username1);
            session.setAttribute("email", email);
            session.setAttribute("roles", roles);


            // kiểm tra role
            boolean isAdmin = roles.stream().anyMatch(r -> r.equals("ADMIN"));

            if (isAdmin) {
                session.setAttribute("roleId", 2);
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/shop");
            }

        } else {
            // gửi lỗi về JSP
            request.setAttribute("error", result.getMessage());

            request.getRequestDispatcher("/WEB-INF/login.jsp")
                    .forward(request, response);
        }
    }
}