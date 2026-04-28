package com.internship.tool.service;

import com.internship.tool.model.Risk;
import com.internship.tool.repository.RiskRepository;
import com.internship.tool.service.AiServiceClient.RiskDescriptionResponse; // Import the DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskService {

    @Autowired
    private RiskRepository riskRepository;

    @Autowired
    private AiServiceClient aiServiceClient;

    /**
     * Day 5 Task: Enrich risk with AI description asynchronously.
     * This prevents the user from waiting 5-10 seconds for the AI to respond.
     */
    @Async
    @Transactional // Ensures database consistency
    public void enrichRiskWithAi(Long riskId) {
        // Fetch the latest version of the risk by ID to avoid stale data
        Risk risk = riskRepository.findById(riskId).orElse(null);
        
        if (risk != null) {
            RiskDescriptionResponse response = aiServiceClient.getAiDescription(risk.getTitle());
            
            // Requirement: Only update if the AI call was successful and not a fallback
            if (response != null && !response.isIs_fallback()) {
                risk.setDescription(response.getDescription());
                riskRepository.save(risk);
            }
        }
    }
}