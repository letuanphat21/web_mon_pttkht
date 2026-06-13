package org.example.webquanao.service;

import org.example.webquanao.action.Result;
import org.example.webquanao.dao.UserDAO;
import org.example.webquanao.dao.UserRoleDAO;
import org.example.webquanao.dto.request.RegisterRequest;
import org.example.webquanao.dto.request.LoginRequest;
import org.example.webquanao.dto.request.GoogleLoginRequest;
import org.example.webquanao.dto.response.LoginResponse;
import org.example.webquanao.entity.User;
import org.example.webquanao.utils.PasswordUtil;

import java.time.LocalDateTime;
import java.util.*;

public class AuthService {
    private UserDAO userDAO = new UserDAO();
    private UserRoleDAO userRoleDAO = new UserRoleDAO();
    private EmailService emailService = new EmailService();

    public Result registerUser(RegisterRequest dto) {
        // 2. check email
        User usermail = userDAO.findByEmail(dto.getEmail());
        if (usermail!= null) {
//            if(usermail.getGoogleId() != null) {
//                String hashed = PasswordUtil.hashPassword(user.getPassword());
//                user.setPassword(hashed);
//                userDAO.updatePasswordAndUsername(user);
//                return Result.ok("Đăng ký thành công", null);
//            }else{
                return Result.fail("Email đã tồn tại");
//            }
        }
        
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());

        // 3. hash password
        String hashed = PasswordUtil.hashPassword(dto.getPassword());
        user.setPassword(hashed);
        // 4. tạo code kích hoạt
        String code = UUID.randomUUID().toString();
        user.setCodeActive(code);
//        user.setCodeActiveCreatedAt(LocalDateTime.now());

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

            String content = "<h3>Xin chào " + user.getFullName() + "</h3>"
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
                // Kiểm tra mã kích hoạt đã hết hạn (24 giờ) hay chưa
                if (user.getCodeActiveCreatedAt() != null && user.getCodeActiveCreatedAt().plusHours(24).isBefore(LocalDateTime.now())) {
                    return Result.fail("Mã kích hoạt đã hết hạn (chỉ có hiệu lực trong 24 giờ)");
                }
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

    public Result login(LoginRequest dto) {

        // 1. Tìm user
        User user = userDAO.findByEmail(dto.getEmail());

        // không lộ tài khoản tồn tại hay không
        if (user == null) {
            return Result.fail("Tài khoản hoặc mật khẩu không đúng");
        }

        // 2. Check bị khóa
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            return Result.fail("Tài khoản bị khóa đến " + user.getLockUntil());
        }

        // 3. Check trạng thái active
        if (user.isActive() == false) {
            return Result.fail("Tài khoản bị khóa vì qui phạm chính sách của công ty");
        }

        // 4. Check password
        boolean isMatch = PasswordUtil.checkPassword(dto.getPassword(), user.getPassword());

        if (!isMatch) {
            handleUserFail(user);
            return Result.fail(" mật khẩu không đúng");
        }

        // 5. Check kích hoạt
        if (user.isVerified() == false) {
            return Result.fail("Tài khoản chưa kích hoạt");
        }

        List<String> roles = userRoleDAO.getRolesByUserId(user.getId());
        LoginResponse responseData = new LoginResponse(user.getId(), user.getEmail(), user.getFullName(), roles);

        Map<String, Object> data = new HashMap<>();
        data.put("user", responseData);

        // 6. Login thành công → reset fail count
        userDAO.updateFailedAttemptsAndLock(user.getId(), 0, null);
        return Result.ok("Đăng nhập thành công", data);
    }

    // xử lý sai theo USER
    private void handleUserFail(User user) {
        int attempts = user.getFailedAttempts() + 1;

        if (attempts >= 5) {

            LocalDateTime lockTime =
                    LocalDateTime.now().plusMinutes(30);

            userDAO.updateFailedAttemptsAndLock(
                    user.getId(),
                    0,
                    lockTime
            );

        } else {
            userDAO.updateFailedAttempts(user.getId(), attempts);
        }
    }

    // Login google
    public Result loginGoogle(GoogleLoginRequest dto) {
        // 1. tìm user theo email
        User user = userDAO.findByEmail(dto.getEmail());

        // 2. nếu chưa có user → tạo mới
        if (user == null) {

            User newUser = new User();
            newUser.setEmail(dto.getEmail());
            newUser.setGoogleId(dto.getGoogleId());
            newUser.setFullName(dto.getName());
            newUser.setActive(true);
            newUser.setVerified(true);
            newUser.setPassword(PasswordUtil.hashPassword("0000000"));

            int userId = userDAO.insertGoogleUser(newUser);
            userRoleDAO.addRoleToUser(userId, 1);
            newUser.setId(userId);

            List<String> roles = userRoleDAO.getRolesByUserId(newUser.getId());
            LoginResponse responseData = new LoginResponse(newUser.getId(), newUser.getEmail(), newUser.getFullName(), roles);

            Map<String, Object> data = new HashMap<>();
            data.put("user", responseData);

            return Result.ok("Đăng nhập Google thành công", data);
        }

        // 3. nếu đã có user nhưng chưa có googleId
        if (user.getGoogleId() == null) {
            userDAO.updateGoogleId(user.getId(), dto.getGoogleId());
            user.setGoogleId(dto.getGoogleId());
        }

        // 4. check googleId có khớp không
        if (user.getGoogleId() != null && !user.getGoogleId().equals(dto.getGoogleId())) {
            return Result.fail("Google account không hợp lệ");
        }

        // 5. check khóa tài khoản do chơi ngu
//        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
//            return Result.fail("Tài khoản bị khóa đến " + user.getLockUntil());
//        }

        // 6 check tài khoản do sai qui định chính sách gì đó mà tui cũng chả biết
        if (user.isActive() == false) {
            return Result.fail("Tài khoản bị khóa do quy định chính sách ngu ngốc gì đó");
        }

        List<String> roles = userRoleDAO.getRolesByUserId(user.getId());
        LoginResponse responseData = new LoginResponse(user.getId(), user.getEmail(), user.getFullName(), roles);

        Map<String, Object> data = new HashMap<>();
        data.put("user", responseData);

        // 6. thành công
        return Result.ok("Đăng nhập Google thành công", data);
    }

}
