package com.app.agent;

import com.app.entity.UserData;
import com.app.entity.FraudWebsite;
import com.app.repository.UserDataRepository;
import com.app.repository.FraudWebsiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Context Agent - Gathers transaction and contextual data
 * Enriches dispute with user data, fraud database checks, and contextual information
 */
@Component
public class ContextAgent {
    
    @Autowired
    private UserDataRepository userDataRepository;
    
    @Autowired
    private FraudWebsiteRepository fraudWebsiteRepository;
    
    public ContextData gatherContext(String userId, String transactionType,
                                    String transactionLocation, String userLocation,
                                    String websiteUrl, double amount) {
        ContextData context = new ContextData();
        
        // 1. Gather User Data
        // Try to find by UserID first (ABC001, ABC002, etc.)
        Optional<UserData> userData = userDataRepository.findByUserId(userId);
        
        // If not found by UserID, try by Aadhaar (backward compatibility)
        if (!userData.isPresent()) {
            userData = userDataRepository.findByGovtId(userId);
        }
        
        if (userData.isPresent()) {
            context.setUserVerified(true);
            context.setUserName(userData.get().getUserName());
            context.setTravelHistory(userData.get().getTravelHistory());
            context.setUserDataFound(true);
            context.setDbCurrentLocation(userData.get().getCurrentLocation());
            context.addContextNote("✓ User verified: " + userData.get().getUserName() + " (UserID: " + userData.get().getUserId() + ")");
            
            // Validate user's claimed location against database current location
            validateUserLocation(userData.get(), userLocation, context);
        } else {
            context.setUserVerified(false);
            context.setUserDataFound(false);
            context.addContextNote("⚠️ User not found in database - verification required");
        }
        
        // 2. Check Location Mismatch
        boolean locationMismatch = checkLocationMismatch(transactionLocation, userLocation);
        context.setLocationMismatch(locationMismatch);
        if (locationMismatch) {
            context.addContextNote("🚨 Location Mismatch: Transaction in " + transactionLocation + 
                                 " but user in " + userLocation);
            context.increaseRiskScore(40);
        }
        
        // 3. Check Travel History (if location mismatch)
        if (locationMismatch && userData.isPresent()) {
            String travelHistory = userData.get().getTravelHistory();
            if (travelHistory != null && travelHistory.contains(transactionLocation)) {
                context.addContextNote("✓ User has travel history to " + transactionLocation);
                context.decreaseRiskScore(20); // Reduce risk
            } else {
                context.addContextNote("⚠️ No travel history to " + transactionLocation);
                context.increaseRiskScore(20);
            }
        }
        
        // 4. Check Fraud Website Database (for Online transactions)
        if ("ONLINE".equalsIgnoreCase(transactionType) && websiteUrl != null && !websiteUrl.isEmpty()) {
            Optional<FraudWebsite> fraudSite = fraudWebsiteRepository.findByWebsiteUrlContainingIgnoreCase(websiteUrl);
            if (fraudSite.isPresent()) {
                context.setFraudWebsiteDetected(true);
                context.setFraudWebsiteRiskLevel(fraudSite.get().getRiskLevel());
                context.addContextNote("🚨 FRAUD WEBSITE DETECTED: " + websiteUrl + 
                                     " (Risk: " + fraudSite.get().getRiskLevel() + ")");
                
                // Increase risk based on fraud website risk level
                if ("CRITICAL".equals(fraudSite.get().getRiskLevel())) {
                    context.increaseRiskScore(50);
                } else if ("HIGH".equals(fraudSite.get().getRiskLevel())) {
                    context.increaseRiskScore(35);
                } else {
                    context.increaseRiskScore(20);
                }
            }
        }
        
        // 5. Transaction Amount Analysis
        if (amount > 50000) {
            context.addContextNote("💰 High-value transaction: ₹" + amount);
            context.increaseRiskScore(15);
        } else if (amount > 20000) {
            context.addContextNote("💰 Medium-value transaction: ₹" + amount);
            context.increaseRiskScore(10);
        }
        
        // 6. Transaction Type Specific Context
        switch (transactionType.toUpperCase()) {
            case "ATM":
                context.addContextNote("🏧 ATM Transaction - Location verification critical");
                break;
            case "ONLINE":
                context.addContextNote("🌐 Online Transaction - Website verification performed");
                break;
            case "MERCHANT":
                context.addContextNote("🏪 Merchant Transaction - Merchant verification recommended");
                break;
            default:
                context.addContextNote("📝 Other Transaction Type");
        }
        
        return context;
    }
    
    private boolean checkLocationMismatch(String transactionLocation, String userLocation) {
        if (transactionLocation == null || userLocation == null) {
            return false;
        }
        return !transactionLocation.trim().equalsIgnoreCase(userLocation.trim());
    }
    
