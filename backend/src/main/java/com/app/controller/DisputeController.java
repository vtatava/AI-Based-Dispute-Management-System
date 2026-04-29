package com.app.controller;

import com.app.dto.DisputeRequest;
import com.app.dto.DisputeResponse;
import com.app.dto.DisputeResponse.AgentFlow;
import com.app.agent.IntentAgent;
import com.app.agent.IntentAgent.IntentAnalysisResult;
import com.app.agent.ContextAgent;
import com.app.agent.ContextAgent.ContextData;
import com.app.agent.DecisionAgent;
import com.app.agent.DecisionAgent.DecisionResult;
import com.app.entity.DisputeRecord;
import com.app.repository.DisputeRecordRepository;
import com.app.service.AIAnalysisService;
import com.app.service.AIAnalysisService.AIAnalysisResult;
import com.app.service.OllamaService;
import com.app.service.OllamaService.OllamaAnalysisResult;
import com.app.service.IbmIcaService;
import com.app.service.IbmIcaService.IcaAnalysisResult;
import com.app.service.IdValidationService;
import com.app.service.TransactionReceiptService;
import com.app.service.TransactionReceiptService.ReceiptData;
import com.app.service.TransactionReceiptService.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dispute")
@CrossOrigin(origins = "*")
public class DisputeController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private IbmIcaService ibmIcaService;
    
    @Autowired
    private IntentAgent intentAgent;
    
    @Autowired
    private ContextAgent contextAgent;
    
    @Autowired
    private DecisionAgent decisionAgent;
    
    @Autowired
    private DisputeRecordRepository disputeRecordRepository;
    
    @Autowired
    private IdValidationService idValidationService;
    
    @Autowired
    private TransactionReceiptService transactionReceiptService;

    @Value("${ollama.enabled:true}")
    private boolean ollamaEnabled;

    @Value("${ibm.ica.enabled:false}")
    private boolean ibmIcaEnabled;

    @Value("${llm.provider:ollama}")
    private String llmProvider;
    
    @Value("${agentic.mode:true}")
    private boolean agenticMode;

    @PostMapping("/raise")
    public DisputeResponse raiseDispute(@RequestBody DisputeRequest request) {
        // Initialize variables
        int riskScore = 0;
        String intent = "NORMAL";
        Double refundAmount = null;
        String reviewReason = null;
        String decision = "PENDING";
        
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
        
        // LLM ANALYSIS: Get AI-powered decision and reasoning
        OllamaAnalysisResult ollamaAnalysis = null;
        IcaAnalysisResult icaAnalysis = null;
        
        // Choose LLM provider based on configuration
        if (("ibm-ica".equalsIgnoreCase(llmProvider) || "openai-compat".equalsIgnoreCase(llmProvider)) && ibmIcaEnabled) {
            // Use IBM ICA / OpenAI-Compatible API (faster cloud-based API)
            try {
                icaAnalysis = ibmIcaService.analyzeDispute(
                    description,
                    request.getAmount(),
                    request.getTransactionLocation(),
                    request.getUserCurrentLocation(),
                    riskScore,
                    decision != null ? decision : "PENDING"
                );
                
                // Use LLM recommendation if available and confident
                if (icaAnalysis != null && "HIGH".equals(icaAnalysis.getConfidenceLevel())) {
                    // Override decision with LLM recommendation for high confidence cases
                    if ("AUTO_REFUND".equals(icaAnalysis.getRecommendedDecision()) && riskScore >= 60) {
                        decision = "AUTO_REFUND & BLOCK_CARD";
                        refundAmount = request.getAmount();
                    } else if ("HUMAN_REVIEW".equals(icaAnalysis.getRecommendedDecision())) {
                        decision = "HUMAN_REVIEW";
                        refundAmount = null;
                    }
                }
            } catch (Exception e) {
                // Log error but continue with rule-based analysis
                System.err.println("IBM ICA analysis failed: " + e.getMessage());
            }
        } else if (ollamaEnabled) {
            // Use Ollama (local LLM)
            try {
                ollamaAnalysis = ollamaService.analyzeDispute(
                    description,
                    request.getAmount(),
                    request.getTransactionLocation(),
                    request.getUserCurrentLocation(),
                    riskScore,
                    decision != null ? decision : "PENDING"
                );
                
                // Use LLM recommendation if available and confident
                if (ollamaAnalysis != null && "HIGH".equals(ollamaAnalysis.getConfidenceLevel())) {
                    // Override decision with LLM recommendation for high confidence cases
                    if ("AUTO_REFUND".equals(ollamaAnalysis.getRecommendedDecision()) && riskScore >= 60) {
                        decision = "AUTO_REFUND & BLOCK_CARD";
                        refundAmount = request.getAmount();
                    } else if ("HUMAN_REVIEW".equals(ollamaAnalysis.getRecommendedDecision())) {
                        decision = "HUMAN_REVIEW";
                        refundAmount = null;
                    }
                }
            } catch (Exception e) {
                // Log error but continue with rule-based analysis
                System.err.println("Ollama analysis failed: " + e.getMessage());
            }
        }
        
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
            
            // Add LLM insights if available (IBM ICA or Ollama)
            if (icaAnalysis != null) {
                reason.append("\n\n🤖 IBM ICA Analysis (").append(icaAnalysis.getModel()).append("):\n");
                reason.append("• Fraud Assessment: ").append(icaAnalysis.getFraudAssessment()).append("\n");
                reason.append("• Confidence: ").append(icaAnalysis.getConfidenceLevel()).append("\n");
                if (icaAnalysis.getKeyReasons() != null && !icaAnalysis.getKeyReasons().equals("Not available")) {
                    reason.append("• Key Reasons: ").append(icaAnalysis.getKeyReasons()).append("\n");
                }
                if (icaAnalysis.getRedFlags() != null && !icaAnalysis.getRedFlags().equals("Not available")) {
                    reason.append("• Red Flags: ").append(icaAnalysis.getRedFlags()).append("\n");
                }
            } else if (ollamaAnalysis != null) {
                reason.append("\n\n🤖 LLM Analysis (").append(ollamaAnalysis.getModel()).append("):\n");
                reason.append("• Fraud Assessment: ").append(ollamaAnalysis.getFraudAssessment()).append("\n");
                reason.append("• Confidence: ").append(ollamaAnalysis.getConfidenceLevel()).append("\n");
                if (ollamaAnalysis.getKeyReasons() != null && !ollamaAnalysis.getKeyReasons().equals("Not available")) {
                    reason.append("• Key Reasons: ").append(ollamaAnalysis.getKeyReasons()).append("\n");
                }
                if (ollamaAnalysis.getRedFlags() != null && !ollamaAnalysis.getRedFlags().equals("Not available")) {
                    reason.append("• Red Flags: ").append(ollamaAnalysis.getRedFlags()).append("\n");
                }
            }
            
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
            
            // Add LLM insights if available (IBM ICA or Ollama)
            if (icaAnalysis != null) {
                reason.append("\n\n🤖 IBM ICA Analysis (").append(icaAnalysis.getModel()).append("):\n");
                reason.append(icaAnalysis.getSummary()).append("\n");
                if (icaAnalysis.getRecommendations() != null && !icaAnalysis.getRecommendations().equals("Not available")) {
                    reason.append("• Recommendations: ").append(icaAnalysis.getRecommendations()).append("\n");
                }
            } else if (ollamaAnalysis != null) {
                reason.append("\n\n🤖 LLM Analysis (").append(ollamaAnalysis.getModel()).append("):\n");
                reason.append(ollamaAnalysis.getSummary()).append("\n");
                if (ollamaAnalysis.getRecommendations() != null && !ollamaAnalysis.getRecommendations().equals("Not available")) {
                    reason.append("• Recommendations: ").append(ollamaAnalysis.getRecommendations()).append("\n");
                }
            }
            
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
            
            // Add LLM insights if available (IBM ICA or Ollama)
            if (icaAnalysis != null) {
                reason.append("\n\n🤖 IBM ICA Analysis (").append(icaAnalysis.getModel()).append("):\n");
                reason.append(icaAnalysis.getSummary());
            } else if (ollamaAnalysis != null) {
                reason.append("\n\n🤖 LLM Analysis (").append(ollamaAnalysis.getModel()).append("):\n");
                reason.append(ollamaAnalysis.getSummary());
            }
            
            reviewReason = reason.toString();
        }
        
        // Save dispute record to database for historical tracking and analytics
        try {
            DisputeRecord record = new DisputeRecord();
            record.setUserId(request.getUserId());
            record.setTransactionType(request.getTransactionType());
            
            // Parse transaction date/time if provided
            if (request.getTransactionDateTime() != null && !request.getTransactionDateTime().isEmpty()) {
                try {
                    record.setTransactionDate(LocalDateTime.parse(request.getTransactionDateTime(),
                        DateTimeFormatter.ISO_DATE_TIME));
                } catch (Exception e) {
                    // If parsing fails, use current time
                    record.setTransactionDate(LocalDateTime.now());
                }
            } else {
                record.setTransactionDate(LocalDateTime.now());
            }
            
            record.setAmount(request.getAmount());
            record.setDescription(request.getDescription());
            record.setIntent(intent);
            record.setRiskScore(riskScore);
            record.setDecision(decision);
            record.setTransactionLocation(request.getTransactionLocation());
            record.setUserCurrentLocation(request.getUserCurrentLocation());
            record.setRefundAmount(refundAmount);
            record.setReviewReason(reviewReason);
            record.setWebsiteUrl(request.getWebsiteUrl());
            
            disputeRecordRepository.save(record);
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Failed to save dispute record: " + e.getMessage());
        }
        
        return new DisputeResponse(intent, riskScore, decision, refundAmount, reviewReason);
    }
    
    /**
     * AGENTIC WORKFLOW: Orchestrates IntentAgent -> ContextAgent -> DecisionAgent
     * This method demonstrates the multi-agent collaboration approach
     */
    @PostMapping("/raise-agentic")
    public DisputeResponse raiseDisputeAgentic(@RequestBody DisputeRequest request) {
        DisputeResponse response = new DisputeResponse();
        AgentFlow agentFlow = new AgentFlow();
        
        try {
            // STEP 1: Intent Agent - Analyze dispute intent
            agentFlow.setIntentAgent("🤖 Intent Agent: Analyzing dispute classification...");
            IntentAnalysisResult intentResult = intentAgent.analyzeIntent(
                request.getDescription(),
                request.getTransactionType() != null ? request.getTransactionType() : "UNKNOWN",
                request.getTransactionLocation(),
                request.getUserCurrentLocation()
            );
            agentFlow.setIntentResult(String.format(
                "✓ Intent: %s | Confidence: %s | Reason: %s",
                intentResult.getIntent(),
                intentResult.getConfidence(),
                intentResult.getReason()
            ));
            
            // STEP 2: Context Agent - Gather contextual data
            agentFlow.setContextAgent("🔍 Context Agent: Gathering transaction context...");
            ContextData contextData = contextAgent.gatherContext(
                request.getUserId() != null ? request.getUserId() : "UNKNOWN",
                request.getTransactionType() != null ? request.getTransactionType() : "UNKNOWN",
                request.getTransactionLocation(),
                request.getUserCurrentLocation(),
                request.getWebsiteUrl() != null ? request.getWebsiteUrl() : "",
                request.getAmount()
            );
            agentFlow.setContextResult(String.format(
                "✓ Risk Score: %d | Location Match: %s | User Verified: %s\n%s",
                contextData.getContextRiskScore(),
                !contextData.isLocationMismatch() ? "Yes" : "No (ALERT)",
                contextData.isUserVerified() ? "Yes" : "No",
                contextData.getContextNotes()
            ));
            
            // STEP 3: Decision Agent - Make final decision
            agentFlow.setDecisionAgent("⚖️ Decision Agent: Making final decision...");
            DecisionResult decisionResult = decisionAgent.makeDecision(
                intentResult,
                contextData,
                request.getDescription(),
                request.getAmount()
            );
            agentFlow.setDecisionResult(String.format(
                "✓ Decision: %s | Action: %s\n%s",
                decisionResult.getDecision(),
                decisionResult.getAction(),
                decisionResult.getExplanation()
            ));
            
            // Build final response
            response.setIntent(intentResult.getIntent());
            response.setRiskScore(decisionResult.getRiskScore());
            response.setDecision(decisionResult.getDecision());
            response.setRefundAmount(decisionResult.getRefundAmount());
            
            // Combine explanation with AI insights
            String fullExplanation = decisionResult.getExplanation();
            if (decisionResult.getAiInsights() != null && !decisionResult.getAiInsights().isEmpty()) {
                fullExplanation += decisionResult.getAiInsights();
            }
            response.setReviewReason(fullExplanation);
            
            response.setAgentFlow(agentFlow);
            response.setUserVerified(contextData.isUserVerified());
            
            // Add verification message
            if (!contextData.isUserVerified()) {
                response.setVerificationMessage("⚠️ User not found in database - additional verification required");
            }
            
            // Save dispute record to database for historical tracking and analytics
            try {
                DisputeRecord record = new DisputeRecord();
                record.setUserId(request.getUserId());
                record.setTransactionType(request.getTransactionType());
                
                // Parse transaction date/time if provided
                if (request.getTransactionDateTime() != null && !request.getTransactionDateTime().isEmpty()) {
                    try {
                        record.setTransactionDate(LocalDateTime.parse(request.getTransactionDateTime(),
                            DateTimeFormatter.ISO_DATE_TIME));
                    } catch (Exception e) {
                        // If parsing fails, use current time
                        record.setTransactionDate(LocalDateTime.now());
                    }
                } else {
                    record.setTransactionDate(LocalDateTime.now());
                }
                
                record.setAmount(request.getAmount());
                record.setDescription(request.getDescription());
                record.setIntent(response.getIntent());
                record.setRiskScore(response.getRiskScore());
                record.setDecision(response.getDecision());
                record.setTransactionLocation(request.getTransactionLocation());
                record.setUserCurrentLocation(request.getUserCurrentLocation());
                record.setRefundAmount(response.getRefundAmount());
                record.setReviewReason(response.getReviewReason());
                record.setWebsiteUrl(request.getWebsiteUrl());
                
                disputeRecordRepository.save(record);
            } catch (Exception saveException) {
                // Log error but don't fail the request
                System.err.println("Failed to save dispute record (agentic): " + saveException.getMessage());
            }
            
        } catch (Exception e) {
            // Fallback to rule-based if agentic workflow fails
            agentFlow.setDecisionResult("⚠️ Agentic workflow error - falling back to rule-based analysis");
            response = raiseDispute(request);
            response.setAgentFlow(agentFlow);
        }
        
        return response;
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
    
    /**
     * Validate user ID document
     */
    @PostMapping("/validate-id")
    public ResponseEntity<Map<String, Object>> validateId(
            @RequestParam("idDocument") MultipartFile idDocument,
            @RequestParam(value = "userId", required = false) String userId) {
        
        Map<String, Object> result = idValidationService.validateIdDocument(idDocument, userId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Validate transaction receipt in real-time (before submission)
     */
    @PostMapping(value = "/validate-receipt", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> validateReceipt(
            @RequestParam("transactionReceipt") MultipartFile transactionReceipt,
            @RequestParam("userId") String userId,
            @RequestParam("amount") double amount,
            @RequestParam("transactionDateTime") String transactionDateTime,
            @RequestParam(value = "transactionLocation", required = false) String transactionLocation,
            @RequestParam("transactionType") String transactionType,
            @RequestParam(value = "websiteUrl", required = false) String websiteUrl,
            @RequestParam(value = "merchantName", required = false) String merchantName) {
        
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("=== Real-Time Receipt Validation Started ===");
        
        // Extract data from receipt
        ReceiptData receiptData = transactionReceiptService.extractReceiptData(transactionReceipt);
        
        if (!receiptData.isSuccess()) {
            response.put("valid", false);
            response.put("message", "❌ Could not read receipt data: " + receiptData.getMessage());
            return ResponseEntity.ok(response);
        }
        
        // Convert transactionDateTime to date string for validation
        String transactionDateForValidation = transactionDateTime;
        if (transactionDateTime != null && transactionDateTime.contains("T")) {
            transactionDateForValidation = transactionDateTime.split("T")[0];
        }
        
        // Validate form data against receipt data
        ValidationResult validation = transactionReceiptService.validateAgainstReceipt(
            userId, transactionDateForValidation, amount, transactionLocation,
            transactionType, websiteUrl, merchantName, receiptData
        );
        
        if (validation.isValid()) {
            response.put("valid", true);
            response.put("message", "✅ Receipt Validated Successfully! All information matches.");
            response.put("details", "Your receipt data matches the form inputs. You can proceed with submission.");
        } else {
            response.put("valid", false);
            response.put("message", "❌ Receipt Validation Failed");
            response.put("mismatches", validation.getMismatches());
            response.put("details", validation.getMessage());
        }
        
        System.out.println("=== Receipt Validation Result: " + (validation.isValid() ? "PASS" : "FAIL") + " ===");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enhanced dispute submission with file uploads and merchant name handling
     */
    @PostMapping(value = "/raise-with-files", consumes = "multipart/form-data")
    public DisputeResponse raiseDisputeWithFiles(
            @RequestParam("amount") double amount,
            @RequestParam("transactionLocation") String transactionLocation,
            @RequestParam("userCurrentLocation") String userCurrentLocation,
            @RequestParam("description") String description,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam("transactionType") String transactionType,
            @RequestParam(value = "websiteUrl", required = false) String websiteUrl,
            @RequestParam(value = "merchantName", required = false) String merchantName,
            @RequestParam(value = "transactionDateTime", required = false) String transactionDateTime,
            @RequestParam(value = "transactionDocuments", required = false) MultipartFile[] transactionDocuments,
            @RequestParam(value = "transactionReceipt", required = false) MultipartFile transactionReceipt,
            @RequestParam(value = "userIdDocument", required = false) MultipartFile userIdDocument,
            @RequestParam(value = "useAgenticMode", required = false, defaultValue = "true") boolean useAgenticMode) {
        
        // Create DisputeRequest from form data
        DisputeRequest request = new DisputeRequest();
        request.setAmount(amount);
        request.setTransactionLocation(transactionLocation);
        request.setUserCurrentLocation(userCurrentLocation);
        request.setDescription(description);
        request.setUserId(userId);
        request.setTransactionType(transactionType);
        request.setWebsiteUrl(websiteUrl);
        request.setMerchantName(merchantName);
        request.setTransactionDateTime(transactionDateTime);
        
        // CRITICAL: Validate user ID if document provided
        // The system will check if the provided UserID matches the UserID associated with the extracted Aadhaar
        if (userIdDocument != null && !userIdDocument.isEmpty()) {
            Map<String, Object> idValidation = idValidationService.validateIdDocument(userIdDocument, userId);
            boolean isValid = (boolean) idValidation.get("valid");
            
            if (!isValid) {
                // Return error response if ID validation fails
                DisputeResponse response = new DisputeResponse();
                response.setIntent("ID_VALIDATION_FAILED");
                response.setRiskScore(100);
                response.setDecision("REJECTED");
                response.setRefundAmount(null);
                
                String errorMessage = idValidation.get("message").toString();
                
                // Add additional context if UserID mismatch detected
                if (idValidation.containsKey("expectedUserId")) {
                    errorMessage += "\n\n🚫 DISPUTE REJECTED: UserID validation failed. " +
                                   "The provided UserID does not match the UserID associated with the uploaded ID document. " +
                                   "Please verify your UserID and try again with the correct credentials.";
                } else {
                    errorMessage += "\n\n🚫 DISPUTE REJECTED: ID validation failed. " +
                                   "Please upload a valid government ID document and provide the correct UserID.";
                }
                
                response.setReviewReason(errorMessage);
                return response;
            }
            
            // ID validation successful - extract verified user information
            String verifiedUserName = idValidation.get("userName") != null ?
                                     idValidation.get("userName").toString() : "Unknown";
            String verifiedUserId = idValidation.get("userIdCode") != null ?
                                   idValidation.get("userIdCode").toString() : userId;
            
            System.out.println("✓ ID Validation Successful: UserID=" + verifiedUserId +
                             ", Name=" + verifiedUserName + ", Aadhaar=" + idValidation.get("userId"));
        } else {
            // If no ID document provided, still require UserID
            if (userId == null || userId.isEmpty() || userId.equals("GUEST_USER")) {
                DisputeResponse response = new DisputeResponse();
                response.setIntent("ID_VALIDATION_REQUIRED");
                response.setRiskScore(100);
                response.setDecision("REJECTED");
                response.setRefundAmount(null);
                response.setReviewReason("🚫 DISPUTE REJECTED: UserID and ID document are required. " +
                                       "Please provide your UserID (e.g., ABC001) and upload a valid government ID document.");
                return response;
            }
        }
        
        // CRITICAL: Validate transaction receipt if provided
        if (transactionReceipt != null && !transactionReceipt.isEmpty()) {
            System.out.println("=== Transaction Receipt Validation Started ===");
            
            // Extract data from receipt
            ReceiptData receiptData = transactionReceiptService.extractReceiptData(transactionReceipt);
            
            if (!receiptData.isSuccess()) {
                // Receipt extraction failed
                DisputeResponse response = new DisputeResponse();
                response.setIntent("RECEIPT_EXTRACTION_FAILED");
                response.setRiskScore(100);
                response.setDecision("REJECTED");
                response.setRefundAmount(null);
                response.setReviewReason("🚫 DISPUTE REJECTED: Could not extract transaction data from receipt. " +
                                       receiptData.getMessage() + "\n\n" +
                                       "Please upload a clear, readable transaction receipt in text format.");
                return response;
            }
            
            // Convert transactionDateTime to date string for validation
            String transactionDateForValidation = transactionDateTime;
            if (transactionDateTime != null && transactionDateTime.contains("T")) {
                // Extract just the date part from ISO format (2026-04-28T00:00:00 -> 2026-04-28)
                transactionDateForValidation = transactionDateTime.split("T")[0];
            }
            
            // Validate form data against receipt data
            ValidationResult validation = transactionReceiptService.validateAgainstReceipt(
                userId, transactionDateForValidation, amount, transactionLocation,
                transactionType, websiteUrl, merchantName, receiptData
            );
            
            if (!validation.isValid()) {
                // Data mismatch - REJECT with detailed error
                DisputeResponse response = new DisputeResponse();
                response.setIntent("RECEIPT_VALIDATION_FAILED");
                response.setRiskScore(100);
                response.setDecision("REJECTED");
                response.setRefundAmount(null);
                
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("🚫 DISPUTE REJECTED: The information you provided does not match the transaction receipt.\n\n");
                errorMsg.append("**Mismatches Detected:**\n");
                for (String mismatch : validation.getMismatches()) {
                    errorMsg.append("• ").append(mismatch).append("\n");
                }
                errorMsg.append("\n**What This Means:**\n");
                errorMsg.append("The details in your dispute form (UserID, Transaction Date, Amount) do not match ");
                errorMsg.append("the information in the uploaded transaction receipt. This suggests either:\n");
                errorMsg.append("1. Incorrect information was provided in the form\n");
                errorMsg.append("2. Wrong receipt was uploaded\n");
                errorMsg.append("3. Potential fraudulent claim\n\n");
                errorMsg.append("**Action Required:**\n");
                errorMsg.append("Please verify your information and upload the correct transaction receipt. ");
                errorMsg.append("If you believe this is an error, contact our support team at disputehelp247@xyz.com");
                
                response.setReviewReason(errorMsg.toString());
                return response;
            }
            
            // Validation successful - continue with full AI analysis instead of short-circuit auto refund
            System.out.println("✓ Receipt validation PASSED - continuing with DB current-location checks and agentic AI analysis");
            request.setDescription(description + " [Transaction Proof Validated]");
        }
        
        // Handle merchant-specific logic
        if ("MERCHANT".equalsIgnoreCase(transactionType) && merchantName != null && !merchantName.isEmpty()) {
            // AI should analyze merchant name and decide auto-refund or human review
            String merchantAnalysis = analyzeMerchantTransaction(merchantName, description, amount);
            
            // Add merchant analysis to description for AI processing
            request.setDescription(description + " [Merchant: " + merchantName + ". " + merchantAnalysis + "]");
        }
        
        // Process the dispute using agentic or regular mode based on parameter
        if (useAgenticMode) {
            return raiseDisputeAgentic(request);
        } else {
            return raiseDispute(request);
        }
    }
    
    /**
     * Analyze merchant transaction to determine if auto-refund or human review is needed
     */
    private String analyzeMerchantTransaction(String merchantName, String description, double amount) {
        // Check if merchant is known/trusted
        // In production, this would check against a database of merchants
        
        String analysis = "";
        String lowerMerchant = merchantName.toLowerCase();
        String lowerDesc = description.toLowerCase();
        
        // Check for known fraud indicators with merchants
        if (lowerDesc.contains("unauthorized") || lowerDesc.contains("not done by me") ||
            lowerDesc.contains("fraud") || lowerDesc.contains("scam")) {
            analysis += "Unauthorized merchant transaction reported. ";
            
            // High-value unauthorized merchant transactions should get auto-refund
            if (amount > 5000) {
                analysis += "High-value unauthorized transaction - recommend AUTO_REFUND. ";
            } else {
                analysis += "Medium-value unauthorized transaction - recommend HUMAN_REVIEW. ";
            }
        } else if (lowerDesc.contains("duplicate") || lowerDesc.contains("charged twice")) {
            analysis += "Duplicate charge reported at merchant. Recommend HUMAN_REVIEW to verify. ";
        } else if (lowerDesc.contains("wrong amount") || lowerDesc.contains("overcharged")) {
            analysis += "Amount dispute at merchant. Recommend HUMAN_REVIEW to verify correct amount. ";
        } else {
            analysis += "General merchant dispute. Recommend HUMAN_REVIEW for investigation. ";
        }
        
        return analysis;
    }
}

// Made with Bob
