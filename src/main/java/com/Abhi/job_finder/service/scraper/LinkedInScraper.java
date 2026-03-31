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

            page.evaluate("async () => {" +
                    "  for (int i = 0; i < 5; i++) {" +
                    "    window.scrollBy(0, 500);" +
                    "    await new Promise(r => setTimeout(r, 1000));" +
                    "  }" +
                    "}");

            page.waitForSelector(".job-card-container");
            Locator jobCards = page.locator(".job-card-container");

            for (int i = 0; i < Math.min(jobCards.count(), 10); i++) {
                try {
                    Locator card = jobCards.nth(i);
                    String title = card.locator(".job-card-list__title").innerText();

                    simulateMouseMovement(page);
                    card.click();
                    page.waitForSelector(".jobs-description-content__text");

                    humanDelay(page , 2000 , 4500);

                    humanScroll(page);

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

    private void humanDelay(Page page , int minMillis , int maxMillis){
        int delay = (int) (Math.random() * (maxMillis - minMillis)) + minMillis;
        page.waitForTimeout(delay);
    }

    private void simulateMouseMovement(Page page){
        double x = Math.random() * 1280;
        double y = Math.random() * 720;
        page.mouse().move(x, y , new Mouse.MoveOptions().setSteps(10));
    }

    private void humanScroll(Page page){
        System.out.println("Simulating human scroll to load more jobs....");

        int scrollSteps = (int) (Math.random() * 3) + 3;

        for( int i = 0 ; i < scrollSteps ; i++){
            int distance = (int) (Math.random() * 400) + 300;
            page.evaluate("window.scrollBy(0, " + distance + ")");
            humanDelay(page , 1000 , 2500);
        }
    }
}