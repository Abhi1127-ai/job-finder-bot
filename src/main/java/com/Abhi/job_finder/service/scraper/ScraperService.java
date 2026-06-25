package com.Abhi.job_finder.service.scraper;

import com.Abhi.job_finder.model.Job;
import com.Abhi.job_finder.repository.JobRepository;
import com.Abhi.job_finder.service.ai.JobMatchService;
import com.Abhi.job_finder.service.ai.JobPrepService;
import com.Abhi.job_finder.service.notification.TelegramNotificationService;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScraperService {

    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);
    private static final int AI_CALL_DELAY_MS = 35_000;
    private static final int MATCH_THRESHOLD = 8;

    private static final Pattern SCORE_PATTERN =
            Pattern.compile("(?i)match\\s*score\\s*[:\\-]?\\s*(\\d+)");

    private final InternshalaScaper internshalaScaper;
    private final JobRepository jobRepository;
    private final LinkedInScraper linkedInScraper;
    private final JobMatchService jobMatchService;
//    private final VectorStore vectorStore;
    private final TelegramNotificationService telegramService;
    private final UnstopScraper unstopScraper;
    private final JobPrepService jobPrepService;

    public ScraperService(
            InternshalaScaper internshalaScaper,
            LinkedInScraper linkedInScraper,
            JobMatchService jobMatchService,
//            VectorStore vectorStore,
            JobRepository jobRepository,
            TelegramNotificationService telegramService,
            UnstopScraper unstopScraper,
            JobPrepService jobPrepService) {

        this.internshalaScaper = internshalaScaper;
        this.linkedInScraper   = linkedInScraper;
        this.jobMatchService   = jobMatchService;
//        this.vectorStore       = vectorStore;
        this.jobRepository     = jobRepository;
        this.telegramService   = telegramService;
        this.unstopScraper     = unstopScraper;
        this.jobPrepService    = jobPrepService;
    }

    public void runJobHunt(String jobTitle, String myResume, String telegramChatId) {

        List<Job> allJobs = new ArrayList<>();

        try {
            List<Job> linkedInJobs = linkedInScraper.scrapejobs(jobTitle);
            log.info("LinkedIn: {} jobs", linkedInJobs.size());
            allJobs.addAll(linkedInJobs);
        } catch (Exception e) {
            log.error("LinkedIn scraper failed: {}", e.getMessage());
        }

        try {
            List<Job> internshalaJobs = internshalaScaper.scrapeJobs(jobTitle);
            log.info("Internshala: {} jobs", internshalaJobs.size());
            allJobs.addAll(internshalaJobs);
        } catch (Exception e) {
            log.error("Internshala scraper failed: {}", e.getMessage());
        }

        try {
            List<Job> unstopJobs = unstopScraper.scrapeJobs(jobTitle);
            log.info("Unstop: {} jobs", unstopJobs.size());
            allJobs.addAll(unstopJobs);
        } catch (Exception e) {
            log.error("Unstop scraper failed: {}", e.getMessage());
        }

        log.info("Total jobs from all platforms: {}", allJobs.size());
        int processed = 0;

        for (Job job : allJobs) {
            if (job.getDescription() == null || job.getDescription().isBlank()) {
                log.warn("Skipping job with no description: {}", job.getUrl());
                continue;
            }
            if (jobRepository.findByUrl(job.getUrl()).isPresent()) {
                log.info("Duplicate — skipping: {}", job.getUrl());
                continue;
            }
            if (processed > 0) sleep(AI_CALL_DELAY_MS);

            try {
                String analysis = jobMatchService.analyzeJob(job.getDescription(), myResume);
                int score = parseScore(analysis);
                log.info("Job: {} | Platform: {} | Score: {}",
                        job.getTitle(), job.getSource(), score);

                if (score >= MATCH_THRESHOLD) {
                    persistAndNotify(job, analysis, score, telegramChatId);
                } else {
                    job.setScrapedAt(LocalDateTime.now());
                    jobRepository.save(job);
                    log.info("Saved (low match {}): {}", score, job.getTitle());
                }
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("GenerateRequestsPerDayPerProjectPerModel")) {
                    log.error("Daily quota exhausted — stopping job hunt.");
                    break;
                }
                log.error("AI analysis failed for job: {} — {}", job.getUrl(), msg);
            }
            processed++;
        }
        log.info("Job hunt complete. Processed {} / {} jobs.", processed, allJobs.size());
    }

    private int parseScore(String analysis) {
        if (analysis == null || analysis.isBlank()) return 0;
        Matcher m = SCORE_PATTERN.matcher(analysis);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); }
            catch (NumberFormatException ignored) {}
        }
        log.warn("Could not parse score from analysis: {}", analysis);
        return 0;
    }

    private void sleep(int millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void persistAndNotify(Job job, String analysis,
                                  int score, String telegramChatId) {
        try {
            job.setScrapedAt(LocalDateTime.now());
            job.setScore(score);
            job.setAnalysis(analysis);
            job.setAlerted(true);
            jobRepository.save(job);

            // vector store
//            String content = job.getTitle() + "\n" + job.getDescription();
//            Document doc = new Document(content, Map.of(
//                    "title",    job.getTitle(),
//                    "url",      job.getUrl(),
//                    "analysis", analysis,
//                    "score",    String.valueOf(score),
//                    "source",   job.getSource() != null ? job.getSource() : "LinkedIn"
//            ));
//            vectorStore.add(List.of(doc));

            // send job alert
            telegramService.sendjobAlert(
                    job.getTitle(),
                    job.getSource() != null ? job.getSource() : "LinkedIn",
                    String.valueOf(score),
                    job.getUrl(),
                    telegramChatId
            );

            try {
                String roadmap = jobPrepService.generatePrepRoadmap(
                        job.getTitle(),
                        job.getDescription(),
                        "Expert Java Spring Boot developer with experience in MongoDB, " +
                                "MySQL, Playwright, microservices, and AI integration."
                );
                if (roadmap != null) {
                    job.setPrep(roadmap);
                    jobRepository.save(job);
                    telegramService.sendPrepRoadmap(job.getTitle(), roadmap, telegramChatId);
                }
            } catch (Exception e) {
                log.warn("Prep roadmap generation failed for '{}' — skipping: {}",
                        job.getTitle(), e.getMessage());
            }

            log.info("✅ Saved and notified: {} (score {}) → channel {}",
                    job.getTitle(), score, telegramChatId);

        } catch (Exception e) {
            log.error("Failed to persist/notify job '{}': {}", job.getTitle(), e.getMessage());
        }
    }
}