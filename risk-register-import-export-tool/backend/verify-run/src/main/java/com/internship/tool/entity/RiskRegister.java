package com.internship.tool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "risk_registers")
@EntityListeners(AuditingEntityListener.class)
public class RiskRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "risk_code", nullable = false, unique = true, length = 50)
    private String riskCode;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "priority", nullable = false, length = 30)
    private String priority;

    @Column(name = "impact_level", nullable = false, length = 30)
    private String impactLevel;

    @Column(name = "likelihood_level", nullable = false, length = 30)
    private String likelihoodLevel;

    @Column(name = "owner_name", nullable = false, length = 120)
    private String ownerName;

    @Column(name = "owner_email", nullable = false, length = 150)
    private String ownerEmail;

    @Column(name = "mitigation_plan", length = 2000)
    private String mitigationPlan;

    @Column(name = "target_resolution_date")
    private LocalDate targetResolutionDate;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
