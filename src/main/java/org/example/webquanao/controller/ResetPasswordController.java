package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.webquanao.action.Result;
import org.example.webquanao.service.PasswordResetService;

import java.io.IOException;

@WebServlet(name = "ResetPasswordController", value = {"/reset-password", "/resetPassword"})
public class ResetPasswordController extends HttpServlet {
    private PasswordResetService passwordResetService;

    @Override
    public void init() {
        passwordResetService = new PasswordResetService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String email = session == null ? null : (String) session.getAttribute("resetPasswordEmail");

        if (email == null || email.isBlank()) {
            request.setAttribute("error", "Vui lòng xác nhận mã OTP trước khi đặt mật khẩu mới");
            request.getRequestDispatcher("/WEB-INF/verifyCode.jsp").forward(request, response);
            return;
        }

        request.setAttribute("email", email);
        request.getRequestDispatcher("/WEB-INF/resetPassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        String email = session == null ? null : (String) session.getAttribute("resetPasswordEmail");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        Result result = passwordResetService.resetPassword(email, password, confirmPassword);
        if (result.isSuccess()) {
            if (session != null) {
                session.removeAttribute("pendingResetEmail");
                session.removeAttribute("resetPasswordEmail");
            }
            request.setAttribute("message", result.getMessage());
            request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", result.getMessage());
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/resetPassword.jsp").forward(request, response);
        }
    }
}
