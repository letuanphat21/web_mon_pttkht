package org.example.webquanao.entity;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
public class User {
    private int id;
    private String email;
    private String username;
    private String password;
    private String googleId;
    private String fullName;
    private String phone;
    private String address;
    private String avatar;
    private Timestamp createdAt;
    private boolean verified;
    private String codeActive;
    private boolean active;
    private int failedAttempts;
    private Timestamp lockUntil;

    // nhiều role
    private List<Role> roles;

    public User() {}

    // getter & setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

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

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    public boolean isAdmin() {
        if (roles == null) return false;
        return roles.stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));
    }


    public String getCodeActive() {
        return codeActive;
    }

    public void setCodeActive(String codeActive) {
        this.codeActive = codeActive;
    }


    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Timestamp getLockUntil() {
        return lockUntil;
    }

    public void setLockUntil(Timestamp lockUntil) {
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