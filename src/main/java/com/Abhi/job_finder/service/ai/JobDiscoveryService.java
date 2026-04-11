package com.Abhi.job_finder.service.ai;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobDiscoveryService {

    private  VectorStore vectorStore;

    public JobDiscoveryService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> findTopMatches(String query){
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .similarityThreshold(0.7)  // optional
                        .build()

        );
    }
}