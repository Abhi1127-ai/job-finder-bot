package com.Abhi.job_finder.service.automation;

import com.Abhi.job_finder.service.scraper.ScraperService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class JobBotScheduler {

    private final ScraperService scraperService;

    public JobBotScheduler(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void runDailyJobHunt(){
        System.out.println("Automation triggered : Started Daily Linkedin Job Hunt at " + LocalDateTime.now());

        String myResume = "Expert Java Spring Boot developer with experience databases(mysql , MongoDb) playwright, microservices , and AI integration...";
        String targetRole = "java Full Stack Developer";

        scraperService.runJobHunt(targetRole, myResume);
    }

    @Scheduled(cron = "0 0 11,13,15 * * *")
    public void runQuickCheck(){
        System.out.println("Running mid-day quick check for new roles...");
    }
}
