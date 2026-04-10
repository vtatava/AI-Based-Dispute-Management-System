package com.app.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;

/**
 * AI-Based Analysis Service for Advanced Dispute Scrutiny
 * Provides intelligent analysis even when locations match
 */
@Service
public class AIAnalysisService {
    
    /**
     * Comprehensive AI analysis for same-location scenarios
     * Even when locations match, we need to scrutinize for fraud patterns
     */
    public AIAnalysisResult analyzeSameLocationDispute(String description, double amount, String location) {
        AIAnalysisResult result = new AIAnalysisResult();
        
        // 0. CRITICAL: Situation-Based Analysis (Seller/Service disputes)
        SituationAnalysis situationAnalysis = analyzeSituation(description);
        result.setSituationType(situationAnalysis.type);
        result.setRequiresHumanReview(situationAnalysis.requiresHumanReview);
        if (situationAnalysis.requiresHumanReview) {
            result.addInsight(situationAnalysis.insight);
            // For situation-based disputes, set medium score to trigger MANUAL_REVIEW
            result.setOverallScore(45); // Medium risk requiring human review
            result.setRiskLevel("MEDIUM");
            result.setRecommendation("MANUAL_REVIEW_REQUIRED - " + situationAnalysis.reason);
            return result; // Early return for situation-based disputes
        }
        
        // 1. Behavioral Pattern Analysis
        BehaviorAnalysis behaviorAnalysis = analyzeBehaviorPatterns(description);
        result.setBehaviorScore(behaviorAnalysis.score);
        result.addInsight(behaviorAnalysis.insight);
        
        // 2. Sentiment & Intent Analysis
        SentimentAnalysis sentimentAnalysis = analyzeSentiment(description);
        result.setSentimentScore(sentimentAnalysis.score);
        result.addInsight(sentimentAnalysis.insight);
        
        // 3. Transaction Pattern Analysis
        TransactionPatternAnalysis patternAnalysis = analyzeTransactionPattern(amount, location, description);
        result.setPatternScore(patternAnalysis.score);
        result.addInsight(patternAnalysis.insight);
        
        // 4. Linguistic Analysis (Deception Detection)
        LinguisticAnalysis linguisticAnalysis = analyzeLinguisticPatterns(description);
        result.setLinguisticScore(linguisticAnalysis.score);
        result.addInsight(linguisticAnalysis.insight);
        
        // 5. Urgency & Pressure Tactics Detection
        UrgencyAnalysis urgencyAnalysis = analyzeUrgencyTactics(description);
        result.setUrgencyScore(urgencyAnalysis.score);
        result.addInsight(urgencyAnalysis.insight);
        
        // Calculate overall AI confidence score
        result.calculateOverallScore();
        
        return result;
    }
    
