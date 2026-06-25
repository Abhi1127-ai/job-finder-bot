package com.Abhi.job_finder.controller;

import com.Abhi.job_finder.dto.RegisterRequest;
import com.Abhi.job_finder.dto.RegisterResponse;
import com.Abhi.job_finder.model.User;
import com.Abhi.job_finder.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request);
        return ResponseEntity.ok(new RegisterResponse(user.getId(), user.getLinkingCode()));
    }

    @PostMapping("/{userId}/regenerate-code")
    public ResponseEntity<String> regenerateCode(@PathVariable String userId) {
        String newCode = userService.regenerateLinkingCode(userId);
        return ResponseEntity.ok(newCode);
    }
}
