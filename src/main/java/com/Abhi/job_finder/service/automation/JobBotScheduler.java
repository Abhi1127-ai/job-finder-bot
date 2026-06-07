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

    public JobBotScheduler(ScraperService scraperService,
                           JobDomainsConfig domainsConfig) {
        this.scraperService = scraperService;
        this.domainsConfig  = domainsConfig;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void runDailyJobHunt() {
        log.info("=== Daily Job Hunt Started at {} ===", java.time.LocalDateTime.now());

        for (JobDomain domain : domainsConfig.getDomains()) {
            log.info("--- Hunting for: {} ---", domain.getTitle());
            try {
                String resume = getResumeForDomain(domain.getTitle());
                scraperService.runJobHunt(
                        domain.getTitle(),
                        resume,
                        domain.getTelegramChatId()
                );
            } catch (Exception e) {
                log.error("Domain '{}' failed: {}", domain.getTitle(), e.getMessage());
            }
        }

        log.info("=== Daily Job Hunt Complete ===");
    }

    private String getResumeForDomain(String title) {
        String t = title.toLowerCase();

        if (t.contains("data analyst"))
            return "Python developer with experience in pandas, numpy, SQL, " +
                    "data visualization, Power BI, Excel, and statistical analysis. " +
                    "Fresher looking for data analyst internship.";

        if (t.contains("machine learning") || t.contains("ai"))
            return "ML engineer with experience in Python, TensorFlow, scikit-learn, " +
                    "NLP, deep learning, and data preprocessing. " +
                    "Fresher looking for AI/ML internship.";

        if (t.contains("frontend") || t.contains("react") || t.contains("web"))
            return "Frontend developer with experience in React, JavaScript, " +
                    "HTML, CSS, Tailwind CSS, REST APIs, and responsive design. " +
                    "Fresher looking for frontend internship.";

        return "Expert Java Spring Boot developer with experience in MongoDB, " +
                "MySQL, Playwright, microservices, Spring Security, JWT, " +
                "and AI integration. Fresher looking for Java developer internship.";
    }
}