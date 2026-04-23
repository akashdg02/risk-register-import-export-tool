package com.internship.tool.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import java.io.Serializable;

@Service
public class AiServiceClient {
    private final RestTemplate restTemplate;
    // The Al Port is 5000 as per project specs 
    private final String AI_SERVICE_URL = "http://ai-service:5000/ai";

    public AiServiceClient(RestTemplateBuilder builder) {
        // Task: Must include a 10-second timeout 
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }

    /**
     * Day 4 Task: Call Flask /describe endpoint 
     */
    public RiskDescriptionResponse getAiDescription(String title) {
        try {
            RiskTitleRequest request = new RiskTitleRequest(title);
            return restTemplate.postForObject(AI_SERVICE_URL + "/describe", request, RiskDescriptionResponse.class);
        } catch (Exception e) {
            // Requirement: Return null on error and handle gracefully 
            return null; 
        }
    }

    /**
     * Day 4 Task: Call Flask /recommend endpoint 
     */
    public Object getAiRecommendations(String description) {
        try {
            // Replace with a specific Recommendation DTO if needed
            return restTemplate.postForObject(AI_SERVICE_URL + "/recommend", 
                                              new RiskDescriptionRequest(description), Object.class);
        } catch (Exception e) {
            return null;
        }
    }

    // --- DTO Classes (Add these to prevent 'Cannot find symbol' errors) ---

    public static class RiskTitleRequest implements Serializable {
        private String title;
        public RiskTitleRequest(String title) { this.title = title; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    public static class RiskDescriptionRequest implements Serializable {
        private String description;
        public RiskDescriptionRequest(String description) { this.description = description; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class RiskDescriptionResponse implements Serializable {
        private String description;
        private String generated_at; // Requirement for Day 3 [cite: 67]
        private boolean is_fallback; // Requirement for error handling [cite: 74]

        // Getters and Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getGenerated_at() { return generated_at; }
        public void setGenerated_at(String generated_at) { this.generated_at = generated_at; }
        public boolean isIs_fallback() { return is_fallback; }
        public void setIs_fallback(boolean is_fallback) { this.is_fallback = is_fallback; }
    }
}