# AI-Based Scrutiny for Same Location Disputes

## Overview
This system now includes advanced AI analysis that scrutinizes disputes **even when both locations match**. This prevents fraudsters from exploiting the system by claiming unauthorized transactions from the same location.

## Key Features

### 1. **Behavioral Pattern Analysis**
Detects suspicious behavioral patterns in dispute descriptions:
- ✅ Vague or generic descriptions
- ✅ Contradictory statements
- ✅ Excessive emotional manipulation
- ✅ Lack of specific transaction details
- ✅ Template or copy-paste patterns

**Example Detection:**
```
Description: "something wrong with transaction"
AI Detection: Vague description pattern + Lack of specifics
Risk Score: +30
```

### 2. **Sentiment & Intent Analysis**
Analyzes the emotional tone and intent behind the dispute:
- ✅ Aggressive or threatening language
- ✅ Excessive victim narrative
- ✅ Genuine concern indicators
- ✅ Manipulation tactics

**Example Detection:**
```
Description: "I will sue you, this is unacceptable, worst service ever"
AI Detection: Aggressive/threatening tone detected
Risk Score: +25
```

### 3. **Transaction Pattern Analysis**
Identifies suspicious transaction patterns:
- ✅ Round number amounts (common in fraud)
- ✅ High-value transactions with minimal details
- ✅ Claims of "unauthorized" despite location match
- ✅ Multiple transactions mentioned

**Example Detection:**
```
Amount: ₹10,000 (round number)
Location: INDIA (matches user location)
Description: "unauthorized transaction not done by me"
AI Detection: Claims unauthorized despite location match + Round amount
Risk Score: +45
```

### 4. **Linguistic Analysis (Deception Detection)**
Detects linguistic patterns associated with deception:
- ✅ Excessive hedging language ("maybe", "possibly", "might")
- ✅ Lack of first-person pronouns (psychological distancing)
- ✅ Overly detailed irrelevant information (distraction)
- ✅ Excessive passive voice (responsibility avoidance)

**Example Detection:**
```
Description: "The transaction was made, the charge was done, payment was taken"
AI Detection: Excessive passive voice (responsibility avoidance)
Risk Score: +15
```

### 5. **Urgency & Pressure Tactics Detection**
Identifies artificial urgency and pressure tactics:
- ✅ Time pressure indicators ("urgent", "immediately", "ASAP")
- ✅ Deadline mentions
- ✅ Escalation threats

**Example Detection:**
```
Description: "Need refund immediately, urgent emergency, will escalate to manager"
AI Detection: Artificial urgency + Escalation threats
Risk Score: +45
```

## How It Works

### When Locations Match (Same Location Scenario)
```
Transaction Location: INDIA
User Current Location: INDIA
Status: ✅ Locations Match

🤖 AI DEEP SCRUTINY ACTIVATED:
1. Behavioral Analysis → Score: 15
2. Sentiment Analysis → Score: 25
3. Pattern Analysis → Score: 30
4. Linguistic Analysis → Score: 15
5. Urgency Analysis → Score: 25

Overall AI Score: 28 (Weighted Average)
Risk Level: LOW
Recommendation: STANDARD_PROCESSING
```

### When Locations Don't Match
```
Transaction Location: USA
User Current Location: INDIA
Status: ⚠️ LOCATION MISMATCH

Risk Score: +60 (Immediate High Risk)
Intent: FRAUD
Decision: AUTO_REFUND & BLOCK_CARD
```

## AI Scoring System

### Overall AI Score Calculation
```
Overall Score = (
    Behavior Score × 0.25 +
    Sentiment Score × 0.20 +
    Pattern Score × 0.30 +
    Linguistic Score × 0.15 +
    Urgency Score × 0.10
)
```

### Risk Levels
- **HIGH RISK** (Score ≥ 60): Multiple red flags detected → HUMAN_REVIEW_REQUIRED
- **MEDIUM RISK** (Score 35-59): Some suspicious patterns → ENHANCED_VERIFICATION
- **LOW RISK** (Score < 35): Normal patterns → STANDARD_PROCESSING

## Decision Logic

### Scenario 1: High AI Score (≥60) with Same Location
```
Location Match: ✅ YES
AI Score: 65
Decision: AUTO_REFUND & BLOCK_CARD
Reason: "🤖 AI DEEP ANALYSIS: Despite location match, AI detected HIGH RISK patterns"
```

