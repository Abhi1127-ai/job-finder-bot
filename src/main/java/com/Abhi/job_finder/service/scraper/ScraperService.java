package com.Abhi.job_finder.service.scraper;

import com.Abhi.job_finder.model.Job;
import com.Abhi.job_finder.repository.JobRepository;
import com.Abhi.job_finder.service.ai.JobMatchService;
import com.Abhi.job_finder.service.notification.TelegramNotificationService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScraperService {

    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);
    private static final int AI_CALL_DELAY_MS = 35_000;
    private static final int MATCH_THRESHOLD = 8;

    // Matches "Match score: 9", "Match Score : 10/10", "score 8" etc. (case-insensitive)
    private static final Pattern SCORE_PATTERN =
            Pattern.compile("(?i)match\\s*score\\s*[:\\-]?\\s*(\\d+)");

    private final JobRepository jobRepository;
    private final LinkedInScraper linkedInScraper;
    private final JobMatchService jobMatchService;
    private final VectorStore vectorStore;
    private final TelegramNotificationService telegramService;

    public ScraperService(
            LinkedInScraper linkedInScraper,
            JobMatchService jobMatchService,
            VectorStore vectorStore,
            JobRepository jobRepository,
            TelegramNotificationService telegramService) {

        this.linkedInScraper  = linkedInScraper;
        this.jobMatchService  = jobMatchService;
        this.vectorStore      = vectorStore;
        this.jobRepository    = jobRepository;
        this.telegramService  = telegramService;
    }

    public void runJobHunt(String jobTitle, String myResume) {
        List<Job> rawJobs = linkedInScraper.scrapejobs(jobTitle);
        log.info("Scraper returned {} jobs for '{}'", rawJobs.size(), jobTitle);

        int processed = 0;

        for (Job job : rawJobs) {

            // ── Skip empty descriptions ──────────────────────────────────────
            if (job.getDescription() == null || job.getDescription().isBlank()) {
                log.warn("Skipping job with no description: {}", job.getUrl());
                continue;
            }

            // ── Skip already-seen URLs ────────────────────────────────────────
            if (jobRepository.findByUrl(job.getUrl()).isPresent()) {
                log.info("Duplicate — skipping: {}", job.getUrl());
                continue;
            }

            // ── Rate-limit: pause before every AI call (except the first) ─────
            if (processed > 0) sleep(AI_CALL_DELAY_MS);

            try {
                String analysis = jobMatchService.analyzeJob(job.getDescription(), myResume);
                int score = parseScore(analysis);
                log.info("Job: {} | Score: {}", job.getTitle(), score);

                if (score >= MATCH_THRESHOLD) {
                    persistAndNotify(job, analysis, score);
                } else {
                    // Still save it even if score is low
                    job.setScrapedAt(LocalDateTime.now());
                    jobRepository.save(job);
                    log.info("Saved (low match {}): {}", score, job.getTitle());
                }
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("GenerateRequestsPerDayPerProjectPerModel")) {
                    log.error("Daily Gemini quota exhausted — stopping job hunt until quota resets.");
                    break;
                }
                log.error("AI analysis failed for job: {} — {}", job.getUrl(), msg);
            }

            processed++;
        }

        log.info("Job hunt complete. Processed {} / {} jobs.", processed, rawJobs.size());
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private void persistAndNotify(Job job, String analysis, int score) {
        try {
            job.setScrapedAt(LocalDateTime.now());
            jobRepository.save(job);

            String content = job.getTitle() + "\n" + job.getDescription();
            Document doc = new Document(
                    content,
                    Map.of(
                            "title",    job.getTitle(),
                            "url",      job.getUrl(),
                            "analysis", analysis,
                            "score",    String.valueOf(score)
                    )
            );
            vectorStore.add(List.of(doc));

            telegramService.sendjobAlert(
                    job.getTitle(),
                    "LinkedIn",
                    String.valueOf(score),
                    job.getUrl()
            );

            log.info("Saved and notified: {} (score {})", job.getTitle(), score);

        } catch (Exception e) {
            log.error("Failed to persist/notify job '{}': {}", job.getTitle(), e.getMessage());
        }
    }

    /**
     * Parse the numeric match score from Gemini's response.
     * Returns 0 if the response is unparseable so the job is safely skipped.
     */
    private int parseScore(String analysis) {
        if (analysis == null || analysis.isBlank()) return 0;

        Matcher m = SCORE_PATTERN.matcher(analysis);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
        }

        log.warn("Could not parse score from analysis: {}", analysis);
        return 0;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}