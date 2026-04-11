package com.Abhi.job_finder.controller;

import com.Abhi.job_finder.dto.JobHuntRequest;
import com.Abhi.job_finder.model.Job;
import com.Abhi.job_finder.service.ai.JobMatchService;
import com.Abhi.job_finder.service.scraper.ScraperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final ScraperService scraperService;
    private final JobMatchService jobMatchService;


    public JobController(ScraperService scrapperService, JobMatchService jobMatchService) {
        this.scraperService = scrapperService;
        this.jobMatchService = jobMatchService;
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
}
