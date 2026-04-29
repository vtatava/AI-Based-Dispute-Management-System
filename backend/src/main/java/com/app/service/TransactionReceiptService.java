package com.app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transaction Receipt Service - Extracts and validates transaction data from uploaded receipts
 * Supports text files with transaction information
 */
@Service
public class TransactionReceiptService {
    
    /**
     * Extract transaction data from uploaded receipt file
     */
    public ReceiptData extractReceiptData(MultipartFile receiptFile) {
        ReceiptData receiptData = new ReceiptData();
        
        if (receiptFile == null || receiptFile.isEmpty()) {
            receiptData.setSuccess(false);
            receiptData.setMessage("No receipt file provided");
            return receiptData;
        }
        
        try {
            // Read file content
            String content = readFileContent(receiptFile);
            System.out.println("=== Transaction Receipt Extraction Started ===");
            System.out.println("Receipt Content:\n" + content);
            
            // Extract data from receipt
            String userId = extractUserId(content);
            String userName = extractUserName(content);
            String transactionDate = extractTransactionDate(content);
            Double amount = extractAmount(content);
            String location = extractLocation(content);
            String transactionType = extractTransactionType(content);
            String websiteUrl = extractWebsiteUrl(content);
            String merchantName = extractMerchantName(content);
            
            receiptData.setUserId(userId);
            receiptData.setUserName(userName);
            receiptData.setTransactionDate(transactionDate);
            receiptData.setAmount(amount);
            receiptData.setLocation(location);
            receiptData.setTransactionType(transactionType);
            receiptData.setWebsiteUrl(websiteUrl);
            receiptData.setMerchantName(merchantName);
            receiptData.setRawContent(content);
            
            // Check if all required fields are extracted
            if (userId != null && transactionDate != null && amount != null) {
                receiptData.setSuccess(true);
                receiptData.setMessage("✓ Receipt data extracted successfully");
                System.out.println("✓ Extraction successful: UserID=" + userId + 
                                 ", Date=" + transactionDate + ", Amount=" + amount + 
                                 ", Location=" + location);
            } else {
                receiptData.setSuccess(false);
                receiptData.setMessage("⚠️ Could not extract all required fields from receipt");
                System.out.println("⚠️ Incomplete extraction: UserID=" + userId + 
                                 ", Date=" + transactionDate + ", Amount=" + amount);
            }
            
        } catch (Exception e) {
            receiptData.setSuccess(false);
            receiptData.setMessage("Error reading receipt: " + e.getMessage());
            System.err.println("Receipt extraction error: " + e.getMessage());
        }
        
        return receiptData;
    }
    
