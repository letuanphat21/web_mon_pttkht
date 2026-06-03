package org.example.webquanao.dto.request;

public class RegisterRequest {
    private String email;
    private String password;
    private String passwordAgain;
    private String fullName;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String passwordAgain, String fullName) {
        this.email = email;
        this.password = password;
        this.passwordAgain = passwordAgain;
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordAgain() {
        return passwordAgain;
    }

    public void setPasswordAgain(String passwordAgain) {
        this.passwordAgain = passwordAgain;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
