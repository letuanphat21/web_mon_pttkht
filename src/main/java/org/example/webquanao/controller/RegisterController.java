package org.example.webquanao.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;
import org.example.webquanao.entity.User;
import org.example.webquanao.service.AuthService;

import java.io.IOException;

@WebServlet(name = "RegisterController", value = "/register")
public class RegisterController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Lấy dữ liệu từ form
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String fullname = request.getParameter("fullname");

        // 2. Tạo object user
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullname);

        // 3. Gọi service
        AuthService authService = new AuthService();
        Result result = authService.registerUser(user);

        // 4. Xử lý kết quả
        if (result.isSuccess()) {
            request.setAttribute("message", result.getMessage());
        } else {
            request.setAttribute("error", result.getMessage());
        }

        // 5. Forward lại trang register
        request.getRequestDispatcher("/WEB-INF/register.jsp").forward(request, response);
    }
}