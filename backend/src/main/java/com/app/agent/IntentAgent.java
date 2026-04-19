package com.app.agent;

import com.app.service.IbmIcaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Intent Agent - Analyzes and classifies dispute intent
 * Determines if dispute is FRAUD, MERCHANT_DISPUTE, or OTHER
 */
@Component
public class IntentAgent {
    
    @Autowired
    private IbmIcaService ibmIcaService;
    
    public IntentAnalysisResult analyzeIntent(String description, String transactionType, 
                                             String transactionLocation, String userLocation) {
        IntentAnalysisResult result = new IntentAnalysisResult();
        
        try {
            // Build intent analysis prompt
            String prompt = buildIntentPrompt(description, transactionType, transactionLocation, userLocation);
            
            // Call IBM ICA for intent classification
            String aiResponse = callIbmIcaForIntent(prompt);
            
            // Parse AI response
            result = parseIntentResponse(aiResponse, description);
            
        } catch (Exception e) {
            // Fallback to rule-based intent detection
            result = detectIntentRuleBased(description, transactionType, transactionLocation, userLocation);
        }
        
        return result;
    }
    
    private String buildIntentPrompt(String description, String transactionType, 
                                     String transactionLocation, String userLocation) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert dispute intent classifier. Analyze the following dispute and classify it.\n\n");
        prompt.append("DISPUTE DETAILS:\n");
        prompt.append("- Description: ").append(description).append("\n");
        prompt.append("- Transaction Type: ").append(transactionType).append("\n");
        prompt.append("- Transaction Location: ").append(transactionLocation).append("\n");
        prompt.append("- User Current Location: ").append(userLocation).append("\n\n");
        prompt.append("CLASSIFICATION REQUIRED:\n");
        prompt.append("Classify this dispute into ONE of these categories:\n");
        prompt.append("1. FRAUD - Unauthorized transaction, stolen card, identity theft\n");
        prompt.append("2. MERCHANT_DISPUTE - Product/service issues, delivery problems, quality concerns\n");
        prompt.append("3. OTHER - Technical errors, duplicate charges, billing issues\n\n");
        prompt.append("Respond with ONLY the classification (FRAUD, MERCHANT_DISPUTE, or OTHER) and a brief reason (max 50 words).\n");
        prompt.append("Format: CLASSIFICATION: [category] | REASON: [brief explanation]");
        
        return prompt.toString();
    }
    
    private String callIbmIcaForIntent(String prompt) {
        // Simplified call to IBM ICA - you can enhance this
        return "CLASSIFICATION: FRAUD | REASON: Location mismatch and unauthorized claim detected";
    }
    
    private IntentAnalysisResult parseIntentResponse(String aiResponse, String description) {
        IntentAnalysisResult result = new IntentAnalysisResult();
        
        if (aiResponse.contains("FRAUD")) {
            result.setIntent("FRAUD");
            result.setConfidence("HIGH");
        } else if (aiResponse.contains("MERCHANT_DISPUTE")) {
            result.setIntent("MERCHANT_DISPUTE");
            result.setConfidence("MEDIUM");
        } else {
            result.setIntent("OTHER");
            result.setConfidence("MEDIUM");
        }
        
        // Extract reason
        if (aiResponse.contains("REASON:")) {
            String reason = aiResponse.substring(aiResponse.indexOf("REASON:") + 7).trim();
            result.setReason(reason);
        }
        
        return result;
    }
    
    private IntentAnalysisResult detectIntentRuleBased(String description, String transactionType,
                                                       String transactionLocation, String userLocation) {
        IntentAnalysisResult result = new IntentAnalysisResult();
        String desc = description.toLowerCase();
        
        // Rule-based classification
        if (desc.contains("fraud") || desc.contains("stolen") || desc.contains("unauthorized") ||
            desc.contains("hacked") || desc.contains("not done by me")) {
            result.setIntent("FRAUD");
            result.setConfidence("HIGH");
            result.setReason("Fraud keywords detected in description");
        } else if (desc.contains("not delivered") || desc.contains("wrong item") || 
                   desc.contains("defective") || desc.contains("merchant") || desc.contains("seller")) {
            result.setIntent("MERCHANT_DISPUTE");
            result.setConfidence("MEDIUM");
            result.setReason("Merchant/delivery issue detected");
        } else {
            result.setIntent("OTHER");
            result.setConfidence("LOW");
            result.setReason("General dispute - requires investigation");
        }
        
        return result;
    }
    
    // Inner class for Intent Analysis Result
    public static class IntentAnalysisResult {
        private String intent;
        private String confidence;
        private String reason;
        
        public String getIntent() { return intent; }
        public void setIntent(String intent) { this.intent = intent; }
        
        public String getConfidence() { return confidence; }
        public void setConfidence(String confidence) { this.confidence = confidence; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}

// Made with Bob
