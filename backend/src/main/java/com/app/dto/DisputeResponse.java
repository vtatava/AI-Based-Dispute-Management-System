package com.app.dto;

public class DisputeResponse {
    private String intent;
    private int riskScore;
    private String decision;
    private Double refundAmount;
    private String reviewReason;

    public DisputeResponse() {
    }

    public DisputeResponse(String intent, int riskScore, String decision, Double refundAmount, String reviewReason) {
        this.intent = intent;
        this.riskScore = riskScore;
        this.decision = decision;
        this.refundAmount = refundAmount;
        this.reviewReason = reviewReason;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
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
}

// Made with Bob
