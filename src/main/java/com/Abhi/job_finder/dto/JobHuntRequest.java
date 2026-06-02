package com.Abhi.job_finder.dto;

import jakarta.validation.constraints.NotBlank;

public class JobHuntRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String resume;

    private String telegramChatId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }
}