    /**
     * Analyze the situation/context of the dispute
     * Detects seller/service issues that require human review
     */
    private SituationAnalysis analyzeSituation(String description) {
        SituationAnalysis analysis = new SituationAnalysis();
        String desc = description.toLowerCase();
        
        // CRITICAL: Detect contradictory/conflicting statements
        String[] contradictions = {
            "not fraud but", "no fraud but", "not fraudulent but",
            "legitimate but", "authorized but", "valid but"
        };
        
        for (String contradiction : contradictions) {
            if (desc.contains(contradiction) && (desc.contains("refund") || desc.contains("money back"))) {
                analysis.type = "CONTRADICTORY_INPUT";
                analysis.requiresHumanReview = true;
                analysis.insight = "⚠️ Contradictory Statement Detected";
                analysis.reason = "User claims transaction is legitimate/not fraud but still requests refund. " +
                                "This conflicting statement requires human investigation to understand the actual issue. " +
                                "Cannot process automatic refund for contradictory claims.";
                return analysis;
            }
        }
        
        // Seller/Merchant disputes - require investigation
        String[] sellerIssues = {
            "seller didn't dispatch", "seller not dispatched", "item not dispatched",
            "order not shipped", "not received", "didn't receive", "never received",
            "seller didn't send", "product not delivered", "item not delivered",
            "delivery pending", "not delivered yet", "waiting for delivery",
            "seller issue", "merchant issue", "vendor issue",
            "wrong item", "defective", "damaged", "broken",
            "quality issue", "not as described", "fake product"
        };
        
        for (String issue : sellerIssues) {
            if (desc.contains(issue)) {
                analysis.type = "SELLER_DISPUTE";
                analysis.requiresHumanReview = true;
                analysis.insight = "🛒 Seller/Delivery Dispute Detected";
                analysis.reason = "This is a seller/delivery dispute that requires investigation. " +
                                "Cannot auto-refund without verifying seller's dispatch status, " +
                                "tracking information, and delivery attempts. Human review needed.";
                return analysis;
            }
        }
        
        // Service disputes - require verification
        String[] serviceIssues = {
            "service not provided", "service issue", "poor service",
            "booking cancelled", "reservation cancelled", "appointment cancelled",
            "refund pending", "refund not received", "waiting for refund",
            "double charged", "charged twice", "duplicate charge"
        };
        
        for (String issue : serviceIssues) {
            if (desc.contains(issue)) {
                analysis.type = "SERVICE_DISPUTE";
                analysis.requiresHumanReview = true;
                analysis.insight = "🎫 Service Dispute Detected";
                analysis.reason = "This is a service-related dispute requiring verification. " +
                                "Need to check service provider records, booking status, and " +
                                "cancellation policies before processing refund.";
                return analysis;
            }
        }
        
        // Quality/Product disputes
        String[] qualityIssues = {
            "poor quality", "bad quality", "low quality",
            "not working", "doesn't work", "stopped working",
            "expired", "expiry date", "old product"
        };
        
        for (String issue : qualityIssues) {
            if (desc.contains(issue)) {
                analysis.type = "QUALITY_DISPUTE";
                analysis.requiresHumanReview = true;
                analysis.insight = "⚠️ Quality/Product Issue Detected";
                analysis.reason = "Product quality dispute requires evidence verification. " +
                                "Need to review photos, warranty status, and merchant return policy " +
                                "before approving refund.";
                return analysis;
            }
        }
        
        // Default: Not a situation-based dispute
        analysis.type = "FRAUD_ANALYSIS";
        analysis.requiresHumanReview = false;
        return analysis;
    }
    
    /**
     * Analyze behavioral patterns in dispute description
     */
    private BehaviorAnalysis analyzeBehaviorPatterns(String description) {
        BehaviorAnalysis analysis = new BehaviorAnalysis();
        String desc = description.toLowerCase();
        int suspicionScore = 0;
        List<String> flags = new ArrayList<>();
        
        // Pattern 1: Overly vague or generic descriptions
        String[] vaguePatterns = {"something wrong", "some issue", "problem occurred", 
                                  "error happened", "not working", "issue with"};
        for (String pattern : vaguePatterns) {
            if (desc.contains(pattern)) {
                suspicionScore += 15;
                flags.add("Vague description pattern detected");
                break;
            }
        }
        
        // Pattern 2: Contradictory statements
        if ((desc.contains("authorized") && desc.contains("not authorized")) ||
            (desc.contains("did") && desc.contains("didn't")) ||
            (desc.contains("made") && desc.contains("not made"))) {
            suspicionScore += 25;
            flags.add("Contradictory statements detected");
        }
        
        // Pattern 3: Excessive emotional language (potential manipulation)
        String[] emotionalWords = {"desperate", "urgent", "emergency", "immediately", 
                                   "please help", "need help", "very worried"};
        int emotionalCount = 0;
        for (String word : emotionalWords) {
            if (desc.contains(word)) emotionalCount++;
        }
        if (emotionalCount >= 2) {
            suspicionScore += 20;
            flags.add("Excessive emotional manipulation detected");
        }
        
        // Pattern 4: Lack of specific details
        boolean hasSpecifics = desc.contains("date") || desc.contains("time") || 
                              desc.contains("merchant") || desc.contains("store") ||
                              desc.contains("website") || desc.contains("app");
        if (!hasSpecifics && desc.length() < 40) {
            suspicionScore += 15;
            flags.add("Lack of specific transaction details");
        }
        
        // Pattern 5: Copy-paste or template-like language
        if (isTemplateLike(desc)) {
            suspicionScore += 30;
            flags.add("Template or copy-paste pattern detected");
        }
        
        analysis.score = suspicionScore;
        analysis.insight = flags.isEmpty() ? 
            "✓ Behavioral patterns appear normal" : 
            "⚠️ Behavioral Analysis: " + String.join(", ", flags);
        
        return analysis;
    }
    
