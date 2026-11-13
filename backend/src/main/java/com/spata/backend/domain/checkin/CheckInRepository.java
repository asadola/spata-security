package com.spata.backend.domain.checkin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    // Get the most recent check-ins for a person (for tracking their journey)
    List<CheckIn> findByPersonnelIdOrderByRecordedAtDesc(UUID personnelId);

    // Get all check-ins for a specific movement
    List<CheckIn> findByMovementIdOrderByRecordedAtAsc(UUID movementId);
}