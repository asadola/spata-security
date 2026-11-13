package com.spata.backend.domain.movement;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;

@Service
public class FieldMovementService {

    private static final Logger logger = LoggerFactory.getLogger(FieldMovementService.class);

    private final FieldMovementRepository movementRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public FieldMovementService(FieldMovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Transactional
    public FieldMovement createMovement(FieldMovementCreateRequest request) {
        FieldMovement movement = new FieldMovement(
            request.missionName(),
            request.createdBy(),
            request.regionId()
        );

        movement.setTransportMode(request.transportMode());
        movement.setPlannedDeparture(request.plannedDeparture());
        movement.setPlannedArrival(request.plannedArrival());
        movement.setPersonnelIds(request.personnelIds().toArray(new UUID[0]));

        // Convert list of [lat, lng] pairs to PostGIS LineString
        if (request.plannedRoute() != null && request.plannedRoute().size() >= 2) {
            Coordinate[] coords = request.plannedRoute().stream()
                .map(point -> new Coordinate(point[1], point[0])) // [lng, lat] for JTS
                .toArray(Coordinate[]::new);
            
            LineString route = geometryFactory.createLineString(coords);
            movement.setPlannedRoute(route);
        }

        return movementRepository.save(movement);
    }

    @Transactional
    public FieldMovement updateCurrentLocation(UUID movementId, Double latitude, Double longitude) {
        FieldMovement movement = movementRepository.findById(movementId)
            .orElseThrow(() -> new RuntimeException("Movement not found"));

        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        movement.setCurrentLocation(location);
        movement.setStatus(MovementStatus.IN_TRANSIT);
        movement.setUpdatedAt(OffsetDateTime.now());

        return movementRepository.save(movement);
    }
        /**
     * Returns movements that have deviated more than the threshold from their route.
     */
        @Transactional(readOnly = true)
    public List<MovementWithDeviationResponse> getDeviatedMovements(Double thresholdMeters) {
            logger.debug("getDeviatedMovements called with threshold: {} meters", thresholdMeters);

            List<FieldMovement> movements = movementRepository.findActiveMovements();
            logger.debug("Found {} active movements in the database", movements.size());

            List<MovementWithDeviationResponse> result = movements.stream()
                .map(movement -> {
                    Integer deviation = calculateDeviationInJava(movement);
                    logger.debug("Movement '{}' ({}) calculated deviation: {} meters",
                        movement.getMissionName(), movement.getId(), deviation);

                    if (deviation != null && deviation > thresholdMeters) {
                        logger.debug("Including movement '{}' because deviation exceeds {} meters",
                            movement.getMissionName(), thresholdMeters);
                        return MovementWithDeviationResponse.from(
                            movement,
                            deviation,
                            toLeafletRoute(movement)
                        );
                    }

                    logger.debug("Excluding movement '{}' because deviation does not exceed {} meters or is null",
                        movement.getMissionName(), thresholdMeters);
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(
                    MovementWithDeviationResponse::deviationMeters,
                    Comparator.reverseOrder()
                ))
                .toList();

            logger.debug("Returning {} deviated movements", result.size());
            return result;
        }

    /**
     * Returns ALL active movements with their deviation distances (for dashboards).
     */
    @Transactional(readOnly = true)
    public List<MovementWithDeviationResponse> getAllMovementsWithDeviation() {
        return movementRepository.findActiveMovements().stream()
            .map(movement -> MovementWithDeviationResponse.from(
                movement,
                calculateDeviationInJava(movement),
                toLeafletRoute(movement)
            ))
            .sorted(Comparator.comparing(
                MovementWithDeviationResponse::deviationMeters,
                Comparator.nullsLast(Comparator.reverseOrder())
            ))
            .toList();
    }

    private List<double[]> toLeafletRoute(FieldMovement movement) {
        List<double[]> routeCoords = new java.util.ArrayList<>();
        if (movement.getPlannedRoute() != null) {
            for (Coordinate coord : movement.getPlannedRoute().getCoordinates()) {
                routeCoords.add(new double[] { coord.y, coord.x });
            }
        }
        return routeCoords;
    }

    /**
     * Calculates the route deviation in a local azimuthal-equidistant projection,
     * keeping distances in meters while using JTS for the point-to-line calculation.
     */
    private Integer calculateDeviationInJava(FieldMovement movement) {
        Point currentLocation = movement.getCurrentLocation();
        LineString plannedRoute = movement.getPlannedRoute();
        if (currentLocation == null || plannedRoute == null) {
            return null;
        }

        double latitudeRadians = Math.toRadians(currentLocation.getY());
        double longitudeRadians = Math.toRadians(currentLocation.getX());
        Coordinate[] routeCoordinates = plannedRoute.getCoordinates();
        Coordinate[] projectedCoordinates = new Coordinate[routeCoordinates.length];

        for (int i = 0; i < routeCoordinates.length; i++) {
            double routeLatitudeRadians = Math.toRadians(routeCoordinates[i].y);
            double longitudeDelta = Math.toRadians(routeCoordinates[i].x) - longitudeRadians;
            double cosAngularDistance =
                Math.sin(latitudeRadians) * Math.sin(routeLatitudeRadians)
                + Math.cos(latitudeRadians) * Math.cos(routeLatitudeRadians) * Math.cos(longitudeDelta);
            double angularDistance = Math.acos(Math.max(-1.0, Math.min(1.0, cosAngularDistance)));
            double scale = angularDistance == 0.0
                ? 1.0
                : angularDistance / Math.sin(angularDistance);

            double x = 6_371_008.8 * scale * Math.cos(routeLatitudeRadians) * Math.sin(longitudeDelta);
            double y = 6_371_008.8 * scale * (
                Math.cos(latitudeRadians) * Math.sin(routeLatitudeRadians)
                - Math.sin(latitudeRadians) * Math.cos(routeLatitudeRadians) * Math.cos(longitudeDelta)
            );
            projectedCoordinates[i] = new Coordinate(x, y);
        }

        Point projectedLocation = geometryFactory.createPoint(new Coordinate(0.0, 0.0));
        LineString projectedRoute = geometryFactory.createLineString(projectedCoordinates);
        double distanceMeters = new DistanceOp(projectedLocation, projectedRoute).distance();
        return (int) Math.min(Integer.MAX_VALUE, Math.round(distanceMeters));
    }

    @Transactional(readOnly = true)
    public List<FieldMovement> getActiveMovements() {
        return movementRepository.findActiveMovements();
    }

    @Transactional(readOnly = true)
    public List<FieldMovement> getMovementsByPersonnel(UUID personnelId) {
        return movementRepository.findByPersonnelId(personnelId);
    }

    // --- NEW METHOD: Find movements near an incident ---
    @Transactional(readOnly = true)
    public List<FieldMovement> getMovementsNearIncident(Double latitude, Double longitude, Double radiusInMeters) {
        Point centerPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        return movementRepository.findMovementsNearPoint(centerPoint, radiusInMeters);
    }
}