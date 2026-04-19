package com.app.service;

import com.app.entity.UserData;
import com.app.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI-Powered ID Extraction and Validation Service
 * Uses intelligent pattern matching and fuzzy comparison to extract and validate Government IDs
 */
@Service
public class AiIdExtractionService {
    
    @Autowired
    private UserDataRepository userDataRepository;
    
    /**
     * Extract and validate Government ID using AI-powered analysis
     * Handles various OCR formats and intelligently matches with database
     */
    public IdExtractionResult extractAndValidateId(String ocrText) {
        IdExtractionResult result = new IdExtractionResult();
        
        if (ocrText == null || ocrText.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("No OCR text provided");
            return result;
        }
        
        System.out.println("=== AI ID Extraction Started ===");
        System.out.println("OCR Input: " + ocrText);
        
        // Step 1: Extract all possible ID patterns
        List<String> extractedIds = extractAllIdPatterns(ocrText);
        System.out.println("Extracted ID Patterns: " + extractedIds);
        
        // Step 2: Extract name from OCR text
        String extractedName = extractName(ocrText);
        System.out.println("Extracted Name: " + extractedName);
        
        // Step 3: Extract DOB if present
        String extractedDob = extractDob(ocrText);
        System.out.println("Extracted DOB: " + extractedDob);
        
        // Step 4: Try to match with database using AI-powered fuzzy matching
        for (String candidateId : extractedIds) {
            System.out.println("Trying to match ID: " + candidateId);
            
            // Try exact match first
            Optional<UserData> exactMatch = userDataRepository.findByGovtId(candidateId);
            if (exactMatch.isPresent()) {
                return buildSuccessResult(exactMatch.get(), candidateId, extractedName, extractedDob, "EXACT_MATCH");
            }
            
            // Try fuzzy match with all database records
            List<UserData> allUsers = userDataRepository.findAll();
            for (UserData user : allUsers) {
                if (fuzzyIdMatch(candidateId, user.getGovtId())) {
                    System.out.println("Fuzzy match found: " + user.getGovtId());
                    
                    // Verify with name if available
                    if (extractedName != null && !extractedName.isEmpty()) {
                        if (fuzzyNameMatch(extractedName, user.getUserName())) {
                            return buildSuccessResult(user, candidateId, extractedName, extractedDob, "FUZZY_MATCH_WITH_NAME");
                        }
                    } else {
                        // No name to verify, accept fuzzy ID match
                        return buildSuccessResult(user, candidateId, extractedName, extractedDob, "FUZZY_MATCH");
                    }
                }
            }
        }
        
        // Step 5: If no match found, try AI-powered semantic matching
        if (extractedName != null && !extractedName.isEmpty()) {
            List<UserData> allUsers = userDataRepository.findAll();
            for (UserData user : allUsers) {
                if (fuzzyNameMatch(extractedName, user.getUserName())) {
                    System.out.println("Name match found, suggesting ID: " + user.getGovtId());
                    result.setSuccess(true);
                    result.setUserId(user.getGovtId());
                    result.setUserName(user.getUserName());
                    result.setExtractedName(extractedName);
                    result.setMatchType("NAME_BASED_SUGGESTION");
                    result.setConfidenceScore(70);
                    result.setMessage("✓ User identified by name. Please verify ID: " + user.getGovtId());
                    return result;
                }
            }
        }
        
        // No match found
        result.setSuccess(false);
        result.setExtractedIds(extractedIds);
        result.setExtractedName(extractedName);
        result.setMessage("⚠️ Could not match ID with database. Extracted IDs: " + extractedIds);
        return result;
    }
    
