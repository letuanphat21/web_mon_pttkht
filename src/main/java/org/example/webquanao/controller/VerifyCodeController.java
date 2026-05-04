package org.example.webquanao.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;
import org.example.webquanao.service.AuthService;

import java.io.IOException;

@WebServlet(name = "VerifyCodeController", value = "/verify")
public class VerifyCodeController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        String email = request.getParameter("email");

        AuthService authService = new AuthService();
        Result result = authService.activeUser(email, code);
        if (result.isSuccess()) {
            request.setAttribute("message", result.getMessage());
        }else {
            request.setAttribute("message", result.getMessage());
        }
        request.getRequestDispatcher("/WEB-INF/success.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}