package org.example.webquanao.entity;

import java.time.LocalDateTime;
import java.util.List;

public class User {
    private int id;
    private String email;
//    private String username;
    private String password;
    private String googleId;
    private String fullName;
    private String phone;
    private String address;
    private String avatar;
    private LocalDateTime createdAt;
    private boolean verified;
    private String codeActive;
    private LocalDateTime codeActiveCreatedAt;
    private boolean active;
    private int failedAttempts;
    private LocalDateTime lockUntil;

    // nhiều role
    private List<Role> roles;

    public User() {}

    // getter & setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

//    public String getUsername() { return username; }
//    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }



    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    public boolean isAdmin() {
        if (roles == null) return false;
        return roles.stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));
    }

    public boolean usesGoogleLogin() {
        return googleId != null && !googleId.isBlank();
    }

    public boolean isTemporarilyLockedAt(LocalDateTime dateTime) {
        return lockUntil != null && lockUntil.isAfter(dateTime);
    }

    public boolean canResetPasswordAt(LocalDateTime dateTime) {
        return active
                && verified
                && !usesGoogleLogin()
                && !isTemporarilyLockedAt(dateTime);
    }


    public String getCodeActive() {
        return codeActive;
    }

    public void setCodeActive(String codeActive) {
        this.codeActive = codeActive;
    }

    public LocalDateTime getCodeActiveCreatedAt() {
        return codeActiveCreatedAt;
    }

    public void setCodeActiveCreatedAt(LocalDateTime codeActiveCreatedAt) {
        this.codeActiveCreatedAt = codeActiveCreatedAt;
    }


    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLockUntil() {
        return lockUntil;
    }

    public void setLockUntil(LocalDateTime lockUntil) {
        this.lockUntil = lockUntil;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
