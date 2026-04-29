# AI-Based Dispute Management System

A full-stack application that uses AI logic to analyze and resolve customer disputes automatically. The system evaluates transaction disputes based on amount, location, and description to determine fraud intent, calculate risk scores, and make automated decisions.

## 🏗️ Tech Stack

### Backend
- **Java Spring Boot 3.1.5** (Maven)
- **Java 17**
- REST API
- In-memory logic (no database)

### Frontend
- **React 18.2.0**
- **Axios** for API calls
- Modern CSS with animations

## 📁 Project Structure

```
production_dispute_ai/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/app/
│   │   │   │   ├── DisputeAiApplication.java
│   │   │   │   ├── controller/
│   │   │   │   │   └── DisputeController.java
│   │   │   │   └── dto/
│   │   │   │       ├── DisputeRequest.java
│   │   │   │       └── DisputeResponse.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   └── pom.xml
├── frontend/
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── App.js
│   │   ├── App.css
│   │   ├── index.js
│   │   └── index.css
│   └── package.json
└── README.md
```

## 🤖 AI Logic & Rules

The system analyzes disputes using the following rules:

### Risk Scoring
1. **Location Check**: If location ≠ "INDIA" → Add 50 risk points
2. **Amount Check**: If amount > 10,000 → Add 40 risk points
3. **Description Analysis**: If description contains "not done" or "fraud" → Intent = FRAUD

### Decision Making
- **Risk Score ≥ 80**: AUTO_REFUND & BLOCK_CARD
- **Risk Score < 50**: REJECT
- **Risk Score 50-79**: HUMAN_REVIEW

## 🚀 Getting Started

### Prerequisites
- **Java 17** or higher
- **Maven 3.6+**
- **Node.js 16+** and npm
- **Git**

### Installation & Running

#### 1. Clone the Repository
```bash
cd c:/Users/0022SM744/Downloads/production_dispute_ai
```

#### 2. Start Backend (Terminal 1)
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will start on: **http://localhost:9090**

#### 3. Start Frontend (Terminal 2)
```bash
cd frontend
npm install
npm start
```

Frontend will start on: **http://localhost:3000**

## 📝 API Documentation

### Endpoint: Raise Dispute

**URL**: `POST http://localhost:9090/api/dispute/raise`

**Request Body**:
```json
{
  "amount": 25000,
  "location": "USA",
  "description": "not done by me"
}
```

**Response**:
```json
{
  "intent": "FRAUD",
  "riskScore": 90,
  "decision": "AUTO_REFUND & BLOCK_CARD"
}
```

## 🧪 Test Scenarios

### Scenario 1: High Risk - Fraudulent Transaction
```json
{
  "amount": 25000,
  "location": "USA",
  "description": "not done by me"
}
```
**Expected**: Risk Score = 90, Decision = AUTO_REFUND & BLOCK_CARD

### Scenario 2: Low Risk - Legitimate Transaction
```json
{
  "amount": 5000,
  "location": "INDIA",
  "description": "Product quality issue"
}
```
**Expected**: Risk Score = 0, Decision = REJECT

### Scenario 3: Medium Risk - Human Review Required
```json
{
  "amount": 15000,
  "location": "INDIA",
  "description": "Item not received"
}
```
**Expected**: Risk Score = 40, Decision = REJECT

### Scenario 4: Medium-High Risk - Human Review
```json
{
  "amount": 8000,
  "location": "USA",
  "description": "Unauthorized charge"
}
```
**Expected**: Risk Score = 50, Decision = HUMAN_REVIEW

## 🎨 Features

### Backend Features
- ✅ RESTful API with Spring Boot
- ✅ CORS enabled for frontend communication
- ✅ AI-based risk scoring algorithm
- ✅ Fraud intent detection
- ✅ Automated decision making
- ✅ Clean architecture with DTOs

### Frontend Features
- ✅ Modern, responsive UI
- ✅ Real-time form validation
- ✅ Loading state with spinner
- ✅ Animated result cards
- ✅ Color-coded risk levels
- ✅ Visual risk score bar
- ✅ Error handling
- ✅ Mobile-friendly design

## 🛠️ Development

### Backend Development
```bash
cd backend
mvn spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm start
```

### Build for Production

**Backend**:
```bash
cd backend
mvn clean package
java -jar target/dispute-ai-1.0.0.jar
```

**Frontend**:
```bash
cd frontend
npm run build
```

## 🔧 Configuration

### Backend Configuration
File: `backend/src/main/resources/application.properties`
```properties
server.port=9090
spring.application.name=dispute-ai
```

### Frontend Configuration
API endpoint is configured in `App.js`:
```javascript
const response = await axios.post('http://localhost:9090/api/dispute/raise', {...});
```

## 📊 System Flow

1. User fills dispute form (amount, location, description)
2. Frontend sends POST request to backend
3. Backend applies AI rules:
   - Calculates risk score
   - Determines fraud intent
   - Makes automated decision
4. Backend returns response
5. Frontend displays results with visual indicators

## 🎯 Key Highlights

- **No Database Required**: Uses in-memory logic for simplicity
- **AI-Powered**: Intelligent rule-based decision making
- **Real-time Analysis**: Instant dispute resolution
- **User-Friendly**: Clean, intuitive interface
- **Scalable Architecture**: Easy to extend with more rules
- **Production Ready**: Complete error handling and validation

## 📄 License

This project is created for educational and demonstration purposes.

## 👨‍💻 Author

Built with ❤️ using Spring Boot and React

---

**Note**: Make sure both backend and frontend are running simultaneously for the application to work properly.
```

Happy Coding! 🚀
