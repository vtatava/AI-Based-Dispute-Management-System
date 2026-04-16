import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [formData, setFormData] = useState({
    amount: '',
    transactionLocation: '',
    userCurrentLocation: '',
    description: ''
  });
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await axios.post('http://localhost:9090/api/dispute/raise', {
        amount: parseFloat(formData.amount),
        transactionLocation: formData.transactionLocation,
        userCurrentLocation: formData.userCurrentLocation,
        description: formData.description
      });
      setResult(response.data);
    } catch (err) {
      setError('Failed to process dispute. Please ensure backend is running.');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setFormData({
      amount: '',
      transactionLocation: '',
      userCurrentLocation: '',
      description: ''
    });
    setResult(null);
    setError(null);
  };

  return (
    <div className="App">
      <div className="container">
        <header className="header">
          <h1>🏦 AI-Based Dispute Management System</h1>
          <p>Intelligent fraud detection and dispute resolution</p>
        </header>

        <div className="form-container">
          <h2>Raise a Dispute</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="amount">Transaction Amount ($)</label>
              <input
                type="number"
                id="amount"
                name="amount"
                value={formData.amount}
                onChange={handleChange}
                placeholder="Enter amount (e.g., 25000)"
                required
                min="0"
                step="0.01"
              />
            </div>

            <div className="form-group">
              <label htmlFor="transactionLocation">
                🌍 Transaction Location
                <span className="field-hint">(Where the transaction occurred)</span>
              </label>
              <input
                type="text"
                id="transactionLocation"
                name="transactionLocation"
                value={formData.transactionLocation}
                onChange={handleChange}
                placeholder="e.g., USA, UK, CHINA, INDIA"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="userCurrentLocation">
                📍 Your Current Location
                <span className="field-hint">(Where you are right now)</span>
              </label>
              <input
                type="text"
                id="userCurrentLocation"
                name="userCurrentLocation"
                value={formData.userCurrentLocation}
                onChange={handleChange}
                placeholder="e.g., INDIA, USA, UK"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="description">Description</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Describe the issue (e.g., 'not done by me', 'fraud transaction')"
                required
                rows="4"
              />
            </div>

            <div className="button-group">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Analyzing...' : 'Submit Dispute'}
              </button>
              <button type="button" className="btn btn-secondary" onClick={handleReset}>
                Reset
              </button>
            </div>
          </form>
        </div>

        {loading && (
          <div className="loading-container">
            <div className="spinner"></div>
            <p>🤖 AI is analyzing your dispute...</p>
          </div>
        )}

        {error && (
          <div className="result-card error-card">
            <h3>❌ Error</h3>
            <p>{error}</p>
          </div>
        )}

        {result && !loading && (
          <div className="result-card">
            <h2>📊 Dispute Analysis Result</h2>
            <div className="result-grid">
              <div className="result-item">
                <span className="result-label">Risk Score:</span>
                <span className={`result-value risk-${getRiskLevel(result.riskScore)}`}>
                  {result.riskScore}/100
                </span>
              </div>
              <div className="result-item full-width">
                <span className="result-label">Decision:</span>
                <span className={`result-value decision-${getDecisionClass(result.decision)}`}>
                  {result.decision}
                </span>
              </div>
              
              {result.refundAmount !== null && result.refundAmount !== undefined && (
                <div className="result-item full-width refund-highlight">
                  <span className="result-label">💰 Refund Amount:</span>
                  <span className={`result-value refund-amount ${result.refundAmount > 0 ? 'refund-approved' : 'refund-denied'}`}>
                    {result.refundAmount > 0 ? `₹${result.refundAmount.toFixed(2)}` : 'No Refund'}
                  </span>
                </div>
              )}
              
              {result.reviewReason && (
                <div className="result-item full-width review-reason">
                  <span className="result-label">📋 Analysis:</span>
                  <p className="review-text">{result.reviewReason}</p>
                </div>
              )}
              
              {result.decision === 'MANUAL_REVIEW' && (
                <div className="result-item full-width human-review-notice">
                  <div className="notice-box">
                    <h3>⚠️ Human Review Required</h3>
                    <p>This dispute may need a Human Review. Please contact our Dispute Resolution Help Desk:</p>
                    <div className="contact-info">
                      <p>📞 <strong>Phone:</strong> 18000-000-000</p>
                      <p>📧 <strong>Email:</strong> disputehelp247@xyz.com</p>
                    </div>
                  </div>
                </div>
              )}
            </div>
            <div className="risk-bar">
              <div
                className={`risk-fill risk-${getRiskLevel(result.riskScore)}`}
                style={{ width: `${result.riskScore}%` }}
              ></div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

function getRiskLevel(score) {
  if (score >= 80) return 'high';
  if (score >= 50) return 'medium';
  return 'low';
}

function getDecisionClass(decision) {
  if (decision.includes('AUTO_REFUND')) return 'approved';
  if (decision.includes('REJECT')) return 'rejected';
  return 'review';
}

export default App;

// Made with Bob
