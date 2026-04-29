import React from 'react';
import { motion } from 'framer-motion';
import { FaCheckCircle, FaExclamationTriangle, FaTimesCircle, FaPhone, FaEnvelope } from 'react-icons/fa';

const ResultCard = ({ result }) => {
  const getRiskLevel = (score) => {
    if (score >= 80) return { level: 'high', color: 'from-red-500 to-red-700', text: 'High Risk' };
    if (score >= 50) return { level: 'medium', color: 'from-yellow-500 to-orange-500', text: 'Medium Risk' };
    return { level: 'low', color: 'from-green-500 to-emerald-600', text: 'Low Risk' };
  };

  const getDecisionStyle = (decision) => {
    if (decision.includes('AUTO_REFUND')) {
      return {
        icon: FaCheckCircle,
        color: 'from-green-500 to-emerald-600',
        bgColor: 'bg-green-500/20',
        textColor: 'text-green-300',
        label: 'Approved'
      };
    }
    if (decision.includes('REJECT')) {
      return {
        icon: FaTimesCircle,
        color: 'from-red-500 to-red-700',
        bgColor: 'bg-red-500/20',
        textColor: 'text-red-300',
        label: 'Rejected'
      };
    }
    return {
      icon: FaExclamationTriangle,
      color: 'from-yellow-500 to-orange-500',
      bgColor: 'bg-yellow-500/20',
      textColor: 'text-yellow-300',
      label: 'Review Required'
    };
  };

  const riskInfo = getRiskLevel(result.riskScore);
  const decisionInfo = getDecisionStyle(result.decision);
  const DecisionIcon = decisionInfo.icon;

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.5 }}
      className="relative bg-white/10 backdrop-blur-xl rounded-2xl p-8 shadow-2xl border border-white/20"
    >
      {/* Animated glow effect */}
      <div className={`absolute inset-0 rounded-2xl bg-gradient-to-r ${decisionInfo.color} opacity-20 blur-2xl -z-10 animate-pulse-slow`}></div>
      
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-3xl font-bold text-white flex items-center gap-3">
          <span>📊</span>
          Analysis Result
        </h2>
        
        {/* Verification Badge */}
        {result.userVerified !== undefined && (
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: 'spring', stiffness: 200, delay: 0.3 }}
            className={`px-4 py-2 rounded-full font-semibold text-sm ${
              result.userVerified 
                ? 'bg-green-500/20 text-green-300 border border-green-500/50' 
                : 'bg-yellow-500/20 text-yellow-300 border border-yellow-500/50'
            }`}
          >
            {result.userVerified ? '✓ Verified' : '⚠️ Unverified'}
          </motion.div>
        )}
      </div>

      {/* Main Decision Card */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className={`bg-gradient-to-br ${decisionInfo.color} p-6 rounded-xl mb-6 shadow-lg`}
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="bg-white/20 backdrop-blur-sm p-4 rounded-full">
              <DecisionIcon className="text-white text-3xl" />
            </div>
            <div>
              <p className="text-white/80 text-sm font-medium">Decision</p>
              <h3 className="text-white text-2xl font-bold">{decisionInfo.label}</h3>
            </div>
          </div>
          
          {/* Refund Amount */}
          {result.refundAmount !== null && result.refundAmount !== undefined && (
            <div className="text-right">
              <p className="text-white/80 text-sm font-medium">Refund Amount</p>
              <h3 className="text-white text-3xl font-bold">
                {result.refundAmount > 0 ? `₹${result.refundAmount.toFixed(2)}` : 'No Refund'}
              </h3>
            </div>
          )}
        </div>
      </motion.div>

      {/* Risk Score */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
        className="mb-6"
      >
        <div className="flex justify-between items-center mb-3">
          <span className="text-gray-300 font-semibold">Risk Score</span>
          <span className={`text-2xl font-bold ${riskInfo.level === 'high' ? 'text-red-400' : riskInfo.level === 'medium' ? 'text-yellow-400' : 'text-green-400'}`}>
            {result.riskScore}/100
          </span>
        </div>
        
        {/* Progress Bar */}
        <div className="relative h-4 bg-white/10 rounded-full overflow-hidden backdrop-blur-sm">
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: `${result.riskScore}%` }}
            transition={{ duration: 1, delay: 0.5, ease: 'easeOut' }}
            className={`h-full bg-gradient-to-r ${riskInfo.color} rounded-full shadow-lg`}
          />
        </div>
        <p className="text-gray-400 text-sm mt-2 text-right">{riskInfo.text}</p>
      </motion.div>

      {/* Analysis/Review Reason */}
      {result.reviewReason && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="bg-white/5 backdrop-blur-sm rounded-xl p-6 border border-white/10 mb-6"
        >
          <h4 className="text-white font-semibold mb-4 flex items-center gap-2">
            <span>📋</span>
            Detailed Analysis
          </h4>
          <div className="text-gray-300 space-y-4">
            {(() => {
              const text = result.reviewReason;
              
              // Parse sections marked with **Section:** or **Section**
              const sections = [];
              let currentSection = { title: null, items: [] };
              
              // Split by common delimiters and emojis
              const parts = text.split(/(\*\*[^*]+\*\*|[⚠️✓🚨🔴⚡🏦📍🚫⛔])/g).filter(s => s && s.trim());
              
              parts.forEach((part, idx) => {
                const trimmed = part.trim();
                if (!trimmed) return;
                
                // Check if it's a section header (text between **)
                if (trimmed.startsWith('**') && trimmed.endsWith('**')) {
                  // Save previous section if it has content
                  if (currentSection.items.length > 0) {
                    sections.push({ ...currentSection });
                  }
                  // Start new section
                  currentSection = {
                    title: trimmed.replace(/\*\*/g, '').replace(/:/g, '').trim(),
                    items: []
                  };
                }
                // Check if it's an emoji marker
                else if (/^[⚠️✓🚨🔴⚡🏦📍🚫⛔]$/.test(trimmed)) {
                  // Look ahead for the text after this emoji
                  const nextPart = parts[idx + 1];
                  if (nextPart && !nextPart.startsWith('**') && !/^[⚠️✓🚨🔴⚡🏦📍🚫⛔]$/.test(nextPart.trim())) {
                    currentSection.items.push({
                      emoji: trimmed,
                      text: nextPart.trim()
                    });
                  }
                }
                // Regular text (only add if not already processed as part of emoji)
                else if (idx === 0 || !(/^[⚠️✓🚨🔴⚡🏦📍🚫⛔]$/.test(parts[idx - 1]?.trim()))) {
                  // Split by bullet points or periods for better readability
                  const sentences = trimmed.split(/•/).filter(s => s.trim());
                  sentences.forEach(sentence => {
                    const clean = sentence.trim();
                    if (clean && clean.length > 10) {
                      currentSection.items.push({
                        emoji: '•',
                        text: clean
                      });
                    }
                  });
                }
              });
              
              // Add the last section
              if (currentSection.items.length > 0) {
                sections.push(currentSection);
              }
              
              // If no sections were created, fall back to simple splitting
              if (sections.length === 0) {
                const simpleItems = text.split(/[.!]\s+/).filter(s => s.trim() && s.trim().length > 10);
                sections.push({
                  title: null,
                  items: simpleItems.map(item => ({ emoji: '•', text: item.trim() }))
                });
              }
              
              return sections.map((section, sectionIdx) => (
                <div key={sectionIdx} className="space-y-2">
                  {section.title && (
                    <h5 className="text-cyan-400 font-semibold text-sm uppercase tracking-wide mb-2">
                      {section.title}
                    </h5>
                  )}
                  <ul className="space-y-2">
                    {section.items.map((item, itemIdx) => {
                      // Determine styling based on emoji
                      let bgColor = 'bg-white/5';
                      let borderColor = 'border-white/10';
                      let emojiSize = 'text-lg';
                      
                      if (item.emoji === '🚨' || item.emoji === '🔴' || item.emoji === '⛔') {
                        bgColor = 'bg-red-500/10';
                        borderColor = 'border-red-500/30';
                        emojiSize = 'text-xl';
                      } else if (item.emoji === '⚠️' || item.emoji === '⚡') {
                        bgColor = 'bg-yellow-500/10';
                        borderColor = 'border-yellow-500/30';
                        emojiSize = 'text-xl';
                      } else if (item.emoji === '✓') {
                        bgColor = 'bg-green-500/10';
                        borderColor = 'border-green-500/30';
                        emojiSize = 'text-xl';
                      }
                      
                      return (
                        <li
                          key={itemIdx}
                          className={`flex items-start gap-3 ${bgColor} rounded-lg p-3 border ${borderColor} hover:bg-white/10 transition-colors`}
                        >
                          <span className={`${emojiSize} flex-shrink-0 mt-0.5`}>{item.emoji}</span>
                          <span className="leading-relaxed text-sm">{item.text}</span>
                        </li>
                      );
                    })}
                  </ul>
                </div>
              ));
            })()}
          </div>
        </motion.div>
      )}

      {/* Verification Message */}
      {result.verificationMessage && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="bg-yellow-500/10 backdrop-blur-sm rounded-xl p-4 border border-yellow-500/30 mb-6"
        >
          <p className="text-yellow-300 text-sm">{result.verificationMessage}</p>
        </motion.div>
      )}

      {/* Manual Review Notice */}
      {result.decision === 'MANUAL_REVIEW' && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6 }}
          className="bg-gradient-to-br from-orange-500/20 to-red-500/20 backdrop-blur-sm rounded-xl p-6 border border-orange-500/30"
        >
          <h4 className="text-orange-300 font-bold text-lg mb-3 flex items-center gap-2">
            <FaExclamationTriangle />
            Human Review Required
          </h4>
          <p className="text-gray-300 mb-4">
            This dispute requires additional review. Please contact our Dispute Resolution Help Desk:
          </p>
          <div className="space-y-3">
            <div className="flex items-center gap-3 bg-white/5 rounded-lg p-3">
              <FaPhone className="text-cyan-400 text-xl" />
              <div>
                <p className="text-gray-400 text-xs">Phone</p>
                <p className="text-white font-semibold">18000-000-000</p>
              </div>
            </div>
            <div className="flex items-center gap-3 bg-white/5 rounded-lg p-3">
              <FaEnvelope className="text-cyan-400 text-xl" />
              <div>
                <p className="text-gray-400 text-xs">Email</p>
                <p className="text-white font-semibold">disputehelp247@xyz.com</p>
              </div>
            </div>
          </div>
        </motion.div>
      )}
    </motion.div>
  );
};

export default ResultCard;

// Made with Bob
