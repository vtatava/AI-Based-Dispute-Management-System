# UserID Validation Implementation Guide

## Overview
This document describes the implementation of UserID validation in the Dispute AI system. The system now requires users to provide a UserID (e.g., ABC001, ABC002) and validates that it matches the UserID associated with the AI-extracted Aadhaar number from the uploaded ID document.

## Changes Made

### 1. Database Schema Changes

#### File: `backend/src/main/resources/schema.sql`
- **Added Column**: `user_id VARCHAR(20) NOT NULL UNIQUE`
- **Purpose**: Store unique UserID codes (ABC001, ABC002, etc.) for each user
- **Constraint**: UNIQUE and NOT NULL to ensure data integrity

```sql
CREATE TABLE IF NOT EXISTS USER_DATA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL UNIQUE,  -- NEW COLUMN
    user_name VARCHAR(255) NOT NULL,
    govt_id VARCHAR(50) NOT NULL UNIQUE,
    dob DATE NOT NULL,
    travel_history TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. Sample Data Updates

#### File: `backend/src/main/resources/data.sql`
- **Updated**: All user records now include UserID codes
- **Format**: ABC001, ABC002, ABC003, ABC004, ABC005

```sql
MERGE INTO USER_DATA (user_id, user_name, govt_id, dob, travel_history) KEY(govt_id) VALUES
('ABC001', 'Rajesh Kumar', 'AADHAAR123456789', '1985-05-15', 'India, Dubai (2024-01), Singapore (2023-12)'),
('ABC002', 'Priya Sharma', 'AADHAAR987654321', '1990-08-22', 'India, USA (2024-02), UK (2023-11)'),
('ABC003', 'Amit Patel', 'AADHAAR456789123', '1988-03-10', 'India'),
('ABC004', 'Sneha Reddy', 'AADHAAR789123456', '1992-11-30', 'India, Thailand (2024-01)'),
('ABC005', 'Vikram Singh', 'AADHAAR321654987', '1987-07-18', 'India, Australia (2023-10), New Zealand (2023-09)');
```

### 3. Entity Updates

#### File: `backend/src/main/java/com/app/entity/UserData.java`
- **Added Field**: `private String userId`
- **Added Getter/Setter**: `getUserId()` and `setUserId()`
- **Updated Constructor**: Now accepts userId parameter

```java
@Column(name = "user_id", nullable = false, unique = true, length = 20)
private String userId;

public String getUserId() {
    return userId;
}

public void setUserId(String userId) {
    this.userId = userId;
}
```

### 4. Repository Updates

#### File: `backend/src/main/java/com/app/repository/UserDataRepository.java`
- **Added Method**: `Optional<UserData> findByUserId(String userId)`
- **Purpose**: Enable efficient lookup of users by UserID code

```java
@Repository
public interface UserDataRepository extends JpaRepository<UserData, Long> {
    Optional<UserData> findByGovtId(String govtId);
    Optional<UserData> findByUserId(String userId);  // NEW METHOD
}
```

### 5. ID Validation Service Updates

#### File: `backend/src/main/java/com/app/service/IdValidationService.java`

**Key Changes:**

1. **UserID Requirement Check**:
   - Validates that UserID is provided before processing
   - Rejects requests with missing or invalid UserID

2. **AI-Powered Extraction with UserID Validation**:
   - After extracting Aadhaar from ID document using AI
   - Looks up the user in database by extracted Aadhaar
   - Compares provided UserID with database UserID
   - **CRITICAL**: Only proceeds if UserIDs match

3. **Legacy OCR Validation with UserID Check**:
   - Same validation logic for legacy OCR extraction
   - Ensures UserID matches before approving validation

4. **Fallback Validation**:
   - Uses new `findByUserId()` repository method
   - Provides backward compatibility with Aadhaar lookup

**Example Validation Flow**:
```
User provides: UserID = "ABC001" + ID Document
↓
System extracts: Aadhaar = "AADHAAR456789123"
↓
System looks up: User with Aadhaar "AADHAAR456789123"
↓
Database returns: UserID = "ABC003", Name = "Amit Patel"
↓
System compares: Provided "ABC001" ≠ Database "ABC003"
↓
Result: ❌ VALIDATION FAILED - UserID Mismatch
```

### 6. Dispute Controller Updates

#### File: `backend/src/main/java/com/app/controller/DisputeController.java`

**Enhanced Validation Logic**:

1. **Mandatory ID Document Check**:
   - Requires both UserID and ID document
   - Rejects disputes without proper identification

2. **UserID Mismatch Handling**:
   - Returns detailed error message when UserID doesn't match
   - Provides expected vs provided UserID information
   - Sets decision to "REJECTED" for security

3. **Successful Validation**:
   - Extracts verified user information
   - Logs successful validation
   - Proceeds with dispute analysis only after validation

**Example Response for Mismatch**:
```json
{
  "intent": "ID_VALIDATION_FAILED",
  "riskScore": 100,
  "decision": "REJECTED",
  "refundAmount": null,
  "reviewReason": "⚠️ UserID Mismatch! Provided UserID (ABC001) does NOT match the UserID associated with extracted Aadhaar (AADHAAR456789123). Expected UserID: ABC003. Please provide the correct UserID.\n\n🚫 DISPUTE REJECTED: UserID validation failed..."
}
```

## Validation Flow

### Complete Validation Process:

```
1. User submits dispute with:
   - UserID (e.g., "ABC003")
   - ID Document (Aadhaar card image)
   - Dispute details

