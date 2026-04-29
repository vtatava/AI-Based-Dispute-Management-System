# AUTO_REFUND Scenarios - Complete Guide

This document explains all scenarios where the AI-Based Dispute Management System will automatically approve a refund (AUTO_REFUND decision).

## 🎯 Core Requirements for AUTO_REFUND

For a dispute to receive AUTO_REFUND, it must pass through multiple validation layers:

### 1. **Transaction Receipt Validation** ✅
- Receipt data must match form data (UserID, Date, Amount, Location)
- All fields must be consistent and verified

### 2. **ID Validation** ✅
- Government ID must be validated successfully
- Name and Aadhaar number must match database records

### 3. **Intent Classification** ✅
- Must be classified as "FRAUD" (genuine fraud victim)
- NOT "FRAUDULENT_CLAIM" or "FRIVOLOUS" (these are auto-rejected)

### 4. **IBM ICA AI Analysis** ✅
- Claim Assessment must be "LEGITIMATE_CLAIM" (customer is honest victim)
- AI Recommendation must be "AUTO_REFUND"
- Confidence Level should be "HIGH" or "MEDIUM"

---

## 📋 Specific AUTO_REFUND Scenarios

### **Scenario 1: High-Confidence Fraud with Location Match**
```
Description: "My card was stolen and someone made unauthorized transactions at a store"
Conditions:
- Intent: FRAUD
- User location matches database (no location fraud)
- Transaction location matches user location
- Transaction receipt validated
- IBM ICA: LEGITIMATE_CLAIM + AUTO_REFUND
- Risk Score: < 70

Result: ✅ AUTO_REFUND + BLOCK_CARD
```

### **Scenario 2: Clear Unauthorized Transaction**
```
Description: "I lost my wallet yesterday and noticed unauthorized charges on my card today. I did not make these transactions."
Conditions:
- Intent: FRAUD
- Clear victim narrative
- No contradictions in description
- Location signals consistent
- IBM ICA: LEGITIMATE_CLAIM + HIGH confidence
- Risk Score: 40-70

Result: ✅ AUTO_REFUND + BLOCK_CARD
```

### **Scenario 3: Validated Proof with Consistent Story**
```
Description: "Someone used my card details for online shopping. I have never shopped at this website and I was at home when this happened."
Conditions:
- Transaction proof validated: [Transaction Proof Validated]
- User location matches database
- Transaction location matches user location
- No contradictory signals
- IBM ICA: LEGITIMATE_CLAIM + AUTO_REFUND
- Risk Score: < 35

Result: ✅ AUTO_REFUND + BLOCK_CARD
```

### **Scenario 4: AI-Validated Legitimate Claim**
```
Description: "I noticed charges on my statement that I didn't authorize. My card was in my possession but someone must have cloned it."
Conditions:
- Intent: FRAUD
- IBM ICA explicitly validates: LEGITIMATE_CLAIM
- AI Recommendation: AUTO_REFUND
- Confidence: HIGH
- No location fraud detected
- Risk Score: < 90

Result: ✅ AUTO_REFUND + BLOCK_CARD
```

---

## ❌ Scenarios that REJECT (No Refund)

### **Rejection Scenario 1: False Claim Detected**
```
Description: "This is not fraud but I want my money back because the seller didn't dispatch the item"
Reason: Contradictory statement - claims "not fraud" but files fraud dispute
Result: ❌ REJECTED - CLAIM_DENIED
```

### **Rejection Scenario 2: Location Fraud**
```
Description: "I am in India and someone used my card in USA"
Database shows: User is actually in USA
Reason: User is lying about their location
Result: ❌ REJECTED - CLAIM_DENIED
```

### **Rejection Scenario 3: AI Detects False Claim**
```
Description: "I made this purchase but now I want a refund because I changed my mind"
IBM ICA Assessment: FALSE_CLAIM (customer is lying)
Reason: Customer admits to making purchase, trying to get free refund
Result: ❌ REJECTED - CLAIM_DENIED
```

### **Rejection Scenario 4: Fraudulent Intent**
```
Description: "I authorized this transaction but now I claim it's fraud to get money back"
Intent Classification: FRAUDULENT_CLAIM
Reason: Customer is attempting to commit fraud against the bank
Result: ❌ REJECTED - CLAIM_DENIED
```

---

## ⚠️ Scenarios Requiring HUMAN_REVIEW

