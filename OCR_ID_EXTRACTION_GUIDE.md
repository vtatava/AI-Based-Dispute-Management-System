# 🆔 OCR ID Text Extraction Guide

## ✅ Setup Complete!

Your system is now configured to automatically extract text from uploaded ID images using Tesseract OCR.

## 📋 Configuration Summary

- **Tesseract Location**: `C:\Program Files\Tesseract-OCR`
- **OCR Status**: ✅ ENABLED
- **Tessdata Path**: `C:/Program Files/Tesseract-OCR/tessdata`
- **Supported Languages**: English (eng)

## 🚀 How to Use

### Step 1: Start the Backend

```bash
cd backend
.\mvnw.cmd spring-boot:run
```

Wait for the message: `Started DisputeAiApplication`

### Step 2: Start the Frontend

```bash
cd frontend
npm start
```

Browser will open at: `http://localhost:3000`

### Step 3: Upload an ID Document

1. Fill in the dispute form
2. Scroll to **"User Identification Document"** section
3. Click **"Choose File"** and select an ID image
4. System will automatically:
   - Extract text from the image
   - Parse AADHAAR number and name
   - Validate against database
   - Show validation result

## 📸 Supported ID Formats

### AADHAAR Card (Primary Support)

The system is optimized for Indian AADHAAR cards with:
- **12-digit AADHAAR number**
- **User's full name**
- **Clear, readable text**

### Example ID Text Format:

```
Government of India
AADHAAR CARD

Name: Rajesh Kumar
AADHAAR: 123456789
DOB: 15/05/1985
Address: Mumbai, Maharashtra
```

## 🧪 Test with Sample Data

### Test Users in Database:

| Name | AADHAAR ID | Status |
|------|------------|--------|
| Rajesh Kumar | AADHAAR123456789 | ✅ Active |
| Priya Sharma | AADHAAR987654321 | ✅ Active |
| Amit Patel | AADHAAR456789123 | ✅ Active |
| Sneha Reddy | AADHAAR789123456 | ✅ Active |
| Vikram Singh | AADHAAR321654987 | ✅ Active |

### Creating Test ID Images:

**Option 1: Use Image Editor**
1. Open Paint or any image editor
2. Create a white background (800x600px)
3. Add text:
   ```
   GOVERNMENT OF INDIA
   AADHAAR CARD
   
   Name: Rajesh Kumar
   AADHAAR: 123456789
   DOB: 15/05/1985
   ```
4. Save as PNG or JPG
5. Upload through the form

**Option 2: Use Word Document**
1. Create a Word document with ID text
2. Export as PDF or take screenshot
3. Upload the file

## 🎯 What Gets Extracted

### Automatic Extraction:

1. **AADHAAR Number**
   - Pattern: `AADHAAR` followed by 12 digits
   - Formats recognized:
     - `AADHAAR123456789012`
     - `AADHAAR 1234 5678 9012`
     - `AADHAAR: 123456789012`

2. **User Name**
   - Looks for capitalized words
   - Typically found after "Name:" label
   - Fuzzy matching handles variations

3. **Validation**
   - Matches against USER_DATA table
   - Verifies name similarity
   - Returns verification status

## 📊 API Response Examples

### ✅ Successful Validation:

```json
{
  "valid": true,
  "message": "✓ ID validated successfully! User: Rajesh Kumar (ID: AADHAAR123456789)",
  "userName": "Rajesh Kumar",
  "userId": "AADHAAR123456789",
  "verified": true,
  "extractedText": "Government of India\nAADHAAR CARD\nName: Rajesh Kumar..."
}
```

### ⚠️ Validation Failed:

```json
{
  "valid": false,
  "message": "⚠️ Please provide your User ID for verification. If you don't have one, please visit the Dispute Admin desk.",
  "verified": false,
  "extractedText": "No text extracted"
}
```

### ❌ ID Not Found:

```json
{
  "valid": false,
  "message": "⚠️ ID number found but name mismatch. Expected: John Doe, Found: Rajesh Kumar",
  "verified": false
}
```

