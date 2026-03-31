package com.Abhi.job_finder.service.scraper;

import com.Abhi.job_finder.model.Job;
import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LinkedInScraper {

    private static final Logger log = LoggerFactory.getLogger(LinkedInScraper.class);

    public List<Job> scrapejobs(String jobTitle) {
        List<Job> jobs = new ArrayList<>();
        Path sessionPath = Paths.get("playwright-session");
        try (Playwright playwright = Playwright.create();
             BrowserContext context = playwright.chromium().launchPersistentContext(sessionPath,
                     new BrowserType.LaunchPersistentContextOptions().setHeadless(true).setSlowMo(500))) {

            Page page = context.newPage();
            page.navigate("https://www.linkedin.com/jobs/search/?keywords=" + jobTitle);

            page.waitForSelector(".job-card-container");
            Locator jobCards = page.locator(".job-card-container");

            for (int i = 0; i < Math.min(jobCards.count(), 10); i++) {
                try {
                    Locator card = jobCards.nth(i);
                    String title = card.locator(".job-card-list__title").innerText();

                    card.click();
                    page.waitForSelector(".jobs-description-content__text");

                    String description = page.locator(".jobs-description-content__text").innerText();
                    String company = page.locator(".jobs-description-content__company").innerText();

                    Job job = new Job();
                    job.setTitle(title);
                    job.setDescription(description);
                    job.setUrl(page.url());
                    jobs.add(job);
                } catch (Exception e) {
                    log.error("Failed to extract job index " + i);
                }
            }
        } catch (Exception e) {
            log.error("Scraper failed", e);
        }
        return jobs;
    }
}