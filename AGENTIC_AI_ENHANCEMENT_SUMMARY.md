# Agentic AI Dispute Management System - Enhancement Summary

## Overview
This document summarizes the comprehensive enhancements made to transform the dispute management system into an enterprise-grade, multi-agent AI platform.

---

## 🎯 Key Enhancements Implemented

### 1. **Database Layer (H2 File-Based)**

#### Tables Created:
- **USER_DATA**: Stores user information for verification
  - userName, govtId, dob, travelHistory
  - Sample data: 5 users with travel history

- **FRAUD_WEBSITES**: Known fraudulent websites database
  - websiteUrl, riskLevel, description
  - Sample data: 10 known fraud sites

- **DISPUTE_RECORDS**: Historical dispute records
  - Tracks all disputes for analytics

#### Files:
- `backend/src/main/resources/schema.sql`
- `backend/src/main/resources/data.sql`
- `backend/src/main/resources/application.properties` (H2 configuration)

---

### 2. **Multi-Agent AI Architecture**

#### Agent 1: Intent Agent (`IntentAgent.java`)
**Purpose**: Classifies dispute intent
- **Classifications**: FRAUD, MERCHANT_DISPUTE, OTHER
- **Process**:
  1. Analyzes description and transaction details
  2. Calls IBM ICA for AI classification
  3. Falls back to rule-based if AI unavailable
- **Output**: Intent classification with confidence level

#### Agent 2: Context Agent (`ContextAgent.java`)
**Purpose**: Gathers contextual data
- **Data Sources**:
  1. User database verification
  2. Travel history check
  3. Location mismatch detection
  4. Fraud website database lookup
  5. Transaction amount analysis
- **Output**: Enriched context with risk scoring

#### Agent 3: Decision Agent (`DecisionAgent.java`)
**Purpose**: Makes final decision
- **Decisions**: AUTO_REFUND or HUMAN_REVIEW
- **Process**:
  1. Combines intent + context
  2. Calculates total risk score
  3. Applies decision logic
  4. Generates detailed explanation
- **Output**: Final decision with reasoning

---

### 3. **Enhanced Data Models**

#### DisputeRequest (Enhanced)
**New Fields**:
- `transactionType`: ATM, MERCHANT, ONLINE, OTHERS
- `transactionDateTime`: Transaction timestamp
- `userId`: Government ID for verification
- `userName`: For document verification
- `userDob`: Date of birth for verification
- `websiteUrl`: For online transaction verification
- `merchantName`: For merchant disputes
- `documentData`: Uploaded document data

#### DisputeResponse (Enhanced)
**New Fields**:
- `agentFlow`: Step-by-step agent execution details
- `userVerified`: User verification status
- `verificationMessage`: Verification result message

**AgentFlow Sub-object**:
- `intentAgent`: Intent agent status
- `contextAgent`: Context agent status
- `decisionAgent`: Decision agent status
- `intentResult`: Intent classification result
- `contextResult`: Context gathering result
- `decisionResult`: Final decision result

---

### 4. **Entity & Repository Layer**

#### Entities:
- `UserData.java`: JPA entity for user data
- `FraudWebsite.java`: JPA entity for fraud websites

#### Repositories:
- `UserDataRepository.java`: CRUD operations for users
- `FraudWebsiteRepository.java`: CRUD operations for fraud sites

---

### 5. **Transaction Type Specific Logic**

#### ATM Transactions:
- Location comparison (critical)
- Travel history verification
- High risk if location mismatch

#### Merchant Transactions:
- Merchant verification
- Always requires human review
- Merchant contact process

#### Online Transactions:
- Website URL verification
- Fraud database lookup
- Risk level based on website reputation

#### Others:
- General investigation process

---

### 6. **User Verification Flow**

1. **Document Upload**: User uploads ID document
2. **Data Extraction**: Extract name, govt ID, DOB
3. **Database Verification**: Compare with USER_DATA table
4. **Result**:
   - ✅ Match: Proceed with dispute
   - ❌ Mismatch: Show error message
     - "User verification failed. Please recheck or visit branch with valid documents"

---

### 7. **Risk Scoring System**

#### Risk Factors:
- Location mismatch: +40 points
- Fraud website (CRITICAL): +50 points
- Fraud website (HIGH): +35 points
- Fraud website (MEDIUM): +20 points
- High amount (>50k): +15 points
- Medium amount (>20k): +10 points
- Intent FRAUD: +40 points
- Intent MERCHANT_DISPUTE: +20 points
- No travel history: +20 points

#### Risk Thresholds:
- **70-100**: HIGH RISK → AUTO_REFUND (if fraud)
- **40-69**: MEDIUM RISK → HUMAN_REVIEW
- **0-39**: LOW RISK → HUMAN_REVIEW

---

### 8. **Decision Logic**

#### AUTO_REFUND Criteria:
- Intent: FRAUD
- Risk Score: ≥70
- High confidence indicators
- **Actions**: Refund + Block Card