    /**
     * Analyze sentiment and intent
     */
    private SentimentAnalysis analyzeSentiment(String description) {
        SentimentAnalysis analysis = new SentimentAnalysis();
        String desc = description.toLowerCase();
        int suspicionScore = 0;
        List<String> insights = new ArrayList<>();
        
        // Aggressive or threatening language
        String[] aggressiveWords = {"sue", "lawyer", "legal action", "report", "complain", 
                                    "unacceptable", "terrible", "worst"};
        int aggressiveCount = 0;
        for (String word : aggressiveWords) {
            if (desc.contains(word)) aggressiveCount++;
        }
        if (aggressiveCount >= 2) {
            suspicionScore += 25;
            insights.add("Aggressive/threatening tone detected");
        }
        
        // Victim mentality overemphasis
        String[] victimWords = {"victim", "suffered", "lost everything", "ruined", "destroyed"};
        for (String word : victimWords) {
            if (desc.contains(word)) {
                suspicionScore += 15;
                insights.add("Excessive victim narrative");
                break;
            }
        }
        
        // Positive indicators (genuine concern)
        String[] genuineWords = {"confused", "concerned", "verify", "check", "understand", "clarify"};
        int genuineCount = 0;
        for (String word : genuineWords) {
            if (desc.contains(word)) genuineCount++;
        }
        if (genuineCount >= 2) {
            suspicionScore -= 10; // Reduce suspicion
            insights.add("Genuine concern indicators present");
        }
        
        analysis.score = Math.max(0, suspicionScore);
        analysis.insight = insights.isEmpty() ? 
            "✓ Sentiment analysis normal" : 
            "🔍 Sentiment: " + String.join(", ", insights);
        
        return analysis;
    }
    
    /**
     * Analyze transaction patterns
     */
    private TransactionPatternAnalysis analyzeTransactionPattern(double amount, String location, String description) {
        TransactionPatternAnalysis analysis = new TransactionPatternAnalysis();
        String desc = description.toLowerCase();
        int suspicionScore = 0;
        List<String> insights = new ArrayList<>();
        
        // Round number amounts (often fraudulent)
        if (amount % 1000 == 0 || amount % 500 == 0) {
            suspicionScore += 15;
            insights.add("Round number amount (common in fraud)");
        }
        
        // High-value transaction with minimal description
        if (amount > 10000 && desc.length() < 30) {
            suspicionScore += 25;
            insights.add("High-value transaction with insufficient details");
        }
        
        // Same location but claims "unauthorized"
        if (desc.contains("unauthorized") || desc.contains("not done by me") || 
            desc.contains("didn't make")) {
            suspicionScore += 30;
            insights.add("Claims unauthorized despite location match - requires deep scrutiny");
        }
        
        // Multiple transactions mentioned
        if (desc.contains("multiple") || desc.contains("several") || desc.contains("many")) {
            suspicionScore += 10;
            insights.add("Multiple transactions claimed");
        }
        
        analysis.score = suspicionScore;
        analysis.insight = insights.isEmpty() ? 
            "✓ Transaction pattern normal" : 
            "📊 Pattern Analysis: " + String.join(", ", insights);
        
        return analysis;
    }
    