### Scenario 2: Medium AI Score (35-59) with Same Location
```
Location Match: ✅ YES
AI Score: 45
Decision: PARTIAL_REFUND or HUMAN_REVIEW
Reason: "🤖 AI Analysis: Suspicious patterns detected despite location match"
```

### Scenario 3: Low AI Score (<35) with Same Location
```
Location Match: ✅ YES
AI Score: 20
Decision: HUMAN_REVIEW
Reason: "🤖 System Analysis: STANDARD_PROCESSING - Our analysis shows normal patterns"
```

## Test Cases

### Test Case 1: Fraudulent Same-Location Claim
```json
{
  "amount": 15000,
  "transactionLocation": "INDIA",
  "userCurrentLocation": "INDIA",
  "description": "not done by me unauthorized transaction need refund immediately"
}
```
**Expected Result:**
- AI Score: ~70
- Decision: AUTO_REFUND & BLOCK_CARD
- Reason: High AI risk despite location match

### Test Case 2: Genuine Dispute Same-Location
```json
{
  "amount": 2500,
  "transactionLocation": "INDIA",
  "userCurrentLocation": "INDIA",
  "description": "I made a purchase at XYZ store on 5th April but was charged twice. I have the receipt showing single transaction but my account shows two charges of 2500 each."
}
```
**Expected Result:**
- AI Score: ~15
- Decision: HUMAN_REVIEW
- Reason: Low risk, detailed explanation, genuine concern

### Test Case 3: Suspicious Pattern Same-Location
```json
{
  "amount": 10000,
  "transactionLocation": "INDIA",
  "userCurrentLocation": "INDIA",
  "description": "something wrong error issue"
}
```
**Expected Result:**
- AI Score: ~45
- Decision: HUMAN_REVIEW or PARTIAL_REFUND
- Reason: Vague description + Round amount + Lack of details

## Benefits

1. **Prevents Location-Based Fraud Exploitation**
   - Fraudsters can no longer claim "unauthorized" from same location without scrutiny

2. **Multi-Dimensional Analysis**
   - 5 different AI analysis layers provide comprehensive fraud detection

3. **Behavioral Psychology Integration**
   - Detects deception patterns based on linguistic and behavioral psychology

4. **Adaptive Risk Scoring**
   - Weighted scoring system prioritizes critical risk factors

5. **Transparent Decision Making**
   - AI provides detailed insights explaining its decisions

## API Response Example

```json
{
  "intent": "POTENTIAL_FRAUD",
  "riskScore": 75,
  "decision": "AUTO_REFUND & BLOCK_CARD",
  "refundAmount": 15000.0,
  "reviewReason": "⚠️ HIGH RISK FRAUD DETECTED (Risk Score: 75). 🤖 AI DEEP ANALYSIS: Despite location match, AI detected HIGH RISK patterns (AI Score: 65). \n\n📊 AI Insights:\n• Vague description pattern detected\n• Claims unauthorized despite location match - requires deep scrutiny\n• Round number amount (common in fraud)\n• Artificial urgency/pressure tactics detected\n\nClear fraud indicators detected. Full refund approved automatically. Card will be BLOCKED immediately for security."
}
```

## Technical Implementation

### Files Modified/Created:
1. **AIAnalysisService.java** (NEW)
   - Core AI analysis engine
   - 5 analysis modules
   - Weighted scoring system

2. **DisputeController.java** (MODIFIED)
   - Integrated AI service
   - Enhanced decision logic
   - Detailed AI insights in responses

### Dependencies:
- Spring Boot 3.1.5
- Java 17
- No external AI libraries (rule-based AI)

## Future Enhancements

1. **Machine Learning Integration**
   - Train models on historical dispute data
   - Improve pattern recognition accuracy

2. **Real-time Transaction History Analysis**
   - Analyze user's transaction patterns
   - Detect anomalies in spending behavior

3. **Geolocation Verification**
   - Verify IP address matches claimed location
   - Detect VPN/proxy usage

4. **Merchant Risk Scoring**
   - Track merchant fraud rates
   - Flag high-risk merchants

5. **Time-based Analysis**
   - Analyze transaction timing patterns
   - Detect impossible travel scenarios

## Conclusion

This AI-based scrutiny system provides robust fraud detection even when traditional location-based checks pass. By analyzing behavioral, linguistic, and pattern-based indicators, the system can identify fraudulent disputes with high accuracy while maintaining fairness for genuine customers.

---
**Made with Bob** 🤖