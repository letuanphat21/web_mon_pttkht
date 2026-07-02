package org.example.webquanao.dto.response;

import org.example.webquanao.entity.Role;

import java.util.List;

public class LoginResponse {
    private int id;
    private String email;
    private String username;
    private List<Role> roles;

    public LoginResponse() {
    }

    public LoginResponse(int id, String email, String username, List<Role> roles) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.roles = roles;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