    /**
     * Analyze linguistic patterns for deception
     */
    private LinguisticAnalysis analyzeLinguisticPatterns(String description) {
        LinguisticAnalysis analysis = new LinguisticAnalysis();
        String desc = description.toLowerCase();
        int suspicionScore = 0;
        List<String> insights = new ArrayList<>();
        
        // Excessive use of qualifiers (hedging language)
        String[] qualifiers = {"maybe", "possibly", "might", "could be", "perhaps", "i think"};
        int qualifierCount = 0;
        for (String qualifier : qualifiers) {
            if (desc.contains(qualifier)) qualifierCount++;
        }
        if (qualifierCount >= 3) {
            suspicionScore += 20;
            insights.add("Excessive hedging language (uncertainty)");
        }
        
        // Lack of first-person pronouns (distancing)
        boolean hasFirstPerson = desc.contains("i ") || desc.contains("my ") || 
                                desc.contains("me ") || desc.contains("i'm");
        if (!hasFirstPerson && desc.length() > 20) {
            suspicionScore += 15;
            insights.add("Lack of personal pronouns (psychological distancing)");
        }
        
        // Overly detailed irrelevant information (distraction tactic)
        if (desc.length() > 150 && !containsRelevantDetails(desc)) {
            suspicionScore += 20;
            insights.add("Excessive irrelevant details (distraction)");
        }
        
        // Passive voice overuse (avoiding responsibility)
        String[] passivePatterns = {"was done", "was made", "was charged", "was taken"};
        int passiveCount = 0;
        for (String pattern : passivePatterns) {
            if (desc.contains(pattern)) passiveCount++;
        }
        if (passiveCount >= 2) {
            suspicionScore += 15;
            insights.add("Excessive passive voice (responsibility avoidance)");
        }
        
        analysis.score = suspicionScore;
        analysis.insight = insights.isEmpty() ? 
            "✓ Linguistic patterns normal" : 
            "📝 Linguistic Analysis: " + String.join(", ", insights);
        
        return analysis;
    }
    
    /**
     * Analyze urgency and pressure tactics
     */
    private UrgencyAnalysis analyzeUrgencyTactics(String description) {
        UrgencyAnalysis analysis = new UrgencyAnalysis();
        String desc = description.toLowerCase();
        int suspicionScore = 0;
        List<String> insights = new ArrayList<>();
        
        // Time pressure indicators
        String[] urgencyWords = {"urgent", "immediately", "asap", "right now", "quickly", 
                                "emergency", "hurry", "fast", "soon as possible"};
        int urgencyCount = 0;
        for (String word : urgencyWords) {
            if (desc.contains(word)) urgencyCount++;
        }
        if (urgencyCount >= 2) {
            suspicionScore += 25;
            insights.add("Artificial urgency/pressure tactics detected");
        }
        
        // Deadline mentions
        if (desc.contains("deadline") || desc.contains("by tomorrow") || 
            desc.contains("today") || desc.contains("within")) {
            suspicionScore += 15;
            insights.add("Deadline pressure applied");
        }
        
        // Threats of escalation
        if (desc.contains("escalate") || desc.contains("higher authority") || 
            desc.contains("manager") || desc.contains("supervisor")) {
            suspicionScore += 20;
            insights.add("Escalation threats present");
        }
        
        analysis.score = suspicionScore;
        analysis.insight = insights.isEmpty() ? 
            "✓ No pressure tactics detected" : 
            "⏰ Urgency Analysis: " + String.join(", ", insights);
        
        return analysis;
    }
    
    // Helper methods
    
