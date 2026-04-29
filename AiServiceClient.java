package com.internship.tool.service;

import com.internship.tool.model.Risk; // Ensure you import your Risk model
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import java.io.Serializable;
import java.util.HashMap; // Required for Map
import java.util.Map;     // Required for Map

@Service
public class AiServiceClient {
    private final RestTemplate restTemplate;
    // Port 5000 is the dedicated AI microservice port
    private final String AI_SERVICE_URL = "http://ai-service:5000/ai";

    public AiServiceClient(RestTemplateBuilder builder) {
        // Requirement: Must include a 10-second timeout
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
            // Requirement: Handle gracefully and return null
            return null; 
        }
    }

    /**
     * Day 4 Task: Call Flask /recommend endpoint
     */
    public Object getAiRecommendations(String description) {
        try {
            return restTemplate.postForObject(AI_SERVICE_URL + "/recommend", 
                                              new RiskDescriptionRequest(description), Object.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Day 7 Task: Call Flask /generate-report endpoint
     */
    public String generateFullReport(Risk risk) {
        try {
            // Map used to send title, description, and recommendations as a single JSON object
            Map<String, Object> request = new HashMap<>();
            request.put("title", risk.getTitle());
            request.put("description", risk.getDescription());
            request.put("recommendations", risk.getRecommendations());

            @SuppressWarnings("unchecked")
            Map<String, String> response = restTemplate.postForObject(
                AI_SERVICE_URL + "/generate-report", 
                request, 
                Map.class
            );

            return response != null ? response.get("report_text") : "Report generation failed.";
        } catch (Exception e) {
            // Returns a user-friendly error string instead of crashing
            return "Service unavailable: Could not generate report.";
        }
    }

    // --- DTO Classes ---

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
        private String generated_at; // Required timestamp from Day 3
        private boolean is_fallback; // Required fallback flag

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getGenerated_at() { return generated_at; }
        public void setGenerated_at(String generated_at) { this.generated_at = generated_at; }
        public boolean isIs_fallback() { return is_fallback; }
        public void setIs_fallback(boolean is_fallback) { this.is_fallback = is_fallback; }
    }
}