package org.example.webquanao.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;
import org.example.webquanao.dto.request.LoginRequest;
import org.example.webquanao.dto.response.LoginResponse;
import org.example.webquanao.entity.Role;
import org.example.webquanao.entity.User;
import org.example.webquanao.service.AuthService;
import org.example.webquanao.service.CartService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "LoginController", value = "/login")
public class LoginController extends HttpServlet {
    private AuthService authService;
    private CartService cartService;

    @Override
    public void init() {
        cartService = new CartService();
        authService = new AuthService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. lấy data từ form
        String emailParam = request.getParameter("email");
        String passwordParam = request.getParameter("password");

        // 2. Tạo Request DTO
        LoginRequest loginRequest = new LoginRequest(emailParam, passwordParam);

        // 3. gọi service với DTO
        Result result = authService.login(loginRequest);

        if (result.isSuccess()) {

            // Lấy Response DTO từ kết quả
            LoginResponse loginResponse = (LoginResponse) result.getData().get("user");
            int userId = loginResponse.getId();
            String username1 = loginResponse.getUsername();
            String email = loginResponse.getEmail();
            List<Role> roles = loginResponse.getRoles();

            User user = new User();
            user.setId(userId);
            user.setEmail(email);
            user.setFullName(username1);
            user.setRoles(roles);

            // tạo session
            HttpSession session = request.getSession();
            session.setAttribute("user",user);


            try {
                // Hợp nhất giỏ hàng tạm dưới DB
                cartService.mergeCartOnLogin(userId, session);

                // SỬA BUG: Lấy tổng số lượng sản phẩm từ DB đưa vào session để cập nhật Badge ngay khi vừa Login
                int totalCartCount = cartService.getTotalCartCount(userId);
                session.setAttribute("totalCartCount", totalCartCount);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // kiểm tra role
            boolean isAdmin = roles.stream().anyMatch(r -> r.getName().equals("ADMIN"));

            if (isAdmin) {
                session.setAttribute("roleId", 2);
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }

        } else {
            // gửi lỗi về JSP
            request.setAttribute("error", result.getMessage());

            request.getRequestDispatcher("/WEB-INF/login.jsp")
                    .forward(request, response);
        }
    }
}