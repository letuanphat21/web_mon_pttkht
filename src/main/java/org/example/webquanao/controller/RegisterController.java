package org.example.webquanao.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;
import org.example.webquanao.dto.RegisterDTO;
import org.example.webquanao.service.AuthService;

import java.io.IOException;

@WebServlet(name = "RegisterController", value = "/register")
public class RegisterController extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() {
        authService = new AuthService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Lấy dữ liệu từ form
        String email = request.getParameter("email");
//        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String password_again = request.getParameter("password_again");
        String fullname = request.getParameter("fullname");

        if(!password.equals(password_again)) {
            request.setAttribute("error","mật khẩu chưa chính xác");
            request.getRequestDispatcher("/WEB-INF/register.jsp").forward(request, response);
            return;
        }

        // 2. Tạo object DTO
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail(email);
        registerDTO.setPassword(password);
        registerDTO.setPasswordAgain(password_again);
        registerDTO.setFullName(fullname);

        // 3. Gọi service với DTO
        Result result = authService.registerUser(registerDTO);

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