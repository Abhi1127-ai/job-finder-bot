package com.Abhi.job_finder.service.scraper;

import com.Abhi.job_finder.model.Job;
import com.Abhi.job_finder.repository.JobRepository;
import com.Abhi.job_finder.service.ai.JobMatchService;
import com.Abhi.job_finder.service.notification.TelegramNotificationService;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ScraperService {

    private final JobRepository jobRepository;
    private final LinkedInScraper linkedInScraper;
    private final JobMatchService jobMatchService;
    private final VectorStore vectorStore;
    private final TelegramNotificationService telegramService;
    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

    public ScraperService(
            LinkedInScraper linkedInScraper,
            JobMatchService jobMatchService,
            VectorStore vectorStore, JobRepository jobRepository,
            TelegramNotificationService telegramService) {

        this.linkedInScraper = linkedInScraper;
        this.jobMatchService = jobMatchService;
        this.vectorStore = vectorStore;
        this.jobRepository = jobRepository;
        this.telegramService = telegramService;
    }

    public void runJobHunt(String jobTitle , String myResume){
        List<Job> rawJobs = linkedInScraper.scrapejobs(jobTitle);

        int limit = Math.min(rawJobs.size(), 10);

        for(int i = 0; i < limit; i++){
            Job job = rawJobs.get(i);

            if(job.getDescription() == null || job.getDescription().isBlank()) continue;

            if (jobRepository.findByUrl(job.getUrl()).isPresent()) {
                log.info("Skipping duplicate job: {}", job.getUrl());
                continue;
            }

            try{
                String analysis = jobMatchService.analyzeJob(job.getDescription(), myResume);

                if(isHighMatch(analysis)){
                    job.setScrapedAt(LocalDateTime.now());

                    String content = job.getTitle() + "\n" + job.getDescription();

                    Document doc = new Document(
                            content,
                            Map.of(
                                    "title", job.getTitle(),
                                    "url", job.getUrl(),
                                    "analysis", analysis
                            )
                    );
                    jobRepository.save(job);
                    vectorStore.add(List.of(doc));

                    telegramService.sendjobAlert(
                            job.getTitle(),
                            "LinkedIn",
                            extractScore(analysis),
                            job.getUrl()
                    );
                }
            }catch(Exception e){
                log.error("AI analysis failed for job: {}", job.getUrl(), e);
            }
        }
    }

    private String extractScore(String analysis) {
        if (analysis.contains("10")){
            return "10";
        }
        if(analysis.contains("9")){
            return "9";
        }
        return "8";
    }

    private boolean isHighMatch(String analysis) {
        return analysis.matches("(?s).*Match score.*(8|9|10).*");
    }


}
