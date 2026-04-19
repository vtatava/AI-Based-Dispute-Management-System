package com.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DISPUTE_RECORDS")
public class DisputeRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", length = 50)
    private String userId;
    
    @Column(name = "transaction_type", length = 50)
    private String transactionType;
    
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    
    @Column(name = "amount")
    private Double amount;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "intent", length = 50)
    private String intent;
    
    @Column(name = "risk_score")
    private Integer riskScore;
    
    @Column(name = "decision", length = 100)
    private String decision;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Additional fields for enhanced tracking
    @Column(name = "transaction_location", length = 100)
    private String transactionLocation;
    
    @Column(name = "user_current_location", length = 100)
    private String userCurrentLocation;
    
    @Column(name = "refund_amount")
    private Double refundAmount;
    
    @Column(name = "review_reason", columnDefinition = "TEXT")
    private String reviewReason;
    
    @Column(name = "website_url", length = 500)
    private String websiteUrl;
    
    // Constructors
    public DisputeRecord() {
        this.createdAt = LocalDateTime.now();
    }
    
    public DisputeRecord(String userId, String transactionType, LocalDateTime transactionDate, 
                        Double amount, String description, String intent, Integer riskScore, String decision) {
        this.userId = userId;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.description = description;
        this.intent = intent;
        this.riskScore = riskScore;
        this.decision = decision;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
    
    public Integer getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }
    
    public String getDecision() {
        return decision;
    }
    
    public void setDecision(String decision) {
        this.decision = decision;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getTransactionLocation() {
        return transactionLocation;
    }
    
    public void setTransactionLocation(String transactionLocation) {
        this.transactionLocation = transactionLocation;
    }
    
    public String getUserCurrentLocation() {
        return userCurrentLocation;
    }
    
    public void setUserCurrentLocation(String userCurrentLocation) {
        this.userCurrentLocation = userCurrentLocation;
    }
    
    public Double getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public String getReviewReason() {
        return reviewReason;
    }
    
    public void setReviewReason(String reviewReason) {
        this.reviewReason = reviewReason;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
}

// Made with Bob