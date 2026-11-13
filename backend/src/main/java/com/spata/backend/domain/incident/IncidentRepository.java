package com.spata.backend.domain.incident;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    /**
     * Finds all non-deleted incidents within a given radius (in meters) of a center point.
     * Uses native PostGIS ST_DWithin for optimal GiST index performance.
     */
    @Query(value = """
        SELECT * FROM incidents i 
        WHERE ST_DWithin(i.location, :centerPoint, :radiusInMeters) 
        AND i.deleted_at IS NULL
        """, nativeQuery = true)
    List<Incident> findIncidentsWithinRadius(
        @Param("centerPoint") Point centerPoint, 
        @Param("radiusInMeters") Double radiusInMeters
    );
}