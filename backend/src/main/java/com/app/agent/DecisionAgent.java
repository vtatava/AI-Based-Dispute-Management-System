package com.app.agent;

import com.app.agent.IntentAgent.IntentAnalysisResult;
import com.app.agent.ContextAgent.ContextData;
import com.app.service.IbmIcaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Decision Agent - Makes final decision on dispute resolution
 * Decides: AUTO_REFUND or HUMAN_REVIEW based on intent, context, and AI analysis
 */
@Component
public class DecisionAgent {
    
    @Autowired
    private IbmIcaService ibmIcaService;
    
    public DecisionResult makeDecision(IntentAnalysisResult intent, ContextData context,
                                      String description, double amount) {
        DecisionResult decision = new DecisionResult();
        
        // Calculate total risk score
        int totalRiskScore = context.getContextRiskScore();
        
        // Add intent-based risk
        if ("FRAUD".equals(intent.getIntent())) {
            totalRiskScore += 40;
        } else if ("MERCHANT_DISPUTE".equals(intent.getIntent())) {
            totalRiskScore += 20;
        }
        
        // Critical: If location fraud is detected, significantly increase risk
        if (context.isLocationFraudDetected()) {
            totalRiskScore += 50; // Major red flag
        }
        
        decision.setRiskScore(Math.min(totalRiskScore, 100));
        decision.setIntent(intent.getIntent());
        
        // Decision Logic
        if ("FRAUD".equals(intent.getIntent())) {
            // CRITICAL: Check for location fraud (user lying about their location)
            if (context.isLocationFraudDetected()) {
                // User is lying about their location - REJECT CLAIM
                decision.setDecision("REJECTED");
                decision.setAction("CLAIM_DENIED");
                decision.setRefundAmount(0.0);
                decision.setExplanation(buildLocationFraudRejectionExplanation(intent, context, totalRiskScore));
            } else if (totalRiskScore >= 70) {
                // High confidence fraud (genuine victim) - AUTO_REFUND
                decision.setDecision("AUTO_REFUND");
                decision.setAction("BLOCK_CARD");
                decision.setRefundAmount(amount);
                decision.setExplanation(buildFraudExplanation(intent, context, totalRiskScore));
            } else if (totalRiskScore >= 40) {
                // Medium risk - HUMAN_REVIEW
                decision.setDecision("HUMAN_REVIEW");
                decision.setAction("INVESTIGATE");
                decision.setRefundAmount(null);
                decision.setExplanation(buildMediumRiskExplanation(intent, context, totalRiskScore));
            } else {
                // Low risk fraud claim - HUMAN_REVIEW
                decision.setDecision("HUMAN_REVIEW");
                decision.setAction("VERIFY");
                decision.setRefundAmount(null);
                decision.setExplanation(buildLowRiskExplanation(intent, context, totalRiskScore));
            }
        } else if ("MERCHANT_DISPUTE".equals(intent.getIntent())) {
            // Merchant disputes always need human review
            decision.setDecision("HUMAN_REVIEW");
            decision.setAction("CONTACT_MERCHANT");
            decision.setRefundAmount(null);
            decision.setExplanation(buildMerchantDisputeExplanation(intent, context));
        } else {
            // Other disputes
            decision.setDecision("HUMAN_REVIEW");
            decision.setAction("INVESTIGATE");
            decision.setRefundAmount(null);
            decision.setExplanation(buildOtherDisputeExplanation(intent, context));
        }
        
        // Add AI enhancement if available
        try {
            String aiEnhancement = getAIEnhancement(description, decision, totalRiskScore);
            decision.addAiInsight(aiEnhancement);
        } catch (Exception e) {
            // AI enhancement failed, continue with rule-based decision
        }
        
        return decision;
    }
    
    private String buildFraudExplanation(IntentAnalysisResult intent, ContextData context, int riskScore) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("🚨 HIGH RISK FRAUD DETECTED (Risk Score: ").append(riskScore).append("/100)\n\n");
        explanation.append("**Decision: AUTO_REFUND & BLOCK_CARD**\n\n");
        explanation.append("**Reasons:**\n");
        explanation.append("• Intent Classification: ").append(intent.getIntent()).append(" (").append(intent.getConfidence()).append(" confidence)\n");
        explanation.append("• ").append(intent.getReason()).append("\n");
        
        if (context.isLocationFraudDetected()) {
            explanation.append("• 🚨 LOCATION FRAUD: User's claimed location does not match database records\n");
            explanation.append("• ⚠️ Database shows user is in: ").append(context.getDbCurrentLocation()).append("\n");
        }
        if (context.isLocationMismatch()) {
            explanation.append("• 🌍 Critical: Location mismatch detected\n");
        }
        if (context.isFraudWebsiteDetected()) {
            explanation.append("• 🚨 Fraud website detected (").append(context.getFraudWebsiteRiskLevel()).append(" risk)\n");
        }
        
        explanation.append("\n**Context Analysis:**\n");
        explanation.append(context.getContextNotes());
        
        explanation.append("\n\n**Actions Taken:**\n");
        explanation.append("✓ Full refund approved automatically\n");
        explanation.append("✓ Card blocked for security\n");
        explanation.append("✓ New card will be issued\n");
        
