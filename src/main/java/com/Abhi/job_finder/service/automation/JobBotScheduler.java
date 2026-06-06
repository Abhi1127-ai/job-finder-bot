package com.Abhi.job_finder.service.automation;

import com.Abhi.job_finder.config.JobDomain;
import com.Abhi.job_finder.config.JobDomainsConfig;
import com.Abhi.job_finder.service.scraper.ScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class JobBotScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobBotScheduler.class);

    private final ScraperService scraperService;
    private final JobDomainsConfig domainsConfig;

    private static final String MY_RESUME =
            "Expert Java Spring Boot developer with experience in databases " +
                    "(MySQL, MongoDB), Playwright, microservices, and AI integration.";

    public JobBotScheduler(ScraperService scraperService,
                           JobDomainsConfig domainsConfig) {
        this.scraperService = scraperService;
        this.domainsConfig  = domainsConfig;
    }


//@Scheduled(cron = "0 0 9 * * *")
@Scheduled(cron = "0 */5 * * * *")
    public void runDailyJobHunt() {
        log.info("=== Daily Job Hunt Started at {} ===", java.time.LocalDateTime.now());

        for (JobDomain domain : domainsConfig.getDomains()) {
            log.info("--- Hunting for: {} ---", domain.getTitle());
            try {
                scraperService.runJobHunt(
                        domain.getTitle(),
                        MY_RESUME,
                        domain.getTelegramChatId()
                );
            } catch (Exception e) {
                log.error("Domain '{}' failed: {}", domain.getTitle(), e.getMessage());
            }
        }
        log.info("=== Daily Job Hunt Complete ===");
    }
}