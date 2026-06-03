package org.example.webquanao.dto.request;

public class GoogleLoginRequest {
    private String email;
    private String googleId;
    private String name;

    public GoogleLoginRequest() {
    }

    public GoogleLoginRequest(String email, String googleId, String name) {
        this.email = email;
        this.googleId = googleId;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
