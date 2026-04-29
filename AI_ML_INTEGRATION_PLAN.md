# AI/ML Integration Implementation Plan
## Production-Grade Dispute AI System with Real Models

---

## 🎯 Objective
Replace rule-based system with real AI/ML models using free/open-source solutions.

---

## 🏗️ Architecture Overview

```
┌─────────────────┐
│   React UI      │
│  (Frontend)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Spring Boot    │
│   (Backend)     │
└────────┬────────┘
         │
         ├──────────────────┐
         │                  │
         ▼                  ▼
┌─────────────────┐  ┌──────────────────┐
│  Ollama/LLaMA   │  │  Python ML       │
│  (Intent)       │  │  (Fraud Model)   │
└─────────────────┘  └──────────────────┘
```

---

## 📋 Components

### 1. **Intent Detection Layer**
- **Model**: Ollama (LLaMA 3.2 or Mistral)
- **Purpose**: Understand dispute intent
- **Free**: Yes, runs locally
- **Output**: Intent classification + confidence

### 2. **Fraud Detection Layer**
- **Model**: Python scikit-learn (Random Forest/XGBoost)
- **Purpose**: Predict fraud probability
- **Free**: Yes, open-source
- **Output**: Fraud score + confidence

### 3. **Decision Layer**
- **Type**: Hybrid AI (Rule-based + ML)
- **Purpose**: Final decision making
- **Logic**: Combines intent + fraud + rules

---

## 🛠️ Implementation Steps

### Phase 1: Setup Python ML Service (Day 1-2)

#### 1.1 Create Python Service Structure
```
ml-service/
├── app.py                 # Flask API
├── models/
│   ├── fraud_model.pkl    # Trained model
│   └── train_model.py     # Training script
├── requirements.txt
└── Dockerfile
```

#### 1.2 Install Dependencies
```bash
pip install flask scikit-learn pandas numpy joblib
```

#### 1.3 Create Fraud Detection Model
```python
# train_model.py
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
import pandas as pd
import joblib

# Features: amount, location_mismatch, description_length, etc.
# Train model and save
model = RandomForestClassifier(n_estimators=100)
# ... training code ...
joblib.dump(model, 'models/fraud_model.pkl')
```

#### 1.4 Create Flask API
```python
# app.py
from flask import Flask, request, jsonify
import joblib

app = Flask(__name__)
model = joblib.load('models/fraud_model.pkl')

@app.route('/predict', methods=['POST'])
def predict():
    data = request.json
    # Extract features
    # Make prediction
    # Return fraud_score + confidence
    return jsonify({
        'fraud_score': 0.85,
        'confidence': 0.92,
        'features_used': ['amount', 'location', 'description']
    })
```

---

### Phase 2: Setup Ollama for Intent Detection (Day 2-3)

#### 2.1 Install Ollama
```bash
# Windows
winget install Ollama.Ollama

# Pull LLaMA model
ollama pull llama3.2
```

#### 2.2 Create Intent Detection Service
```python
# intent_service.py
import requests

def detect_intent(description):
    prompt = f"""
    Analyze this dispute description and classify the intent:
    Description: {description}
    
    Classify as one of:
    - FRAUD_CLAIM (user claims unauthorized transaction)
    - SELLER_DISPUTE (issue with seller/delivery)
    - SERVICE_ISSUE (service not provided)
    - QUALITY_COMPLAINT (product quality issue)
    - FRIVOLOUS (test/joke request)
    
    Respond with JSON: {{"intent": "...", "confidence": 0.0-1.0, "reasoning": "..."}}
    """
    
    response = requests.post('http://localhost:11434/api/generate',
        json={
            'model': 'llama3.2',
            'prompt': prompt,
            'stream': False
        })
    
    return response.json()
```

---

### Phase 3: Update Java Backend (Day 3-4)

#### 3.1 Add HTTP Client Dependencies
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

#### 3.2 Create ML Service Client
```java
@Service
public class MLServiceClient {
    
    private final WebClient webClient;
    
    public MLServiceClient() {
        this.webClient = WebClient.builder()
            .baseUrl("http://localhost:5000")
            .build();
    }
    
    public FraudPrediction predictFraud(DisputeRequest request) {
        return webClient.post()
            .uri("/predict")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(FraudPrediction.class)
            .block();
    }
}
```

#### 3.3 Create Intent Service Client
```java
@Service
public class IntentServiceClient {
    
    private final WebClient webClient;
    
    public IntentServiceClient() {
        this.webClient = WebClient.builder()
            .baseUrl("http://localhost:5001")
            .build();
    }
    
    public IntentAnalysis analyzeIntent(String description) {
        return webClient.post()
            .uri("/analyze")
            .bodyValue(Map.of("description", description))
            .retrieve()
            .bodyToMono(IntentAnalysis.class)
            .block();
    }
}
```

