package com.spata.backend.domain.movement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FieldMovementRepository extends JpaRepository<FieldMovement, UUID> {

    // Find all movements that contain a specific personnel ID
    @Query(value = "SELECT * FROM field_movements WHERE :personnelId = ANY(personnel_ids) AND deleted_at IS NULL", nativeQuery = true)
    List<FieldMovement> findByPersonnelId(@Param("personnelId") UUID personnelId);

    // Find movements currently in transit
   @Query("SELECT m FROM FieldMovement m WHERE m.status = 'IN_TRANSIT'")
List<FieldMovement> findActiveMovements();

        // Find movements within a radius of a specific point
    @Query(value = """
        SELECT * FROM field_movements m 
        WHERE ST_DWithin(m.current_location, :centerPoint, :radiusInMeters) 
        AND m.status = 'IN_TRANSIT'
        AND m.deleted_at IS NULL
        """, nativeQuery = true)
    List<FieldMovement> findMovementsNearPoint(
        @Param("centerPoint") org.locationtech.jts.geom.Point centerPoint,
        @Param("radiusInMeters") Double radiusInMeters
    );
}
