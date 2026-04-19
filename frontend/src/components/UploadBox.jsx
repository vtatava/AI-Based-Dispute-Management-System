import React from 'react';
import { motion } from 'framer-motion';
import { FaCloudUploadAlt, FaCheckCircle } from 'react-icons/fa';

const UploadBox = ({ 
  label, 
  hint, 
  onChange, 
  accept, 
  multiple = false, 
  required = false,
  files,
  name
}) => {
  const hasFiles = files && (multiple ? files.length > 0 : files);

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
        <input
          type="file"
          name={name}
          onChange={onChange}
          accept={accept}
          multiple={multiple}
          required={required}
          className="hidden"
          id={name}
        />
        <label
          htmlFor={name}
          className={`flex flex-col items-center justify-center w-full h-32 border-2 border-dashed rounded-xl cursor-pointer transition-all duration-300 ${
            hasFiles 
              ? 'border-green-500 bg-green-500/10 hover:bg-green-500/20' 
              : 'border-white/20 bg-white/5 hover:bg-white/10 hover:border-cyan-500'
          }`}
        >
          <div className="flex flex-col items-center justify-center pt-5 pb-6">
            {hasFiles ? (
              <>
                <FaCheckCircle className="text-green-400 text-4xl mb-3" />
                <p className="text-sm text-green-300 font-semibold">
                  {multiple && files.length > 1 
                    ? `${files.length} files selected` 
                    : files.name || `${files.length} file(s) selected`}
                </p>
              </>
            ) : (
              <>
                <FaCloudUploadAlt className="text-gray-400 text-4xl mb-3" />
                <p className="text-sm text-gray-300 font-semibold">
                  Click to upload or drag and drop
                </p>
                <p className="text-xs text-gray-500 mt-1">
                  {accept || 'Any file type'}
                </p>
              </>
            )}
          </div>
        </label>
      </div>

      {/* File list */}
      {hasFiles && multiple && files.length > 0 && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          className="mt-3 bg-white/5 rounded-lg p-3 border border-white/10"
        >
          <p className="text-xs text-gray-400 mb-2">Selected files:</p>
          <ul className="space-y-1">
            {Array.from(files).map((file, index) => (
              <li key={index} className="text-sm text-gray-300 flex items-center gap-2">
                <span className="text-green-400">•</span>
                {file.name}
              </li>
            ))}
          </ul>
        </motion.div>
      )}
    </motion.div>
  );
};

export default UploadBox;

// Made with Bob
