package com.Abhi.job_finder.dto;

import jakarta.validation.constraints.NotBlank;

public class JobHuntRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String resume;

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
}