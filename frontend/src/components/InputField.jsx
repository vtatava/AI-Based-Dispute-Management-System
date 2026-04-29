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
  id,
  ariaDescribedBy,
  ariaInvalid = false,
  ...props
}) => {
  const inputId = id || name;
  const hintId = hint ? `${inputId}-hint` : undefined;
  const describedBy = [hintId, ariaDescribedBy].filter(Boolean).join(' ') || undefined;

  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.3 }}
      className="mb-4"
    >
      <label htmlFor={inputId} className="block text-sm font-semibold text-gray-200 mb-2">
        {label}
        {required && (
          <>
            <span className="text-red-400 ml-1" aria-hidden="true">*</span>
            <span className="sr-only">required</span>
          </>
        )}
      </label>
      {hint && (
        <p id={hintId} className="text-xs text-gray-300 mb-2">
          {hint}
        </p>
      )}
      <div className="relative">
        {Icon && (
          <div
            className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-300"
            aria-hidden="true"
          >
            <Icon size={20} />
          </div>
        )}
        <input
          id={inputId}
          type={type}
          name={name}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          aria-required={required}
          aria-invalid={ariaInvalid}
          aria-describedby={describedBy}
          className={`w-full ${Icon ? 'pl-12' : 'pl-4'} pr-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-cyan-400 focus:border-cyan-300 transition-all duration-300 backdrop-blur-sm hover:bg-white/10`}
          {...props}
        />
      </div>
    </motion.div>
  );
};

export default InputField;

// Made with Bob
