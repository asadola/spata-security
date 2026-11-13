package com.spata.backend.domain.incident;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IncidentResponse(
    UUID id,
    String title,
    String description,
    String incidentType,
    IncidentSeverity severity,
    IncidentStatus status,
    Double latitude,
    Double longitude,
    OffsetDateTime createdAt
) {
    /**
     * Factory method to cleanly convert a JPA Entity into a safe API Response.
     * Note: JTS stores coordinates as (X, Y), which is (Longitude, Latitude).
     */
    public static IncidentResponse from(Incident incident) {
        Double lat = null;
        Double lng = null;
        
        if (incident.getLocation() != null) {
            lng = incident.getLocation().getX(); // X is Longitude
            lat = incident.getLocation().getY(); // Y is Latitude
        }

        return new IncidentResponse(
            incident.getId(),
            incident.getTitle(),
            incident.getDescription(),
            incident.getIncidentType(),
            incident.getSeverity(),
            incident.getStatus(),
            lat,
            lng,
            incident.getCreatedAt()
        );
    }
}