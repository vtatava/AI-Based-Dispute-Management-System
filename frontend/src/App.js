import React, { useState } from 'react';
import axios from 'axios';
import { motion } from 'framer-motion';
import { 
  FaDollarSign, 
  FaMapMarkerAlt, 
  FaCalendarAlt, 
  FaUser, 
  FaGlobe,
  FaSpinner
} from 'react-icons/fa';
import FormCard from './components/FormCard';
import InputField from './components/InputField';
import AIFlowStepper from './components/AIFlowStepper';
import ResultCard from './components/ResultCard';
import UploadBox from './components/UploadBox';

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
      if (files[0]) {
        validateUserId(files[0]);
      }
    }
  };

  const validateUserId = async (file) => {
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
      const hasFiles = transactionDocuments.length > 0 || userIdDocument !== null;
      
      if (hasFiles) {
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
        
        transactionDocuments.forEach((file) => {
          submitData.append('transactionDocuments', file);
        });
        
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
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 relative overflow-hidden">
      {/* Animated background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-0 left-1/4 w-96 h-96 bg-cyan-500/20 rounded-full blur-3xl animate-float"></div>
        <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-violet-500/20 rounded-full blur-3xl animate-float" style={{ animationDelay: '1s' }}></div>
        <div className="absolute top-1/2 left-1/2 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl animate-float" style={{ animationDelay: '2s' }}></div>
      </div>

      <div className="relative z-10 container mx-auto px-4 py-8 max-w-7xl">
        {/* Header */}
        <motion.header
          initial={{ opacity: 0, y: -50 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="text-center mb-12"
        >
          <h1 className="text-5xl md:text-6xl font-bold text-white mb-4 bg-clip-text text-transparent bg-gradient-to-r from-cyan-400 via-violet-400 to-purple-400">
            🏦 AI Dispute Management
          </h1>
          <p className="text-xl text-gray-300">
            Intelligent fraud detection powered by <span className="font-semibold text-cyan-400">IBM Agentic-AI</span>
          </p>
        </motion.header>

        {/* Mode Toggle */}
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="max-w-2xl mx-auto mb-8"
        >
          <div className="bg-white/10 backdrop-blur-xl rounded-xl p-4 border border-white/20">
            <label className="flex items-center justify-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                checked={useAgenticMode}
                onChange={(e) => setUseAgenticMode(e.target.checked)}
                className="w-5 h-5 rounded accent-cyan-500"
              />
              <span className="text-white font-semibold text-lg">
                {useAgenticMode ? '🤖 Multi-Agent AI Mode' : '📊 Rule-Based Mode'}
              </span>
            </label>
            <p className="text-gray-400 text-sm text-center mt-2">
              {useAgenticMode
                ? 'Advanced analysis using Intent, Context, and Decision agents'
                : 'Traditional rule-based analysis'}
            </p>
          </div>
        </motion.div>

        {/* Main Content - Centered Form */}
        <div className="max-w-4xl mx-auto mb-8">
          {/* Dispute Form */}
          <FormCard title="🎯 Raise a Dispute" subtitle="Fill in the details below to start your AI-powered dispute analysis">
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* User ID */}
              <InputField
                label="User ID"
                icon={FaUser}
                name="userId"
                value={formData.userId}
                onChange={handleChange}
                placeholder="e.g., ABC003"
                hint="Your unique user identifier"
                required
              />

              {/* Transaction Type */}
              <div className="mb-4">
                <label className="block text-sm font-semibold text-gray-200 mb-2">
                  Transaction Type <span className="text-red-400">*</span>
                </label>
                <select
                  name="transactionType"
                  value={formData.transactionType}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:border-transparent transition-all duration-300 backdrop-blur-sm hover:bg-white/10"
                >
                  <option value="ONLINE" className="bg-slate-800">Online</option>
                  <option value="ATM" className="bg-slate-800">ATM</option>
                  <option value="MERCHANT" className="bg-slate-800">Merchant</option>
                  <option value="OTHER" className="bg-slate-800">Other</option>
                </select>
              </div>

              {/* Amount and Date */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <InputField
                  label="Amount"
                  icon={FaDollarSign}
                  type="number"
                  name="amount"
                  value={formData.amount}
                  onChange={handleChange}
                  placeholder="25000"
                  hint="Transaction amount in ₹"
                  required
                  min="0"
                  step="0.01"
                />
                <InputField
                  label="Date & Time"
                  icon={FaCalendarAlt}
                  type="datetime-local"
                  name="transactionDateTime"
                  value={formData.transactionDateTime}
                  onChange={handleChange}
                  required
                />
              </div>

              {/* Conditional Fields */}
              {formData.transactionType === 'ONLINE' && (
                <InputField
                  label="Website URL"
                  icon={FaGlobe}
                  name="websiteUrl"
                  value={formData.websiteUrl}
                  onChange={handleChange}
                  placeholder="www.example.com"
                  hint="Optional"
                />
              )}

              {formData.transactionType === 'MERCHANT' && (
                <InputField
                  label="Merchant Name"
                  name="merchantName"
                  value={formData.merchantName}
                  onChange={handleChange}
                  placeholder="Enter merchant name"
                  required
                />
              )}

              {/* Locations */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <InputField
                  label="Transaction Location"
                  icon={FaMapMarkerAlt}
                  name="transactionLocation"
                  value={formData.transactionLocation}
                  onChange={handleChange}
                  placeholder="USA, UK, INDIA"
                  hint="Where transaction occurred"
                  required
                />
                <InputField
                  label="Your Current Location"
                  icon={FaMapMarkerAlt}
                  name="userCurrentLocation"
                  value={formData.userCurrentLocation}
                  onChange={handleChange}
                  placeholder="INDIA, USA, UK"
                  hint="Where you are now"
                  required
                />
              </div>

              {/* Description */}
              <div className="mb-4">
                <label className="block text-sm font-semibold text-gray-200 mb-2">
                  Description <span className="text-red-400">*</span>
                </label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  placeholder="Describe the issue (e.g., 'not done by me', 'fraud transaction')"
                  required
                  rows="4"
                  className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:border-transparent transition-all duration-300 backdrop-blur-sm hover:bg-white/10 resize-none"
                />
              </div>

              {/* File Uploads */}
              <UploadBox
                label="Transaction Documents"
                hint="Upload receipts, screenshots, etc."
                name="transactionDocuments"
                onChange={(e) => handleFileChange(e, 'transaction')}
                accept="image/*,.pdf"
                multiple
                files={transactionDocuments}
              />

              <UploadBox
                label="User ID Document"
                hint="Upload government ID for verification"
                name="userIdDocument"
                onChange={(e) => handleFileChange(e, 'userId')}
                accept="image/*,.pdf"
                required
                files={userIdDocument}
              />

              {/* ID Validation Result */}
              {idValidationResult && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className={`p-4 rounded-xl border ${
                    idValidationResult.valid
                      ? 'bg-green-500/10 border-green-500/30 text-green-300'
                      : 'bg-red-500/10 border-red-500/30 text-red-300'
                  }`}
                >
                  <p className="font-semibold">
                    {idValidationResult.valid ? '✓ ' : '⚠️ '}
                    {idValidationResult.message}
                  </p>
                  {!idValidationResult.valid && (
                    <p className="text-sm mt-2 text-red-400">
                      Please upload a correct ID or visit the Dispute Admin desk at your nearest branch office.
                    </p>
                  )}
                </motion.div>
              )}

              {/* Buttons */}
              <div className="flex gap-4 pt-4">
                <motion.button
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  type="submit"
                  disabled={loading}
                  className="flex-1 bg-gradient-to-r from-cyan-500 to-violet-500 text-white font-bold py-4 px-6 rounded-xl shadow-lg hover:shadow-cyan-500/50 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                >
                  {loading ? (
                    <>
                      <FaSpinner className="animate-spin" />
                      Analyzing...
                    </>
                  ) : (
                    'Submit Dispute'
                  )}
                </motion.button>
                <motion.button
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  type="button"
                  onClick={handleReset}
                  className="px-6 py-4 bg-white/10 hover:bg-white/20 text-white font-semibold rounded-xl border border-white/20 transition-all duration-300"
                >
                  Reset
                </motion.button>
              </div>
            </form>
          </FormCard>
        </div>

        {/* Loading State */}
        {loading && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="max-w-4xl mx-auto bg-white/10 backdrop-blur-xl rounded-2xl p-12 border border-white/20 flex flex-col items-center justify-center mb-8"
          >
            <FaSpinner className="text-cyan-400 text-6xl animate-spin mb-6" />
            <h3 className="text-2xl font-bold text-white mb-2">AI Analysis in Progress</h3>
            <p className="text-gray-300 text-center">
              Our IBM Agentic-AI system is analyzing your dispute...
            </p>
          </motion.div>
        )}

        {/* Error Display */}
        {error && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-red-500/10 backdrop-blur-xl rounded-2xl p-6 border border-red-500/30 mb-8"
          >
            <h3 className="text-red-300 font-bold text-xl mb-2 flex items-center gap-2">
              ❌ Error
            </h3>
            <p className="text-red-200">{error}</p>
          </motion.div>
        )}

        {/* AI Flow Stepper */}
        {result && useAgenticMode && result.agentFlow && (
          <AIFlowStepper agentFlow={result.agentFlow} loading={loading} />
        )}

        {/* Result Card */}
        {result && !loading && (
          <ResultCard result={result} />
        )}
      </div>
    </div>
  );
}

export default App;

// Made with Bob
