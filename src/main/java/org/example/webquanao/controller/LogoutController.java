package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LogoutController", value = "/logout")
public class LogoutController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy Session hiện tại (nếu có)
        HttpSession session = request.getSession(false);

        if (session != null) {
            // 2. Hủy bỏ toàn bộ dữ liệu Session (Xóa thông tin User, Giỏ hàng cũ)
            session.invalidate();
        }

        // 3. Vì index.jsp nằm trong WEB-INF (bảo mật), bắt buộc dùng Forward để gọi nội bộ
        request.getRequestDispatcher("/WEB-INF/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}