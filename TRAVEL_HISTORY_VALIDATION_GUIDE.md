# Travel History Validation & Location Fraud Detection

## Overview
This feature enhances the AI Dispute Management System to detect fraudulent claims by validating a user's claimed location against their actual current location stored in the database and their travel history.

## Problem Statement
**Scenario**: A user claims they are in India and submits a dispute, but the database shows their current location is USA. This is a clear indication of a fraudulent claim.

## Solution Implementation

### 1. Database Schema Updates

#### USER_DATA Table Enhancement
Added `current_location` field to track user's actual current location:

```sql
CREATE TABLE IF NOT EXISTS USER_DATA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL UNIQUE,
    user_name VARCHAR(255) NOT NULL,
    govt_id VARCHAR(50) NOT NULL UNIQUE,
    dob DATE NOT NULL,
    travel_history TEXT,
    current_location VARCHAR(100),  -- NEW FIELD
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Sample Data
```sql
-- User ABC002 (Priya Sharma) is currently in USA
('ABC002', 'Priya Sharma', 'AADHAAR987654321', '1990-08-22', 
 'India, USA (2024-02-15 to 2024-03-20), UK (2023-11)', 'USA')
```

### 2. Location Fraud Detection Logic

#### ContextAgent Enhancement
The `ContextAgent` now includes a `validateUserLocation()` method that:

1. **Compares Claimed vs Actual Location**
   - Retrieves user's current location from database
   - Compares with the location user claims in dispute
   - Flags mismatch as potential fraud

2. **Travel History Analysis**
   - If mismatch detected, checks travel history
   - Determines if user has ever been to claimed location
   - Assigns risk scores based on findings

3. **Risk Scoring**
   - **80 points**: User claims location with NO travel history + location mismatch
   - **60 points**: User has past travel history to claimed location but currently elsewhere
   - **70 points**: No travel history available + location mismatch

### 3. Fraud Detection Flow

```
User submits dispute claiming to be in "India"
         ↓
System retrieves user data from database
         ↓
Database shows current_location = "USA"
         ↓
LOCATION MISMATCH DETECTED!
         ↓
Check travel history for "India"
         ↓
┌─────────────────────────────────────┐
│ Has travel history to India?        │
├─────────────────────────────────────┤
│ YES → Risk Score +60                │
│ "User has past travel to India      │
│  but currently in USA"              │
│                                     │
│ NO → Risk Score +80                 │
│ "User has NO travel history to     │
│  India and currently in USA"       │
└─────────────────────────────────────┘
         ↓
Flag as LOCATION FRAUD
         ↓
Add warning messages to context
         ↓
DecisionAgent increases total risk score
         ↓
Likely outcome: HUMAN_REVIEW or REJECT
```

### 4. Warning Messages

When location fraud is detected, the system generates clear warnings:

```
🚨 LOCATION FRAUD DETECTED!
⚠️ User claims to be in: India
⚠️ Database shows current location: USA
🚨 CRITICAL: User has NO travel history to India and database shows they are in USA
⚠️ WARNING: This appears to be a fraudulent claim. User's stated location does not match their actual current location in our records.
```

### 5. Decision Impact

The `DecisionAgent` considers location fraud in final decision:

```java
// Critical: If location fraud is detected, significantly increase risk
if (context.isLocationFraudDetected()) {
    totalRiskScore += 50; // Major red flag
}
```

**Decision Outcomes:**
- **High Risk (70-100)**: AUTO_REFUND rejected, likely HUMAN_REVIEW with fraud investigation
- **Medium Risk (40-69)**: HUMAN_REVIEW with location verification required
- **Low Risk (<40)**: Standard verification process

### 6. API Integration

The validation happens automatically when processing disputes:

```java
// In ContextAgent.gatherContext()
if (userData.isPresent()) {
    // Validate user's claimed location against database
    validateUserLocation(userData.get(), userLocation, context);
}
```

## Test Scenarios

### Scenario 1: Fraudulent Claim (Location Mismatch + No History)
**Input:**
- User: ABC002 (Priya Sharma)
- Claimed Location: India
- Database Current Location: USA
- Travel History: USA, UK (no India)

**Expected Output:**
```
🚨 LOCATION FRAUD DETECTED!
Risk Score: +80
Decision: HUMAN_REVIEW / REJECT
```

### Scenario 2: Suspicious Claim (Location Mismatch + Has History)
**Input:**
- User: ABC002 (Priya Sharma)
- Claimed Location: India
- Database Current Location: USA
- Travel History: India, USA, UK

**Expected Output:**
```
⚠️ Location Mismatch Detected
Risk Score: +60
Decision: HUMAN_REVIEW
Note: User has past travel to India but currently in USA
```

### Scenario 3: Valid Claim (Location Match)
**Input:**
- User: ABC001 (Rajesh Kumar)
- Claimed Location: India
- Database Current Location: India
- Travel History: India, Dubai, Singapore

**Expected Output:**
```
✓ User's claimed location matches database records: India
Risk Score: Normal processing
Decision: Based on other factors
```

## Benefits

1. **Fraud Prevention**: Catches users lying about their location
2. **Risk Assessment**: Provides accurate risk scoring based on location data
3. **Evidence-Based Decisions**: Uses concrete data (database records) vs user claims
4. **Travel Pattern Analysis**: Considers historical travel patterns
5. **Clear Warnings**: Provides explicit fraud warnings to reviewers

## Technical Details

### New Fields in ContextData
```java
private String dbCurrentLocation;           // User's actual location from DB
private boolean locationFraudDetected;      // Flag for location fraud
```

### New Methods
```java
// ContextAgent
private void validateUserLocation(UserData userData, String userClaimedLocation, ContextData context)

// ContextData
public String getDbCurrentLocation()
public void setDbCurrentLocation(String dbCurrentLocation)
public boolean isLocationFraudDetected()
public void setLocationFraudDetected(boolean locationFraudDetected)
```

## Future Enhancements

1. **Real-time Location Tracking**: Integrate with GPS/IP-based location services
2. **Geofencing**: Define acceptable location variance
3. **Time-based Validation**: Consider time zones and travel time
4. **Machine Learning**: Pattern recognition for suspicious location claims
5. **Multi-factor Location Verification**: Cross-reference with transaction locations

## Conclusion

This feature significantly enhances fraud detection by validating user location claims against database records and travel history. It provides a robust mechanism to identify and flag fraudulent disputes where users misrepresent their location.

---
**Made with Bob** - AI Dispute Management System