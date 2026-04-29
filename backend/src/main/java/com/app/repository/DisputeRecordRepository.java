package com.app.repository;

import com.app.entity.DisputeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DisputeRecordRepository extends JpaRepository<DisputeRecord, Long> {
    
    // Find disputes by user ID
    List<DisputeRecord> findByUserId(String userId);
    
    // Find disputes by decision type
    List<DisputeRecord> findByDecision(String decision);
    
    // Find disputes by intent
    List<DisputeRecord> findByIntent(String intent);
    
    // Find disputes within a date range
    List<DisputeRecord> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find high-risk disputes (risk score >= threshold)
    List<DisputeRecord> findByRiskScoreGreaterThanEqual(Integer riskScore);
    
    // Find disputes by user and date range
    List<DisputeRecord> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Count disputes by decision type
    @Query("SELECT COUNT(d) FROM DisputeRecord d WHERE d.decision = :decision")
    Long countByDecision(@Param("decision") String decision);
    
    // Count disputes by intent
    @Query("SELECT COUNT(d) FROM DisputeRecord d WHERE d.intent = :intent")
    Long countByIntent(@Param("intent") String intent);
    
    // Get average risk score
    @Query("SELECT AVG(d.riskScore) FROM DisputeRecord d")
    Double getAverageRiskScore();
    
    // Get total refund amount
    @Query("SELECT SUM(d.refundAmount) FROM DisputeRecord d WHERE d.refundAmount IS NOT NULL")
    Double getTotalRefundAmount();
    
    // Find recent disputes (last N records)
    List<DisputeRecord> findTop10ByOrderByCreatedAtDesc();
    
    // Find disputes by transaction location
    List<DisputeRecord> findByTransactionLocation(String transactionLocation);
    
    // Analytics: Get dispute count by decision type
    @Query("SELECT d.decision, COUNT(d) FROM DisputeRecord d GROUP BY d.decision")
    List<Object[]> getDisputeCountByDecision();
    
    // Analytics: Get dispute count by intent
    @Query("SELECT d.intent, COUNT(d) FROM DisputeRecord d GROUP BY d.intent")
    List<Object[]> getDisputeCountByIntent();
    
    // Analytics: Get disputes with location mismatch
    @Query("SELECT d FROM DisputeRecord d WHERE d.transactionLocation != d.userCurrentLocation")
    List<DisputeRecord> findDisputesWithLocationMismatch();
    
    // Find disputes by amount range
    @Query("SELECT d FROM DisputeRecord d WHERE d.amount BETWEEN :minAmount AND :maxAmount")
    List<DisputeRecord> findByAmountRange(@Param("minAmount") Double minAmount, @Param("maxAmount") Double maxAmount);
}

// Made with Bob