import React from 'react';
import { motion } from 'framer-motion';
import { FaBrain, FaSearch, FaBalanceScale } from 'react-icons/fa';

const AIFlowStepper = ({ agentFlow, loading }) => {
  const steps = [
    {
      id: 1,
      icon: FaBrain,
      title: 'Intent Agent',
      status: agentFlow?.intentAgent || 'Pending',
      result: agentFlow?.intentResult || 'Analyzing user intent...',
      color: 'from-cyan-500 to-blue-500'
    },
    {
      id: 2,
      icon: FaSearch,
      title: 'Context Agent',
      status: agentFlow?.contextAgent || 'Pending',
      result: agentFlow?.contextResult || 'Gathering context...',
      color: 'from-violet-500 to-purple-500'
    },
    {
      id: 3,
      icon: FaBalanceScale,
      title: 'Decision Agent',
      status: agentFlow?.decisionAgent || 'Pending',
      result: agentFlow?.decisionResult || 'Making decision...',
      color: 'from-purple-500 to-pink-500'
    }
  ];

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.2 }}
      className="relative bg-white/10 backdrop-blur-xl rounded-2xl p-8 shadow-2xl border border-white/20 mb-6"
    >
      {/* Glow effect */}
      <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-cyan-500/20 via-violet-500/20 to-purple-500/20 blur-xl -z-10"></div>
      
      <h2 className="text-2xl font-bold text-white mb-8 text-center flex items-center justify-center gap-3">
        <span className="text-3xl">🤖</span>
        Multi-Agent AI Analysis
      </h2>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {steps.map((step, index) => (
          <React.Fragment key={step.id}>
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.4, delay: index * 0.2 }}
              className="relative"
            >
              {/* Card */}
              <div className={`relative bg-gradient-to-br ${step.color} p-6 rounded-xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2`}>
                {/* Icon */}
                <div className="flex justify-center mb-4">
                  <div className="bg-white/20 backdrop-blur-sm p-4 rounded-full">
                    <step.icon className="text-white text-4xl" />
                  </div>
                </div>

                {/* Title */}
                <h3 className="text-xl font-bold text-white text-center mb-3">
                  {step.title}
                </h3>

                {/* Status */}
                <div className="text-center mb-4">
                  <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${
                    loading ? 'bg-yellow-400/30 text-yellow-100 animate-pulse' : 
                    'bg-green-400/30 text-green-100'
                  }`}>
                    {loading ? 'Processing...' : step.status}
                  </span>
                </div>

                {/* Result */}
                <div className="bg-white/10 backdrop-blur-sm rounded-lg p-4 border border-white/20 max-h-96 overflow-y-auto">
                  <div className="text-white text-sm space-y-3">
                    {(() => {
                      const text = step.result || '';
                      
                      // Parse sections and items
                      const sections = [];
                      let currentSection = { title: null, items: [] };
                      
                      // Split by common delimiters and emojis
                      const parts = text.split(/(\*\*[^*]+\*\*|[⚠️✓🚨🔴⚡🏦📍🚫⛔])/g).filter(s => s && s.trim());
                      
                      parts.forEach((part, idx) => {
                        const trimmed = part.trim();
                        if (!trimmed) return;
                        
                        // Check if it's a section header
                        if (trimmed.startsWith('**') && trimmed.endsWith('**')) {
                          if (currentSection.items.length > 0) {
                            sections.push({ ...currentSection });
                          }
                          currentSection = {
                            title: trimmed.replace(/\*\*/g, '').replace(/:/g, '').trim(),
                            items: []
                          };
                        }
                        // Check if it's an emoji marker
                        else if (/^[⚠️✓🚨🔴⚡🏦📍🚫⛔]$/.test(trimmed)) {
                          const nextPart = parts[idx + 1];
                          if (nextPart && !nextPart.startsWith('**') && !/^[⚠️✓🚨🔴⚡🏦📍🚫⛔]$/.test(nextPart.trim())) {
                            currentSection.items.push({
                              emoji: trimmed,
                              text: nextPart.trim()
                            });
                          }
                        }
                        // Regular text
                        else if (idx === 0 || !(/^[⚠️✓🚨🔴⚡🏦📍🚫⛔]$/.test(parts[idx - 1]?.trim()))) {
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
                      
                      if (currentSection.items.length > 0) {
                        sections.push(currentSection);
                      }
                      
                      // Fallback to simple splitting
                      if (sections.length === 0) {
                        const simpleItems = text.split(/[.!]\s+/).filter(s => s.trim() && s.trim().length > 10);
                        if (simpleItems.length > 0) {
                          sections.push({
                            title: null,
                            items: simpleItems.map(item => ({ emoji: '•', text: item.trim() }))
                          });
                        } else {
                          // Just display the text as-is
                          return <p className="leading-relaxed">{text}</p>;
                        }
                      }
                      
                      return sections.map((section, sectionIdx) => (
                        <div key={sectionIdx} className="space-y-2">
                          {section.title && (
                            <h5 className="text-cyan-300 font-semibold text-xs uppercase tracking-wide mb-2">
                              {section.title}
                            </h5>
                          )}
                          <ul className="space-y-2">
                            {section.items.map((item, itemIdx) => {
                              let bgColor = 'bg-white/5';
                              let borderColor = 'border-white/10';
                              
                              if (item.emoji === '🚨' || item.emoji === '🔴' || item.emoji === '⛔') {
                                bgColor = 'bg-red-500/10';
                                borderColor = 'border-red-500/30';
                              } else if (item.emoji === '⚠️' || item.emoji === '⚡') {
                                bgColor = 'bg-yellow-500/10';
                                borderColor = 'border-yellow-500/30';
                              } else if (item.emoji === '✓') {
                                bgColor = 'bg-green-500/10';
                                borderColor = 'border-green-500/30';
                              }
                              
                              return (
                                <li
                                  key={itemIdx}
                                  className={`flex items-start gap-2 ${bgColor} rounded-lg p-2 border ${borderColor}`}
                                >
                                  <span className="text-base flex-shrink-0 mt-0.5">{item.emoji}</span>
                                  <span className="leading-relaxed text-xs">{item.text}</span>
                                </li>
                              );
                            })}
                          </ul>
                        </div>
                      ));
                    })()}
                  </div>
                </div>

                {/* Shimmer effect when loading */}
                {loading && (
                  <div className="absolute inset-0 rounded-xl overflow-hidden">
                    <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent animate-shimmer"></div>
                  </div>
                )}
              </div>
            </motion.div>

            {/* Arrow between steps */}
            {index < steps.length - 1 && (
              <div className="hidden md:flex items-center justify-center">
                <motion.div
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.5, delay: index * 0.2 + 0.3 }}
                  className="text-4xl text-cyan-400 font-bold"
                >
                  →
                </motion.div>
              </div>
            )}
          </React.Fragment>
        ))}
      </div>
    </motion.div>
  );
};

export default AIFlowStepper;

// Made with Bob
