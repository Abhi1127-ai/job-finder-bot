package com.Abhi.job_finder.service.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.Abhi.job_finder.service.scraper.ScraperService.log;


@Service
public class TelegramNotificationService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String defaultChatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendjobAlert(String jobTitle, String company,
                             String matchScore, String url, String chatId) {
        String targetChatId = (chatId != null && !chatId.isBlank()) ? chatId : defaultChatId;
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
            log.info("📲 Telegram notification sent to chat {} for: {}", targetChatId, jobTitle);
        } catch (Exception e) {
            log.error("❌ Failed to send Telegram alert: {}", e.getMessage());
        }
    }

    public void sendjobAlert(String jobTitle, String company,
                             String matchScore, String url) {
        sendjobAlert(jobTitle, company, matchScore, url, defaultChatId);
    }
}
