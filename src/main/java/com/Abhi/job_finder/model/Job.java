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
    private String description;
    private String url;
    private List<Double> embedding;
    private double matchScore;
    private LocalDateTime scrapedAt;
    private int score;
    private boolean alerted;
    private String mode = "Remote";
    private String analysis;
    private String company;
    private String source = "LinkedIn";
//    private String source = "Internshala";
//    private String source = "Unstop";

}