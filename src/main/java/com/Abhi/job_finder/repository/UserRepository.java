package com.Abhi.job_finder.repository;

import com.Abhi.job_finder.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByLinkingCodeAndLinkingCodeExpiryAfter(String linkingCode, LocalDateTime now);

    Optional<User> findByEmail(String email);

    Optional<User> findByTelegramChatId(String telegramChatId);
}