package org.example.webquanao.service;

import org.example.webquanao.action.Result;
import org.example.webquanao.dao.RoleDAO;
import org.example.webquanao.dao.UserDAO;
import org.example.webquanao.dao.UserRoleDAO;
import org.example.webquanao.entity.Role;
import org.example.webquanao.entity.User;
import org.example.webquanao.utils.PasswordUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final UserRoleDAO userRoleDAO = new UserRoleDAO();

    public Result getUserManagementData() {
        Map<String, Object> data = new HashMap<>();

        try {
            List<User> users = userDAO.findAll();
            for (User user : users) {
                user.setRoles(roleDAO.getRolesByUser(user.getId()));
            }

            data.put("users", users);
            data.put("roles", roleDAO.findAll());
            return Result.ok("Lay danh sach user thanh cong", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Lay danh sach user that bai");
        }
    }

    public Result addUser(User user, List<Integer> roleIds) {
        try {
            // UC-1.11 / 2a1.5: He thong kiem tra du lieu truoc khi them user.
            String validationMessage = validateUser(user, roleIds, true);
            if (validationMessage != null) {
                return Result.fail(validationMessage);
            }

            User existingEmail = userDAO.findByEmail(user.getEmail());
            if (existingEmail != null) {
                return Result.fail("Email da duoc su dung");
            }

            // NFR1.11-2: Mat khau khoi tao phai duoc hash mot chieu truoc khi luu CSDL.
            user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            user.setActive(true);
            user.setVerified(true);

            // UC-1.11 / 2a1.6: Cap nhat CSDL, sau do gan quyen cho user moi.
            int userId = userDAO.insertManagedUser(user);
            userRoleDAO.replaceRolesForUser(userId, normalizeRoleIds(roleIds));

            return Result.ok("Them user thanh cong", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Loi he thong khi them user");
        }
    }

    public Result updateUser(User user, List<Integer> roleIds) {
        try {
            // UC-1.11 / 2a2.6: He thong kiem tra du lieu truoc khi sua user.
            String validationMessage = validateUser(user, roleIds, false);
            if (validationMessage != null) {
                return Result.fail(validationMessage);
            }

            User existing = userDAO.findById(user.getId());
            if (existing == null) {
                return Result.fail("Khong tim thay user");
            }

            User existingEmail = userDAO.findByEmail(user.getEmail());
            if (existingEmail != null && existingEmail.getId() != user.getId()) {
                return Result.fail("Email da duoc su dung");
            }

            user.setActive(existing.isActive());
            user.setVerified(existing.isVerified());

            // UC-1.11 / 2a2.7: Cap nhat thong tin va phan quyen trong CSDL.
            if (isBlank(user.getPassword())) {
                userDAO.updateManagedUser(user);
            } else {
                user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
                userDAO.updateManagedUserWithPassword(user);
            }
            userRoleDAO.replaceRolesForUser(user.getId(), normalizeRoleIds(roleIds));

            return Result.ok("Sua user thanh cong", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Cap nhat user that bai");
        }
    }

    public Result toggleUserLock(int targetUserId, int currentAdminId) {
        try {
            User user = userDAO.findById(targetUserId);
            if (user == null) {
                return Result.fail("Khong tim thay user");
            }

            // BR1.11-4 / 2b3.5: Admin khong duoc khoa tai khoan dang dang nhap cua chinh minh.
            if (targetUserId == currentAdminId && user.isActive()) {
                return Result.fail("Khong the khoa tai khoan dang phien hoat dong");
            }

            // UC-1.11 / 2a3.5 -> 2a3.6: Kiem tra dieu kien khoa, sau do cap nhat trang thai.
            userDAO.updateActive(targetUserId, !user.isActive());

            String message = user.isActive() ? "Khoa user thanh cong" : "Mo khoa user thanh cong";
            return Result.ok(message, null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Thay doi trang thai user that bai");
        }
    }

    private String validateUser(User user, List<Integer> roleIds, boolean requirePassword) {
        trimUser(user);

        if (isBlank(user.getFullName()) || isBlank(user.getEmail())) {
            return "Vui long nhap day du cac truong bat buoc";
        }

        if (!user.getEmail().contains("@")) {
            return "Email khong hop le";
        }

        if (requirePassword && isBlank(user.getPassword())) {
            return "Vui long nhap mat khau";
        }

        if (roleIds == null || roleIds.isEmpty()) {
            return "Vui long chon quyen han";
        }

        List<Integer> existingRoleIds = roleDAO.findAll().stream()
                .map(Role::getId)
                .toList();
        for (Integer roleId : roleIds) {
            if (!existingRoleIds.contains(roleId)) {
                return "Quyen han khong hop le";
            }
        }

        return null;
    }

    private void trimUser(User user) {
        user.setEmail(trim(user.getEmail()));
        user.setFullName(trim(user.getFullName()));
        user.setPhone(trim(user.getPhone()));
        user.setAddress(trim(user.getAddress()));
    }

    private List<Integer> normalizeRoleIds(List<Integer> roleIds) {
        Set<Integer> uniqueIds = new LinkedHashSet<>(roleIds);
        return new ArrayList<>(uniqueIds);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
