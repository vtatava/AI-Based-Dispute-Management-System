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
  name,
  id,
  ariaDescribedBy
}) => {
  const inputId = id || name;
  const hasFiles = files && (multiple ? files.length > 0 : files);
  const hintId = hint ? `${inputId}-hint` : undefined;
  const fileStatusId = `${inputId}-status`;
  const describedBy = [hintId, fileStatusId, ariaDescribedBy].filter(Boolean).join(' ') || undefined;

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
        <input
          type="file"
          name={name}
          onChange={onChange}
          accept={accept}
          multiple={multiple}
          required={required}
          className="sr-only"
          id={inputId}
          aria-required={required}
          aria-describedby={describedBy}
        />
        <label
          htmlFor={inputId}
          role="button"
          tabIndex={0}
          aria-describedby={describedBy}
          className={`flex flex-col items-center justify-center w-full min-h-32 border-2 border-dashed rounded-xl cursor-pointer transition-all duration-300 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-cyan-400/60 ${
            hasFiles
              ? 'border-green-400 bg-green-500/10 hover:bg-green-500/20'
              : 'border-white/20 bg-white/5 hover:bg-white/10 hover:border-cyan-400'
          }`}
          onKeyDown={(event) => {
            if (event.key === 'Enter' || event.key === ' ') {
              event.preventDefault();
              document.getElementById(inputId)?.click();
            }
          }}
        >
          <div className="flex flex-col items-center justify-center pt-5 pb-6 px-4 text-center">
            {hasFiles ? (
              <>
                <FaCheckCircle className="text-green-300 text-4xl mb-3" aria-hidden="true" />
                <p className="text-sm text-green-200 font-semibold">
                  {multiple && files.length > 1
                    ? `${files.length} files selected`
                    : files.name || `${files.length} file(s) selected`}
                </p>
                <p className="text-xs text-green-100 mt-1">
                  Press Enter or Space to replace the selected file{multiple ? 's' : ''}.
                </p>
              </>
            ) : (
              <>
                <FaCloudUploadAlt className="text-gray-300 text-4xl mb-3" aria-hidden="true" />
                <p className="text-sm text-gray-100 font-semibold">
                  Click to upload or drag and drop
                </p>
                <p className="text-xs text-gray-300 mt-1">
                  Press Enter or Space to browse files
                </p>
                <p className="text-xs text-gray-400 mt-1">
                  Accepted: {accept || 'Any file type'}
                </p>
              </>
            )}
          </div>
        </label>
      </div>

      <p id={fileStatusId} className="sr-only" aria-live="polite">
        {hasFiles
          ? multiple
            ? `${files.length} file${files.length > 1 ? 's' : ''} selected`
            : `${files.name} selected`
          : 'No files selected'}
      </p>

      {hasFiles && multiple && files.length > 0 && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          className="mt-3 bg-white/5 rounded-lg p-3 border border-white/10"
        >
          <p className="text-xs text-gray-300 mb-2 font-medium">Selected files:</p>
          <ul className="space-y-1">
            {Array.from(files).map((file, index) => (
              <li key={index} className="text-sm text-gray-200 flex items-center gap-2">
                <span className="text-green-300" aria-hidden="true">•</span>
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
