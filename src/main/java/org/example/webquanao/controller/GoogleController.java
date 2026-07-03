package org.example.webquanao.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;
import org.example.webquanao.dto.request.GoogleLoginRequest;
import org.example.webquanao.dto.response.LoginResponse;
import org.example.webquanao.entity.Role;
import org.example.webquanao.entity.User;
import org.example.webquanao.helper.HttpClientHelper;
import org.example.webquanao.service.AuthService;
import org.example.webquanao.utils.GoogleProperties;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "GoogleController", value = "/loginGoogle")
public class GoogleController extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() {
        authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");
        if (code != null) {
            // 1. Đổi code lấy access token
            String tokenResponse = HttpClientHelper.post(
                    "https://oauth2.googleapis.com/token",
                    "code=" + code +
                            "&client_id=" + GoogleProperties.getClientId() +
                            "&client_secret=" + GoogleProperties.getClientSecret() +
                            "&redirect_uri=" + GoogleProperties.getRedirectUri() +
                            "&grant_type=authorization_code");
            JsonObject json = JsonParser.parseString(tokenResponse).getAsJsonObject();
            String accessToken = json.get("access_token").getAsString();

            // 2. Lấy thông tin người dùng
            String userInfo = HttpClientHelper
                    .get("https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken);
            JsonObject userJson = JsonParser.parseString(userInfo).getAsJsonObject();

            String email = userJson.get("email").getAsString();
            String name = userJson.get("name").getAsString();
            String gooogleId = userJson.get("sub").getAsString();

            // 3. Đóng gói request DTO
            GoogleLoginRequest googleLoginRequest = new GoogleLoginRequest(email, gooogleId, name);

            // 4. Gọi Service
            Result result = authService.loginGoogle(googleLoginRequest);

            if (result.isSuccess()) {
                LoginResponse loginResponse = (LoginResponse) result.getData().get("user");
                int userId = loginResponse.getId();
                String username1 = loginResponse.getUsername();
                String email1 = loginResponse.getEmail();
                List<Role> roles = loginResponse.getRoles();

                User user  = new User();
                user.setId(userId);
                user.setEmail(email1);
                user.setFullName(username1);
                user.setRoles(roles);

                HttpSession session = request.getSession();
                session.setAttribute("user",user);
                session.setAttribute("userId", userId);
                session.setAttribute("roles", roles.stream()
                        .map(Role::getName)
                        .toList());

                boolean isAdmin = roles.stream()
                        .anyMatch(role -> "ADMIN".equals(role.getName()));
                if (isAdmin) {
                    session.setAttribute("roleId", 2);
                }

                response.sendRedirect(request.getContextPath()
                        + (isAdmin ? "/admin/dashboard" : "/"));
            }else {
                request.setAttribute("error", "Hazz bạn không đăng nhập thành công rồi huhu");
                request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
            }
        } else {
            request.setAttribute("error", "Hazz bạn không đăng nhập thành công rồi huhu");
            request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }
}
