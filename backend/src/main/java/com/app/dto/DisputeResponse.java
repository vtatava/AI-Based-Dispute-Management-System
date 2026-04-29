package com.app.dto;

public class DisputeResponse {
    private String intent;
    private int riskScore;
    private String decision;
    private Double refundAmount;
    private String reviewReason;
    
    // Agent flow information
    private AgentFlow agentFlow;
    private boolean userVerified;
    private String verificationMessage;

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

    public AgentFlow getAgentFlow() {
        return agentFlow;
    }

    public void setAgentFlow(AgentFlow agentFlow) {
        this.agentFlow = agentFlow;
    }

    public boolean isUserVerified() {
        return userVerified;
    }

    public void setUserVerified(boolean userVerified) {
        this.userVerified = userVerified;
    }

    public String getVerificationMessage() {
        return verificationMessage;
    }

    public void setVerificationMessage(String verificationMessage) {
        this.verificationMessage = verificationMessage;
    }

    // Inner class for Agent Flow visualization
    public static class AgentFlow {
        private String intentAgent;
        private String contextAgent;
        private String decisionAgent;
        private String intentResult;
        private String contextResult;
        private String decisionResult;

        public AgentFlow() {
        }

        public AgentFlow(String intentAgent, String contextAgent, String decisionAgent,
                        String intentResult, String contextResult, String decisionResult) {
            this.intentAgent = intentAgent;
            this.contextAgent = contextAgent;
            this.decisionAgent = decisionAgent;
            this.intentResult = intentResult;
            this.contextResult = contextResult;
            this.decisionResult = decisionResult;
        }

        // Getters and Setters
        public String getIntentAgent() { return intentAgent; }
        public void setIntentAgent(String intentAgent) { this.intentAgent = intentAgent; }

        public String getContextAgent() { return contextAgent; }
        public void setContextAgent(String contextAgent) { this.contextAgent = contextAgent; }

        public String getDecisionAgent() { return decisionAgent; }
        public void setDecisionAgent(String decisionAgent) { this.decisionAgent = decisionAgent; }

        public String getIntentResult() { return intentResult; }
        public void setIntentResult(String intentResult) { this.intentResult = intentResult; }

        public String getContextResult() { return contextResult; }
        public void setContextResult(String contextResult) { this.contextResult = contextResult; }

        public String getDecisionResult() { return decisionResult; }
        public void setDecisionResult(String decisionResult) { this.decisionResult = decisionResult; }
    }
}

// Made with Bob