## 🔍 Frontend Integration

The frontend automatically handles OCR validation:

```javascript
// When user uploads ID document
const validateUserId = async (file) => {
  const formData = new FormData();
  formData.append('idDocument', file);
  
  const response = await axios.post(
    'http://localhost:9090/api/dispute/validate-id',
    formData
  );
  
  // Shows validation result immediately
  setIdValidationResult(response.data);
};
```

### Visual Feedback:

- ✅ **Green badge**: ID validated successfully
- ⚠️ **Yellow warning**: ID validation failed
- 🔄 **Loading**: Processing OCR

## 🛠️ Troubleshooting

### Issue: "OCR extraction failed"

**Possible Causes:**
1. Tesseract not properly installed
2. Image quality too poor
3. Text not in English
4. Image format not supported

**Solutions:**
1. Run verification script: `.\test-ocr-setup.ps1`
2. Check Tesseract installation
3. Use clearer, higher resolution images
4. Ensure text is horizontal and readable

### Issue: "Name mismatch"

**Causes:**
- Name in ID doesn't match database
- OCR misread the name
- Different name format

**Solutions:**
1. Verify name spelling in database
2. Use clearer image
3. Manually enter User ID as fallback

### Issue: "No text extracted"

**Causes:**
- Image is blank or corrupted
- Text is too small or blurry
- Wrong file format

**Solutions:**
1. Check image file is valid
2. Use minimum 800x600 resolution
3. Ensure good lighting and contrast
4. Use PNG or JPG format

## 📈 Performance Tips

### For Best OCR Accuracy:

1. **Image Quality**
   - Minimum 800x600 pixels
   - Clear, high-contrast text
   - Good lighting, no shadows

2. **Text Orientation**
   - Keep text horizontal
   - No rotation or skewing
   - Proper alignment

3. **File Format**
   - PNG (best quality)
   - JPG (good compression)
   - PDF (single page)

4. **File Size**
   - Keep under 5MB
   - Compress if needed
   - Remove unnecessary elements

## 🔐 Security Features

- ✅ File type validation (images and PDFs only)
- ✅ File size limit (5MB max)
- ✅ In-memory processing (no permanent storage)
- ✅ Secure validation against database
- ✅ Privacy-focused (extracted text not logged)

## 📞 Support

### If OCR is not working:

1. **Verify Setup**
   ```bash
   .\test-ocr-setup.ps1
   ```

2. **Check Logs**
   - Backend console output
   - Look for "OCR Extracted Text:" messages

3. **Enable Debug Logging**
   Add to `application.properties`:
   ```properties
   logging.level.com.app.service.IdValidationService=DEBUG
   ```

4. **Fallback Mode**
   If OCR fails, system automatically falls back to manual User ID entry

### Contact Support:
- Email: disputehelp247@xyz.com
- Phone: 18000-000-000

## 🎓 Advanced Usage

### Adding More ID Types:

Edit `IdValidationService.java` to add patterns for:
- PAN Card
- Passport
- Driver's License
- Voter ID

### Multi-Language Support:

1. Download language data from: https://github.com/tesseract-ocr/tessdata
2. Place in tessdata folder
3. Update code:
   ```java
   tesseract.setLanguage("eng+hin"); // English + Hindi
   ```

### Custom Validation Rules:

Modify `parseIdInformation()` method to add:
- Date of birth extraction
- Address parsing
- Additional ID formats

## ✨ Features

- ✅ Automatic text extraction from images
- ✅ AADHAAR number recognition
- ✅ Name extraction and parsing
- ✅ Fuzzy name matching
- ✅ Database validation
- ✅ Real-time feedback
- ✅ Fallback to manual entry
- ✅ Support for multiple formats
- ✅ Secure processing

## 🎉 Success!

Your OCR ID extraction system is now fully operational! Upload an ID image and watch the magic happen! 🚀

---

**Made with Bob** 🤖