package com.Abhi.job_finder.service.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramNotificationService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendjobAlert(String jobTitle, String company, String matchScore ,String Url){
        String message = String.format(
                "🚀 *High Quality Match Found!*\n\n" +
                        "📌 *Role:* %s\n" +
                        "🏢 *Company:* %s\n" +
                        "⭐ *Match Score:* %s/10\n\n" +
                        "🔗 [Apply Here](%s)",
                jobTitle, company, matchScore, Url
        );

        String telegramurl = "https://api.telegram.org/bot" + botToken +
                "/sendMessage?chat_id=" + chatId +
                "&text=" + message +
                "&parse_mode=Markdown";

        try{
            restTemplate.getForObject(telegramurl, String.class);
            System.out.println("📲 Telegram notification sent for: " + jobTitle);
        }catch(Exception e){
            System.out.println("❌ Failed to send Telegram alert: " + e.getMessage());
        }
    }
}