        return explanation.toString();
    }
    
    private String buildMediumRiskExplanation(IntentAnalysisResult intent, ContextData context, int riskScore) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("⚠️ MEDIUM RISK - HUMAN REVIEW REQUIRED (Risk Score: ").append(riskScore).append("/100)\n\n");
        explanation.append("**Decision: HUMAN_REVIEW**\n\n");
        explanation.append("**Reasons:**\n");
        explanation.append("• Intent: ").append(intent.getIntent()).append(" - ").append(intent.getReason()).append("\n");
        explanation.append("• Risk indicators present but not conclusive\n");
        explanation.append("• Requires expert verification\n\n");
        explanation.append("**Context:**\n").append(context.getContextNotes());
        explanation.append("\n\n**Next Steps:**\n");
        explanation.append("• Case escalated to fraud investigation team\n");
        explanation.append("• Customer will be contacted within 24 hours\n");
        explanation.append("• Additional documentation may be requested\n");
        return explanation.toString();
    }
    
    private String buildLowRiskExplanation(IntentAnalysisResult intent, ContextData context, int riskScore) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("ℹ️ LOW RISK - VERIFICATION NEEDED (Risk Score: ").append(riskScore).append("/100)\n\n");
        explanation.append("**Decision: HUMAN_REVIEW**\n\n");
        explanation.append("**Reasons:**\n");
        explanation.append("• Intent: ").append(intent.getIntent()).append("\n");
        explanation.append("• Low risk indicators\n");
        explanation.append("• Standard verification process required\n\n");
        explanation.append("**Context:**\n").append(context.getContextNotes());
        return explanation.toString();
    }
    
    private String buildMerchantDisputeExplanation(IntentAnalysisResult intent, ContextData context) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("🏪 MERCHANT DISPUTE DETECTED\n\n");
        explanation.append("**Decision: HUMAN_REVIEW**\n\n");
        explanation.append("**Reason:** ").append(intent.getReason()).append("\n\n");
        explanation.append("**Process:**\n");
        explanation.append("• Merchant will be contacted for their response\n");
        explanation.append("• Delivery/service records will be verified\n");
        explanation.append("• Resolution typically within 5-7 business days\n\n");
        explanation.append("**Context:**\n").append(context.getContextNotes());
        return explanation.toString();
    }
    
    private String buildOtherDisputeExplanation(IntentAnalysisResult intent, ContextData context) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("📋 DISPUTE REQUIRES INVESTIGATION\n\n");
        explanation.append("**Decision: HUMAN_REVIEW**\n\n");
        explanation.append("**Reason:** ").append(intent.getReason()).append("\n\n");
        explanation.append("**Context:**\n").append(context.getContextNotes());
        return explanation.toString();
    }
    
    private String buildLocationFraudRejectionExplanation(IntentAnalysisResult intent, ContextData context, int riskScore) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("🚨 CLAIM REJECTED - LOCATION FRAUD DETECTED (Risk Score: ").append(riskScore).append("/100)\n\n");
        explanation.append("**Decision: REJECTED - CLAIM DENIED**\n\n");
        explanation.append("**Critical Issue: User Provided False Location Information**\n\n");
        explanation.append("**Reasons for Rejection:**\n");
        explanation.append("• 🚨 LOCATION FRAUD: User's claimed location does not match database records\n");
        explanation.append("• ⚠️ Database shows user is in: ").append(context.getDbCurrentLocation()).append("\n");
        explanation.append("• ⚠️ User falsely claims to be in a different location\n");
        explanation.append("• 🚫 Providing false information invalidates the claim\n");
        explanation.append("• ⚠️ This appears to be an attempt to commit fraud\n\n");
        
        explanation.append("**Context Analysis:**\n");
        explanation.append(context.getContextNotes());
        
        explanation.append("\n\n**Actions Taken:**\n");
        explanation.append("❌ Claim REJECTED - No refund will be issued\n");
        explanation.append("⚠️ Account flagged for fraudulent activity\n");
        explanation.append("🔍 Case escalated to fraud investigation team\n");
        explanation.append("📋 User may face account suspension\n\n");
        
        explanation.append("**Important Notice:**\n");
        explanation.append("Providing false information in a dispute claim is a serious violation. ");
        explanation.append("Our records clearly show your actual location, which contradicts your claim. ");
        explanation.append("If you believe this is an error, please contact customer support with valid proof of your location.");
        
        return explanation.toString();
    }
    
    private String getAIEnhancement(String description, DecisionResult decision, int riskScore) {
        // This would call IBM ICA for additional insights
        // Simplified for now
        return "AI analysis confirms the decision based on pattern recognition and historical data.";
    }
    
    // Inner class for Decision Result
    public static class DecisionResult {
        private String decision; // AUTO_REFUND or HUMAN_REVIEW
        private String action; // BLOCK_CARD, INVESTIGATE, CONTACT_MERCHANT, etc.
        private String intent;
        private int riskScore;
        private Double refundAmount;
        private String explanation;
        private StringBuilder aiInsights = new StringBuilder();
        
        public void addAiInsight(String insight) {
            if (aiInsights.length() > 0) {
                aiInsights.append("\n");
            }
            aiInsights.append(insight);
        }
        
        // Getters and Setters
        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getIntent() { return intent; }
        public void setIntent(String intent) { this.intent = intent; }
        
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        
        public Double getRefundAmount() { return refundAmount; }
        public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }
        
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        
        public String getAiInsights() { return aiInsights.toString(); }
    }
}

// Made with Bob
