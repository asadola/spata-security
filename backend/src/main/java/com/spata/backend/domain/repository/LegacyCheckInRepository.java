package com.spata.backend.domain.repository;

// 1. CHANGE THIS IMPORT to point to your correct CheckIn entity
import com.spata.backend.domain.checkin.CheckIn; 
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID; // 2. ADD THIS IMPORT for the UUID primary key

public interface LegacyCheckInRepository extends JpaRepository<CheckIn, UUID> { // 3. CHANGE String to UUID
    
    // Find check-ins for a specific movement, ordered by newest first
    List<CheckIn> findByMovementIdOrderByRecordedAtDesc(UUID movementId);
    
}