    /**
     * Extract all possible ID patterns from OCR text
     * Handles multiple formats: AADHAAR123456789, 123456789, etc.
     */
    private List<String> extractAllIdPatterns(String text) {
        List<String> ids = new ArrayList<>();
        String upperText = text.toUpperCase();
        
        // Pattern 1: AADHAAR followed by 12 digits (with or without spaces)
        Pattern pattern1 = Pattern.compile("AADHAAR[:\\s]*([0-9\\s]{12,20})");
        Matcher matcher1 = pattern1.matcher(upperText);
        while (matcher1.find()) {
            String digits = matcher1.group(1).replaceAll("\\s", "");
            if (digits.length() >= 12) {
                ids.add("AADHAAR" + digits.substring(0, 12));
            }
        }
        
        // Pattern 2: "Govt ID:" or "ID:" followed by AADHAAR
        Pattern pattern2 = Pattern.compile("(?:GOVT\\s*ID|ID)[:\\s]*AADHAAR\\s*([0-9\\s]{12,20})");
        Matcher matcher2 = pattern2.matcher(upperText);
        while (matcher2.find()) {
            String digits = matcher2.group(1).replaceAll("\\s", "");
            if (digits.length() >= 12) {
                ids.add("AADHAAR" + digits.substring(0, 12));
            }
        }
        
        // Pattern 3: Standalone 12-digit numbers
        Pattern pattern3 = Pattern.compile("\\b([0-9]{12})\\b");
        Matcher matcher3 = pattern3.matcher(text);
        while (matcher3.find()) {
            ids.add("AADHAAR" + matcher3.group(1));
        }
        
        // Pattern 4: 12 digits with spaces (e.g., "1234 5678 9012")
        Pattern pattern4 = Pattern.compile("([0-9]{4}\\s+[0-9]{4}\\s+[0-9]{4})");
        Matcher matcher4 = pattern4.matcher(text);
        while (matcher4.find()) {
            String digits = matcher4.group(1).replaceAll("\\s", "");
            ids.add("AADHAAR" + digits);
        }
        
        // Pattern 5: Look for "AADHAAR" text and extract nearby numbers
        if (upperText.contains("AADHAAR")) {
            Pattern pattern5 = Pattern.compile("([0-9]{12})");
            Matcher matcher5 = pattern5.matcher(text);
            while (matcher5.find()) {
                ids.add("AADHAAR" + matcher5.group(1));
            }
        }
        
        return ids;
    }
    
    /**
     * Extract name from OCR text using AI patterns
     */
    private String extractName(String text) {
        // Pattern 1: "Name:" followed by text
        Pattern pattern1 = Pattern.compile("(?:Name|NAME)[:\\s]+([A-Za-z\\s]{3,50})");
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.find()) {
            String name = matcher1.group(1).trim();
            name = cleanName(name);
            if (name.length() >= 3) {
                return name;
            }
        }
        
        // Pattern 2: Look for capitalized words (likely name)
        String[] lines = text.split("\\n");
        for (String line : lines) {
            line = line.trim();
            // Skip lines with "GOVERNMENT", "INDIA", "CARD", etc.
            if (line.matches(".*(?:GOVERNMENT|INDIA|CARD|AADHAAR|DOB|ID).*")) {
                continue;
            }
            // Look for 2-4 capitalized words
            if (line.matches("^[A-Z][a-z]+\\s+[A-Z][a-z]+(\\s+[A-Z][a-z]+)?$")) {
                return line;
            }
        }
        
