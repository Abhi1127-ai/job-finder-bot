package com.Abhi.job_finder.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    private String title;
    private String company;
    private String description;
    private String url;
    private String source;   // "LinkedIn" | "Internshala" | "Unstop"
    private String mode;     // "Remote" | "On-site" | "Hybrid"
    private String analysis;

    private int            score;
    private boolean        alerted;
    private LocalDateTime  scrapedAt;
    private List<Double> embedding;

    private String prep;
}