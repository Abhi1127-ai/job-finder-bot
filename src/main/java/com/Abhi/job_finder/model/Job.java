package com.Abhi.job_finder.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    private String title;
    private String description;
    private String url;
    private List<Double> embedding;

    private double matchScore;
    private LocalDateTime scrapedAt;
}