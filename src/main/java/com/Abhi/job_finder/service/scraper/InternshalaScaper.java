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

    // Title selectors for the DETAIL page
    private static final String[] TITLE_SELECTORS = {
            ".profile_on_detail_page",
            ".internship_heading",
            "h1.heading_4_5",
            "h1"
    };

    // ✅ Updated description selectors — broader fallback chain
    private static final String[] DESCRIPTION_SELECTORS = {
            ".internship_other_details_container",
            ".details_container",
            ".about_company_text_container",
            "#internship_meta_details",       // common on newer Internshala pages
            ".internship-detail-wrapper",
            ".section_heading + div",
            "[class*='detail']",              // any class containing 'detail'
            "main",                           // broadest fallback
            "body"
    };

    public List<Job> scrapeJobs(String jobTitle) {
        List<Job> jobs = new ArrayList<>();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions()
                             .setHeadless(false)
                             .setSlowMo(300));
             BrowserContext context = browser.newContext()) {

            Page page = context.newPage();

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
            dismissModalIfPresent(page);

            // Collect all detail-page URLs first — avoids stale locator issues entirely
            List<String> detailUrls = collectDetailUrls(page, jobTitle, 10);
            log.info("Collected {} detail URLs to scrape", detailUrls.size());

            for (int i = 0; i < detailUrls.size(); i++) {
                try {
                    String detailUrl = detailUrls.get(i);
                    log.info("Navigating to detail page [{}]: {}", i, detailUrl);

                    page.navigate(detailUrl,
                            new Page.NavigateOptions()
                                    .setTimeout(30000)
                                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                    humanDelay(page, 1500, 2500);
                    dismissModalIfPresent(page);

                    // ✅ Log what selectors are actually present on this page
                    logAvailableSelectors(page);

                    String title       = extractTitle(page);
                    String description = extractDescription(page);
                    String url         = page.url();
                    String company     = extractCompany(page);

                    if (description == null || description.isBlank() || description.length() < 50) {
                        log.warn("Internshala job [{}] has no usable description — skipping", i);
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

    /**
     * Collects href links from the listing page instead of clicking cards.
     * This completely avoids the stale locator + modal interception problem.
     */
    private List<String> collectDetailUrls(Page page, String jobTitle, int maxCount) {
        List<String> urls = new ArrayList<>();
        try {
            // evaluate() returns Object — cast it properly
            Object result = page.evaluate(
                    "() => {" +
                            "  const links = [];" +
                            "  document.querySelectorAll('.internship_meta a, .view_detail_button, " +
                            "    a[href*=\"/internship/detail\"]')" +
                            "    .forEach(a => {" +
                            "      const href = a.href;" +
                            "      if (href && href.includes('internshala.com') && !links.includes(href)) {" +
                            "        links.push(href);" +
                            "      }" +
                            "    });" +
                            "  return links.slice(0, " + maxCount + ");" +
                            "}"
            );

            if (result instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof String s) urls.add(s);
                }
            }

            // Fallback: extract from card IDs if JS returned nothing
            if (urls.isEmpty()) {
                log.warn("JS href collection returned empty — trying card ID fallback");
                Locator cards = page.locator("[id^='internshiplist']");
                int count = Math.min(cards.count(), maxCount);
                for (int i = 0; i < count; i++) {
                    try {
                        String id = cards.nth(i).getAttribute("id");
                        if (id != null) {
                            String internshipId = id.replace("internshiplist_", "");
                            urls.add("https://internshala.com/internship/detail/" + internshipId);
                        }
                    } catch (Exception ignored) {}
                }
            }

        } catch (Exception e) {
            log.error("Failed to collect detail URLs: {}", e.getMessage());
        }
        return urls;
    }

    // ✅ Only checks VISIBLE modals — fixes the phantom button issue
    private void dismissModalIfPresent(Page page) {
        try {
            Locator modal = page.locator("[role='dialog'][aria-modal='true']:visible");
            if (modal.count() > 0) {
                page.keyboard().press("Escape");
                page.waitForTimeout(600);
                log.info("Dismissed visible modal via Escape");
            }
        } catch (Exception e) {
            log.warn("Modal check failed: {}", e.getMessage());
        }
    }

    // ✅ Diagnostic — logs which selectors exist so you can update DESCRIPTION_SELECTORS
    private void logAvailableSelectors(Page page) {
        String[] checkSelectors = {
                ".internship_other_details_container",
                ".details_container",
                ".about_company_text_container",
                "#internship_meta_details",
                ".internship-detail-wrapper",
                "main",
                "[class*='detail']"
        };
        for (String sel : checkSelectors) {
            try {
                int count = page.locator(sel).count();
                if (count > 0) {
                    log.info("  ✅ Selector present: {} (count={})", sel, count);
                }
            } catch (Exception ignored) {}
        }
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
        try {
            page.waitForSelector(combined,
                    new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
        } catch (Exception e) {
            log.warn("None of the expected selectors appeared within {}ms", timeoutMs);
        }
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
                    if (text.length() > 50) {
                        log.debug("Description extracted via selector: {}", selector);
                        return text;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String extractCompany(Page page) {
        String[] companySelectors = {
                ".company_name",
                ".link_display_like_text",
                "[class*='company']",
                ".heading_6"
        };
        for (String sel : companySelectors) {
            try {
                Locator el = page.locator(sel).first();
                if (el.count() > 0) {
                    String text = el.innerText().strip();
                    if (!text.isBlank()) return text;
                }
            } catch (Exception ignored) {}
        }
        return "Internshala";
    }

    private void humanDelay(Page page, int minMillis, int maxMillis) {
        int delay = (int) (Math.random() * (maxMillis - minMillis)) + minMillis;
        page.waitForTimeout(delay);
    }
}