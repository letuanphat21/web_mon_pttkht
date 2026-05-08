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

@WebServlet(name = "VerifyResetCodeController", value = "/verifyCode")
public class VerifyResetCodeController extends HttpServlet {
    private PasswordResetService passwordResetService;

    @Override
    public void init() {
        passwordResetService = new PasswordResetService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String email = session == null ? null : (String) session.getAttribute("pendingResetEmail");

        if (email == null || email.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập email để nhận mã xác nhận");
            request.getRequestDispatcher("/WEB-INF/forgotPassword.jsp").forward(request, response);
            return;
        }

        request.setAttribute("email", email);
        request.getRequestDispatcher("/WEB-INF/verifyCode.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        String email = session == null ? null : (String) session.getAttribute("pendingResetEmail");
        String otp = request.getParameter("otp");

        Result result = passwordResetService.verifyOtp(email, otp);
        if (result.isSuccess()) {
            request.getSession().setAttribute("resetPasswordEmail", result.getData().get("email"));
            request.setAttribute("email", result.getData().get("email"));
            request.getRequestDispatcher("/WEB-INF/resetPassword.jsp").forward(request, response);
        } else {
            request.setAttribute("error", result.getMessage());
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/verifyCode.jsp").forward(request, response);
        }
    }
}
