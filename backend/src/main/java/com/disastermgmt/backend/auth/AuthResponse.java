package com.disastermgmt.backend.auth;

public class AuthResponse {

    private String token;
    private String role;
    private String name;
    private String region;

    public AuthResponse(String token, String role, String name, String region) {
        this.token = token;
        this.role = role;
        this.name = name;
        this.region = region;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}

