package com.Abhi.job_finder.service;

import com.Abhi.job_finder.dto.RegisterRequest;
import com.Abhi.job_finder.model.User;
import com.Abhi.job_finder.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(RegisterRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setSkills(request.skills());
        user.setTargetRoles(request.targetRoles());
        user.setLocations(request.locations());
        user.setExperienceLevel(request.experienceLevel());
        user.setLinkingCode(generateLinkingCode());
        user.setLinkingCodeExpiry(LocalDateTime.now().plusMinutes(15));
        user.setTelegramLinked(false);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public String regenerateLinkingCode(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String newCode = generateLinkingCode();
        user.setLinkingCode(newCode);
        user.setLinkingCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        return newCode;
    }

    private String generateLinkingCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // skip ambiguous chars (0/O, 1/I)
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public Optional<User> findByLinkingCode(String code) {
        return userRepository.findByLinkingCodeAndLinkingCodeExpiryAfter(code, LocalDateTime.now());
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
