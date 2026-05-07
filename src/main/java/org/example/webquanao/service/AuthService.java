package org.example.webquanao.service;

import org.example.webquanao.action.Result;
import org.example.webquanao.dao.UserDAO;
import org.example.webquanao.dao.UserRoleDAO;
import org.example.webquanao.entity.User;
import org.example.webquanao.utils.PasswordUtil;

import java.util.*;

public class AuthService {
    private UserDAO userDAO = new UserDAO();
    private UserRoleDAO userRoleDAO = new UserRoleDAO();
    private EmailService emailService = new EmailService();

    public Result registerUser(User user) {
        // 1. check username
        if (userDAO.findByUsername(user.getUsername()) != null) {
            return Result.fail("Tên đăng nhập đã tồn tại");
        }
        // 2. check email
        User usermail = userDAO.findByEmail(user.getEmail());
        if (usermail!= null) {
            if(usermail.getGoogleId() != null) {
                String hashed = PasswordUtil.hashPassword(user.getPassword());
                user.setPassword(hashed);
                userDAO.updatePasswordAndUsername(user);
                return Result.ok("Đăng ký thành công", null);
            }else{
                return Result.fail("Email đã tồn tại");
            }
        }
        // 3. hash password
        String hashed = PasswordUtil.hashPassword(user.getPassword());
        user.setPassword(hashed);
        // 4. tạo code kích hoạt
        String code = UUID.randomUUID().toString();
        user.setCodeActive(code);

        int userId;
        // Kiểm tra coi insert tài khoản vô database ổn khng
        try {
            userId = userDAO.insertUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Lỗi hệ thống khi tạo tài khoản");
        }
        try{
            userRoleDAO.addRoleToUser(userId, 1);
        }catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Lỗi hệ thống khi tạo tài khoản");
        }

        // Gửi mail ổn không
        try {
            String link = "http://localhost:8080/verify?code=" + code + "&email=" + user.getEmail();

            String content = "<h3>Xin chào " + user.getUsername() + "</h3>"
                    + "<p>Click để kích hoạt:</p>"
                    + "<a href='" + link + "'>Kích hoạt</a>";

            emailService.sendEmail(user.getEmail(), "Kích hoạt tài khoản", content);

        } catch (Exception e) {
            e.printStackTrace();

            return Result.fail(" gửi email xác nhận thất bại");
        }
        return Result.ok("Đăng ký thành công! Vui lòng kiểm tra email", null);
    }

    public Result activeUser(String email, String code) {

        try {
            User user = userDAO.findByEmail(email);
            if (user == null) {
                return Result.fail("Kích hoạt tài khoản thất bại");
            }
            if (user.getCodeActive().equals(code)) {
                user.setVerified(true);
                userDAO.updateCodeActive(user);
            }else {
                return Result.fail("Kích hoạt tài khoản thất bại");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Kích hoạt tài khoản thất bại");
        }
        return Result.ok("Kích hoạt tài khoàn thành công", null);
    }

    public Result login(String tenDangNhap, String password) {

        // 1. Tìm user
        User user = userDAO.findByUsername(tenDangNhap);

        // không lộ tài khoản tồn tại hay không
        if (user == null) {
            return Result.fail("Tài khoản hoặc mật khẩu không đúng");
        }

        // 2. Check bị khóa
        if (user.getLockUntil() != null && user.getLockUntil().after(new Date())) {
            return Result.fail("Tài khoản bị khóa đến " + user.getLockUntil());
        }

        // 3. Check trạng thái active
        if (user.isActive() == false) {
            return Result.fail("Tài khoản bị khóa vì qui phạm chính sách của công ty");
        }

        // 4. Check password
        boolean isMatch = PasswordUtil.checkPassword(password, user.getPassword());

        if (!isMatch) {
            handleUserFail(user);
            return Result.fail("Tài khoản hoặc mật khẩu không đúng");
        }

        // 5. Check kích hoạt
        if (user.isVerified() == false) {
            return Result.fail("Tài khoản chưa kích hoạt");
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        List<String> roles = userRoleDAO.getRolesByUserId(user.getId());
        data.put("roles", roles);
        data.put("id", user.getId());

        // 6. Login thành công → reset fail count
        userDAO.updateFailedAttemptsAndLock(user.getId(), 0, null);
        return Result.ok("Đăng nhập thành công", data);
    }

    // xử lý sai theo USER
    private void handleUserFail(User user) {
        int attempts = user.getFailedAttempts() + 1;

        if (attempts >= 5) {
            Date lockTime = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
            userDAO.updateFailedAttemptsAndLock(user.getId(), 0, lockTime);
        } else {
            userDAO.updateFailedAttempts(user.getId(), attempts);
        }
    }

    // Login google
    public Result loginGoogle(String email, String googleId, String name) {
        // 1. tìm user theo email
        User user = userDAO.findByEmail(email);

        // 2. nếu chưa có user → tạo mới
        if (user == null) {

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setGoogleId(googleId);
            newUser.setFullName(name);
            newUser.setActive(true);
            newUser.setVerified(true);

            int userId = userDAO.insertGoogleUser(newUser);
            userRoleDAO.addRoleToUser(userId, 1);
            newUser.setId(userId);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("username", newUser.getFullName());
            data.put("email", newUser.getEmail());
            List<String> roles = userRoleDAO.getRolesByUserId(newUser.getId());
            data.put("roles", roles);
            data.put("id", user.getId());

            return Result.ok("Đăng nhập Google thành công", data);
        }

        // 3. nếu đã có user nhưng chưa có googleId
        if (user.getGoogleId() == null) {
            userDAO.updateGoogleId(user.getId(), googleId);
            user.setGoogleId(googleId);
        }

        // 4. check googleId có khớp không
        if (user.getGoogleId() != null && !user.getGoogleId().equals(googleId)) {
            return Result.fail("Google account không hợp lệ");
        }

        // 5. check khóa tài khoản do chơi ngu
        if (user.getLockUntil() != null && user.getLockUntil().after(new java.util.Date())) {
            return Result.fail("Tài khoản đang bị khóa");
        }

        // 6 check tài khoản do sai qui định chính sách gì đó mà tui cũng chả biết
        if (user.isActive() == false) {
            return Result.fail("Tài khoản bị khóa do quy định chính sách ngu ngốc gì đó");
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("username", user.getFullName());
        data.put("email", user.getEmail());
        List<String> roles = userRoleDAO.getRolesByUserId(user.getId());
        data.put("roles", roles);
        // 6. thành công
        return Result.ok("Đăng nhập Google thành công", data);
    }

}
