package com.Abhi.job_finder.service.scraper;

import com.Abhi.job_finder.model.Job;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnstopScraper {

    private static final Logger log = LoggerFactory.getLogger(UnstopScraper.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Job> scrapeJobs(String jobTitle) {
        List<Job> jobs = new ArrayList<>();

        try {
            String url = "https://unstop.com/api/public/opportunity/search-result?opportunity=jobs"
                    + "&keyword=" + jobTitle.replace(" ", "%20")
                    + "&status=open&per_page=10&page=1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "application/json");
            headers.set("Referer", "https://unstop.com/");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Calling Unstop API for: {}", jobTitle);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("Unstop API returned: {}", response.getStatusCode());
                return jobs;
            }

            JsonNode root = mapper.readTree(response.getBody());

            // Unstop API response: data.data[] array of opportunities
            JsonNode items = root.path("data").path("data");

            if (!items.isArray()) {
                log.warn("Unstop API response structure unexpected: {}", root.toString().substring(0, Math.min(200, root.toString().length())));
                return jobs;
            }

            log.info("Unstop API returned {} jobs", items.size());

            for (JsonNode item : items) {
                try {
                    String title   = item.path("title").asText("").strip();
                    String company = item.path("organisation").path("name").asText(
                            item.path("org_name").asText("Unstop")).strip();
                    int id         = item.path("id").asInt();
                    String slug    = item.path("seo_url").asText("");
                    String jobUrl  = slug.isEmpty()
                            ? "https://unstop.com/jobs/" + id
                            : "https://unstop.com/" + slug;

                    // Build description from available fields
                    StringBuilder desc = new StringBuilder();
                    appendIfPresent(desc, "About", item.path("description").asText(""));
                    appendIfPresent(desc, "Eligibility", item.path("eligibility").asText(""));
                    appendIfPresent(desc, "Skills", item.path("skills_required").asText(""));
                    appendIfPresent(desc, "Location", item.path("city").asText(""));
                    appendIfPresent(desc, "Stipend", item.path("salary").asText(""));

                    String description = desc.toString().strip();

                    if (title.isBlank() || description.length() < 30) {
                        log.warn("Unstop job {} skipped — insufficient data", id);
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

                    log.info("Unstop job: {} at {}", title, company);

                } catch (Exception e) {
                    log.error("Failed to parse Unstop job: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Unstop API scraper failed: {}", e.getMessage(), e);
        }

        return jobs;
    }

    private void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank() && !value.equals("null")) {
            sb.append(label).append(": ").append(value).append("\n");
        }
    }
}