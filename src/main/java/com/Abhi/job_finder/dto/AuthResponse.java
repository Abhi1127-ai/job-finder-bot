package com.Abhi.job_finder.dto;

public class AuthResponse {
    private String token;
    private UserDto user;

    public AuthResponse(String token, String email, String role, String name) {
        this.token = token;
        this.user = new UserDto(email, role, name);
    }

    public String getToken() { return token; }
    public UserDto getUser() { return user; }

    public static class UserDto {
        private String email;
        private String role;
        private String name;

        public UserDto(String email, String role, String name) {
            this.email = email;
            this.role = role;
            this.name = (name != null && !name.isBlank()) ? name : email.split("@")[0];
        }

        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getName() { return name; }
    }
}