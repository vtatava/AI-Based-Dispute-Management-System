package com.app.repository;

import com.app.entity.FraudWebsite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraudWebsiteRepository extends JpaRepository<FraudWebsite, Long> {
    Optional<FraudWebsite> findByWebsiteUrlContainingIgnoreCase(String websiteUrl);
}

// Made with Bob
