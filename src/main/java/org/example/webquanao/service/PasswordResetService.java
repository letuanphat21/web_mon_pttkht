package org.example.webquanao.service;

import org.example.webquanao.action.Result;
import org.example.webquanao.dao.PasswordResetTokenDAO;
import org.example.webquanao.dao.UserDAO;
import org.example.webquanao.entity.PasswordResetToken;
import org.example.webquanao.entity.User;
import org.example.webquanao.utils.PasswordUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Map;
import java.util.regex.Pattern;

public class PasswordResetService {
    private static final int TOKEN_EXPIRE_MINUTES = 15;
    private static final int REQUEST_LIMIT_PER_HOUR = 3;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern OTP_PATTERN = Pattern.compile("\\d{6}");
    private static final Pattern SPECIAL_CHARACTER_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    private final UserDAO userDAO = new UserDAO();
    private final PasswordResetTokenDAO tokenDAO = new PasswordResetTokenDAO();
    private final EmailService emailService = new EmailService();

    public Result requestReset(String email, String requestIp, String userAgent) {
        email = trim(email);

        if (isBlank(email) || !email.contains("@")) {
            return Result.fail("Email không hợp lệ");
        }

        User user = userDAO.findByEmail(email);
        if (user == null) {
            return Result.fail("Email không tồn tại trong hệ thống. Vui lòng kiểm tra lại");
        }

        if (user.getGoogleId() != null && !user.getGoogleId().isBlank()) {
            return Result.fail("Tài khoản này đăng nhập bằng Google. Vui lòng sử dụng tính năng đăng nhập Google");
        }

        if (!user.isActive()) {
            return Result.fail("Tài khoản của bạn đang bị khóa, không thể đặt lại mật khẩu lúc này");
        }

        if (user.getLockUntil() != null && user.getLockUntil().after(new java.util.Date())) {
            return Result.fail("Tài khoản của bạn đang bị khóa đến " + user.getLockUntil());
        }

        Timestamp requestLimitFrom = new Timestamp(System.currentTimeMillis() - 60L * 60L * 1000L);
        int recentRequests = tokenDAO.countRecentRequests(user.getId(), requestIp, requestLimitFrom);
        if (recentRequests >= REQUEST_LIMIT_PER_HOUR) {
            return Result.fail("Bạn đã gửi yêu cầu quá nhiều lần. Vui lòng thử lại sau");
        }

        String otp = generateOtp();
        String tokenHash = hashToken(otp);
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + TOKEN_EXPIRE_MINUTES * 60L * 1000L);

        try {
            tokenDAO.markAllUnusedByUserAsUsed(user.getId());
            tokenDAO.insert(user.getId(), tokenHash, expiresAt, requestIp, limitLength(userAgent, 255));

            String content = "<h3>Xin chào " + escapeHtml(user.getFullName()) + "</h3>"
                    + "<p>Bạn vừa yêu cầu khôi phục mật khẩu.</p>"
                    + "<p>Mã xác nhận của bạn là:</p>"
                    + "<h2 style='letter-spacing: 4px;'>" + otp + "</h2>"
                    + "<p>Mã OTP có hiệu lực trong " + TOKEN_EXPIRE_MINUTES + " phút.</p>"
                    + "<p>Nếu bạn không yêu cầu thao tác này, vui lòng bỏ qua email.</p>";

            emailService.sendEmail(user.getEmail(), "Khôi phục mật khẩu", content);
        } catch (Exception e) {
            e.printStackTrace();
            tokenDAO.markAllUnusedByUserAsUsed(user.getId());
            return Result.fail("Gửi email thất bại. Vui lòng thử lại sau ít phút");
        }

        return Result.ok("Mã xác nhận đã được gửi đến email của bạn", Map.of("email", user.getEmail()));
    }

    public Result verifyOtp(String email, String otp) {
        email = trim(email);
        otp = trim(otp);

        if (isBlank(email) || !OTP_PATTERN.matcher(otp).matches()) {
            return Result.fail("Mã xác nhận không hợp lệ hoặc đã hết hạn");
        }

        User user = userDAO.findByEmail(email);
        if (user == null || !user.isActive()) {
            return Result.fail("Tài khoản không hợp lệ hoặc đang bị khóa");
        }

        PasswordResetToken token = tokenDAO.findValidByUserIdAndHash(user.getId(), hashToken(otp));
        if (token == null) {
            return Result.fail("Mã xác nhận không hợp lệ hoặc đã hết hạn");
        }

        return Result.ok("Mã xác nhận hợp lệ", Map.of("email", user.getEmail()));
    }

    public Result resetPassword(String email, String password, String confirmPassword) {
        email = trim(email);
        if (isBlank(email)) {
            return Result.fail("Phiên khôi phục mật khẩu không hợp lệ hoặc đã hết hạn");
        }

        if (isBlank(password) || isBlank(confirmPassword)) {
            return Result.fail("Vui lòng nhập đầy đủ mật khẩu mới và xác nhận mật khẩu");
        }

        if (!password.equals(confirmPassword)) {
            return Result.fail("Mật khẩu mới không khớp");
        }

        if (!isStrongPassword(password)) {
            return Result.fail("Mật khẩu chưa đủ mạnh. Vui lòng nhập ít nhất 6 ký tự và có ký tự đặc biệt");
        }

        User user = userDAO.findByEmail(email);
        if (user == null || !user.isActive()) {
            return Result.fail("Tài khoản không hợp lệ hoặc đang bị khóa");
        }

        try {
            userDAO.updatePassword(user.getId(), PasswordUtil.hashPassword(password));
            tokenDAO.deleteByUserId(user.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Cập nhật mật khẩu thất bại. Vui lòng thử lại");
        }

        return Result.ok("Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới", null);
    }

    private String generateOtp() {
        return String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot hash token", e);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 6 && SPECIAL_CHARACTER_PATTERN.matcher(password).find();
    }

    private String limitLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
