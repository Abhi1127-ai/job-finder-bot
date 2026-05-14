package com.Abhi.job_finder.dto;

public class AuthResponse {
    private String token;
    private UserDto user;

    public AuthResponse(String token, String email, String role) {
        this.token = token;
        this.user = new UserDto(email, role);
    }

    public String getToken() { return token; }
    public UserDto getUser() { return user; }

    public static class UserDto {
        private String email;
        private String role;
        private String name;

        public UserDto(String email, String role) {
            this.email = email;
            this.role = role;
            this.name = email.split("@")[0]; // use email prefix as name
        }

        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getName() { return name; }
    }
}