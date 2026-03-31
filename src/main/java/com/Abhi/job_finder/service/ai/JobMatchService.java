package com.Abhi.job_finder.service.ai;

import com.Abhi.job_finder.model.Job;
import com.Abhi.job_finder.repository.JobRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobMatchService {

    private final JobRepository jobRepository;
    private final ChatClient chatClient;

    public JobMatchService(JobRepository jobRepository, ChatClient chatClient){
        this.jobRepository = jobRepository;
        this.chatClient = chatClient;
    }

    public List<Job> findTopMatches() {
        return jobRepository.findAll();
    }

    public String analyzeJob(String description, String myResume) {
        String prompt = """
                Analyze the following job description against my resume.
                Provide a 'Match score' out of 10 and a 2-line reason why.
                Job Description: %s.
                My Resume: %s.
                """.formatted(description, myResume);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