2. System validates UserID format:
   - Not null
   - Not empty
   - Not "GUEST_USER"

3. System extracts Aadhaar from ID document:
   - Uses AI-powered extraction (primary)
   - Falls back to OCR (secondary)
   - Extracts: "AADHAAR456789123"

4. System looks up user in database:
   - Query: findByGovtId("AADHAAR456789123")
   - Returns: UserData with userId="ABC003"

5. System validates UserID match:
   - Provided: "ABC003"
   - Database: "ABC003"
   - Result: ✅ MATCH

6. System proceeds with dispute analysis:
   - Only if validation successful
   - Otherwise, dispute is REJECTED
```

## Security Benefits

1. **Two-Factor Verification**:
   - UserID (something you know)
   - ID Document (something you have)

2. **Prevents Identity Theft**:
   - Cannot use someone else's ID document with wrong UserID
   - System validates both credentials match

3. **Audit Trail**:
   - All validation attempts are logged
   - Failed validations are tracked

4. **Data Integrity**:
   - Ensures disputes are filed by legitimate users
   - Prevents fraudulent dispute submissions

## Testing the Implementation

### Test Case 1: Valid UserID and ID Document
```
Input:
- UserID: "ABC003"
- ID Document: Amit Patel's Aadhaar (AADHAAR456789123)

Expected Result: ✅ Validation Successful
```

### Test Case 2: Invalid UserID with Valid ID Document
```
Input:
- UserID: "ABC001"
- ID Document: Amit Patel's Aadhaar (AADHAAR456789123)

Expected Result: ❌ Validation Failed - UserID Mismatch
Message: "Expected UserID: ABC003"
```

### Test Case 3: Missing UserID
```
Input:
- UserID: "" (empty)
- ID Document: Valid Aadhaar

Expected Result: ❌ Validation Failed
Message: "UserID is required. Please provide your UserID (e.g., ABC001)"
```

### Test Case 4: Missing ID Document
```
Input:
- UserID: "ABC003"
- ID Document: null

Expected Result: ❌ Validation Failed
Message: "UserID and ID document are required"
```

## User Guide

### For End Users:

1. **Obtain Your UserID**:
   - Your UserID is provided when you register
   - Format: ABC001, ABC002, etc.
   - Keep it secure

2. **Submit Dispute**:
   - Enter your UserID in the form
   - Upload your government ID (Aadhaar card)
   - Fill in dispute details

3. **Validation Process**:
   - System will extract your Aadhaar number from the uploaded image
   - System will verify it matches your UserID
   - You'll receive immediate feedback

4. **If Validation Fails**:
   - Check you entered the correct UserID
   - Ensure the ID document is clear and readable
   - Verify you're using your own ID document

## Database Migration

### For Existing Deployments:

If you have existing data, run this migration:

```sql
-- Add user_id column
ALTER TABLE USER_DATA ADD COLUMN user_id VARCHAR(20);

-- Update existing records with generated UserIDs
UPDATE USER_DATA SET user_id = CONCAT('ABC', LPAD(id, 3, '0'));

-- Make column NOT NULL and UNIQUE
ALTER TABLE USER_DATA MODIFY COLUMN user_id VARCHAR(20) NOT NULL UNIQUE;
```

## API Changes

### Endpoint: `/api/dispute/raise-with-files`

**New Requirement**: `userId` parameter is now mandatory

**Before**:
```
userId: optional
```

**After**:
```
userId: required (must not be empty or "GUEST_USER")
```

### Endpoint: `/api/dispute/validate-id`

**Enhanced Response**:
```json
{
  "valid": true/false,
  "message": "Validation message",
  "userName": "User Name",
  "userId": "AADHAAR number",
  "userIdCode": "ABC001",  // NEW FIELD
  "verified": true/false,
  "expectedUserId": "ABC003",  // NEW FIELD (on mismatch)
  "providedUserId": "ABC001",  // NEW FIELD (on mismatch)
  "extractedAadhaar": "AADHAAR456789123"  // NEW FIELD (on mismatch)
}
```

## Troubleshooting

### Issue: Compilation Error
**Solution**: Ensure all files are updated and run `mvn compile`

### Issue: Database Error on Startup
**Solution**: Drop existing tables and let Spring Boot recreate them with new schema

### Issue: UserID Not Found
**Solution**: Check data.sql has been executed and users have UserIDs assigned

### Issue: Validation Always Fails
**Solution**: 
1. Check UserID format (should be ABC001, ABC002, etc.)
2. Verify ID document is clear and readable
3. Check logs for extraction errors

## Conclusion

The UserID validation implementation adds a critical security layer to the dispute management system. By requiring both a UserID and matching ID document, the system ensures that only legitimate users can file disputes, preventing fraud and identity theft.

**Key Takeaway**: The system will ONLY proceed with dispute analysis if the provided UserID matches the UserID associated with the AI-extracted Aadhaar number from the uploaded ID document.

---
**Implementation Date**: April 18, 2026
**Status**: ✅ Completed and Tested
**Build Status**: ✅ Compilation Successful