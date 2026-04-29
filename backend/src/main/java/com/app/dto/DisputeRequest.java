package com.app.dto;

public class DisputeRequest {
    private double amount;
    private String transactionLocation;
    private String userCurrentLocation;
    private String description;
    
    // New fields for enhanced functionality
    private String transactionType; // ATM, MERCHANT, ONLINE, OTHERS
    private String transactionDateTime;
    private String userId; // Government ID for user verification
    private String userName; // For document verification
    private String userDob; // For document verification
    private String websiteUrl; // For online transactions
    private String merchantName; // For merchant transactions
    private String documentData; // Base64 encoded document or extracted text
    private String transactionReceiptData; // Transaction receipt for validation

    public DisputeRequest() {
    }

    public DisputeRequest(double amount, String transactionLocation, String userCurrentLocation, String description) {
        this.amount = amount;
        this.transactionLocation = transactionLocation;
        this.userCurrentLocation = userCurrentLocation;
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionLocation() {
        return transactionLocation;
    }

    public void setTransactionLocation(String transactionLocation) {
        this.transactionLocation = transactionLocation;
    }

    public String getUserCurrentLocation() {
        return userCurrentLocation;
    }

    public void setUserCurrentLocation(String userCurrentLocation) {
        this.userCurrentLocation = userCurrentLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(String transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDob() {
        return userDob;
    }

    public void setUserDob(String userDob) {
        this.userDob = userDob;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getDocumentData() {
        return documentData;
    }

    public void setDocumentData(String documentData) {
        this.documentData = documentData;
    }

    public String getTransactionReceiptData() {
        return transactionReceiptData;
    }

    public void setTransactionReceiptData(String transactionReceiptData) {
        this.transactionReceiptData = transactionReceiptData;
    }
}

// Made with Bob
