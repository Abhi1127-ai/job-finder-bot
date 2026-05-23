package com.Abhi.job_finder.service.scraper;

import com.Abhi.job_finder.model.Job;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class InternshalaScaper {

    private static final Logger log = LoggerFactory.getLogger(InternshalaScaper.class);

    private static final String[] TITLE_SELECTORS = {
            ".profile_on_detail_page",
            ".internship_heading",
            "h1.heading_4_5"
    };

    private static final String[] DESCRIPTION_SELECTORS = {
            ".internship_other_details_container",
            ".details_container",
            ".about_company_text_container"
    };

    public List<Job> scrapeJobs(String jobTitle) {
        List<Job> jobs = new ArrayList<>();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions()
                             .setHeadless(false)
                             .setSlowMo(500));
             BrowserContext context = browser.newContext()) {

            Page page = context.newPage();

            // No login needed for Internshala!
            String searchUrl = "https://internshala.com/internships/"
                    + jobTitle.toLowerCase().replace(" ", "-") + "-internship";

            log.info("Navigating to: {}", searchUrl);

            page.navigate(searchUrl,
                    new Page.NavigateOptions()
                            .setTimeout(60000)
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            page.waitForSelector(".internship_meta",
                    new Page.WaitForSelectorOptions().setTimeout(30000));

            scrollPage(page);

            Locator cards = page.locator(".internship_meta");
            int total = Math.min(cards.count(), 10);
            log.info("Found {} internship cards", total);

            for (int i = 0; i < total; i++) {
                try {
                    Locator card = cards.nth(i);
                    card.scrollIntoViewIfNeeded();
                    card.click();

                    humanDelay(page, 2000, 4000);

                    waitForAnySelector(page, DESCRIPTION_SELECTORS, 10000);

                    String title       = extractTitle(page);
                    String description = extractDescription(page);
                    String url         = page.url();
                    String company     = extractCompany(page);

                    if (description == null || description.isBlank() || description.length() < 50) {
                        log.warn("Internshala job {} has no usable description — skipping", i);
                        continue;
                    }

                    Job job = new Job();
                    job.setTitle(title);
                    job.setDescription(description.substring(0, Math.min(description.length(), 1500)));
                    job.setUrl(url);
                    job.setCompany(company);
                    job.setMode("Remote");
                    job.setSource("Internshala");
                    jobs.add(job);

                    log.info("Scraped internship [{}]: {} at {}", i, title, company);

                } catch (Exception e) {
                    log.error("Failed to extract internship at index {}: {}", i, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Internshala scraper failed: {}", e.getMessage(), e);
        }

        return jobs;
    }

    private void scrollPage(Page page) {
        log.info("Scrolling page to load cards...");
        try {
            page.evaluate(
                    "() => {" +
                            "  let scrolled = 0;" +
                            "  const interval = setInterval(() => {" +
                            "    window.scrollBy(0, 400);" +
                            "    scrolled++;" +
                            "    if (scrolled >= 5) clearInterval(interval);" +
                            "  }, 800);" +
                            "}"
            );
            page.waitForTimeout(5000);
        } catch (Exception e) {
            log.warn("Scroll failed (non-fatal): {}", e.getMessage());
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
        return page.title().replaceAll("\\s*[-|].*$", "").strip();
    }

    private String extractDescription(Page page) {
        for (String selector : DESCRIPTION_SELECTORS) {
            try {
                Locator el = page.locator(selector).first();
                if (el.count() > 0) {
                    String text = el.innerText().strip();
                    if (text.length() > 50) return text;
                }
            } catch (Exception ignored) {}
        }

        try {
            return page.locator("body").innerText().strip();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractCompany(Page page) {
        try {
            Locator el = page.locator(".company_name, .link_display_like_text").first();
            if (el.count() > 0) return el.innerText().strip();
        } catch (Exception ignored) {}
        return "Internshala";
    }

    private void humanDelay(Page page, int minMillis, int maxMillis) {
        int delay = (int) (Math.random() * (maxMillis - minMillis)) + minMillis;
        page.waitForTimeout(delay);
    }
}