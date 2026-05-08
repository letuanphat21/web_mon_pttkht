package org.example.webquanao.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.example.webquanao.action.Result;

import org.example.webquanao.helper.HttpClientHelper;
import org.example.webquanao.service.AuthService;
import org.example.webquanao.utils.GoogleProperties;

import java.io.IOException;

import java.util.List;

@WebServlet(name = "GoogleController", value = "/loginGoogle")
public class GoogleController extends HttpServlet {


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

            AuthService authService = new AuthService();
            Result result = authService.loginGoogle(email, gooogleId, name);

            if (result.isSuccess()) {
                String username1 = (String) result.getData().get("username");
                String email1 = (String) result.getData().get("email");
                List<String> roles = (List<String>) result.getData().get("roles");

                HttpSession session = request.getSession();
                session.setAttribute("username", username1);
                session.setAttribute("email", email1);
                // kiểm tra role
                boolean isAdmin = roles.stream().anyMatch(r -> r.equals("ADMIN"));

                if (isAdmin) {
                    request.getRequestDispatcher("/WEB-INF/managerCategory.jsp").forward(request, response);
                } else {
                    request.getRequestDispatcher("/WEB-INF/home.jsp").forward(request, response);
                }
            }
        } else {
            request.setAttribute("message", "Hazz bạn không đăng nhập thành công rồi huhu");
            request.getRequestDispatcher("/WEB-INF/fail.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }
}