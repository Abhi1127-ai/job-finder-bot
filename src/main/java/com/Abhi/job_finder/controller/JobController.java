package com.Abhi.job_finder.controller;

import com.Abhi.job_finder.dto.JobHuntRequest;
import com.Abhi.job_finder.model.Job;
import com.Abhi.job_finder.repository.JobRepository;
import com.Abhi.job_finder.service.ai.JobMatchService;
import com.Abhi.job_finder.service.scraper.ScraperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final ScraperService scraperService;
    private final JobMatchService jobMatchService;
    private final JobRepository jobRepository;

    public JobController(ScraperService scrapperService, JobMatchService jobMatchService, JobRepository jobRepository) {
        this.scraperService = scrapperService;
        this.jobMatchService = jobMatchService;
        this.jobRepository = jobRepository;
    }

    @PostMapping("/hunt")
    public ResponseEntity<String> runJobHunt(@RequestBody JobHuntRequest request){
        String title = request.getTitle();
        String resume = request.getResume();
        String telegramChatId = request.getTelegramChatId();
        CompletableFuture.runAsync(() -> scraperService.runJobHunt(title, resume, telegramChatId));
        return ResponseEntity.ok("Job Hunt Started :" + title + ". Check logs for progress!");
    }

    @GetMapping("/search")
    public List<Job> seachJobs(String query){
        return jobMatchService.findTopMatches();
    }

    @GetMapping
    public List<Job> getAllJobs(
            @RequestParam(required = false) Boolean alerted,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "score") String sort) {

        List<Job> jobs = jobRepository.findAll();

        if (Boolean.TRUE.equals(alerted)) {
            jobs = jobs.stream().filter(Job::isAlerted).collect(Collectors.toList());
        }

        jobs.sort("score".equals(sort)
                ? Comparator.comparingInt(Job::getScore).reversed()
                : Comparator.comparing(Job::getScrapedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        return jobs.stream().limit(limit).collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable String id) {
        jobRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Job> all = jobRepository.findAll();
        long today = all.stream()
                .filter(j -> j.getScrapedAt() != null &&
                        j.getScrapedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobs",   all.size());
        stats.put("totalToday",  today);
        stats.put("highMatches", all.stream().filter(j -> j.getScore() >= 8).count());
        stats.put("alertsSent",  all.stream().filter(Job::isAlerted).count());
        stats.put("lastRunAt",   all.stream()
                .map(Job::getScrapedAt).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).map(Object::toString).orElse(null));
        stats.put("jobTitle",    "java Full Stack Developer");
        stats.put("threshold",   8);
        stats.put("recentAlerts", all.stream()
                .filter(j -> j.getScore() >= 8)
                .sorted(Comparator.comparing(Job::getScrapedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(j -> Map.of("title", j.getTitle(), "score", j.getScore()))
                .toList());
        return ResponseEntity.ok(stats);
    }
}