    private boolean isTemplateLike(String description) {
        // Check for repetitive structure or common template phrases
        String[] templatePhrases = {
            "i am writing to inform",
            "this is to inform you",
            "i would like to report",
            "i am hereby requesting",
            "please be informed that"
        };
        for (String phrase : templatePhrases) {
            if (description.contains(phrase)) return true;
        }
        return false;
    }
    
    private boolean containsRelevantDetails(String description) {
        String[] relevantKeywords = {"merchant", "store", "date", "time", "amount", 
                                     "transaction", "card", "account", "receipt"};
        int relevantCount = 0;
        for (String keyword : relevantKeywords) {
            if (description.contains(keyword)) relevantCount++;
        }
        return relevantCount >= 3;
    }
    
    // Inner classes for analysis results
    
    private static class BehaviorAnalysis {
        int score;
        String insight;
    }
    
    private static class SentimentAnalysis {
        int score;
        String insight;
    }
    
    private static class TransactionPatternAnalysis {
        int score;
        String insight;
    }
    
    private static class LinguisticAnalysis {
        int score;
        String insight;
    }
    
    private static class UrgencyAnalysis {
        int score;
        String insight;
    }
    
    private static class SituationAnalysis {
        String type;
        boolean requiresHumanReview;
        String insight;
        String reason;
    }
    
    /**
     * Comprehensive AI Analysis Result
     */
    public static class AIAnalysisResult {
        private int behaviorScore = 0;
        private int sentimentScore = 0;
        private int patternScore = 0;
        private int linguisticScore = 0;
        private int urgencyScore = 0;
        private int overallScore = 0;
        private List<String> insights = new ArrayList<>();
        private String riskLevel;
        private String recommendation;
        private String situationType;
        private boolean requiresHumanReview = false;
        
        public void calculateOverallScore() {
            // Weighted scoring
            overallScore = (int) (
                behaviorScore * 0.25 +
                sentimentScore * 0.20 +
                patternScore * 0.30 +
                linguisticScore * 0.15 +
                urgencyScore * 0.10
            );
            
            // Determine risk level
            if (overallScore >= 60) {
                riskLevel = "HIGH";
                recommendation = "HUMAN_REVIEW_REQUIRED - Multiple AI red flags detected";
            } else if (overallScore >= 35) {
                riskLevel = "MEDIUM";
                recommendation = "ENHANCED_VERIFICATION - Some suspicious patterns found";
            } else {
                riskLevel = "LOW";
                recommendation = "STANDARD_PROCESSING - AI analysis shows normal patterns";
            }
        }
        
        public void addInsight(String insight) {
            if (insight != null && !insight.isEmpty()) {
                insights.add(insight);
            }
        }
        
        // Getters and setters
        public int getBehaviorScore() { return behaviorScore; }
        public void setBehaviorScore(int score) { this.behaviorScore = score; }
        
        public int getSentimentScore() { return sentimentScore; }
        public void setSentimentScore(int score) { this.sentimentScore = score; }
        
        public int getPatternScore() { return patternScore; }
        public void setPatternScore(int score) { this.patternScore = score; }
        
        public int getLinguisticScore() { return linguisticScore; }
        public void setLinguisticScore(int score) { this.linguisticScore = score; }
        
        public int getUrgencyScore() { return urgencyScore; }
        public void setUrgencyScore(int score) { this.urgencyScore = score; }
        
        public int getOverallScore() { return overallScore; }
        public void setOverallScore(int score) { this.overallScore = score; }
        
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String level) { this.riskLevel = level; }
        
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String rec) { this.recommendation = rec; }
        
        public List<String> getInsights() { return insights; }
        
        public String getSituationType() { return situationType; }
        public void setSituationType(String type) { this.situationType = type; }
        
        public boolean isRequiresHumanReview() { return requiresHumanReview; }
        public void setRequiresHumanReview(boolean requires) { this.requiresHumanReview = requires; }
    }
}

// Made with Bob