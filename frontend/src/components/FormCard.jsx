import React from 'react';
import { motion } from 'framer-motion';

const FormCard = ({ children, title, subtitle }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="relative bg-white/10 backdrop-blur-xl rounded-2xl p-8 shadow-2xl border border-white/20"
      style={{
        background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
      }}
    >
      {/* Animated glow effect */}
      <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-cyan-500/20 via-violet-500/20 to-purple-500/20 blur-xl -z-10 animate-pulse-slow"></div>
      
      {title && (
        <div className="mb-8 text-center">
          <motion.h2
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="text-3xl font-bold text-white mb-3 bg-clip-text text-transparent bg-gradient-to-r from-cyan-400 via-violet-400 to-purple-400"
          >
            {title}
          </motion.h2>
          {subtitle && (
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.3 }}
              className="text-gray-300 text-base"
            >
              {subtitle}
            </motion.p>
          )}
          {/* Decorative line */}
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: '100%' }}
            transition={{ delay: 0.4, duration: 0.6 }}
            className="h-1 bg-gradient-to-r from-transparent via-cyan-500 to-transparent rounded-full mt-4 mx-auto max-w-xs"
          />
        </div>
      )}
      
      {children}
    </motion.div>
  );
};

export default FormCard;

// Made with Bob