    /**
     * Validate form data against receipt data
     */
    public ValidationResult validateAgainstReceipt(String formUserId, String formDate,
                                                   Double formAmount, String formLocation,
                                                   String formTransactionType, String formWebsiteUrl,
                                                   String formMerchantName, ReceiptData receiptData) {
        ValidationResult result = new ValidationResult();
        List<String> mismatches = new ArrayList<>();
        
        System.out.println("=== Validating Form Data Against Receipt ===");
        System.out.println("Form - UserID: " + formUserId + ", Date: " + formDate +
                         ", Amount: " + formAmount + ", Location: " + formLocation);
        System.out.println("Receipt - UserID: " + receiptData.getUserId() +
                         ", Date: " + receiptData.getTransactionDate() +
                         ", Amount: " + receiptData.getAmount() +
                         ", Location: " + receiptData.getLocation());
        
        // Validate UserID
        if (receiptData.getUserId() != null) {
            if (!validateUserId(formUserId, receiptData.getUserId())) {
                mismatches.add("UserID mismatch: Form shows '" + formUserId + 
                             "' but receipt shows '" + receiptData.getUserId() + "'");
            }
        }
        
        // Validate Transaction Date
        if (receiptData.getTransactionDate() != null) {
            if (!validateDate(formDate, receiptData.getTransactionDate())) {
                mismatches.add("Transaction Date mismatch: Form shows '" + formDate + 
                             "' but receipt shows '" + receiptData.getTransactionDate() + "'");
            }
        }
        
        // Validate Amount
        if (receiptData.getAmount() != null) {
            if (!validateAmount(formAmount, receiptData.getAmount())) {
                mismatches.add("Transaction Amount mismatch: Form shows '" + formAmount +
                             "' but receipt shows '" + receiptData.getAmount() + "'");
            }
        }
        
        // Validate Transaction Location
        if (receiptData.getLocation() != null && formLocation != null) {
            if (!validateLocation(formLocation, receiptData.getLocation())) {
                mismatches.add("Transaction Location mismatch: Form shows '" + formLocation +
                             "' but receipt shows '" + receiptData.getLocation() + "'");
            }
        }
        
        // Validate Transaction Type
        if (receiptData.getTransactionType() != null && formTransactionType != null) {
            if (!formTransactionType.equalsIgnoreCase(receiptData.getTransactionType())) {
                mismatches.add("Transaction Type mismatch: Form shows '" + formTransactionType +
                             "' but receipt shows '" + receiptData.getTransactionType() + "'");
            }
        }
        
        // Transaction Type-Specific Validation
        if (receiptData.getTransactionType() != null) {
            String receiptType = receiptData.getTransactionType().toUpperCase();
            
            // For ONLINE transactions, validate Website URL
            if ("ONLINE".equals(receiptType)) {
                if (receiptData.getWebsiteUrl() != null && formWebsiteUrl != null) {
                    if (!validateWebsiteUrl(formWebsiteUrl, receiptData.getWebsiteUrl())) {
                        mismatches.add("Website URL mismatch: Form shows '" + formWebsiteUrl +
                                     "' but receipt shows '" + receiptData.getWebsiteUrl() + "'");
                    }
                } else if (receiptData.getWebsiteUrl() != null && formWebsiteUrl == null) {
                    mismatches.add("Website URL missing: Receipt shows '" + receiptData.getWebsiteUrl() +
                                 "' but form does not provide Website URL");
                }
            }
            
            // For MERCHANT transactions, validate Merchant Name
            if ("MERCHANT".equals(receiptType)) {
                if (receiptData.getMerchantName() != null && formMerchantName != null) {
                    if (!validateMerchantName(formMerchantName, receiptData.getMerchantName())) {
                        mismatches.add("Merchant Name mismatch: Form shows '" + formMerchantName +
                                     "' but receipt shows '" + receiptData.getMerchantName() + "'");
                    }
                } else if (receiptData.getMerchantName() != null && formMerchantName == null) {
                    mismatches.add("Merchant Name missing: Receipt shows '" + receiptData.getMerchantName() +
                                 "' but form does not provide Merchant Name");
                }
            }
        }
        
        // Set validation result
        if (mismatches.isEmpty()) {
            result.setValid(true);
            result.setMessage("✓ All information matches the transaction receipt");
            System.out.println("✓ Validation PASSED - All data matches");
        } else {
            result.setValid(false);
            result.setMismatches(mismatches);
            result.setMessage("❌ Information does not match the transaction receipt:\n" + 
                            String.join("\n", mismatches));
            System.out.println("❌ Validation FAILED - Mismatches found: " + mismatches);
        }
        
        return result;
    }
    
