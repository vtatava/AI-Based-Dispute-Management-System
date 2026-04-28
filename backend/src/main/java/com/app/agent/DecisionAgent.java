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
        
        // CRITICAL: Check for fraudulent/frivolous intent FIRST
        if ("FRAUDULENT_CLAIM".equals(intent.getIntent()) || "FRIVOLOUS".equals(intent.getIntent())) {
            decision.setRiskScore(100);
            decision.setIntent(intent.getIntent());
            decision.setDecision("REJECTED");
            decision.setAction("CLAIM_DENIED");
            decision.setRefundAmount(0.0);
            decision.setExplanation(buildFraudulentClaimRejection(intent, context));
            return decision; // Early return - no need for further analysis
        }
        
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
        
        // Get IBM ICA AI Analysis BEFORE making final decision
        IbmIcaService.IcaAnalysisResult icaResult = null;
        try {
            icaResult = ibmIcaService.analyzeDispute(
                description, amount, "UNKNOWN", "UNKNOWN", totalRiskScore, "PENDING"
            );
        } catch (Exception e) {
            System.err.println("IBM ICA analysis failed: " + e.getMessage());
        }
        
        // CRITICAL: Check IBM ICA AI recommendation FIRST
        // If AI says NO or UNCERTAIN with high risk, reject the claim
        if (icaResult != null) {
            String fraudAssessment = icaResult.getFraudAssessment();
            String recommendation = icaResult.getRecommendedDecision();
            
            // If AI explicitly says NO to fraud assessment, reject
            if ("NO".equals(fraudAssessment)) {
                decision.setDecision("REJECTED");
                decision.setAction("CLAIM_DENIED");
                decision.setRefundAmount(0.0);
                decision.setExplanation(buildAIRejectionExplanation(intent, context, totalRiskScore, icaResult));
                return decision;
            }
            
            // If AI is uncertain but risk is very high (100) and doesn't recommend AUTO_REFUND, reject
            if (("UNCERTAIN".equals(fraudAssessment) || fraudAssessment == null) &&
                totalRiskScore >= 90 && !"AUTO_REFUND".equals(recommendation)) {
                decision.setDecision("REJECTED");
                decision.setAction("CLAIM_DENIED");
                decision.setRefundAmount(0.0);
                decision.setExplanation(buildAIRejectionExplanation(intent, context, totalRiskScore, icaResult));
                return decision;
            }
        }
        
        // Decision Logic - Consider AI recommendation along with rules
        if ("FRAUD".equals(intent.getIntent())) {
            // CRITICAL: Check for location fraud (user lying about their location)
            if (context.isLocationFraudDetected()) {
                // User is lying about their location - REJECT CLAIM
                decision.setDecision("REJECTED");
                decision.setAction("CLAIM_DENIED");
                decision.setRefundAmount(0.0);
                decision.setExplanation(buildLocationFraudRejectionExplanation(intent, context, totalRiskScore));
            } else if (totalRiskScore >= 70) {
                // High confidence fraud (genuine victim) - but check AI recommendation
                if (icaResult != null && "AUTO_REFUND".equals(icaResult.getRecommendedDecision())) {
                    decision.setDecision("AUTO_REFUND");
                    decision.setAction("BLOCK_CARD");
                    decision.setRefundAmount(amount);
                    decision.setExplanation(buildFraudExplanation(intent, context, totalRiskScore));
                } else {
                    // AI suggests caution - send to human review
                    decision.setDecision("HUMAN_REVIEW");
                    decision.setAction("INVESTIGATE");
                    decision.setRefundAmount(null);
                    decision.setExplanation(buildMediumRiskExplanation(intent, context, totalRiskScore));
                }
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
    
    private String buildFraudulentClaimRejection(IntentAnalysisResult intent, ContextData context) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("🚫 CLAIM REJECTED - FRAUDULENT/CONTRADICTORY INTENT DETECTED\n\n");
        explanation.append("**Decision: REJECTED - CLAIM DENIED**\n\n");
        explanation.append("**Critical Issue: ").append(intent.getReason()).append("**\n\n");
        explanation.append("**Reasons for Rejection:**\n");
        explanation.append("• 🚨 FRAUDULENT INTENT: ").append(intent.getReason()).append("\n");
        explanation.append("• ⚠️ User's statement contains contradictory or fraudulent elements\n");
        explanation.append("• 🚫 This type of claim cannot be processed as it violates dispute policies\n");
        explanation.append("• ⚠️ Attempting to file false claims may result in account suspension\n\n");
        
        explanation.append("**Important Notice:**\n");
        explanation.append("Providing false or contradictory information in a dispute claim is a serious violation. ");
        explanation.append("If you believe this is an error, please contact customer support with accurate information. ");
        explanation.append("Genuine disputes with clear, honest descriptions will be processed appropriately.");
        
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
    
    private String buildAIRejectionExplanation(IntentAnalysisResult intent, ContextData context, int riskScore, IbmIcaService.IcaAnalysisResult icaResult) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("🚫 CLAIM REJECTED - AI FRAUD DETECTION\n\n");
        explanation.append("**Decision: REJECTED - CLAIM DENIED**\n\n");
        explanation.append("**IBM ICA AI Analysis Result:**\n");
        explanation.append("• 🤖 AI Assessment: ").append(icaResult.getFraudAssessment()).append("\n");
        explanation.append("• 📊 Confidence Level: ").append(icaResult.getConfidenceLevel()).append("\n");
        explanation.append("• ⚖️ AI Recommendation: ").append(icaResult.getRecommendedDecision()).append("\n");
        explanation.append("• 🚨 Risk Score: ").append(riskScore).append("/100\n\n");
        
        explanation.append("**Critical Issues Detected:**\n");
        explanation.append("• ⚠️ AI has identified this claim as potentially fraudulent or contradictory\n");
        explanation.append("• 🚫 The description contains elements that suggest this is not a legitimate fraud claim\n");
        explanation.append("• 🔍 Pattern analysis indicates inconsistencies in the claim\n\n");
        
        explanation.append("**AI Key Reasons:**\n");
        explanation.append(icaResult.getKeyReasons()).append("\n\n");
        
        explanation.append("**Red Flags Identified:**\n");
        explanation.append(icaResult.getRedFlags()).append("\n\n");
        
        explanation.append("**Actions Taken:**\n");
        explanation.append("❌ Claim REJECTED - No refund will be issued\n");
        explanation.append("⚠️ Account flagged for review\n");
        explanation.append("📋 Case documented for fraud pattern analysis\n\n");
        
        explanation.append("**Important Notice:**\n");
        explanation.append("Our AI-powered fraud detection system has identified issues with this claim. ");
        explanation.append("If you believe this is an error, please contact customer support with additional ");
        explanation.append("documentation and a clear, honest explanation of the situation. ");
        explanation.append("Genuine disputes with accurate information will be processed appropriately.");
        
        return explanation.toString();
    }
    
    private String getAIEnhancement(String description, DecisionResult decision, int riskScore) {
        try {
            // Call IBM ICA for AI-powered analysis
            IbmIcaService.IcaAnalysisResult icaResult = ibmIcaService.analyzeDispute(
                description,
                decision.getRefundAmount() != null ? decision.getRefundAmount() : 0.0,
                "UNKNOWN", // Transaction location not available in this context
                "UNKNOWN", // User location not available in this context
                riskScore,
                decision.getDecision()
            );
            
            // Build comprehensive AI insight
            StringBuilder aiInsight = new StringBuilder();
            aiInsight.append("\n\n🤖 IBM ICA AI Analysis:\n");
            aiInsight.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // Add the summary first (most important)
            if (icaResult.getSummary() != null && !icaResult.getSummary().isEmpty()) {
                aiInsight.append("\n").append(icaResult.getSummary()).append("\n\n");
            }
            
            aiInsight.append("Model: ").append(icaResult.getModel()).append("\n");
            aiInsight.append("Fraud Assessment: ").append(icaResult.getFraudAssessment()).append("\n");
            aiInsight.append("Confidence Level: ").append(icaResult.getConfidenceLevel()).append("\n");
            aiInsight.append("Recommended Decision: ").append(icaResult.getRecommendedDecision()).append("\n\n");
            
            if (icaResult.getKeyReasons() != null && !icaResult.getKeyReasons().equals("Not available")) {
                aiInsight.append("Key Reasons:\n").append(icaResult.getKeyReasons()).append("\n\n");
            }
            
            if (icaResult.getRedFlags() != null && !icaResult.getRedFlags().equals("Not available")) {
                aiInsight.append("Red Flags:\n").append(icaResult.getRedFlags()).append("\n\n");
            }
            
            if (icaResult.getRecommendations() != null && !icaResult.getRecommendations().equals("Not available")) {
                aiInsight.append("Recommendations:\n").append(icaResult.getRecommendations()).append("\n");
            }
            
            aiInsight.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            return aiInsight.toString();
            
        } catch (Exception e) {
            // If IBM ICA fails, return fallback message
            System.err.println("IBM ICA AI enhancement failed: " + e.getMessage());
            return "\n\n⚠️ AI analysis unavailable - using rule-based decision only.";
        }
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
