package com.Abhi.job_finder.controller;

import com.Abhi.job_finder.dto.JobHuntRequest;
import com.Abhi.job_finder.model.Job;
import com.Abhi.job_finder.repository.JobRepository;
import com.Abhi.job_finder.service.ai.JobMatchService;
import com.Abhi.job_finder.service.scraper.ScraperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        CompletableFuture.runAsync(() -> scraperService.runJobHunt(title,resume)) ;
        return ResponseEntity.ok("Job Hunt Started :" + title + ". Check logs for progress!");
    }

    @GetMapping("/search")
    public List<Job> seachJobs(String query){
        return jobMatchService.findTopMatches();
    }

    @GetMapping
    public List<Job> getAllJobs(
            @RequestParam(required = false) Boolean alerted,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort) {
        return jobRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable String id) {
        jobRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobs", jobRepository.count());
        stats.put("totalToday", jobRepository.count());
        stats.put("highMatches", jobRepository.findAll().stream().filter(j -> j.getScore() >= 8).count());
        stats.put("alertsSent", jobRepository.findAll().stream().filter(j -> j.isAlerted()).count());
        stats.put("jobTitle", "java Full Stack Developer");
        stats.put("threshold", 8);
        return ResponseEntity.ok(stats);
    }
}
