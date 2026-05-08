package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.webquanao.action.Result;
import org.example.webquanao.service.PasswordResetService;

import java.io.IOException;

@WebServlet(name = "ForgotPasswordController", value = {"/forgot-password", "/forgotPassword"})
public class ForgotPasswordController extends HttpServlet {
    private PasswordResetService passwordResetService;

    @Override
    public void init() {
        passwordResetService = new PasswordResetService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/forgotPassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String email = request.getParameter("email");
        Result result = passwordResetService.requestReset(
                email,
                getClientIp(request),
                request.getHeader("User-Agent")
        );

        if (result.isSuccess()) {
            request.getSession().setAttribute("pendingResetEmail", result.getData().get("email"));
            request.setAttribute("message", result.getMessage());
            request.setAttribute("email", result.getData().get("email"));
            request.getRequestDispatcher("/WEB-INF/verifyCode.jsp").forward(request, response);
        } else {
            request.setAttribute("error", result.getMessage());
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/forgotPassword.jsp").forward(request, response);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
