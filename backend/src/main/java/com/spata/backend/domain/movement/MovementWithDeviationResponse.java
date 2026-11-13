package com.spata.backend.domain.movement;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MovementWithDeviationResponse(
    UUID id,
    String missionName,
    MovementStatus status,
    TransportMode transportMode,
    Double currentLatitude,
    Double currentLongitude,
    Integer deviationMeters,
    String deviationSeverity,
    List<UUID> personnelIds,
    List<double[]> plannedRoute,
    OffsetDateTime createdAt
) {
    /**
     * Factory method that calculates severity based on deviation distance.
     */
    public static MovementWithDeviationResponse from(FieldMovement movement, Integer deviationMeters) {
        return from(movement, deviationMeters, extractPlannedRoute(movement));
    }

    public static MovementWithDeviationResponse from(FieldMovement movement, Integer deviationMeters, List<double[]> plannedRoute) {
        Double lat = null;
        Double lng = null;

        if (movement.getCurrentLocation() != null) {
            lng = movement.getCurrentLocation().getX();
            lat = movement.getCurrentLocation().getY();
        }

        return new MovementWithDeviationResponse(
            movement.getId(),
            movement.getMissionName(),
            movement.getStatus(),
            movement.getTransportMode(),
            lat,
            lng,
            deviationMeters,
            calculateSeverity(deviationMeters),
            movement.getPersonnelIds() != null ? List.of(movement.getPersonnelIds()) : List.of(),
            plannedRoute,
            movement.getCreatedAt()
        );
    }

    private static List<double[]> extractPlannedRoute(FieldMovement movement) {
        List<double[]> routeCoords = new java.util.ArrayList<>();
        if (movement.getPlannedRoute() != null) {
            for (org.locationtech.jts.geom.Coordinate coord : movement.getPlannedRoute().getCoordinates()) {
                routeCoords.add(new double[] { coord.y, coord.x });
            }
        }
        return routeCoords;
    }

    /**
     * Business logic: Classify deviation into severity levels.
     * This is the kind of logic that powers dashboard color-coding.
     */
    private static String calculateSeverity(Integer deviationMeters) {
        if (deviationMeters == null) return "UNKNOWN";
        if (deviationMeters < 1000) return "NONE";        // < 1km: On route
        if (deviationMeters < 5000) return "MINOR";       // 1-5km: Slight deviation
        if (deviationMeters < 15000) return "MAJOR";      // 5-15km: Significant deviation
        return "CRITICAL";                                 // > 15km: Major concern
    }
}