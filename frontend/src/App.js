import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [formData, setFormData] = useState({
    amount: '',
    transactionLocation: '',
    userCurrentLocation: '',
    description: '',
    userId: '',
    transactionType: 'ONLINE',
    websiteUrl: '',
    merchantName: '',
    transactionDateTime: ''
  });
  const [transactionDocuments, setTransactionDocuments] = useState([]);
  const [userIdDocument, setUserIdDocument] = useState(null);
  const [idValidationResult, setIdValidationResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [useAgenticMode, setUseAgenticMode] = useState(true);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFileChange = (e, type) => {
    const files = Array.from(e.target.files);
    if (type === 'transaction') {
      setTransactionDocuments(files);
    } else if (type === 'userId') {
      setUserIdDocument(files[0]);
      // Validate ID when uploaded
      if (files[0]) {
        validateUserId(files[0]);
      }
    }
  };

  const validateUserId = async (file) => {
    // Check if userId is provided
    if (!formData.userId || formData.userId.trim() === '') {
      setIdValidationResult({
        valid: false,
        message: '⚠️ Please enter your UserID first before uploading ID document.'
      });
      return;
    }
    
    const validationFormData = new FormData();
    validationFormData.append('idDocument', file);
    validationFormData.append('userId', formData.userId);
    
    try {
      const response = await axios.post('http://localhost:9090/api/dispute/validate-id', validationFormData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      setIdValidationResult(response.data);
    } catch (err) {
      setIdValidationResult({
        valid: false,
        message: 'Failed to validate ID. Please try again or visit branch office.'
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Use file upload endpoint if files are present, otherwise use regular endpoint
      const hasFiles = transactionDocuments.length > 0 || userIdDocument !== null;
      
      if (hasFiles) {
        // Use multipart/form-data endpoint
        const submitData = new FormData();
        submitData.append('amount', parseFloat(formData.amount));
        submitData.append('transactionLocation', formData.transactionLocation);
        submitData.append('userCurrentLocation', formData.userCurrentLocation);
        submitData.append('description', formData.description);
        submitData.append('userId', formData.userId || 'GUEST_USER');
        submitData.append('transactionType', formData.transactionType);
        submitData.append('websiteUrl', formData.websiteUrl || '');
        submitData.append('merchantName', formData.merchantName || '');
        submitData.append('transactionDateTime', formData.transactionDateTime || '');
        submitData.append('useAgenticMode', useAgenticMode);
        
        // Add transaction documents
        transactionDocuments.forEach((file) => {
          submitData.append('transactionDocuments', file);
        });
        
        // Add user ID document
        if (userIdDocument) {
          submitData.append('userIdDocument', userIdDocument);
        }
        
        const response = await axios.post('http://localhost:9090/api/dispute/raise-with-files', submitData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        setResult(response.data);
      } else {
        // Use regular JSON endpoint
        const endpoint = useAgenticMode ? '/api/dispute/raise-agentic' : '/api/dispute/raise';
        const response = await axios.post(`http://localhost:9090${endpoint}`, {
          amount: parseFloat(formData.amount),
          transactionLocation: formData.transactionLocation,
          userCurrentLocation: formData.userCurrentLocation,
          description: formData.description,
          userId: formData.userId || 'GUEST_USER',
          transactionType: formData.transactionType,
          websiteUrl: formData.websiteUrl,
          merchantName: formData.merchantName,
          transactionDateTime: formData.transactionDateTime
        });
        setResult(response.data);
      }
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
      description: '',
      userId: '',
      transactionType: 'ONLINE',
      websiteUrl: '',
      merchantName: '',
      transactionDateTime: ''
    });
    setTransactionDocuments([]);
    setUserIdDocument(null);
    setIdValidationResult(null);
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
          
          <div className="mode-toggle">
            <label className="toggle-label">
              <input
                type="checkbox"
                checked={useAgenticMode}
                onChange={(e) => setUseAgenticMode(e.target.checked)}
              />
              <span className="toggle-text">
                {useAgenticMode ? '🤖 Agentic AI Mode (Multi-Agent)' : '📊 Rule-Based Mode'}
              </span>
            </label>
            <p className="mode-description">
              {useAgenticMode
                ? 'Uses Intent, Context, and Decision agents for comprehensive analysis'
                : 'Uses traditional rule-based analysis'}
            </p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="userId">
                  User ID (Required)
                  <span className="field-hint">(e.g., ABC001, ABC002, ABC003)</span>
                </label>
                <input
                  type="text"
                  id="userId"
                  name="userId"
                  value={formData.userId}
                  onChange={handleChange}
                  placeholder="e.g., ABC003"
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="transactionType">Transaction Type</label>
                <select
                  id="transactionType"
                  name="transactionType"
                  value={formData.transactionType}
                  onChange={handleChange}
                  required
                >
                  <option value="ONLINE">Online</option>
                  <option value="ATM">ATM</option>
                  <option value="MERCHANT">Merchant</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
            </div>

            <div className="form-row">
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
                <label htmlFor="transactionDateTime">Transaction Date & Time</label>
                <input
                  type="datetime-local"
                  id="transactionDateTime"
                  name="transactionDateTime"
                  value={formData.transactionDateTime}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            {formData.transactionType === 'ONLINE' && (
              <div className="form-group">
                <label htmlFor="websiteUrl">Website URL (Optional)</label>
                <input
                  type="text"
                  id="websiteUrl"
                  name="websiteUrl"
                  value={formData.websiteUrl}
                  onChange={handleChange}
                  placeholder="e.g., www.example.com"
                />
              </div>
            )}

            {formData.transactionType === 'MERCHANT' && (
              <div className="form-group">
                <label htmlFor="merchantName">Merchant Name</label>
                <input
                  type="text"
                  id="merchantName"
                  name="merchantName"
                  value={formData.merchantName}
                  onChange={handleChange}
                  placeholder="Enter merchant name"
                  required
                />
              </div>
            )}

            <div className="form-row">
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

            <div className="form-group">
              <label htmlFor="transactionDocuments">
                📄 Transaction Supporting Documents
                <span className="field-hint">(Upload receipts, screenshots, etc.)</span>
              </label>
              <input
                type="file"
                id="transactionDocuments"
                name="transactionDocuments"
                onChange={(e) => handleFileChange(e, 'transaction')}
                multiple
                accept="image/*,.pdf"
                className="file-input"
              />
              {transactionDocuments.length > 0 && (
                <div className="file-list">
                  <p>Selected files: {transactionDocuments.map(f => f.name).join(', ')}</p>
                </div>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="userIdDocument">
                🆔 User Identification Document
                <span className="field-hint">(Upload government ID for verification)</span>
              </label>
              <input
                type="file"
                id="userIdDocument"
                name="userIdDocument"
                onChange={(e) => handleFileChange(e, 'userId')}
                accept="image/*,.pdf"
                className="file-input"
                required
              />
              {userIdDocument && (
                <div className="file-list">
                  <p>Selected: {userIdDocument.name}</p>
                </div>
              )}
              {idValidationResult && (
                <div className={`validation-result ${idValidationResult.valid ? 'valid' : 'invalid'}`}>
                  <p>
                    {idValidationResult.valid ? '✓ ' : '⚠️ '}
                    {idValidationResult.message}
                  </p>
                  {!idValidationResult.valid && (
                    <p className="validation-warning">
                      Please upload a correct ID or visit the Dispute Admin desk at your nearest branch office.
                    </p>
                  )}
                </div>
              )}
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
          <>
            {/* Agent Flow Visualization - Only shown in Agentic Mode */}
            {useAgenticMode && result.agentFlow && (
              <div className="agent-flow-container">
                <h2>🤖 Multi-Agent Analysis Flow</h2>
                <div className="agent-flow">
                  <div className="agent-step">
                    <div className="agent-icon">🎯</div>
                    <div className="agent-content">
                      <h3>Intent Agent</h3>
                      <p className="agent-status">{result.agentFlow.intentAgent}</p>
                      <div className="agent-result">{result.agentFlow.intentResult}</div>
                    </div>
                  </div>
                  
                  <div className="flow-arrow">→</div>
                  
                  <div className="agent-step">
                    <div className="agent-icon">🔍</div>
                    <div className="agent-content">
                      <h3>Context Agent</h3>
                      <p className="agent-status">{result.agentFlow.contextAgent}</p>
                      <div className="agent-result">{result.agentFlow.contextResult}</div>
                    </div>
                  </div>
                  
                  <div className="flow-arrow">→</div>
                  
                  <div className="agent-step">
                    <div className="agent-icon">⚖️</div>
                    <div className="agent-content">
                      <h3>Decision Agent</h3>
                      <p className="agent-status">{result.agentFlow.decisionAgent}</p>
                      <div className="agent-result">{result.agentFlow.decisionResult}</div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            <div className="result-card">
              <h2>📊 Dispute Analysis Result</h2>
              {result.userVerified !== undefined && (
                <div className={`verification-badge ${result.userVerified ? 'verified' : 'unverified'}`}>
                  {result.userVerified ? '✓ User Verified' : '⚠️ User Not Verified'}
                </div>
              )}
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
                
                {result.verificationMessage && (
                  <div className="result-item full-width verification-message">
                    <p>{result.verificationMessage}</p>
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
          </>
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