### **Review Scenario 1: Seller/Merchant Dispute**
```
Description: "The seller didn't dispatch my order and I want a refund"
Reason: This is a seller dispute, not fraud - requires merchant investigation
Result: 🔍 HUMAN_REVIEW - CONTACT_MERCHANT
```

### **Review Scenario 2: Service Quality Issue**
```
Description: "The product I received is defective and poor quality"
Reason: Quality dispute requires evidence verification
Result: 🔍 HUMAN_REVIEW - VERIFY
```

### **Review Scenario 3: Uncertain AI Assessment**
```
Description: "Something happened with my card and I'm not sure what"
IBM ICA Assessment: UNCERTAIN
Reason: Vague description, AI cannot determine legitimacy
Result: 🔍 HUMAN_REVIEW - INVESTIGATE
```

### **Review Scenario 4: Medium Risk Fraud**
```
Description: "I think someone used my card but I'm not completely sure"
Risk Score: 40-69
Reason: Some fraud indicators but not conclusive
Result: 🔍 HUMAN_REVIEW - INVESTIGATE
```

---

## 🎓 Example Descriptions for AUTO_REFUND

### ✅ Good Example 1 (Will get AUTO_REFUND):
```
"I lost my credit card on April 25th and immediately reported it to the bank. 
However, I noticed unauthorized transactions on April 26th at a store in Mumbai. 
I was not in Mumbai at that time and did not make these purchases. 
Someone must have found my card and used it fraudulently."
```
**Why it works:**
- Clear timeline
- Specific details (date, location)
- Consistent story
- Genuine victim narrative
- No contradictions

### ✅ Good Example 2 (Will get AUTO_REFUND):
```
"My card details were stolen and used for online shopping on a website I've never 
visited. I received SMS alerts about transactions I didn't authorize. 
I immediately checked my account and confirmed these are fraudulent charges."
```
**Why it works:**
- Clear fraud indication
- Specific details
- Immediate action taken
- No contradictions
- Genuine concern

### ❌ Bad Example 1 (Will be REJECTED):
```
"This is not fraud but I want my money back because the seller didn't send the item"
```
**Why it fails:**
- Contradictory statement
- Not a fraud claim
- Seller dispute (requires different process)

### ❌ Bad Example 2 (Will be REJECTED):
```
"I made this purchase but now I don't want it anymore so I'm claiming it's fraud"
```
**Why it fails:**
- Admits to making purchase
- Attempting false claim
- Trying to commit fraud

---

## 📊 Decision Flow Summary

```
1. Receipt Validation → PASS
2. ID Validation → PASS
3. Intent Classification → FRAUD (not FRAUDULENT_CLAIM)
4. Location Check → No fraud detected
5. IBM ICA Analysis → LEGITIMATE_CLAIM + AUTO_REFUND
6. Risk Score → < 70 (for high confidence) or < 90 (with AI validation)
   ↓
   ✅ AUTO_REFUND + BLOCK_CARD
```

---

## 🔑 Key Takeaways

**To get AUTO_REFUND, you need:**
1. ✅ Be a genuine fraud victim (not lying)
2. ✅ Provide clear, consistent description
3. ✅ Have validated transaction receipt
4. ✅ Have validated government ID
5. ✅ No location fraud (don't lie about your location)
6. ✅ No contradictory statements
7. ✅ IBM ICA AI validates your claim as legitimate

**Automatic REJECTION happens when:**
1. ❌ You contradict yourself
2. ❌ You lie about your location
3. ❌ AI detects you're making a false claim
4. ❌ You admit it's not fraud but still want refund
5. ❌ You're trying to commit fraud against the bank

**HUMAN_REVIEW happens when:**
1. 🔍 It's a seller/merchant dispute (not fraud)
2. 🔍 It's a quality/service issue
3. 🔍 Description is too vague or uncertain
4. 🔍 Medium risk indicators present

---

## 💡 Tips for Customers

**If you're a genuine fraud victim:**
- Be honest and clear in your description
- Provide specific details (dates, locations, amounts)
- Don't contradict yourself
- Don't lie about your location
- Explain what happened in a straightforward manner
- The AI will recognize genuine victims and approve refunds quickly

**If you're trying to scam the system:**
- Don't bother - the AI will detect false claims
- Contradictions will be caught
- Location fraud will be detected
- You'll be rejected and flagged for fraud
- Repeated attempts may result in account suspension

---

*Last Updated: April 29, 2026*
*System Version: AI-Based Dispute Management v2.0*