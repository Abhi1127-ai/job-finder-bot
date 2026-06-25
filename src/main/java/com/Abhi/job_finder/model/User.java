package com.Abhi.job_finder.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private String role;

    private List<String> skills;
    private List<String> targetRoles;
    private List<String> locations;
    private String experienceLevel;
    private String telegramChatId;
    private String linkingCode;
    private LocalDateTime linkingCodeExpiry;
    private boolean telegramLinked;

    private LocalDateTime createdAt;
    private boolean active;

    public User() {}

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getSkills() {
        return skills;
    }
    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getTargetRoles() {
        return targetRoles;
    }
    public void setTargetRoles(List<String> targetRoles) {
        this.targetRoles = targetRoles;
    }

    public List<String> getLocations() {
        return locations; }
    public void setLocations(List<String> locations) { this.locations = locations; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(String telegramChatId) { this.telegramChatId = telegramChatId; }

    public String getLinkingCode() { return linkingCode; }
    public void setLinkingCode(String linkingCode) { this.linkingCode = linkingCode; }

    public LocalDateTime getLinkingCodeExpiry() { return linkingCodeExpiry; }
    public void setLinkingCodeExpiry(LocalDateTime linkingCodeExpiry) { this.linkingCodeExpiry = linkingCodeExpiry; }

    public boolean isTelegramLinked() { return telegramLinked; }
    public void setTelegramLinked(boolean telegramLinked) { this.telegramLinked = telegramLinked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}