#### 3.4 Update Controller
```java
@PostMapping("/raise")
public DisputeResponse raiseDispute(@RequestBody DisputeRequest request) {
    
    // 1. Get intent from LLaMA
    IntentAnalysis intent = intentService.analyzeIntent(request.getDescription());
    
    // 2. Get fraud prediction from ML model
    FraudPrediction fraud = mlService.predictFraud(request);
    
    // 3. Hybrid decision
    String decision = hybridDecision(intent, fraud, request);
    
    // 4. Return with confidence scores
    return new DisputeResponse(
        intent.getIntent(),
        fraud.getFraudScore(),
        decision,
        calculateRefund(decision, request.getAmount()),
        buildReason(intent, fraud),
        intent.getConfidence(),
        fraud.getConfidence()
    );
}
```

---

### Phase 4: Update DTOs for Confidence Scores (Day 4)

#### 4.1 Update DisputeResponse
```java
public class DisputeResponse {
    private String intent;
    private int riskScore;
    private String decision;
    private Double refundAmount;
    private String reviewReason;
    
    // NEW: Confidence scores
    private Double intentConfidence;
    private Double fraudConfidence;
    private Double overallConfidence;
    
    // Getters/Setters
}
```

---

### Phase 5: Update UI for Confidence Scores (Day 5)

#### 5.1 Update App.js
```javascript
{result && (
  <div className="result-card">
    <h2>📊 AI Analysis Result</h2>
    
    {/* Confidence Scores */}
    <div className="confidence-section">
      <h3>🎯 AI Confidence Scores</h3>
      <div className="confidence-bars">
        <div className="confidence-item">
          <span>Intent Detection:</span>
          <div className="confidence-bar">
            <div style={{width: `${result.intentConfidence * 100}%`}}>
              {(result.intentConfidence * 100).toFixed(1)}%
            </div>
          </div>
        </div>
        <div className="confidence-item">
          <span>Fraud Detection:</span>
          <div className="confidence-bar">
            <div style={{width: `${result.fraudConfidence * 100}%`}}>
              {(result.fraudConfidence * 100).toFixed(1)}%
            </div>
          </div>
        </div>
        <div className="confidence-item">
          <span>Overall Confidence:</span>
          <div className="confidence-bar">
            <div style={{width: `${result.overallConfidence * 100}%`}}>
              {(result.overallConfidence * 100).toFixed(1)}%
            </div>
          </div>
        </div>
      </div>
    </div>
    
    {/* Rest of the result display */}
  </div>
)}
```

---

## 🚀 Deployment

### Local Development
```bash
# Terminal 1: Start Python ML Service
cd ml-service
python app.py

# Terminal 2: Start Intent Service
cd intent-service
python intent_service.py

# Terminal 3: Start Ollama
ollama serve

# Terminal 4: Start Java Backend
cd backend
mvnw spring-boot:run

# Terminal 5: Start React Frontend
cd frontend
npm start
```

### Docker Compose
```yaml
version: '3.8'
services:
  ml-service:
    build: ./ml-service
    ports:
      - "5000:5000"
  
  intent-service:
    build: ./intent-service
    ports:
      - "5001:5001"
  
  ollama:
    image: ollama/ollama
    ports:
      - "11434:11434"
  
  backend:
    build: ./backend
    ports:
      - "9090:9090"
    depends_on:
      - ml-service
      - intent-service
  
  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
```

---

## 📊 Free AI/ML Stack

| Component | Technology | Cost | Purpose |
|-----------|-----------|------|---------|
| Intent Detection | Ollama (LLaMA 3.2) | FREE | NLP understanding |
| Fraud Model | scikit-learn | FREE | ML predictions |
| Backend | Spring Boot | FREE | API layer |
| Frontend | React | FREE | UI |
| Deployment | Docker | FREE | Containerization |

---

## ⚠️ Considerations

### Pros
- ✅ 100% Free/Open-source
- ✅ Runs locally (no API costs)
- ✅ Full control over models
- ✅ Privacy (data stays local)

### Cons
- ❌ Requires significant compute (8GB+ RAM for LLaMA)
- ❌ Slower than cloud APIs
- ❌ Need to train ML models
- ❌ Complex setup

---

## 📈 Timeline

- **Day 1-2**: Python ML service + model training
- **Day 3**: Ollama setup + intent service
- **Day 4**: Java backend integration
- **Day 5**: UI updates + confidence scores
- **Day 6-7**: Testing + deployment

**Total**: ~1 week for full implementation

---

## 🎓 Skills Required

- Python (Flask, scikit-learn)
- Java (Spring Boot, WebClient)
- React (state management)
- Docker (containerization)
- ML basics (model training)

---

## 📝 Next Steps

1. ✅ Review this plan
2. ⬜ Setup Python environment
3. ⬜ Install Ollama
4. ⬜ Create ML service
5. ⬜ Integrate with backend
6. ⬜ Update UI
7. ⬜ Test end-to-end

---

**Ready to start implementation?**

This is a production-grade AI system that will take approximately 1 week to fully implement.