    /**
     * Read file content as text
     */
    private String readFileContent(MultipartFile file) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    /**
     * Extract UserID from receipt
     */
    private String extractUserId(String content) {
        // Pattern 1: "UserId:" or "User ID:" followed by value
        Pattern pattern1 = Pattern.compile("(?i)User\\s*Id\\s*[:\\s]+([A-Z0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher1 = pattern1.matcher(content);
        if (matcher1.find()) {
            return matcher1.group(1).trim();
        }
        
        // Pattern 2: Standalone alphanumeric ID (e.g., ABC003)
        Pattern pattern2 = Pattern.compile("\\b([A-Z]{3}[0-9]{3})\\b");
        Matcher matcher2 = pattern2.matcher(content);
        if (matcher2.find()) {
            return matcher2.group(1);
        }
        
        return null;
    }
    
    /**
     * Extract User Name from receipt
     */
    private String extractUserName(String content) {
        // Pattern: "Name:" followed by text
        Pattern pattern = Pattern.compile("(?i)Name\\s*[:\\s]+([A-Za-z\\s]+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            // Clean up - take only the name part (before any other field)
            name = name.split("\\n")[0].trim();
            return name;
        }
        return null;
    }
    
    /**
     * Extract Transaction Date from receipt
     */
    private String extractTransactionDate(String content) {
        // Pattern 1: "Transaction Date:" followed by date
        Pattern pattern1 = Pattern.compile("(?i)Transaction\\s+Date\\s*[:\\s]+([0-9]{1,2}[-/][A-Za-z]{3}[-/][0-9]{4})");
        Matcher matcher1 = pattern1.matcher(content);
        if (matcher1.find()) {
            return matcher1.group(1).trim();
        }
        
        // Pattern 2: Date format like "28-Apr-2026"
        Pattern pattern2 = Pattern.compile("\\b([0-9]{1,2}[-/][A-Za-z]{3}[-/][0-9]{4})\\b");
        Matcher matcher2 = pattern2.matcher(content);
        if (matcher2.find()) {
            return matcher2.group(1);
        }
        
        // Pattern 3: Date format like "28/04/2026" or "28-04-2026"
        Pattern pattern3 = Pattern.compile("\\b([0-9]{1,2}[-/][0-9]{1,2}[-/][0-9]{4})\\b");
        Matcher matcher3 = pattern3.matcher(content);
        if (matcher3.find()) {
            return matcher3.group(1);
        }
        
        return null;
    }
    
    /**
     * Extract Transaction Type from receipt
     */
    private String extractTransactionType(String content) {
        // Pattern: "Transaction Type:" followed by type
        Pattern pattern = Pattern.compile("(?i)Transaction\\s+Type\\s*[:\\s]+([A-Za-z]+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim().toUpperCase();
        }
        return null;
    }
    
    /**
     * Extract Website URL from receipt
     */
    private String extractWebsiteUrl(String content) {
        // Pattern 1: "Website URL:" followed by URL
        Pattern pattern1 = Pattern.compile("(?i)Website\\s+URL\\s*[:\\s]+([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
        Matcher matcher1 = pattern1.matcher(content);
        if (matcher1.find()) {
            return matcher1.group(1).trim().toLowerCase();
        }
        
        // Pattern 2: "URL:" followed by URL
        Pattern pattern2 = Pattern.compile("(?i)URL\\s*[:\\s]+([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
        Matcher matcher2 = pattern2.matcher(content);
        if (matcher2.find()) {
            return matcher2.group(1).trim().toLowerCase();
        }
        
        return null;
    }
    
    /**
     * Extract Merchant Name from receipt
     */
    private String extractMerchantName(String content) {
        // Pattern: "Merchant Name:" or "Merchant:" followed by name
        Pattern pattern = Pattern.compile("(?i)Merchant(?:\\s+Name)?\\s*[:\\s]+([A-Za-z0-9\\s]+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            // Clean up - take only the merchant name part (before any other field)
            name = name.split("\\n")[0].trim();
            return name;
        }
        return null;
    }
    
    /**
     * Extract Amount from receipt
     */
    private Double extractAmount(String content) {
        System.out.println("=== Extracting Amount from Receipt ===");
        
        // Pattern 1: "Amount:" followed by number
        Pattern pattern1 = Pattern.compile("(?i)Amount\\s*[:\\s]+([0-9,]+\\.?[0-9]*)");
        Matcher matcher1 = pattern1.matcher(content);
        if (matcher1.find()) {
            String amountStr = matcher1.group(1).replace(",", "");
            System.out.println("Found amount with Pattern 1: " + amountStr);
            try {
                Double amount = Double.parseDouble(amountStr);
                System.out.println("Extracted Amount: " + amount);
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse amount: " + e.getMessage());
                // Continue to next pattern
            }
        }
        
        // Pattern 2: Currency symbol followed by amount
        Pattern pattern2 = Pattern.compile("[$₹€£]\\s*([0-9,]+\\.?[0-9]*)");
        Matcher matcher2 = pattern2.matcher(content);
        if (matcher2.find()) {
            String amountStr = matcher2.group(1).replace(",", "");
            System.out.println("Found amount with Pattern 2: " + amountStr);
            try {
                Double amount = Double.parseDouble(amountStr);
                System.out.println("Extracted Amount: " + amount);
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse amount: " + e.getMessage());
                // Continue
            }
        }
        
        System.out.println("⚠️ No amount found in receipt");
        return null;
    }
    
    /**
     * Extract Location from receipt
     */
    private String extractLocation(String content) {
        // Pattern: "Location:" followed by text
        Pattern pattern = Pattern.compile("(?i)Location\\s*[:\\s]+([A-Za-z\\s]+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String location = matcher.group(1).trim();
            // Clean up - take only the location part (before any other field)
            location = location.split("\\n")[0].trim();
            return location;
        }
        return null;
    }
    
    /**
     * Validate Website URL match
     */
    private boolean validateWebsiteUrl(String formUrl, String receiptUrl) {
        if (formUrl == null || receiptUrl == null) {
            return false;
        }
        
        // Normalize URLs (remove protocol, www, trailing slashes)
        String url1 = normalizeUrl(formUrl);
        String url2 = normalizeUrl(receiptUrl);
        
        // Exact match
        if (url1.equals(url2)) {
            return true;
        }
        
        // Check if one contains the other
        if (url1.contains(url2) || url2.contains(url1)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Normalize URL for comparison
     */
    private String normalizeUrl(String url) {
        if (url == null) {
            return "";
        }
        
        String normalized = url.toLowerCase().trim();
        
        // Remove protocol
        normalized = normalized.replaceAll("^https?://", "");
        normalized = normalized.replaceAll("^www\\.", "");
        
        // Remove trailing slash
        normalized = normalized.replaceAll("/$", "");
        
        return normalized;
    }
    
    /**
     * Validate Merchant Name match
     */
    private boolean validateMerchantName(String formMerchant, String receiptMerchant) {
        if (formMerchant == null || receiptMerchant == null) {
            return false;
        }
        
        // Normalize merchant names
        String merchant1 = formMerchant.trim().toUpperCase();
        String merchant2 = receiptMerchant.trim().toUpperCase();
        
        // Exact match
        if (merchant1.equals(merchant2)) {
            return true;
        }
        
        // Check if one contains the other
        if (merchant1.contains(merchant2) || merchant2.contains(merchant1)) {
            return true;
        }
        
        // Check word-by-word matching (for variations like "XYZ Store" vs "XYZ")
        String[] words1 = merchant1.split("\\s+");
        String[] words2 = merchant2.split("\\s+");
        
        int matchCount = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2) && word1.length() > 2) {
                    matchCount++;
                }
            }
        }
        
        // If at least one significant word matches
        return matchCount > 0;
    }
    
    /**
     * Validate Location match
     */
    private boolean validateLocation(String formLocation, String receiptLocation) {
        if (formLocation == null || receiptLocation == null) {
            return false;
        }
        
        // Normalize both locations for comparison
        String loc1 = formLocation.trim().toUpperCase();
        String loc2 = receiptLocation.trim().toUpperCase();
        
        // Exact match
        if (loc1.equals(loc2)) {
            return true;
        }
        
        // Check if one contains the other (e.g., "USA" matches "UNITED STATES")
        if (loc1.contains(loc2) || loc2.contains(loc1)) {
            return true;
        }
        
        // Common location aliases
        Map<String, String[]> locationAliases = new HashMap<>();
        locationAliases.put("USA", new String[]{"UNITED STATES", "US", "AMERICA", "UNITED STATES OF AMERICA"});
        locationAliases.put("UK", new String[]{"UNITED KINGDOM", "GREAT BRITAIN", "BRITAIN", "ENGLAND"});
        locationAliases.put("UAE", new String[]{"UNITED ARAB EMIRATES", "DUBAI", "ABU DHABI"});
        
        // Check aliases
        for (Map.Entry<String, String[]> entry : locationAliases.entrySet()) {
            String key = entry.getKey();
            String[] aliases = entry.getValue();
            
            boolean loc1Match = loc1.equals(key) || Arrays.asList(aliases).contains(loc1);
            boolean loc2Match = loc2.equals(key) || Arrays.asList(aliases).contains(loc2);
            
            if (loc1Match && loc2Match) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate UserID match
     */
    private boolean validateUserId(String formUserId, String receiptUserId) {
        if (formUserId == null || receiptUserId == null) {
            return false;
        }
        return formUserId.trim().equalsIgnoreCase(receiptUserId.trim());
    }
    
    /**
     * Validate Date match (flexible date comparison)
     */
    private boolean validateDate(String formDate, String receiptDate) {
        System.out.println("=== Date Validation ===");
        System.out.println("Form Date: " + formDate);
        System.out.println("Receipt Date: " + receiptDate);
        
        if (formDate == null || receiptDate == null) {
            System.out.println("⚠️ One or both dates are null");
            return false;
        }
        
        try {
            // Try to parse both dates and compare
            LocalDate date1 = parseFlexibleDate(formDate);
            LocalDate date2 = parseFlexibleDate(receiptDate);
            
            System.out.println("Parsed Form Date: " + date1);
            System.out.println("Parsed Receipt Date: " + date2);
            
            if (date1 != null && date2 != null) {
                boolean match = date1.equals(date2);
                System.out.println("Date Match: " + (match ? "✓ PASS" : "✗ FAIL"));
                return match;
            }
        } catch (Exception e) {
            System.err.println("Date parsing error: " + e.getMessage());
        }
        
        // Fallback: string comparison
        boolean match = formDate.trim().equalsIgnoreCase(receiptDate.trim());
        System.out.println("Fallback string match: " + (match ? "✓ PASS" : "✗ FAIL"));
        return match;
    }
    
    /**
     * Parse date with multiple formats
     */
    private LocalDate parseFlexibleDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        // Clean up the date string
        dateStr = dateStr.trim();
        
        // Try multiple date formats
        List<DateTimeFormatter> formatters = Arrays.asList(
            DateTimeFormatter.ofPattern("dd-MMM-yyyy"),      // 28-Apr-2026
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),       // 2026-04-28
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),       // 28/04/2026
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),       // 28-04-2026
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),       // 04/28/2026
            DateTimeFormatter.ISO_DATE                       // 2026-04-28
        );
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate parsed = LocalDate.parse(dateStr, formatter);
                System.out.println("Successfully parsed date '" + dateStr + "' using format: " + formatter);
                return parsed;
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        System.out.println("⚠️ Could not parse date: " + dateStr);
        return null;
    }
    
    /**
     * Validate Amount match (with small tolerance for rounding)
     */
    private boolean validateAmount(Double formAmount, Double receiptAmount) {
        if (formAmount == null || receiptAmount == null) {
            System.out.println("⚠️ Amount validation: One or both amounts are null");
            System.out.println("   Form Amount: " + formAmount);
            System.out.println("   Receipt Amount: " + receiptAmount);
            return false;
        }
        
        double difference = Math.abs(formAmount - receiptAmount);
        System.out.println("=== Amount Validation ===");
        System.out.println("Form Amount: " + formAmount);
        System.out.println("Receipt Amount: " + receiptAmount);
        System.out.println("Difference: " + difference);
        
        // Allow 0.01 difference for rounding errors only
        boolean isValid = difference < 0.01;
        System.out.println("Amount Match: " + (isValid ? "✓ PASS" : "✗ FAIL"));
        
        return isValid;
    }
    
    /**
     * Receipt Data class
     */
    public static class ReceiptData {
        private boolean success;
        private String userId;
        private String userName;
        private String transactionDate;
        private Double amount;
        private String location;
        private String transactionType;
        private String websiteUrl;
        private String merchantName;
        private String rawContent;
        private String message;
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getTransactionDate() { return transactionDate; }
        public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public String getLocation() { return location; }
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        
        public String getWebsiteUrl() { return websiteUrl; }
        public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
        
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
        
        public void setLocation(String location) { this.location = location; }
        
        public String getRawContent() { return rawContent; }
        public void setRawContent(String rawContent) { this.rawContent = rawContent; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    /**
     * Validation Result class
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private List<String> mismatches;
        
        public ValidationResult() {
            this.mismatches = new ArrayList<>();
        }
        
        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public List<String> getMismatches() { return mismatches; }
        public void setMismatches(List<String> mismatches) { this.mismatches = mismatches; }
    }
}

// Made with Bob