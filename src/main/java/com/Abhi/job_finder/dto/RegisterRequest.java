package com.Abhi.job_finder.dto;

import java.util.List;

public record RegisterRequest(
        String name,
        String email,
        List<String> skills,
        List<String> targetRoles,
        List<String> locations,
        String experienceLevel
) {}


