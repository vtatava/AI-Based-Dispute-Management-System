package com.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for integrating with Ollama LLM (Llama3.2 or Mistral)
 * Provides AI-powered dispute analysis and decision-making
 */
@Service
public class OllamaService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3.2}")
    private String ollamaModel;

    @Value("${ollama.timeout:60}")
    private int timeoutSeconds;

    public OllamaService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)) // 16MB buffer
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Analyze dispute using Ollama LLM and provide AI decision
     */
    public OllamaAnalysisResult analyzeDispute(
            String description,
            double amount,
            String transactionLocation,
            String userCurrentLocation,
            int currentRiskScore,
            String currentDecision
    ) {
        try {
            // Build comprehensive prompt for LLM
            String prompt = buildDisputeAnalysisPrompt(
                    description, amount, transactionLocation, 
                    userCurrentLocation, currentRiskScore, currentDecision
            );

            // Call Ollama API
            String response = callOllamaAPI(prompt);

            // Parse and structure the response
            return parseOllamaResponse(response, currentRiskScore, currentDecision);

        } catch (Exception e) {
            // Fallback to rule-based decision if Ollama fails
            return createFallbackResponse(currentRiskScore, currentDecision, e.getMessage());
        }
    }

    /**
     * Build detailed prompt for LLM analysis
     */
    private String buildDisputeAnalysisPrompt(
            String description, double amount, String transactionLocation,
            String userCurrentLocation, int riskScore, String decision
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert fraud detection AI analyst for a financial institution. ");
        prompt.append("Analyze the following transaction dispute and provide a detailed assessment.\n\n");
        
        prompt.append("DISPUTE DETAILS:\n");
        prompt.append("- Description: ").append(description).append("\n");
        prompt.append("- Transaction Amount: $").append(amount).append("\n");
        prompt.append("- Transaction Location: ").append(transactionLocation).append("\n");
        prompt.append("- User Current Location: ").append(userCurrentLocation).append("\n");
        prompt.append("- Current Risk Score: ").append(riskScore).append("/100\n");
        prompt.append("- Current System Decision: ").append(decision).append("\n\n");
        
        prompt.append("ANALYSIS REQUIRED:\n");
        prompt.append("1. FRAUD ASSESSMENT: Is this likely fraud? (YES/NO/UNCERTAIN)\n");
        prompt.append("2. CONFIDENCE LEVEL: How confident are you? (HIGH/MEDIUM/LOW)\n");
        prompt.append("3. FINAL DECISION: Should we AUTO_REFUND or require HUMAN_REVIEW?\n");
        prompt.append("4. KEY REASONS: List 3-5 specific reasons for your decision\n");
        prompt.append("5. RED FLAGS: Identify any suspicious patterns or concerns\n");
        prompt.append("6. RECOMMENDATIONS: Suggest next steps or actions\n\n");
        
        prompt.append("DECISION CRITERIA:\n");
        prompt.append("- AUTO_REFUND: Clear fraud indicators, high confidence, customer protection priority\n");
        prompt.append("- HUMAN_REVIEW: Ambiguous cases, conflicting signals, requires investigation\n\n");
        
        prompt.append("Provide your analysis in a structured format with clear sections for each point above. ");
        prompt.append("Be specific, factual, and focus on fraud detection patterns.");

        return prompt.toString();
    }

    /**
     * Call Ollama API with the prompt
     */
    private String callOllamaAPI(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", ollamaModel);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            String response = webClient.post()
                    .uri(ollamaBaseUrl + "/api/generate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            // Parse JSON response to extract the actual text
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("response").asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Ollama API: " + e.getMessage(), e);
        }
    }

    /**
     * Parse Ollama response and structure it
     */
    private OllamaAnalysisResult parseOllamaResponse(String response, int currentRiskScore, String currentDecision) {
        OllamaAnalysisResult result = new OllamaAnalysisResult();
        result.setRawResponse(response);
        result.setModel(ollamaModel);

        // Extract fraud assessment
        if (response.toUpperCase().contains("FRAUD ASSESSMENT:")) {
            if (response.toUpperCase().contains("YES")) {
                result.setFraudAssessment("YES");
            } else if (response.toUpperCase().contains("NO")) {
                result.setFraudAssessment("NO");
            } else {
                result.setFraudAssessment("UNCERTAIN");
            }
        }

        // Extract confidence level
        if (response.toUpperCase().contains("HIGH")) {
            result.setConfidenceLevel("HIGH");
        } else if (response.toUpperCase().contains("MEDIUM")) {
            result.setConfidenceLevel("MEDIUM");
        } else {
            result.setConfidenceLevel("LOW");
        }

        // Extract final decision
        if (response.toUpperCase().contains("AUTO_REFUND") || response.toUpperCase().contains("AUTO REFUND")) {
            result.setRecommendedDecision("AUTO_REFUND");
        } else {
            result.setRecommendedDecision("HUMAN_REVIEW");
        }

        // Extract key reasons (look for numbered lists or bullet points)
        result.setKeyReasons(extractSection(response, "KEY REASONS:", "RED FLAGS:"));
        
        // Extract red flags
        result.setRedFlags(extractSection(response, "RED FLAGS:", "RECOMMENDATIONS:"));
        
        // Extract recommendations
        result.setRecommendations(extractSection(response, "RECOMMENDATIONS:", null));

        // Generate summary
        result.setSummary(generateSummary(result, currentRiskScore));

        return result;
    }

    /**
     * Extract a section from the response
     */
    private String extractSection(String response, String startMarker, String endMarker) {
        try {
            int startIndex = response.toUpperCase().indexOf(startMarker.toUpperCase());
            if (startIndex == -1) return "Not available";

            startIndex += startMarker.length();
            int endIndex = endMarker != null ? response.toUpperCase().indexOf(endMarker.toUpperCase(), startIndex) : response.length();
            
            if (endIndex == -1) endIndex = response.length();

            String section = response.substring(startIndex, endIndex).trim();
            return section.isEmpty() ? "Not available" : section;
        } catch (Exception e) {
            return "Not available";
        }
    }

    /**
     * Generate a concise summary
     */
    private String generateSummary(OllamaAnalysisResult result, int riskScore) {
        StringBuilder summary = new StringBuilder();
        summary.append("🤖 AI Analysis (").append(result.getModel()).append("): ");
        
        if ("YES".equals(result.getFraudAssessment())) {
            summary.append("Fraud detected with ").append(result.getConfidenceLevel()).append(" confidence. ");
        } else if ("NO".equals(result.getFraudAssessment())) {
            summary.append("No clear fraud indicators. ");
        } else {
            summary.append("Uncertain fraud assessment. ");
        }

        summary.append("Risk Score: ").append(riskScore).append("/100. ");
        summary.append("Recommendation: ").append(result.getRecommendedDecision()).append(".");

        return summary.toString();
    }

    /**
     * Create fallback response if Ollama fails
     */
    private OllamaAnalysisResult createFallbackResponse(int riskScore, String decision, String error) {
        OllamaAnalysisResult result = new OllamaAnalysisResult();
        result.setModel(ollamaModel + " (fallback)");
        result.setFraudAssessment(riskScore >= 60 ? "YES" : "UNCERTAIN");
        result.setConfidenceLevel("MEDIUM");
        result.setRecommendedDecision(decision);
        result.setKeyReasons("Fallback to rule-based analysis due to LLM unavailability: " + error);
        result.setRedFlags("LLM service unavailable");
        result.setRecommendations("Review manually or retry LLM analysis");
        result.setSummary("⚠️ LLM analysis unavailable. Using rule-based decision: " + decision);
        result.setRawResponse("Error: " + error);
        return result;
    }

    /**
     * Result class for Ollama analysis
     */
    public static class OllamaAnalysisResult {
        private String model;
        private String fraudAssessment; // YES, NO, UNCERTAIN
        private String confidenceLevel; // HIGH, MEDIUM, LOW
        private String recommendedDecision; // AUTO_REFUND, HUMAN_REVIEW
        private String keyReasons;
        private String redFlags;
        private String recommendations;
        private String summary;
        private String rawResponse;

        // Getters and Setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public String getFraudAssessment() { return fraudAssessment; }
        public void setFraudAssessment(String fraudAssessment) { this.fraudAssessment = fraudAssessment; }

        public String getConfidenceLevel() { return confidenceLevel; }
        public void setConfidenceLevel(String confidenceLevel) { this.confidenceLevel = confidenceLevel; }

        public String getRecommendedDecision() { return recommendedDecision; }
        public void setRecommendedDecision(String recommendedDecision) { this.recommendedDecision = recommendedDecision; }

        public String getKeyReasons() { return keyReasons; }
        public void setKeyReasons(String keyReasons) { this.keyReasons = keyReasons; }

        public String getRedFlags() { return redFlags; }
        public void setRedFlags(String redFlags) { this.redFlags = redFlags; }

        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public String getRawResponse() { return rawResponse; }
        public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
    }
}

// Made with Bob