package com.spata.backend.domain.movement;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FieldMovementResponse(
    UUID id,
    String missionName,
    MovementStatus status,
    TransportMode transportMode,
    Double currentLatitude,
    Double currentLongitude,
    List<double[]> plannedRoute,
    OffsetDateTime plannedDeparture,
    OffsetDateTime plannedArrival,
    OffsetDateTime actualDeparture,
    OffsetDateTime actualArrival,
    List<UUID> personnelIds,
    UUID regionId,
    OffsetDateTime createdAt
) {
    public static FieldMovementResponse from(FieldMovement movement) {
        Double lat = null;
        Double lng = null;
        
        if (movement.getCurrentLocation() != null) {
            lng = movement.getCurrentLocation().getX();
            lat = movement.getCurrentLocation().getY();
        }

        // Convert LineString to list of [lat, lng] pairs
        List<double[]> route = null;
        if (movement.getPlannedRoute() != null) {
            route = new java.util.ArrayList<>();
            for (var coord : movement.getPlannedRoute().getCoordinates()) {
                route.add(new double[]{coord.y, coord.x}); // [lat, lng]
            }
        }

        return new FieldMovementResponse(
            movement.getId(),
            movement.getMissionName(),
            movement.getStatus(),
            movement.getTransportMode(),
            lat,
            lng,
            route,
            movement.getPlannedDeparture(),
            movement.getPlannedArrival(),
            movement.getActualDeparture(),
            movement.getActualArrival(),
            movement.getPersonnelIds() != null ? List.of(movement.getPersonnelIds()) : List.of(),
            movement.getRegionId(),
            movement.getCreatedAt()
        );
    }
    
}
