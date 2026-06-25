package com.Abhi.job_finder.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramNotificationService {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id:}")
    private String defaultChatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendjobAlert(String jobTitle, String company,
                             String matchScore, String url, String chatId) {

        String targetChatId = (chatId != null && !chatId.isBlank())
                ? chatId : defaultChatId;

        if (targetChatId == null || targetChatId.isBlank()) {
            log.warn("No Telegram chat ID configured — skipping alert for: {}", jobTitle);
            return;
        }

        String message = String.format(
                "🚀 *High Quality Match Found!*\n\n" +
                        "📌 *Role:* %s\n" +
                        "🏢 *Company:* %s\n" +
                        "⭐ *Match Score:* %s/10\n\n" +
                        "🔗 [Apply Here](%s)",
                jobTitle, company, matchScore, url
        );

        try {
            String telegramUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = String.format(
                    "{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"Markdown\"}",
                    targetChatId,
                    message.replace("\"", "\\\"").replace("\n", "\\n")
            );

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForObject(telegramUrl, entity, String.class);
            log.info(" Telegram sent to chat {} for: {}", targetChatId, jobTitle);

        } catch (Exception e) {
            log.error(" Failed to send Telegram alert: {}", e.getMessage());
        }
    }

    public void sendjobAlert(String jobTitle, String company,
                             String matchScore, String url) {
        sendjobAlert(jobTitle, company, matchScore, url, defaultChatId);
    }

    public void sendMessage(String chatId, String text) {
        if (chatId == null || chatId.isBlank()) {
            log.warn("No Telegram chat ID provided — skipping message: {}", text);
            return;
        }

        try {
            String telegramUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = String.format(
                    "{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"Markdown\"}",
                    chatId,
                    text.replace("\"", "\\\"").replace("\n", "\\n")
            );

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForObject(telegramUrl, entity, String.class);
            log.info("Telegram message sent to chat {}", chatId);

        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage());
        }
    }

    public void sendPrepRoadmap(String jobTitle, String roadmap, String chatId) {
        if (roadmap == null || roadmap.isBlank()) return;

        String targetChatId = (chatId != null && !chatId.isBlank()) ? chatId : defaultChatId;
        String message = String.format(
                "🎯 *Interview Prep for:* %s\n\n%s",
                jobTitle, roadmap
        );

        try {
            String telegramUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            String body = String.format(
                    "{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"Markdown\"}",
                    targetChatId,
                    message.replace("\"", "\\\"").replace("\n", "\\n")
            );
            org.springframework.http.HttpEntity<String> entity =
                    new org.springframework.http.HttpEntity<>(body, headers);
            restTemplate.postForObject(telegramUrl, entity, String.class);
            log.info("📚 Prep roadmap sent for: {}", jobTitle);
        } catch (Exception e) {
            log.error(" Failed to send prep roadmap: {}", e.getMessage());
        }
    }
}