package com.Abhi.job_finder.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final Map<String, Object> settings = new HashMap<>(Map.of(
            "jobTitle",        "java Full Stack Developer",
            "threshold",       8,
            "schedule",        "0 0 9 * * *",
            "maxJobs",         10,
            "telegramEnabled", true,
            "dedupeEnabled",   true,
            "resume",          ""
    ));

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> saveSettings(
            @RequestBody Map<String, Object> updated) {
        settings.putAll(updated);
        return ResponseEntity.ok(settings);
    }
}