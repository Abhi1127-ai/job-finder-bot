package com.Abhi.job_finder.service.scraper;

import com.Abhi.job_finder.model.Job;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
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

    private static final String[] TITLE_SELECTORS = {
            ".job-details-jobs-unified-top-card__job-title",
            ".jobs-unified-top-card__job-title",
            "h1.t-24"
    };

    private static final String[] DESCRIPTION_SELECTORS = {
            ".jobs-description__content",
            ".jobs-box__html-content",
            ".jobs-description-content__text"
    };


    public List<Job> scrapejobs(String jobTitle) {
        List<Job> jobs = new ArrayList<>();
        Path sessionPath = Paths.get("playwright-session");

        try (Playwright playwright = Playwright.create();
             BrowserContext context = playwright.chromium().launchPersistentContext(
                     sessionPath,
                     new BrowserType.LaunchPersistentContextOptions()
                             .setHeadless(false)
                             .setSlowMo(500))) {

            Page page = context.newPage();

            String searchUrl = "https://www.linkedin.com/jobs/search/?keywords="
                    + jobTitle.replace(" ", "%20");

            page.navigate(searchUrl,
                    new Page.NavigateOptions()
                            .setTimeout(90000)
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            page.waitForSelector(".job-card-list",
                    new Page.WaitForSelectorOptions().setTimeout(60000));

            scrollJobList(page);

            Locator jobCards = page.locator(".job-card-list");
            int total = Math.min(jobCards.count(), 10);
            log.info("Found {} job cards to process", total);

            for (int i = 0; i < total; i++) {
                try {
                    Locator card = jobCards.nth(i);
                    card.scrollIntoViewIfNeeded();
                    card.click();

                    humanDelay(page, 2000, 4000);

                    waitForAnySelector(page, DESCRIPTION_SELECTORS, 10000);

                    String title       = extractTitle(page);
                    String description = extractDescription(page);
                    String url         = page.url();

                    if (description == null || description.isBlank() || description.length() < 100) {
                        log.warn("Job {} has no usable description — skipping", i);
                        continue;
                    }

                    Job job = new Job();
                    job.setTitle(title);
                    job.setDescription(description.substring(0, Math.min(description.length(), 1500)));
                    job.setUrl(url);
                    jobs.add(job);

                    log.info("Scraped job [{}]: {}", i, title);

                } catch (Exception e) {
                    log.error("Failed to extract job at index {}: {}", i, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Scraper failed: {}", e.getMessage(), e);
        }

        return jobs;
    }

    private void scrollJobList(Page page) {
        log.info("Scrolling job list to load cards...");
        try {
            page.evaluate(
                    "() => {" +
                            "  const list = document.querySelector('.jobs-search-results-list');" +
                            "  if (!list) return;" +
                            "  let scrolled = 0;" +
                            "  const interval = setInterval(() => {" +
                            "    list.scrollBy(0, 400);" +
                            "    scrolled++;" +
                            "    if (scrolled >= 5) clearInterval(interval);" +
                            "  }, 800);" +
                            "}"
            );
            page.waitForTimeout(5000);
        } catch (Exception e) {
            log.warn("Job list scroll failed (non-fatal): {}", e.getMessage());
        }
    }

    private void waitForAnySelector(Page page, String[] selectors, int timeoutMs) {
        String combined = String.join(", ", selectors);
        page.waitForSelector(combined,
                new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
    }

    private String extractTitle(Page page) {
        for (String selector : TITLE_SELECTORS) {
            try {
                Locator el = page.locator(selector).first();
                if (el.count() > 0) {
                    String text = el.innerText().strip();
                    if (!text.isBlank()) return text;
                }
            } catch (Exception ignored) {}
        }
        return page.title().replaceAll("\\s*\\|.*$", "").strip();
    }
    private String extractDescription(Page page) {
        for (String selector : DESCRIPTION_SELECTORS) {
            try {
                Locator el = page.locator(selector).first();
                if (el.count() > 0) {
                    String text = el.innerText().strip();
                    if (text.length() > 100) return text;
                }
            } catch (Exception ignored) {}
        }
        log.warn("No description selector matched — returning null");
        return null;
    }

    private void humanDelay(Page page, int minMillis, int maxMillis) {
        int delay = (int) (Math.random() * (maxMillis - minMillis)) + minMillis;
        page.waitForTimeout(delay);
    }
}