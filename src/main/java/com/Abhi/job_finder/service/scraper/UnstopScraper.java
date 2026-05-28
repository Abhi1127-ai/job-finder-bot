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
public class UnstopScraper {

    private static final Logger log = LoggerFactory.getLogger(UnstopScraper.class);

    public List<Job> scrapeJobs(String jobTitle) {
        List<Job> jobs = new ArrayList<>();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions()
                             .setHeadless(false)
                             .setSlowMo(500));
             BrowserContext context = browser.newContext(
                     new Browser.NewContextOptions()
                             .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
             )) {

            Page page = context.newPage();

            String searchUrl = "https://unstop.com/job?oppstatus=open&searchTerm="
                    + jobTitle.replace(" ", "%20");

            log.info("Navigating to Unstop: {}", searchUrl);

            page.navigate(searchUrl,
                    new Page.NavigateOptions()
                            .setTimeout(60000)
                            .setWaitUntil(WaitUntilState.NETWORKIDLE));

            page.waitForSelector("un-opportunity-card, .opportunity_card, .card_holder",
                    new Page.WaitForSelectorOptions().setTimeout(30000));

            scrollPage(page);
            Locator cards = page.locator("un-opportunity-card, .opportunity_card, .card_holder");
            int total = Math.min(cards.count(), 10);
            log.info("Found {} job cards on Unstop", total);

            for (int i = 0; i < total; i++) {
                try {
                    Locator card = cards.nth(i);
                    card.scrollIntoViewIfNeeded();

                    String jobUrl = card.locator("a").first().getAttribute("href");
                    if (jobUrl == null) continue;
                    if (!jobUrl.startsWith("http")) jobUrl = "https://unstop.com" + jobUrl;
                    Page jobPage = context.newPage();
                    jobPage.navigate(jobUrl,
                            new Page.NavigateOptions()
                                    .setTimeout(30000)
                                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                    humanDelay(jobPage, 2000, 3000);

                    String title       = extractTitle(jobPage);
                    String description = extractDescription(jobPage);
                    String company     = extractCompany(jobPage);

                    jobPage.close();

                    if (description == null || description.length() < 50) {
                        log.warn("Unstop job {} has no usable description — skipping", i);
                        continue;
                    }

                    Job job = new Job();
                    job.setTitle(title);
                    job.setDescription(description.substring(0, Math.min(description.length(), 1500)));
                    job.setUrl(jobUrl);
                    job.setCompany(company);
                    job.setMode("Remote");
                    job.setSource("Unstop");
                    jobs.add(job);

                    log.info("Scraped Unstop job [{}]: {} at {}", i, title, company);

                } catch (Exception e) {
                    log.error("Failed to extract Unstop job at index {}: {}", i, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Unstop scraper failed: {}", e.getMessage(), e);
        }

        return jobs;
    }

    private void scrollPage(Page page) {
        try {
            page.evaluate(
                    "() => {" +
                            "  let scrolled = 0;" +
                            "  const interval = setInterval(() => {" +
                            "    window.scrollBy(0, 400);" +
                            "    scrolled++;" +
                            "    if (scrolled >= 6) clearInterval(interval);" +
                            "  }, 800);" +
                            "}"
            );
            page.waitForTimeout(6000);
        } catch (Exception e) {
            log.warn("Scroll failed (non-fatal): {}", e.getMessage());
        }
    }

    private String extractTitle(Page page) {
        String[] selectors = {
                ".opportunity-title",
                ".title_bar h1",
                ".job_details h1",
                "h1"
        };
        for (String selector : selectors) {
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
        String[] selectors = {
                ".job-description",
                ".description_container",
                ".about_section",
                ".single_detail_wrapper"
        };
        for (String selector : selectors) {
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
        String[] selectors = {
                ".company_name",
                ".org_name",
                ".recruiter_name"
        };
        for (String selector : selectors) {
            try {
                Locator el = page.locator(selector).first();
                if (el.count() > 0) return el.innerText().strip();
            } catch (Exception ignored) {}
        }
        return "Unstop";
    }

    private void humanDelay(Page page, int minMillis, int maxMillis) {
        int delay = (int) (Math.random() * (maxMillis - minMillis)) + minMillis;
        page.waitForTimeout(delay);
    }
}