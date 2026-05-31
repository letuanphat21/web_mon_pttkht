package org.example.webquanao.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.webquanao.action.Result;
import org.example.webquanao.entity.User;
import org.example.webquanao.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ManagerUserController", value = "/admin/managerUser")
public class ManagerUserController extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Ban khong co quyen truy cap.");
            return;
        }

        // UC-1.11 / Basic Flow 1: Admin vao man hinh quan ly user, he thong hien thi danh sach.
        Result result = userService.getUserManagementData();
        if (result.isSuccess()) {
            request.setAttribute("users", result.getData().get("users"));
            request.setAttribute("roles", result.getData().get("roles"));
        } else {
            request.setAttribute("error", result.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/admin/managerUser.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        Result result;

        // Controller chi dieu phoi request theo activity. Nghiep vu/kiem tra du lieu nam trong UserService.
        if ("add".equals(action)) {
            result = userService.addUser(buildUserFromRequest(request, false), getRoleIds(request));
        } else if ("update".equals(action)) {
            result = userService.updateUser(buildUserFromRequest(request, true), getRoleIds(request));
        } else if ("toggleLock".equals(action)) {
            result = userService.toggleUserLock(parseInt(request.getParameter("id")), getCurrentAdminId(session));
        } else {
            result = Result.fail("Hanh dong khong hop le");
        }

        if (result.isSuccess()) {
            session.setAttribute("message", result.getMessage());
        } else {
            session.setAttribute("error", result.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/managerUser");
    }

    private User buildUserFromRequest(HttpServletRequest request, boolean includeId) {
        User user = new User();
        if (includeId) {
            user.setId(parseInt(request.getParameter("id")));
        }
        user.setFullName(request.getParameter("fullName"));
        user.setEmail(request.getParameter("email"));
        user.setPassword(request.getParameter("password"));
        user.setPhone(request.getParameter("phone"));
        user.setAddress(request.getParameter("address"));
        return user;
    }

    private List<Integer> getRoleIds(HttpServletRequest request) {
        String[] values = request.getParameterValues("roleIds");
        List<Integer> roleIds = new ArrayList<>();

        if (values == null) {
            return roleIds;
        }

        for (String value : values) {
            int roleId = parseInt(value);
            if (roleId > 0) {
                roleIds.add(roleId);
            }
        }
        return roleIds;
    }

    private int getCurrentAdminId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId == null ? 0 : parseInt(userId.toString());
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        List<String> roles = (List<String>) session.getAttribute("roles");
        if (roles != null && roles.contains("ADMIN")) {
            return true;
        }

        Object roleId = session.getAttribute("roleId");
        return roleId != null && roleId.toString().equals("2");
    }
}
