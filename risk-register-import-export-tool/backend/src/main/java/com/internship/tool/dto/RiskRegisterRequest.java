package com.internship.tool.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class RiskRegisterRequest {

    @NotBlank(message = "Risk code is required")
    @Size(max = 50, message = "Risk code must not exceed 50 characters")
    private String riskCode;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    @NotBlank(message = "Priority is required")
    @Size(max = 30, message = "Priority must not exceed 30 characters")
    private String priority;

    @NotBlank(message = "Impact level is required")
    @Size(max = 30, message = "Impact level must not exceed 30 characters")
    private String impactLevel;

    @NotBlank(message = "Likelihood level is required")
    @Size(max = 30, message = "Likelihood level must not exceed 30 characters")
    private String likelihoodLevel;

    @NotBlank(message = "Owner name is required")
    @Size(max = 120, message = "Owner name must not exceed 120 characters")
    private String ownerName;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email must be valid")
    @Size(max = 150, message = "Owner email must not exceed 150 characters")
    private String ownerEmail;

    @Size(max = 2000, message = "Mitigation plan must not exceed 2000 characters")
    private String mitigationPlan;

    @FutureOrPresent(message = "Target resolution date must be today or later")
    private LocalDate targetResolutionDate;

    @Size(max = 100, message = "Source system must not exceed 100 characters")
    private String sourceSystem;

    @NotNull(message = "Active flag is required")
    private Boolean active;

    public String getRiskCode() {
        return riskCode;
    }

    public void setRiskCode(String riskCode) {
        this.riskCode = riskCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getImpactLevel() {
        return impactLevel;
    }

    public void setImpactLevel(String impactLevel) {
        this.impactLevel = impactLevel;
    }

    public String getLikelihoodLevel() {
        return likelihoodLevel;
    }

    public void setLikelihoodLevel(String likelihoodLevel) {
        this.likelihoodLevel = likelihoodLevel;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getMitigationPlan() {
        return mitigationPlan;
    }

    public void setMitigationPlan(String mitigationPlan) {
        this.mitigationPlan = mitigationPlan;
    }

    public LocalDate getTargetResolutionDate() {
        return targetResolutionDate;
    }

    public void setTargetResolutionDate(LocalDate targetResolutionDate) {
        this.targetResolutionDate = targetResolutionDate;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
