package com.spata.backend.domain.incident;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; // <-- ADDED THIS IMPORT
import java.util.UUID;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public Incident createIncident(IncidentCreateRequest request) {
        // 1. Convert Lat/Lng into a PostGIS Point
        // Note: JTS expects coordinates in (Longitude, Latitude) order! (X, Y)
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));

        // 2. Create the Entity
        Incident incident = new Incident(
            request.title(),
            location,
            request.reportedBy()
        );
        
        incident.setDescription(request.description());
        incident.setIncidentType(request.incidentType());

        // 3. Save to Database
        return incidentRepository.save(incident);
    }

    // --- ADDED THIS NEW METHOD FOR SPATIAL QUERIES ---
    @Transactional(readOnly = true)
    public List<Incident> findIncidentsNearby(Double latitude, Double longitude, Double radiusInMeters) {
        // REMEMBER: JTS expects (Longitude, Latitude) which is (X, Y)!
        Point centerPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        
        return incidentRepository.findIncidentsWithinRadius(centerPoint, radiusInMeters);
    }
}