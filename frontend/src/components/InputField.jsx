import React from 'react';
import { motion } from 'framer-motion';

const InputField = ({ 
  label, 
  icon: Icon, 
  type = 'text', 
  placeholder, 
  value, 
  onChange, 
  name,
  required = false,
  hint,
  ...props 
}) => {
  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.3 }}
      className="mb-4"
    >
      <label className="block text-sm font-semibold text-gray-200 mb-2">
        {label}
        {required && <span className="text-red-400 ml-1">*</span>}
      </label>
      {hint && <p className="text-xs text-gray-400 mb-2 italic">{hint}</p>}
      <div className="relative">
        {Icon && (
          <div className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400">
            <Icon size={20} />
          </div>
        )}
        <input
          type={type}
          name={name}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          className={`w-full ${Icon ? 'pl-12' : 'pl-4'} pr-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:border-transparent transition-all duration-300 backdrop-blur-sm hover:bg-white/10`}
          {...props}
        />
      </div>
    </motion.div>
  );
};

export default InputField;

// Made with Bob
