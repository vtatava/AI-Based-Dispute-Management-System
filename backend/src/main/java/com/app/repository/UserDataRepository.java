package com.app.repository;

import com.app.entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Long> {
    Optional<UserData> findByGovtId(String govtId);
    Optional<UserData> findByUserId(String userId);
}

// Made with Bob
