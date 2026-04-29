package com.app.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_DATA")
public class UserData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true, length = 20)
    private String userId;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(name = "govt_id", nullable = false, unique = true)
    private String govtId;
    
    @Column(name = "dob", nullable = false)
    private LocalDate dob;
    
    @Column(name = "travel_history", columnDefinition = "TEXT")
    private String travelHistory;
    
    @Column(name = "current_location", length = 100)
    private String currentLocation;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public UserData() {
        this.createdAt = LocalDateTime.now();
    }
    
    public UserData(String userId, String userName, String govtId, LocalDate dob, String travelHistory) {
        this.userId = userId;
        this.userName = userName;
        this.govtId = govtId;
        this.dob = dob;
        this.travelHistory = travelHistory;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getGovtId() {
        return govtId;
    }
    
    public void setGovtId(String govtId) {
        this.govtId = govtId;
    }
    
    public LocalDate getDob() {
        return dob;
    }
    
    public void setDob(LocalDate dob) {
        this.dob = dob;
    }
    
    public String getTravelHistory() {
        return travelHistory;
    }
    
    public void setTravelHistory(String travelHistory) {
        this.travelHistory = travelHistory;
    }
    
    public String getCurrentLocation() {
        return currentLocation;
    }
    
    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

// Made with Bob
