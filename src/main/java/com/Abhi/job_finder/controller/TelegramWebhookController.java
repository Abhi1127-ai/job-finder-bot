package com.Abhi.job_finder.controller;

import com.Abhi.job_finder.dto.TelegramUpdate;
import com.Abhi.job_finder.model.User;
import com.Abhi.job_finder.service.UserService;
import com.Abhi.job_finder.service.notification.TelegramNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    private final UserService userService;
    private final TelegramNotificationService telegramNotificationService;

    public TelegramWebhookController(UserService userService, TelegramNotificationService telegramNotificationService) {
        this.userService = userService;
        this.telegramNotificationService = telegramNotificationService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleUpdate(@RequestBody TelegramUpdate update) {
        if (update.getMessage() == null) {
            return ResponseEntity.ok().build();
        }

        String text = update.getMessage().getText();
        String chatId = update.getMessage().getChat().getId().toString();

        if (text != null && text.startsWith("/link ")) {
            String code = text.replace("/link ", "").trim().toUpperCase();
            handleLinking(code, chatId);
        }

        return ResponseEntity.ok().build();
    }

    private void handleLinking(String code, String chatId) {
        Optional<User> userOpt = userService.findByLinkingCode(code);

        if (userOpt.isEmpty()) {
            telegramNotificationService.sendMessage(chatId,
                    "❌ Invalid or expired code. Please generate a new one from the website.");
            return;
        }

        User user = userOpt.get();
        user.setTelegramChatId(chatId);
        user.setTelegramLinked(true);
        user.setLinkingCode(null);
        userService.save(user);

        telegramNotificationService.sendMessage(chatId,
                "✅ Successfully linked! You'll now receive job alerts here, " + user.getName() + ".");
    }
}
