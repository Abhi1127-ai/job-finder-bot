package com.Abhi.job_finder.repository;

import com.Abhi.job_finder.model.Job;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {

    List<Job> findAll();
    List<Job> findByTitle(String title);
    Optional<Job> findByUrl(String url);

    @Query("{ 'score' : { $gte : ?0 } }")
    List<Job> findHighQualityMatches(int minScore);

    @Query("{ 'alerted' : true }")
    List<Job> findAlertedJobs();
}
