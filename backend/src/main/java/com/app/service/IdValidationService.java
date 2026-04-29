package com.app.service;

import com.app.entity.UserData;
import com.app.repository.UserDataRepository;
import com.app.service.AiIdExtractionService.IdExtractionResult;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IdValidationService {
    
    @Autowired
    private UserDataRepository userDataRepository;
    
    @Autowired
    private AiIdExtractionService aiIdExtractionService;
    
    @Value("${tesseract.datapath:}")
    private String tesseractDataPath;
    
    @Value("${ocr.enabled:true}")
    private boolean ocrEnabled;
    
    @Value("${ai.id.extraction.enabled:true}")
    private boolean aiIdExtractionEnabled;
    
    /**
     * Validate user ID document using OCR and AI-powered extraction
     * Now also validates that the provided UserID matches the UserID associated with the extracted Aadhaar
     */
    public Map<String, Object> validateIdDocument(MultipartFile idDocument, String userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if UserID is provided
            if (userId == null || userId.isEmpty() || userId.equals("GUEST_USER")) {
                result.put("valid", false);
                result.put("message", "⚠️ UserID is required. Please provide your UserID (e.g., ABC001).");
                return result;
            }
            
            // Check if file is provided
            if (idDocument == null || idDocument.isEmpty()) {
                result.put("valid", false);
                result.put("message", "No ID document provided. Please upload a valid government ID.");
                return result;
            }
            
            // Check file type
            String contentType = idDocument.getContentType();
            if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                result.put("valid", false);
                result.put("message", "Invalid file type. Please upload an image or PDF file.");
                return result;
            }
            
            // Check file size (max 5MB)
            if (idDocument.getSize() > 5 * 1024 * 1024) {
                result.put("valid", false);
                result.put("message", "File size too large. Maximum size is 5MB.");
                return result;
            }
            
            // Extract text from ID document using OCR
            String extractedText = "";
            if (ocrEnabled) {
                try {
                    extractedText = extractTextFromDocument(idDocument);
                    System.out.println("OCR Extracted Text: " + extractedText);
                } catch (Exception ocrException) {
                    System.err.println("OCR extraction failed: " + ocrException.getMessage());
                    // Continue with fallback validation
                }
            }
            
            // NEW: Use AI-powered ID extraction if enabled
            if (aiIdExtractionEnabled && !extractedText.isEmpty()) {
                System.out.println("=== Using AI-Powered ID Extraction ===");
                IdExtractionResult aiResult = aiIdExtractionService.extractAndValidateId(extractedText);
                
                if (aiResult.isSuccess()) {
                    // CRITICAL: Validate that the provided UserID matches the UserID in database
                    Optional<UserData> userData = userDataRepository.findByGovtId(aiResult.getUserId());
                    
                    if (userData.isPresent()) {
                        UserData user = userData.get();
                        
                        // Check if the provided UserID matches the database UserID
                        if (!user.getUserId().equalsIgnoreCase(userId)) {
                            result.put("valid", false);
                            result.put("message", "⚠️ UserID Mismatch! Provided UserID (" + userId +
                                       ") does NOT match the UserID associated with extracted Aadhaar (" +
                                       aiResult.getUserId() + "). Expected UserID: " + user.getUserId() +
                                       ". Please provide the correct UserID.");
                            result.put("verified", false);
                            result.put("expectedUserId", user.getUserId());
                            result.put("providedUserId", userId);
                            result.put("extractedAadhaar", aiResult.getUserId());
                            return result;
                        }
                        
                        // UserID matches - proceed with validation
                        result.put("valid", true);
                        result.put("message", "✓ ID validated successfully! UserID (" + userId +
                                   ") matches with extracted Aadhaar (" + aiResult.getUserId() +
                                   "). User: " + aiResult.getUserName());
                        result.put("userName", aiResult.getUserName());
                        result.put("userId", aiResult.getUserId());
                        result.put("userIdCode", user.getUserId());
                        result.put("verified", true);
                        result.put("matchType", aiResult.getMatchType());
                        result.put("confidenceScore", aiResult.getConfidenceScore());
                        result.put("extractedText", extractedText.substring(0, Math.min(100, extractedText.length())) + "...");
                        result.put("aiPowered", true);
                        return result;
                    }
                } else {
                    System.out.println("AI extraction did not find a match, trying legacy method...");
                }
            }
            
            // LEGACY: Parse extracted text to find ID information (fallback)
            Map<String, String> extractedInfo = parseIdInformation(extractedText);
            String extractedName = extractedInfo.get("name");
            String extractedId = extractedInfo.get("id");
            
            // Try to validate using extracted information first
            if (extractedId != null && !extractedId.isEmpty()) {
                Optional<UserData> userData = userDataRepository.findByGovtId(extractedId);
                
                if (userData.isPresent()) {
                    UserData user = userData.get();
                    
                    // CRITICAL: Check if the provided UserID matches the database UserID
                    if (!user.getUserId().equalsIgnoreCase(userId)) {
                        result.put("valid", false);
                        result.put("message", "⚠️ UserID Mismatch! Provided UserID (" + userId +
                                   ") does NOT match the UserID associated with extracted Aadhaar (" +
                                   extractedId + "). Expected UserID: " + user.getUserId() +
                                   ". Please provide the correct UserID.");
                        result.put("verified", false);
                        result.put("expectedUserId", user.getUserId());
                        result.put("providedUserId", userId);
                        result.put("extractedAadhaar", extractedId);
                        return result;
                    }
                    
                    // Verify name match if extracted
                    boolean nameMatch = true;
                    if (extractedName != null && !extractedName.isEmpty()) {
                        nameMatch = fuzzyNameMatch(user.getUserName(), extractedName);
                    }
                    
                    if (nameMatch) {
                        result.put("valid", true);
                        result.put("message", "✓ ID validated successfully! UserID (" + userId +
                                   ") matches with extracted Aadhaar. User: " + user.getUserName() +
                                   " (ID: " + user.getGovtId() + ")");
                        result.put("userName", user.getUserName());
                        result.put("userId", user.getGovtId());
                        result.put("userIdCode", user.getUserId());
                        result.put("verified", true);
                        result.put("extractedText", extractedText.substring(0, Math.min(100, extractedText.length())) + "...");
                        return result;
                    } else {
                        result.put("valid", false);
                        result.put("message", "⚠️ ID number found but name mismatch. Expected: " + user.getUserName() + ", Found: " + extractedName);
                        result.put("verified", false);
                        return result;
                    }
                }
            }
            
            // Fallback: Try with userId parameter if OCR failed (userId here is the UserID code like ABC001)
            if (userId != null && !userId.isEmpty() && !userId.equals("GUEST_USER")) {
                // First try to find by UserID code (ABC001, ABC002, etc.)
                Optional<UserData> userDataByUserId = userDataRepository.findByUserId(userId);
                
                if (userDataByUserId.isPresent()) {
                    UserData user = userDataByUserId.get();
                    result.put("valid", true);
                    result.put("message", "✓ ID validated successfully! UserID: " + user.getUserId() +
                               ", User: " + user.getUserName() + " (Manual verification - OCR failed)");
                    result.put("userName", user.getUserName());
                    result.put("userId", user.getGovtId());
                    result.put("userIdCode", user.getUserId());
                    result.put("verified", true);
                    return result;
                }
                
                // Also try by Aadhaar (backward compatibility)
                Optional<UserData> userDataByAadhaar = userDataRepository.findByGovtId(userId);
                if (userDataByAadhaar.isPresent()) {
                    UserData user = userDataByAadhaar.get();
                    result.put("valid", true);
                    result.put("message", "✓ ID validated successfully! User: " + user.getUserName() + " (Manual verification)");
                    result.put("userName", user.getUserName());
                    result.put("userId", user.getGovtId());
                    result.put("userIdCode", user.getUserId());
                    result.put("verified", true);
                    return result;
                }
            }
            
            // If all validation attempts failed
            result.put("valid", false);
            result.put("message", "⚠️ Could not validate ID from image. Please provide your User ID manually or upload a clearer image.");
            result.put("verified", false);
            result.put("extractedText", extractedText.isEmpty() ? "No text extracted" : extractedText.substring(0, Math.min(100, extractedText.length())) + "...");
            
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "Error validating ID: " + e.getMessage());
            result.put("verified", false);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Extract text from ID document using Tesseract OCR
     */
    private String extractTextFromDocument(MultipartFile file) throws IOException, TesseractException {
        if (!ocrEnabled) {
            return "";
        }
        
        try {
            // Create Tesseract instance
            Tesseract tesseract = new Tesseract();
            
            // Set tessdata path if configured
            if (tesseractDataPath != null && !tesseractDataPath.isEmpty()) {
                tesseract.setDatapath(tesseractDataPath);
            }
            
            // Set language (English by default, can be configured for multiple languages)
            tesseract.setLanguage("eng");
            
            // Convert MultipartFile to BufferedImage
            BufferedImage image = ImageIO.read(file.getInputStream());
            
            if (image == null) {
                throw new IOException("Failed to read image from file");
            }
            
            // Perform OCR
            String extractedText = tesseract.doOCR(image);
            
            return extractedText != null ? extractedText.trim() : "";
            
        } catch (TesseractException e) {
            System.err.println("Tesseract OCR failed: " + e.getMessage());
            throw e;
        } catch (IOException e) {
            System.err.println("Failed to read image file: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Parse ID information from extracted text
     * Looks for patterns like:
     * - Name: Usually at the top or after "Name:" label
     * - AADHAAR: Pattern like AADHAAR followed by 12 digits
     * - ID Number: Various patterns
     */
    private Map<String, String> parseIdInformation(String extractedText) {
        Map<String, String> info = new HashMap<>();
        
        if (extractedText == null || extractedText.isEmpty()) {
            return info;
        }
        
        String text = extractedText.toUpperCase();
        
        // Extract AADHAAR number - Multiple patterns for better OCR handling
        
        // Pattern 1: "AADHAAR" followed by digits (with optional spaces/colons)
        Pattern aadhaarPattern1 = Pattern.compile("AADHAAR[:\\s]*([0-9\\s]{12,20})");
        Matcher aadhaarMatcher1 = aadhaarPattern1.matcher(text);
        if (aadhaarMatcher1.find()) {
            String aadhaarNumber = aadhaarMatcher1.group(1).replaceAll("\\s", "");
            if (aadhaarNumber.length() >= 12) {
                info.put("id", "AADHAAR" + aadhaarNumber.substring(0, 12));
            }
        }
        
        // Pattern 2: "GOVT ID:" or "ID:" followed by AADHAAR and digits
        if (!info.containsKey("id")) {
            Pattern aadhaarPattern2 = Pattern.compile("(?:GOVT\\s*ID|ID)[:\\s]*AADHAAR\\s*([0-9\\s]{12,20})");
            Matcher aadhaarMatcher2 = aadhaarPattern2.matcher(text);
            if (aadhaarMatcher2.find()) {
                String aadhaarNumber = aadhaarMatcher2.group(1).replaceAll("\\s", "");
                if (aadhaarNumber.length() >= 12) {
                    info.put("id", "AADHAAR" + aadhaarNumber.substring(0, 12));
                }
            }
        }
        
        // Pattern 3: Look for "AADHAAR" followed immediately by 12 digits (no space)
        if (!info.containsKey("id")) {
            Pattern aadhaarPattern3 = Pattern.compile("AADHAAR([0-9]{12})");
            Matcher aadhaarMatcher3 = aadhaarPattern3.matcher(text);
            if (aadhaarMatcher3.find()) {
                info.put("id", "AADHAAR" + aadhaarMatcher3.group(1));
            }
        }
        
        // Pattern 4: Look for standalone 12-digit number
        if (!info.containsKey("id")) {
            Pattern digitPattern = Pattern.compile("\\b([0-9]{12})\\b");
            Matcher digitMatcher = digitPattern.matcher(text);
            if (digitMatcher.find()) {
                info.put("id", "AADHAAR" + digitMatcher.group(1));
            }
        }
        
        // Extract name - look for common patterns
        // Pattern 1: "Name:" followed by text
        Pattern namePattern1 = Pattern.compile("NAME[:\\s]+([A-Z\\s]{3,50})");
        Matcher nameMatcher1 = namePattern1.matcher(text);
        if (nameMatcher1.find()) {
            String name = nameMatcher1.group(1).trim();
            // Clean up the name (remove extra spaces, numbers, etc.)
            name = name.replaceAll("[^A-Z\\s]", "").replaceAll("\\s+", " ").trim();
            if (name.length() >= 3) {
                info.put("name", capitalizeWords(name));
            }
        }
        
        // Pattern 2: Look for capitalized words at the beginning (likely name)
        if (!info.containsKey("name")) {
            String[] lines = extractedText.split("\\n");
            for (String line : lines) {
                line = line.trim();
                // Look for lines with 2-4 capitalized words (typical name format)
                if (line.matches("^[A-Z][a-z]+\\s+[A-Z][a-z]+(\\s+[A-Z][a-z]+)?$")) {
                    info.put("name", line);
                    break;
                }
            }
        }
        
        return info;
    }
    
    /**
     * Fuzzy name matching - checks if two names are similar
     * Handles variations like:
     * - Different word order
     * - Partial matches
     * - Case differences
     */
    private boolean fuzzyNameMatch(String dbName, String extractedName) {
        if (dbName == null || extractedName == null) {
            return false;
        }
        
        String name1 = dbName.toLowerCase().trim();
        String name2 = extractedName.toLowerCase().trim();
        
        // Exact match
        if (name1.equals(name2)) {
            return true;
        }
        
        // Check if one contains the other
        if (name1.contains(name2) || name2.contains(name1)) {
            return true;
        }
        
        // Split into words and check for common words
        String[] words1 = name1.split("\\s+");
        String[] words2 = name2.split("\\s+");
        
        int matchCount = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2) && word1.length() > 2) {
                    matchCount++;
                }
            }
        }
        
        // If at least 2 words match or 50% of words match, consider it a match
        return matchCount >= 2 || (matchCount >= Math.min(words1.length, words2.length) / 2.0);
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
     * Validate extracted ID information against database
     */
    public boolean validateExtractedInfo(String extractedName, String extractedId, String extractedDob) {
        if (extractedId == null || extractedId.isEmpty()) {
            return false;
        }
        
        Optional<UserData> userData = userDataRepository.findByGovtId(extractedId);
        
        if (userData.isPresent()) {
            UserData user = userData.get();
            
            // Validate name match (fuzzy matching in production)
            boolean nameMatch = extractedName != null && 
                               user.getUserName().toLowerCase().contains(extractedName.toLowerCase());
            
            // Validate DOB match
            boolean dobMatch = extractedDob != null && 
                              user.getDob().toString().equals(extractedDob);
            
            return nameMatch || dobMatch; // At least one should match
        }
        
        return false;
    }
}

// Made with Bob