    /**
     * Validates user's claimed location against their actual current location in database
     * This detects fraud when user claims to be in one location but database shows different location
     */
    private void validateUserLocation(UserData userData, String userClaimedLocation, ContextData context) {
        String dbCurrentLocation = userData.getCurrentLocation();
        
        if (dbCurrentLocation == null || userClaimedLocation == null) {
            return; // Cannot validate if data is missing
        }
        
        // Normalize locations for comparison
        String normalizedDbLocation = dbCurrentLocation.trim().toUpperCase();
        String normalizedClaimedLocation = userClaimedLocation.trim().toUpperCase();
        
        // Check if claimed location matches database current location
        if (!normalizedDbLocation.equals(normalizedClaimedLocation)) {
            // FRAUD ALERT: User's claimed location doesn't match database records
            context.setLocationFraudDetected(true);
            context.addContextNote("🚨 LOCATION FRAUD DETECTED!");
            context.addContextNote("⚠️ User claims to be in: " + userClaimedLocation);
            context.addContextNote("⚠️ Database shows current location: " + dbCurrentLocation);
            
            // Check if claimed location exists in travel history
            String travelHistory = userData.getTravelHistory();
            boolean hasHistoryToClaimedLocation = false;
            
            if (travelHistory != null && !travelHistory.isEmpty()) {
                // Check if the claimed location appears in travel history
                hasHistoryToClaimedLocation = travelHistory.toUpperCase().contains(normalizedClaimedLocation);
                
                if (hasHistoryToClaimedLocation) {
                    context.addContextNote("⚠️ User has past travel history to " + userClaimedLocation +
                                         ", but database shows they are currently in " + dbCurrentLocation);
                    context.increaseRiskScore(60); // High risk - possible fraud
                } else {
                    context.addContextNote("🚨 CRITICAL: User has NO travel history to " + userClaimedLocation +
                                         " and database shows they are in " + dbCurrentLocation);
                    context.increaseRiskScore(80); // Very high risk - likely fraud
                }
            } else {
                context.addContextNote("🚨 CRITICAL: No travel history available and location mismatch detected");
                context.increaseRiskScore(70);
            }
            
            context.addContextNote("⚠️ WARNING: This appears to be a fraudulent claim. User's stated location does not match their actual current location in our records.");
        } else {
            // Location matches - good sign
            context.addContextNote("✓ User's claimed location matches database records: " + dbCurrentLocation);
        }
    }
    
    // Inner class for Context Data
    public static class ContextData {
        private boolean userVerified = false;
        private boolean userDataFound = false;
        private String userName;
        private String travelHistory;
        private String dbCurrentLocation;
        private boolean locationMismatch = false;
        private boolean locationFraudDetected = false;
        private boolean fraudWebsiteDetected = false;
        private String fraudWebsiteRiskLevel;
        private int contextRiskScore = 0;
        private StringBuilder contextNotes = new StringBuilder();
        
        public void addContextNote(String note) {
            if (contextNotes.length() > 0) {
                contextNotes.append("\n");
            }
            contextNotes.append(note);
        }
        
        public void increaseRiskScore(int points) {
            this.contextRiskScore += points;
        }
        
        public void decreaseRiskScore(int points) {
            this.contextRiskScore = Math.max(0, this.contextRiskScore - points);
        }
        
        // Getters and Setters
        public boolean isUserVerified() { return userVerified; }
        public void setUserVerified(boolean userVerified) { this.userVerified = userVerified; }
        
        public boolean isUserDataFound() { return userDataFound; }
        public void setUserDataFound(boolean userDataFound) { this.userDataFound = userDataFound; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getTravelHistory() { return travelHistory; }
        public void setTravelHistory(String travelHistory) { this.travelHistory = travelHistory; }
        
        public String getDbCurrentLocation() { return dbCurrentLocation; }
        public void setDbCurrentLocation(String dbCurrentLocation) { this.dbCurrentLocation = dbCurrentLocation; }
        
        public boolean isLocationMismatch() { return locationMismatch; }
        public void setLocationMismatch(boolean locationMismatch) { this.locationMismatch = locationMismatch; }
        
        public boolean isLocationFraudDetected() { return locationFraudDetected; }
        public void setLocationFraudDetected(boolean locationFraudDetected) { this.locationFraudDetected = locationFraudDetected; }
        
        public boolean isFraudWebsiteDetected() { return fraudWebsiteDetected; }
        public void setFraudWebsiteDetected(boolean fraudWebsiteDetected) { this.fraudWebsiteDetected = fraudWebsiteDetected; }
        
        public String getFraudWebsiteRiskLevel() { return fraudWebsiteRiskLevel; }
        public void setFraudWebsiteRiskLevel(String fraudWebsiteRiskLevel) { this.fraudWebsiteRiskLevel = fraudWebsiteRiskLevel; }
        
        public int getContextRiskScore() { return contextRiskScore; }
        public void setContextRiskScore(int contextRiskScore) { this.contextRiskScore = contextRiskScore; }
        
        public String getContextNotes() { return contextNotes.toString(); }
    }
}

// Made with Bob