#### HUMAN_REVIEW Criteria:
- Medium/Low risk scores
- Merchant disputes (always)
- Ambiguous cases
- Missing verification

---

## 📁 File Structure

```
backend/
├── src/main/
│   ├── java/com/app/
│   │   ├── agent/
│   │   │   ├── IntentAgent.java          ✅ NEW
│   │   │   ├── ContextAgent.java         ✅ NEW
│   │   │   └── DecisionAgent.java        ✅ NEW
│   │   ├── entity/
│   │   │   ├── UserData.java             ✅ NEW
│   │   │   └── FraudWebsite.java         ✅ NEW
│   │   ├── repository/
│   │   │   ├── UserDataRepository.java   ✅ NEW
│   │   │   └── FraudWebsiteRepository.java ✅ NEW
│   │   ├── dto/
│   │   │   ├── DisputeRequest.java       ✅ ENHANCED
│   │   │   └── DisputeResponse.java      ✅ ENHANCED
│   │   ├── controller/
│   │   │   └── DisputeController.java    ⏳ TO BE UPDATED
│   │   └── service/
│   │       ├── IbmIcaService.java        ✅ EXISTING
│   │       └── AIAnalysisService.java    ✅ EXISTING
│   └── resources/
│       ├── schema.sql                     ✅ NEW
│       ├── data.sql                       ✅ NEW
│       └── application.properties         ✅ ENHANCED
└── pom.xml                                ✅ ENHANCED (JPA + H2)
```

---

## 🔄 Agent Flow Diagram

```
User Request
     ↓
[Intent Agent]
     ↓
  Classify Intent
  (FRAUD/MERCHANT/OTHER)
     ↓
[Context Agent]
     ↓
  Gather Data:
  - User verification
  - Location check
  - Travel history
  - Fraud DB lookup
  - Amount analysis
     ↓
[Decision Agent]
     ↓
  Calculate Risk
  Apply Logic
  Make Decision
     ↓
Response to User
(AUTO_REFUND or HUMAN_REVIEW)
```

---

## 🎨 Frontend Enhancements (To Be Implemented)

### New UI Components:
1. **Transaction Type Dropdown**
   - ATM, Merchant, Online, Others

2. **Transaction Date & Time Picker**

3. **User Identification Section**
   - Government ID input (mandatory)
   - Name input
   - DOB input

4. **Document Upload**
   - File upload component
   - "Upload Transaction Supporting Documents"

5. **Conditional Fields**
   - Website URL (for Online)
   - Merchant Name (for Merchant)

6. **Agent Flow Visualization**
   - Step-by-step progress
   - Intent Detection → Data Gathering → Decision Making
   - Visual indicators for each step

7. **Enhanced Results Display**
   - Intent classification
   - Risk score with visual meter
   - Decision with explanation
   - "Why this decision?" section
   - Agent flow summary

8. **Professional Dashboard Style**
   - Modern cards layout
   - Better spacing and typography
   - Color-coded risk levels
   - Icons for each section
   - Reset button

---

## 🔧 Next Steps

### Backend:
1. ✅ Database setup complete
2. ✅ Entities and repositories created
3. ✅ Multi-agent system implemented
4. ⏳ Update DisputeController to orchestrate agents
5. ⏳ Add user verification service
6. ⏳ Add document processing service
7. ⏳ Rebuild and test

### Frontend:
1. ⏳ Add new input fields
2. ⏳ Implement transaction type dropdown
3. ⏳ Add document upload component
4. ⏳ Create agent flow visualization
5. ⏳ Enhance UI with professional design
6. ⏳ Add step-by-step progress indicators
7. ⏳ Improve results display

---

## 🚀 How to Build & Run

### Backend:
```bash
cd backend
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

### Frontend:
```bash
cd frontend
npm install
npm start
```

### Access H2 Console:
```
URL: http://localhost:9090/h2-console
JDBC URL: jdbc:h2:file:./data/disputedb
Username: sa
Password: (leave empty)
```

---

## 📊 Sample Data

### Users in Database:
1. Rajesh Kumar (AADHAAR123456789) - Travel: India, Dubai, Singapore
2. Priya Sharma (AADHAAR987654321) - Travel: India, USA, UK
3. Amit Patel (AADHAAR456789123) - Travel: India only
4. Sneha Reddy (AADHAAR789123456) - Travel: India, Thailand
5. Vikram Singh (AADHAAR321654987) - Travel: India, Australia, NZ

### Fraud Websites:
- fake-amazon-deals.com (HIGH)
- free-iphone-giveaway.net (CRITICAL)
- secure-bank-login.xyz (CRITICAL)
- And 7 more...

---

## 🎯 Key Features

✅ Multi-agent AI architecture
✅ User verification with database
✅ Fraud website detection
✅ Travel history verification
✅ Transaction type specific logic
✅ Risk scoring system
✅ Intelligent decision making
✅ Detailed explanations
✅ Agent flow tracking
✅ File-based H2 database
✅ Sample data pre-loaded

---

**Status**: Backend structure complete. Ready for controller update and frontend implementation.

**Made with Bob** 🤖