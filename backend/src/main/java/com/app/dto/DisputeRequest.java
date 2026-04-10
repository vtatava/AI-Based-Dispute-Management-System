package com.app.dto;

public class DisputeRequest {
    private double amount;
    private String transactionLocation;
    private String userCurrentLocation;
    private String description;

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
}

// Made with Bob
