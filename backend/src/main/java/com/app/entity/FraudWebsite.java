package com.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FRAUD_WEBSITES")
public class FraudWebsite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "website_url", nullable = false, unique = true, length = 500)
    private String websiteUrl;
    
    @Column(name = "risk_level", nullable = false)
    private String riskLevel;
    
    @Column(name = "reported_date")
    private LocalDateTime reportedDate;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // Constructors
    public FraudWebsite() {
        this.reportedDate = LocalDateTime.now();
    }
    
    public FraudWebsite(String websiteUrl, String riskLevel, String description) {
        this.websiteUrl = websiteUrl;
        this.riskLevel = riskLevel;
        this.description = description;
        this.reportedDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public LocalDateTime getReportedDate() {
        return reportedDate;
    }
    
    public void setReportedDate(LocalDateTime reportedDate) {
        this.reportedDate = reportedDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}

// Made with Bob
