import React, { useEffect, useMemo, useRef, useState } from 'react';
import axios from 'axios';
import { motion } from 'framer-motion';
import {
  FaCalendarAlt,
  FaCheckCircle,
  FaDollarSign,
  FaGlobe,
  FaMapMarkerAlt,
  FaMicrophone,
  FaSpinner,
  FaStop,
  FaUser
} from 'react-icons/fa';
import FormCard from './components/FormCard';
import InputField from './components/InputField';
import AIFlowStepper from './components/AIFlowStepper';
import ResultCard from './components/ResultCard';
import UploadBox from './components/UploadBox';

function App() {
  const recognitionRef = useRef(null);
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
  const [transactionReceipt, setTransactionReceipt] = useState(null);
  const [receiptValidationResult, setReceiptValidationResult] = useState(null);
  const [userIdDocument, setUserIdDocument] = useState(null);
  const [idValidationResult, setIdValidationResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [useAgenticMode, setUseAgenticMode] = useState(true);
  const [highContrast, setHighContrast] = useState(false);
  const [largeText, setLargeText] = useState(false);
  const [speechSupported, setSpeechSupported] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [liveMessage, setLiveMessage] = useState('Accessibility enhancements ready.');
  const [assertiveMessage, setAssertiveMessage] = useState('');

  const descriptionHintId = 'description-hint';
  const descriptionFieldHelpId = 'description-help';
  const transactionTypeHintId = 'transaction-type-hint';
  const userIdValidationMessageId = 'user-id-validation-message';
  const speechStatusId = 'speech-status';

  const pageClassName = useMemo(() => {
    const classes = ['min-h-screen', 'bg-gradient-to-br', 'from-slate-900', 'via-purple-900', 'to-slate-900', 'relative', 'overflow-hidden'];
    if (highContrast) classes.push('accessibility-high-contrast');
    if (largeText) classes.push('accessibility-large-text');
    return classes.join(' ');
  }, [highContrast, largeText]);

  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

    if (!SpeechRecognition) {
      setSpeechSupported(false);
      return;
    }

    setSpeechSupported(true);
    const recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.lang = 'en-US';

    recognition.onstart = () => {
      setIsListening(true);
      setLiveMessage('Voice input started. Speak your dispute description.');
    };

    recognition.onresult = (event) => {
      let finalTranscript = '';

      for (let index = event.resultIndex; index < event.results.length; index += 1) {
        const transcript = event.results[index][0].transcript;
        if (event.results[index].isFinal) {
          finalTranscript += `${transcript} `;
        }
      }

      if (finalTranscript.trim()) {
        setFormData((prev) => ({
          ...prev,
          description: prev.description
            ? `${prev.description.trim()} ${finalTranscript.trim()}`.trim()
            : finalTranscript.trim()
        }));
        setLiveMessage(`Voice text captured: ${finalTranscript.trim()}`);
      }
    };

    recognition.onerror = (event) => {
      setIsListening(false);
      setAssertiveMessage(`Speech recognition error: ${event.error}.`);
    };

    recognition.onend = () => {
      setIsListening(false);
      setLiveMessage('Voice input stopped.');
    };

    recognitionRef.current = recognition;

    return () => {
      if (recognitionRef.current) {
        recognitionRef.current.onstart = null;
        recognitionRef.current.onresult = null;
        recognitionRef.current.onerror = null;
        recognitionRef.current.onend = null;
        recognitionRef.current.stop();
      }
    };
  }, []);

  useEffect(() => {
    if (loading) {
      setLiveMessage('AI analysis in progress. Please wait.');
    }
  }, [loading]);

  useEffect(() => {
    if (result && !loading) {
      setLiveMessage(`Analysis complete. Decision: ${result.decision || 'available'}.`);
    }
  }, [result, loading]);

  useEffect(() => {
    if (error) {
      setAssertiveMessage(error);
    }
  }, [error]);

  useEffect(() => {
    if (idValidationResult?.message) {
      setLiveMessage(idValidationResult.message);
    }
  }, [idValidationResult]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFileChange = (e, type) => {
    const files = Array.from(e.target.files);
    if (type === 'receipt') {
      setTransactionReceipt(files[0]);
      if (files[0]) {
        setLiveMessage(`Transaction proof selected: ${files[0].name}. Validating receipt...`);
        validateReceipt(files[0]);
      } else {
        setLiveMessage('Transaction proof cleared.');
        setReceiptValidationResult(null);
      }
    } else if (type === 'userId') {
      setUserIdDocument(files[0]);
      if (files[0]) {
        setLiveMessage(`User ID document selected: ${files[0].name}. Validating document.`);
        validateUserId(files[0]);
      } else {
        setLiveMessage('User ID document cleared.');
        setIdValidationResult(null);
      }
    }
  };

  const validateReceipt = async (file) => {
    // Check if required fields are filled
    if (!formData.userId || !formData.amount || !formData.transactionDateTime) {
      const validationFailure = {
        valid: false,
        pending: true,
        message: '⚠️ Please fill in User ID, Amount, and Transaction Date/Time before uploading receipt.'
      };
      setReceiptValidationResult(validationFailure);
      setAssertiveMessage(validationFailure.message);
      return;
    }

    // Set validating state
    setReceiptValidationResult({
      validating: true,
      message: '🔄 Validating receipt against your form data...'
    });

    const validationFormData = new FormData();
    validationFormData.append('transactionReceipt', file);
    validationFormData.append('userId', formData.userId);
    validationFormData.append('amount', formData.amount);
    validationFormData.append('transactionDateTime', formData.transactionDateTime);
    validationFormData.append('transactionLocation', formData.transactionLocation || '');
    validationFormData.append('transactionType', formData.transactionType);
    validationFormData.append('websiteUrl', formData.websiteUrl || '');
    validationFormData.append('merchantName', formData.merchantName || '');

    try {
      const response = await axios.post('http://localhost:9090/api/dispute/validate-receipt', validationFormData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      setReceiptValidationResult(response.data);
      
      if (response.data.valid) {
        setLiveMessage('✅ Receipt validation successful! All data matches.');
      } else {
        setAssertiveMessage('❌ Receipt validation failed: ' + response.data.message);
      }
    } catch (err) {
      const validationFailure = {
        valid: false,
        message: '❌ Failed to validate receipt. Please try again or contact support.'
      };
      setReceiptValidationResult(validationFailure);
      setAssertiveMessage(validationFailure.message);
    }
  };

  const validateUserId = async (file) => {
    if (!formData.userId || formData.userId.trim() === '') {
      const validationFailure = {
        valid: false,
        message: 'Please enter your User ID first before uploading an ID document.'
      };
      setIdValidationResult(validationFailure);
      setAssertiveMessage(validationFailure.message);
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
      const validationFailure = {
        valid: false,
        message: 'Failed to validate ID. Please try again or visit branch office.'
      };
      setIdValidationResult(validationFailure);
      setAssertiveMessage(validationFailure.message);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);
    setAssertiveMessage('');
    setLiveMessage('Submitting your dispute for AI analysis.');

    try {
      const hasFiles = userIdDocument !== null || transactionReceipt !== null;

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

        if (transactionReceipt) {
          submitData.append('transactionReceipt', transactionReceipt);
        }

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

  const startVoiceInput = () => {
    if (!speechSupported || !recognitionRef.current) {
      setAssertiveMessage('Speech recognition is not supported in this browser.');
      return;
    }

    setAssertiveMessage('');
    recognitionRef.current.start();
  };

  const stopVoiceInput = () => {
    if (recognitionRef.current) {
      recognitionRef.current.stop();
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
    setTransactionReceipt(null);
    setReceiptValidationResult(null);
    setUserIdDocument(null);
    setIdValidationResult(null);
    setResult(null);
    setError(null);
    setAssertiveMessage('');
    if (recognitionRef.current) {
      recognitionRef.current.stop();
    }
    setLiveMessage('Form reset. All fields cleared.');
  };

  // Check if submit button should be disabled
  const isSubmitDisabled = () => {
    // If loading, disable
    if (loading) return true;
    
    // If user uploaded ID document but validation hasn't completed or failed
    if (userIdDocument && (!idValidationResult || !idValidationResult.valid)) {
      return true;
    }
    
    // Transaction proof is mandatory
    if (!transactionReceipt) {
      return true;
    }

    // If validation is pending (waiting for required fields)
    if (receiptValidationResult?.pending) {
      return true;
    }
    // If validation is in progress
    if (receiptValidationResult?.validating) {
      return true;
    }
    // If validation failed
    if (receiptValidationResult && !receiptValidationResult.valid) {
      return true;
    }
    // If no validation result yet
    if (!receiptValidationResult) {
      return true;
    }
    
    return false;
  };

  return (
    <div className={pageClassName}>
      <a
        href="#main-content"
        className="skip-link"
      >
        Skip to main content
      </a>

      <div className="sr-only" aria-live="polite" aria-atomic="true">
        {liveMessage}
      </div>
      <div className="sr-only" aria-live="assertive" aria-atomic="true">
        {assertiveMessage}
      </div>

      <div className="absolute inset-0 overflow-hidden pointer-events-none" aria-hidden="true">
        <div className="absolute top-0 left-1/4 w-96 h-96 bg-cyan-500/20 rounded-full blur-3xl animate-float"></div>
        <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-violet-500/20 rounded-full blur-3xl animate-float" style={{ animationDelay: '1s' }}></div>
        <div className="absolute top-1/2 left-1/2 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl animate-float" style={{ animationDelay: '2s' }}></div>
      </div>

      <div className="relative z-10 container mx-auto px-4 py-8 max-w-7xl">
        <motion.header
          initial={{ opacity: 0, y: -50 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="text-center mb-12"
        >
          <h1 className="text-5xl md:text-6xl font-bold text-white mb-4 bg-clip-text text-transparent bg-gradient-to-r from-cyan-400 via-violet-400 to-purple-400">
            AI Dispute Management
          </h1>
          <p className="text-xl text-gray-200">
            Accessible fraud dispute filing powered by <span className="font-semibold text-cyan-300">IBM Agentic-AI</span>
          </p>
          <p className="text-base text-gray-300 mt-3">
            Use keyboard navigation, high contrast mode, and large text mode for a more accessible experience.
          </p>
        </motion.header>

        <motion.section
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="max-w-4xl mx-auto mb-8"
          aria-labelledby="accessibility-controls-title"
        >
          <div className="bg-white/10 backdrop-blur-xl rounded-xl p-4 border border-white/20">
            <h2 id="accessibility-controls-title" className="text-xl font-semibold text-white text-center mb-4">
              Accessibility and analysis controls
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <label className="flex items-center justify-center gap-3 cursor-pointer rounded-xl border border-white/15 bg-white/5 p-4">
                <input
                  type="checkbox"
                  checked={useAgenticMode}
                  onChange={(e) => setUseAgenticMode(e.target.checked)}
                  className="w-5 h-5 rounded accent-cyan-500"
                  aria-describedby="agentic-mode-help"
                />
                <span className="text-white font-semibold text-base">
                  {useAgenticMode ? 'Multi-Agent AI Mode' : 'Rule-Based Mode'}
                </span>
              </label>

              <label className="flex items-center justify-center gap-3 cursor-pointer rounded-xl border border-white/15 bg-white/5 p-4">
                <input
                  type="checkbox"
                  checked={highContrast}
                  onChange={(e) => {
                    setHighContrast(e.target.checked);
                    setLiveMessage(e.target.checked ? 'High contrast mode enabled.' : 'High contrast mode disabled.');
                  }}
                  className="w-5 h-5 rounded accent-cyan-500"
                />
                <span className="text-white font-semibold text-base">High contrast mode</span>
              </label>

              <label className="flex items-center justify-center gap-3 cursor-pointer rounded-xl border border-white/15 bg-white/5 p-4">
                <input
                  type="checkbox"
                  checked={largeText}
                  onChange={(e) => {
                    setLargeText(e.target.checked);
                    setLiveMessage(e.target.checked ? 'Large text mode enabled.' : 'Large text mode disabled.');
                  }}
                  className="w-5 h-5 rounded accent-cyan-500"
                />
                <span className="text-white font-semibold text-base">Large text mode</span>
              </label>
            </div>
            <p id="agentic-mode-help" className="text-gray-300 text-sm text-center mt-3">
              {useAgenticMode
                ? 'Advanced analysis using Intent, Context, and Decision agents.'
                : 'Traditional rule-based analysis.'}
            </p>
          </div>
        </motion.section>

        <main id="main-content" className="max-w-4xl mx-auto mb-8" tabIndex="-1">
          <FormCard
            title="Raise a dispute"
            subtitle="Fill in the details below to start your AI-powered dispute analysis."
          >
            <form onSubmit={handleSubmit} className="space-y-4" aria-labelledby="dispute-form-title">
              <h2 id="dispute-form-title" className="sr-only">Dispute filing form</h2>

              <InputField
                label="User ID"
                icon={FaUser}
                name="userId"
                value={formData.userId}
                onChange={handleChange}
                placeholder="e.g., ABC003"
                hint="Enter your unique user identifier. Example: ABC003."
                required
              />

              <div className="mb-4">
                <label htmlFor="transactionType" className="block text-sm font-semibold text-gray-200 mb-2">
                  Transaction Type
                  <span className="text-red-400 ml-1" aria-hidden="true">*</span>
                  <span className="sr-only">required</span>
                </label>
                <p id={transactionTypeHintId} className="text-xs text-gray-300 mb-2">
                  Select the type of transaction involved in the dispute.
                </p>
                <select
                  id="transactionType"
                  name="transactionType"
                  value={formData.transactionType}
                  onChange={handleChange}
                  required
                  aria-required="true"
                  aria-describedby={transactionTypeHintId}
                  className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-cyan-400 focus:border-cyan-300 transition-all duration-300 backdrop-blur-sm hover:bg-white/10"
                >
                  <option value="ONLINE" className="bg-slate-800">Online</option>
                  <option value="ATM" className="bg-slate-800">ATM</option>
                  <option value="MERCHANT" className="bg-slate-800">Merchant</option>
                  <option value="OTHER" className="bg-slate-800">Other</option>
                </select>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <InputField
                  label="Amount"
                  icon={FaDollarSign}
                  type="number"
                  name="amount"
                  value={formData.amount}
                  onChange={handleChange}
                  placeholder="25000"
                  hint="Transaction amount in Indian Rupees."
                  required
                  min="0"
                  step="0.01"
                />
                <InputField
                  label="Date and Time"
                  icon={FaCalendarAlt}
                  type="datetime-local"
                  name="transactionDateTime"
                  value={formData.transactionDateTime}
                  onChange={handleChange}
                  hint="Select the date and time of the transaction."
                  required
                />
              </div>

              {formData.transactionType === 'ONLINE' && (
                <InputField
                  label="Website URL"
                  icon={FaGlobe}
                  name="websiteUrl"
                  value={formData.websiteUrl}
                  onChange={handleChange}
                  placeholder="www.example.com"
                  hint="Optional. Add the website used for the transaction."
                />
              )}

              {formData.transactionType === 'MERCHANT' && (
                <InputField
                  label="Merchant Name"
                  name="merchantName"
                  value={formData.merchantName}
                  onChange={handleChange}
                  placeholder="Enter merchant name"
                  hint="Enter the merchant or business name."
                  required
                />
              )}

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <InputField
                  label="Transaction Location"
                  icon={FaMapMarkerAlt}
                  name="transactionLocation"
                  value={formData.transactionLocation}
                  onChange={handleChange}
                  placeholder="USA, UK, INDIA"
                  hint="Where the transaction occurred."
                  required
                />
                <InputField
                  label="Your Current Location"
                  icon={FaMapMarkerAlt}
                  name="userCurrentLocation"
                  value={formData.userCurrentLocation}
                  onChange={handleChange}
                  placeholder="INDIA, USA, UK"
                  hint="Where you are currently located."
                  required
                />
              </div>

              <div className="mb-4">
                <label htmlFor="description" className="block text-sm font-semibold text-gray-200 mb-2">
                  Description
                  <span className="text-red-400 ml-1" aria-hidden="true">*</span>
                  <span className="sr-only">required</span>
                </label>
                <p id={descriptionHintId} className="text-xs text-gray-300 mb-2">
                  Describe what happened, for example: “This transaction was not done by me.”
                </p>
                <p id={descriptionFieldHelpId} className="sr-only">
                  Provide enough details for the AI system to review the dispute.
                </p>
                <div className="flex flex-col sm:flex-row gap-3 mb-3">
                  <button
                    type="button"
                    onClick={isListening ? stopVoiceInput : startVoiceInput}
                    disabled={!speechSupported}
                    aria-describedby={speechStatusId}
                    aria-pressed={isListening}
                    className={`inline-flex items-center justify-center gap-2 px-4 py-3 rounded-xl font-semibold border transition-all duration-300 ${
                      isListening
                        ? 'bg-red-500/20 text-red-100 border-red-400'
                        : 'bg-cyan-500/20 text-cyan-100 border-cyan-400'
                    } ${!speechSupported ? 'opacity-50 cursor-not-allowed' : 'hover:bg-white/10'}`}
                  >
                    {isListening ? (
                      <>
                        <FaStop aria-hidden="true" />
                        Stop voice input
                      </>
                    ) : (
                      <>
                        <FaMicrophone aria-hidden="true" />
                        Start voice input
                      </>
                    )}
                  </button>
                  <div id={speechStatusId} className="text-sm text-gray-200 flex items-center">
                    {!speechSupported
                      ? 'Speech recognition is not supported in this browser. Use Chrome or Edge.'
                      : isListening
                        ? 'Listening now. Speak clearly to add text to the description.'
                        : 'Press the microphone button to dictate the dispute description.'}
                  </div>
                </div>
                <textarea
                  id="description"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  placeholder="Describe the issue, for example: not done by me or fraud transaction."
                  required
                  rows="4"
                  aria-required="true"
                  aria-describedby={`${descriptionHintId} ${descriptionFieldHelpId} ${speechStatusId}`}
                  className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-cyan-400 focus:border-cyan-300 transition-all duration-300 backdrop-blur-sm hover:bg-white/10 resize-none"
                />
              </div>

              <UploadBox
                label="User ID Document"
                hint="Upload a government ID for verification."
                name="userIdDocument"
                onChange={(e) => handleFileChange(e, 'userId')}
                accept="image/*,.pdf"
                required
                files={userIdDocument}
                ariaDescribedBy={idValidationResult ? userIdValidationMessageId : undefined}
              />

              {idValidationResult && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  id={userIdValidationMessageId}
                  role="status"
                  aria-live="polite"
                  className={`p-4 rounded-xl border ${
                    idValidationResult.valid
                      ? 'bg-green-500/10 border-green-500/30 text-green-200'
                      : 'bg-red-500/10 border-red-500/30 text-red-200'
                  }`}
                >
                  <p className="font-semibold flex items-start gap-2">
                    <FaCheckCircle className={`mt-1 flex-shrink-0 ${idValidationResult.valid ? 'text-green-300' : 'text-red-300'}`} aria-hidden="true" />
                    <span>{idValidationResult.message}</span>
                  </p>
                  {!idValidationResult.valid && (
                    <p className="text-sm mt-2 text-red-100">
                      Please upload a correct ID or visit the Dispute Admin desk at your nearest branch office.
                    </p>
                  )}
                </motion.div>
              )}

              <UploadBox
                label="Transaction Proofs"
                hint="Upload transaction proof for automatic validation and faster processing."
                name="transactionReceipt"
                onChange={(e) => handleFileChange(e, 'receipt')}
                accept=".txt,.pdf,image/*"
                required
                files={transactionReceipt}
                ariaDescribedBy={receiptValidationResult ? 'receipt-validation-message' : undefined}
              />

              {receiptValidationResult && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  id="receipt-validation-message"
                  role="status"
                  aria-live="polite"
                  className={`p-4 rounded-xl border ${
                    receiptValidationResult.valid
                      ? 'bg-green-500/10 border-green-500/30 text-green-200'
                      : receiptValidationResult.validating
                      ? 'bg-blue-500/10 border-blue-500/30 text-blue-200'
                      : 'bg-red-500/10 border-red-500/30 text-red-200'
                  }`}
                >
                  <p className="font-semibold flex items-start gap-2">
                    {receiptValidationResult.validating ? (
                      <FaSpinner className="mt-1 flex-shrink-0 text-blue-300 animate-spin" aria-hidden="true" />
                    ) : (
                      <FaCheckCircle className={`mt-1 flex-shrink-0 ${
                        receiptValidationResult.valid ? 'text-green-300' : 'text-red-300'
                      }`} aria-hidden="true" />
                    )}
                    <span>{receiptValidationResult.message}</span>
                  </p>
                  {receiptValidationResult.details && (
                    <p className="text-sm mt-2">
                      {receiptValidationResult.details}
                    </p>
                  )}
                  {receiptValidationResult.mismatches && receiptValidationResult.mismatches.length > 0 && (
                    <div className="mt-3 text-sm">
                      <p className="font-semibold mb-1">Mismatches Found:</p>
                      <ul className="list-disc list-inside space-y-1">
                        {receiptValidationResult.mismatches.map((mismatch, index) => (
                          <li key={index}>{mismatch}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </motion.div>
              )}

              <div className="flex flex-col sm:flex-row gap-4 pt-4">
                <div className="flex-1">
                  <motion.button
                    whileHover={{ scale: isSubmitDisabled() ? 1 : 1.02 }}
                    whileTap={{ scale: isSubmitDisabled() ? 1 : 0.98 }}
                    type="submit"
                    disabled={isSubmitDisabled()}
                    aria-busy={loading}
                    aria-describedby="submit-button-help"
                    className="w-full bg-gradient-to-r from-cyan-500 to-violet-500 text-white font-bold py-4 px-6 rounded-xl shadow-lg hover:shadow-cyan-500/50 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                  >
                    {loading ? (
                      <>
                        <FaSpinner className="animate-spin" aria-hidden="true" />
                        Analyzing dispute
                      </>
                    ) : (
                      'Submit Dispute'
                    )}
                  </motion.button>
                  {isSubmitDisabled() && !loading && (
                    <p id="submit-button-help" className="text-sm text-yellow-300 mt-2 text-center">
                      {userIdDocument && (!idValidationResult || !idValidationResult.valid)
                        ? '⚠️ Please wait for ID validation to complete or upload a valid ID document'
                        : !transactionReceipt
                        ? '⚠️ Please upload Transaction Proofs to continue'
                        : receiptValidationResult?.pending && (!formData.userId || !formData.transactionDateTime || !formData.amount)
                        ? '⚠️ Please fill in User ID, Date/Time, and Amount to validate the transaction proof'
                        : '⚠️ Please complete all validations before submitting'}
                    </p>
                  )}
                </div>
                <motion.button
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  type="button"
                  onClick={handleReset}
                  className="px-6 py-4 bg-white/10 hover:bg-white/20 text-white font-semibold rounded-xl border border-white/20 transition-all duration-300"
                >
                  Reset form
                </motion.button>
              </div>
            </form>
          </FormCard>
        </main>

        {loading && (
          <motion.section
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="max-w-4xl mx-auto bg-white/10 backdrop-blur-xl rounded-2xl p-12 border border-white/20 flex flex-col items-center justify-center mb-8"
            role="status"
            aria-live="polite"
          >
            <FaSpinner className="text-cyan-300 text-6xl animate-spin mb-6" aria-hidden="true" />
            <h2 className="text-2xl font-bold text-white mb-2">AI Analysis in Progress</h2>
            <p className="text-gray-200 text-center">
              Our IBM Agentic-AI system is analyzing your dispute.
            </p>
          </motion.section>
        )}

        {error && (
          <motion.section
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="max-w-4xl mx-auto bg-red-500/10 backdrop-blur-xl rounded-2xl p-6 border border-red-500/30 mb-8"
            role="alert"
          >
            <h2 className="text-red-200 font-bold text-xl mb-2">
              Error
            </h2>
            <p className="text-red-100">{error}</p>
          </motion.section>
        )}

        {result && useAgenticMode && result.agentFlow && (
          <AIFlowStepper agentFlow={result.agentFlow} loading={loading} />
        )}

        {result && !loading && (
          <ResultCard result={result} />
        )}
      </div>
    </div>
  );
}

export default App;

// Made with Bob
