package com.Abhi.job_finder.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("jobTitle", "java Full Stack Developer");
        settings.put("threshold", 8);
        settings.put("schedule", "0 0 9 * * *");
        settings.put("maxJobs", 10);
        settings.put("telegramEnabled", true);
        settings.put("dedupeEnabled", true);
        settings.put("resume", "");
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> saveSettings(@RequestBody Map<String, Object> settings) {
        return ResponseEntity.ok(settings);
    }
}
