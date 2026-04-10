package com.app.controller;

import com.app.dto.DisputeRequest;
import com.app.dto.DisputeResponse;
import com.app.service.AIAnalysisService;
import com.app.service.AIAnalysisService.AIAnalysisResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispute")
@CrossOrigin(origins = "*")
public class DisputeController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @PostMapping("/raise")
    public DisputeResponse raiseDispute(@RequestBody DisputeRequest request) {
        // Initialize variables
        int riskScore = 0;
        String intent = "NORMAL";
        Double refundAmount = null;
        String reviewReason = null;
        String decision;
        
        String description = request.getDescription() != null ? request.getDescription().toLowerCase() : "";
        
        // CRITICAL FIRST: Check for contradictory/frivolous statements (highest priority)
        if (hasContradictoryStatement(description)) {
            decision = "MANUAL_REVIEW";
            refundAmount = null;
            
            // Determine specific reason
            String specificReason;
            if (description.contains("for fun") || description.contains("test") || description.contains("trying") ||
                description.contains("checking") || description.contains("experiment") || description.contains("joke")) {
                specificReason = "⚠️ Frivolous/Test Request Detected - The description suggests this is not a genuine dispute. " +
                               "Automatic refunds are only processed for legitimate fraud cases. ";
            } else {
                specificReason = "⚠️ Contradictory Statement Detected - User claims transaction is legitimate/not fraud but still requests refund. " +
                               "This conflicting statement requires human investigation to understand the actual issue. ";
            }
            
            reviewReason = specificReason +
                          "Cannot process automatic refund for such claims. " +
                          "Please contact our Dispute Team - Contact: disputehelp247@xyz.com";
            return new DisputeResponse(intent, Math.min(riskScore, 100), decision, refundAmount, reviewReason);
        }
        
        // CRITICAL: Check if transaction location and user current location match
        boolean locationMismatch = checkLocationMismatch(
            request.getTransactionLocation(),
            request.getUserCurrentLocation()
        );
        
        // NEW: AI-Based Scrutiny for SAME LOCATION scenarios
        AIAnalysisResult aiAnalysis = null;
        if (!locationMismatch) {
            // When locations match, apply deep AI scrutiny
            aiAnalysis = aiAnalysisService.analyzeSameLocationDispute(
                description,
                request.getAmount(),
                request.getTransactionLocation()
            );
            
            // Add AI-detected risk to overall score
            riskScore += aiAnalysis.getOverallScore();
            
            // If AI detects high risk even with matching locations
            if (aiAnalysis.getOverallScore() >= 60) {
                intent = "POTENTIAL_FRAUD";
            }
        }
        
        // If locations don't match, it's a HIGH RISK fraud indicator
        if (locationMismatch) {
            riskScore += 60; // Major risk factor
            intent = "FRAUD";
        }
        
        // AI Analysis: Analyze description for clarity and fraud indicators
        boolean hasInsufficientClarity = checkInsufficientClarity(description);
        boolean hasFraudIndicators = checkFraudIndicators(description);
        boolean hasUnauthorizedKeywords = checkUnauthorizedKeywords(description);
        boolean hasDisputeClarity = checkDisputeClarity(description);
        
        // Calculate risk score based on description analysis
        if (hasFraudIndicators) {
            riskScore += 50;
            intent = "FRAUD";
        }
        
        if (hasUnauthorizedKeywords) {
            riskScore += 40;
        }
        
        if (!hasDisputeClarity) {
            riskScore += 30;
        }
        
        // Check if transaction is international (adds context)
        boolean isInternational = request.getTransactionLocation() != null &&
                                 !request.getTransactionLocation().equalsIgnoreCase("INDIA");
        if (isInternational) {
            riskScore += 20;
        }
        
        // Amount is secondary factor
        boolean isHighAmount = request.getAmount() > 10000;
        if (isHighAmount) {
            riskScore += 10;
        }
        
        // Cap risk score at 100
        riskScore = Math.min(riskScore, 100);
        
        // AI Decision Logic based on comprehensive analysis
        if (hasInsufficientClarity) {
            // Insufficient clarity - Route to human review
            decision = "HUMAN_REVIEW";
            refundAmount = null;
            reviewReason = "Insufficient clarity in dispute description. " +
                          "AI cannot determine appropriate action without more details. " +
                          "This case needs to be routed to Human Review for proper assessment. " +
                          "Please contact our Dispute Team immediately - Contact: disputeteam247@xyz.com";
                          
        } else if (riskScore >= 80 || (aiAnalysis != null && aiAnalysis.getOverallScore() >= 60)) {
            // High risk with clear fraud indicators - Auto refund and block card
            decision = "AUTO_REFUND & BLOCK_CARD";
            refundAmount = request.getAmount();
            
            StringBuilder reason = new StringBuilder();
            reason.append("⚠️ HIGH RISK FRAUD DETECTED (Risk Score: ").append(riskScore).append("). ");
            
            if (locationMismatch) {
                reason.append("CRITICAL: Transaction location (").append(request.getTransactionLocation())
                      .append(") does NOT match user's current location (").append(request.getUserCurrentLocation()).append("). ");
            } else if (aiAnalysis != null && aiAnalysis.getOverallScore() >= 60) {
                reason.append("🤖 System Deep Analysis: Despite location match, system detected HIGH RISK patterns (Score: ")
                      .append(aiAnalysis.getOverallScore()).append("). ");
                reason.append("\n\n📊 Our Insights:\n");
                for (String insight : aiAnalysis.getInsights()) {
                    reason.append("• ").append(insight).append("\n");
                }
            }
            
            reason.append("\nClear fraud indicators detected. Full refund approved automatically. ")
                  .append("Card will be BLOCKED immediately for security.");
            
            reviewReason = reason.toString();
            
        } else if (riskScore >= 40 && riskScore < 80) {
            // Medium risk - Human review needed
            decision = "HUMAN_REVIEW";
            refundAmount = null;
            
            StringBuilder reason = new StringBuilder();
            reason.append("Medium risk (Risk Score: ").append(riskScore).append("). ");
            
            if (locationMismatch) {
                reason.append("⚠️ Location mismatch detected: Transaction (")
                      .append(request.getTransactionLocation()).append(") vs User (")
                      .append(request.getUserCurrentLocation()).append("). ");
            } else if (aiAnalysis != null && aiAnalysis.getOverallScore() > 0) {
                reason.append("🤖 System detected some concerns (Score: ")
                      .append(aiAnalysis.getOverallScore()).append("). ");
                if (!aiAnalysis.getInsights().isEmpty()) {
                    reason.append("\n📊 Our Observations:\n");
                    for (String insight : aiAnalysis.getInsights()) {
                        reason.append("• ").append(insight).append("\n");
                    }
                }
            }
            
            reason.append("\nDispute description requires human judgment. ")
                  .append(buildReviewContext(hasFraudIndicators, hasUnauthorizedKeywords, locationMismatch, isInternational))
                  .append("Please contact our Dispute Team - Contact: disputeteam247@xyz.com");
            
            reviewReason = reason.toString();
            
        } else {
            // Low risk but still needs review (no automatic rejection)
            decision = "HUMAN_REVIEW";
            refundAmount = null;
            
            StringBuilder reason = new StringBuilder();
            reason.append("Low risk score (Risk Score: ").append(riskScore).append("). ");
            
            if (aiAnalysis != null && !aiAnalysis.getInsights().isEmpty()) {
                reason.append("🤖 System Analysis: ").append(aiAnalysis.getRecommendation()).append(". ");
            }
            
            reason.append("While risk appears low, it is recommended to have human verification for fairness. ")
                  .append("This case needs to be routed to Human Review for final decision. ")
                  .append("Please contact our Dispute Team immediately - Contact: disputeteam247@xyz.com");
            
            reviewReason = reason.toString();
        }
        
        return new DisputeResponse(intent, riskScore, decision, refundAmount, reviewReason);
    }
    
    /**
     * Check if description has insufficient clarity
     */
    private boolean checkInsufficientClarity(String description) {
        if (description == null || description.trim().length() < 10) {
            return true;
        }
        
        // Check for vague descriptions
        String[] vagueTerms = {"wrong", "error", "mistake", "issue", "problem"};
        int vagueCount = 0;
        for (String term : vagueTerms) {
            if (description.contains(term)) {
                vagueCount++;
            }
        }
        
        // If only vague terms and short description, it's unclear
        return vagueCount > 0 && description.length() < 30;
    }
    
    /**
     * Check for fraud indicators in description
     */
    private boolean checkFraudIndicators(String description) {
        String[] fraudKeywords = {"fraud", "fraudulent", "scam", "stolen", "hacked", "compromised"};
        for (String keyword : fraudKeywords) {
            if (description.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check for unauthorized transaction keywords
     */
    private boolean checkUnauthorizedKeywords(String description) {
        String[] unauthorizedKeywords = {"not done by me", "didn't make", "unauthorized", 
                                        "not authorized", "never made", "didn't authorize"};
        for (String keyword : unauthorizedKeywords) {
            if (description.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if dispute has clear explanation
     */
    private boolean checkDisputeClarity(String description) {
        if (description == null || description.trim().length() < 15) {
            return false;
        }
        
        // Check for detailed descriptions
        String[] clarityIndicators = {"because", "when", "where", "how", "transaction", 
                                     "charge", "purchase", "payment", "merchant"};
        for (String indicator : clarityIndicators) {
            if (description.contains(indicator)) {
                return true;
            }
        }
        
        // If description is detailed enough (>50 chars), consider it clear
        return description.length() > 50;
    }
    
    /**
     * Check if transaction location and user current location match
     * This is a CRITICAL fraud indicator
     */
    private boolean checkLocationMismatch(String transactionLocation, String userCurrentLocation) {
        if (transactionLocation == null || userCurrentLocation == null) {
            return false; // Can't determine mismatch if data is missing
        }
        
        // Normalize locations for comparison
        String txLoc = transactionLocation.trim().toUpperCase();
        String userLoc = userCurrentLocation.trim().toUpperCase();
        
        // If locations are different, it's a mismatch (potential fraud)
        return !txLoc.equals(userLoc);
    }
    
    /**
     * Check for contradictory statements in description
     */
    private boolean hasContradictoryStatement(String description) {
        // Check for contradictory statements
        String[] contradictions = {
            "not fraud but", "no fraud but", "not fraudulent but",
            "legitimate but", "authorized but", "valid but",
            "not a fraud but", "isn't fraud but", "isnt fraud but"
        };
        
        for (String contradiction : contradictions) {
            if (description.contains(contradiction) &&
                (description.contains("refund") || description.contains("money back") || description.contains("want"))) {
                return true;
            }
        }
        
        // Check for frivolous/test/abuse patterns
        String[] frivolousPatterns = {
            "for fun", "just testing", "test", "trying", "checking",
            "see if it works", "experiment", "joke", "kidding",
            "not serious", "playing around", "messing around"
        };
        
        for (String pattern : frivolousPatterns) {
            if (description.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Build context for human review
     */
    private String buildReviewContext(boolean hasFraudIndicators, boolean hasUnauthorizedKeywords,
                                     boolean locationMismatch, boolean isInternational) {
        StringBuilder context = new StringBuilder("Review factors: ");
        
        if (locationMismatch) {
            context.append("⚠️ LOCATION MISMATCH (High Priority). ");
        }
        if (hasFraudIndicators) {
            context.append("Fraud indicators detected. ");
        }
        if (hasUnauthorizedKeywords) {
            context.append("Unauthorized transaction claimed. ");
        }
        if (isInternational) {
            context.append("International transaction. ");
        }
        
        return context.toString();
    }
}

// Made with Bob
