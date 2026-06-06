package com.Abhi.job_finder.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class JobPrepService {

    private static final Logger log = LoggerFactory.getLogger(JobPrepService.class);
    private final ChatClient chatClient;

    public JobPrepService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String generatePrepRoadmap(String jobTitle, String jobDescription, String resume) {
        String prompt = String.format("""
                You are a career coach. A candidate has been matched with the following job.
                
                Job Title: %s
                
                Job Description:
                %s
                
                Candidate Resume Summary:
                %s
                
                Generate a concise interview preparation roadmap for this specific job.
                Format your response exactly like this:
                
                📚 KEY TOPICS TO STUDY:
                - [topic 1]
                - [topic 2]
                - [topic 3]
                
                🛠 SKILLS TO BRUSH UP:
                - [skill 1]
                - [skill 2]
                
                ❓ LIKELY INTERVIEW QUESTIONS:
                - [question 1]
                - [question 2]
                - [question 3]
                
                🔗 QUICK RESOURCES:
                - [resource 1]
                - [resource 2]
                
                Keep it short and actionable. Max 300 words.
                """,
                jobTitle,
                jobDescription.substring(0, Math.min(jobDescription.length(), 800)),
                resume
        );

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to generate prep roadmap for: {} — {}", jobTitle, e.getMessage());
            return null;
        }
    }
}