        return null;
    }
    
    /**
     * Extract Date of Birth from OCR text
     */
    private String extractDob(String text) {
        // Pattern 1: "DOB:" followed by date
        Pattern pattern1 = Pattern.compile("(?:DOB|Date of Birth)[:\\s]+([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{4})");
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.find()) {
            return matcher1.group(1);
        }
        
        // Pattern 2: Standalone date format
        Pattern pattern2 = Pattern.compile("\\b([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{4})\\b");
        Matcher matcher2 = pattern2.matcher(text);
        if (matcher2.find()) {
            return matcher2.group(1);
        }
        
        return null;
    }
    
    /**
     * Fuzzy ID matching - handles OCR errors and variations
     */
    private boolean fuzzyIdMatch(String extractedId, String dbId) {
        if (extractedId == null || dbId == null) {
            return false;
        }
        
        // Normalize both IDs
        String id1 = extractedId.toUpperCase().replaceAll("[^A-Z0-9]", "");
        String id2 = dbId.toUpperCase().replaceAll("[^A-Z0-9]", "");
        
        // Exact match
        if (id1.equals(id2)) {
            return true;
        }
        
        // Extract numeric parts
        String digits1 = id1.replaceAll("[^0-9]", "");
        String digits2 = id2.replaceAll("[^0-9]", "");
        
        // If numeric parts match (at least 10 digits)
        if (digits1.length() >= 10 && digits2.length() >= 10) {
            // Calculate similarity
            int matchCount = 0;
            int minLength = Math.min(digits1.length(), digits2.length());
            for (int i = 0; i < minLength; i++) {
                if (digits1.charAt(i) == digits2.charAt(i)) {
                    matchCount++;
                }
            }
            // If 90% or more digits match, consider it a match
            return (matchCount >= minLength * 0.9);
        }
        
        return false;
    }
    
    /**
     * Fuzzy name matching - handles variations and OCR errors
     */
    private boolean fuzzyNameMatch(String extractedName, String dbName) {
        if (extractedName == null || dbName == null) {
            return false;
        }
        
        String name1 = extractedName.toLowerCase().trim();
        String name2 = dbName.toLowerCase().trim();
        
        // Exact match
        if (name1.equals(name2)) {
            return true;
        }
        
        // Contains match
        if (name1.contains(name2) || name2.contains(name1)) {
            return true;
        }
        
        // Word-by-word matching
        String[] words1 = name1.split("\\s+");
        String[] words2 = name2.split("\\s+");
        
        int matchCount = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2) && word1.length() > 2) {
                    matchCount++;
                } else if (word1.length() > 3 && word2.length() > 3) {
                    // Levenshtein distance for OCR errors
                    if (calculateSimilarity(word1, word2) > 0.8) {
                        matchCount++;
                    }
                }
            }
        }
        
        // If at least 2 words match or 60% of words match
        return matchCount >= 2 || (matchCount >= Math.min(words1.length, words2.length) * 0.6);
    }
    
    /**
     * Calculate string similarity (0.0 to 1.0)
     */
    private double calculateSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return 1.0 - ((double) distance / maxLength);
    }
    
    /**
     * Levenshtein distance algorithm
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1),     // insertion
                    dp[i - 1][j - 1] + cost // substitution
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Clean extracted name
     */
    private String cleanName(String name) {
        // Remove numbers, special characters
        name = name.replaceAll("[^A-Za-z\\s]", "");
        // Remove extra spaces
        name = name.replaceAll("\\s+", " ").trim();
        // Capitalize properly
        return capitalizeWords(name);
    }
    
    /**
     * Capitalize first letter of each word
     */
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Build success result
     */
    private IdExtractionResult buildSuccessResult(UserData user, String extractedId, 
                                                   String extractedName, String extractedDob, 
                                                   String matchType) {
        IdExtractionResult result = new IdExtractionResult();
        result.setSuccess(true);
        result.setUserId(user.getGovtId());
        result.setUserName(user.getUserName());
        result.setExtractedId(extractedId);
        result.setExtractedName(extractedName);
        result.setExtractedDob(extractedDob);
        result.setMatchType(matchType);
        result.setConfidenceScore(matchType.equals("EXACT_MATCH") ? 100 : 85);
        result.setMessage("✓ ID validated successfully! User: " + user.getUserName() + 
                         " (ID: " + user.getGovtId() + ") [" + matchType + "]");
        
        System.out.println("=== Match Found ===");
        System.out.println("Match Type: " + matchType);
        System.out.println("Database ID: " + user.getGovtId());
        System.out.println("Database Name: " + user.getUserName());
        
        return result;
    }
    
    /**
     * Result class for ID extraction
     */
    public static class IdExtractionResult {
        private boolean success;
        private String userId;
        private String userName;
        private String extractedId;
        private String extractedName;
        private String extractedDob;
        private List<String> extractedIds;
        private String matchType;
        private int confidenceScore;
        private String message;
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getExtractedId() { return extractedId; }
        public void setExtractedId(String extractedId) { this.extractedId = extractedId; }
        
        public String getExtractedName() { return extractedName; }
        public void setExtractedName(String extractedName) { this.extractedName = extractedName; }
        
        public String getExtractedDob() { return extractedDob; }
        public void setExtractedDob(String extractedDob) { this.extractedDob = extractedDob; }
        
        public List<String> getExtractedIds() { return extractedIds; }
        public void setExtractedIds(List<String> extractedIds) { this.extractedIds = extractedIds; }
        
        public String getMatchType() { return matchType; }
        public void setMatchType(String matchType) { this.matchType = matchType; }
        
        public int getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(int confidenceScore) { this.confidenceScore = confidenceScore; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

// Made with Bob