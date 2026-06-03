package org.example.webquanao.dto.request;

public class PasswordResetRequest {
    private String email;
    private String otp;
    private String password;
    private String confirmPassword;
    private String requestIp;
    private String userAgent;

    public PasswordResetRequest() {
    }

    public PasswordResetRequest(String email, String otp, String password, String confirmPassword, String requestIp, String userAgent) {
        this.email = email;
        this.otp = otp;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.requestIp = requestIp;
        this.userAgent = userAgent;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
