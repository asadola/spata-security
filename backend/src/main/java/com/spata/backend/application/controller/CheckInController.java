package com.spata.backend.application.controller;

import com.spata.backend.application.dto.CheckInRequest;
import com.spata.backend.application.dto.CheckInResponse;
import com.spata.backend.application.service.VpnDetectionService;
import com.spata.backend.domain.checkin.CheckIn;
import com.spata.backend.domain.checkin.CheckInRepository; // Ensure this points to your existing repo
import com.spata.backend.domain.checkin.CheckInStatus;
import com.spata.backend.domain.checkin.TelemetrySource;
import com.spata.backend.infrastructure.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/check-ins")
public class CheckInController {

    private final CheckInRepository checkInRepository;
    private final VpnDetectionService vpnDetectionService;
    
    // Geometry factory to create PostGIS Points (SRID 4326 is standard GPS)
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public CheckInController(CheckInRepository checkInRepository, VpnDetectionService vpnDetectionService) {
        this.checkInRepository = checkInRepository;
        this.vpnDetectionService = vpnDetectionService;
    }

    @PostMapping
    public ResponseEntity<?> submitCheckIn(@RequestBody CheckInRequest request, HttpServletRequest httpRequest) {
        
        // 1. BRUTAL VPN CHECK
        String clientIp = IpUtils.getClientIp(httpRequest);
        if (vpnDetectionService.isVpnOrProxy(clientIp)) {
            System.err.println("🚨 SECURITY ALERT: VPN/Proxy blocked during check-in from IP: " + clientIp);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Check-ins are not allowed from VPN or Proxy networks.");
        }

        try {
            // 2. Convert lat/lon to PostGIS Point
            Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
            location.setSRID(4326);

            // 3. Parse text identifiers, using fixed fallback UUIDs for invalid values
            UUID personnelUuid;
            UUID movementUuid;

            try {
                personnelUuid = UUID.fromString(request.personnelId());
            } catch (IllegalArgumentException | NullPointerException e) {
                personnelUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            }

            try {
                movementUuid = UUID.fromString(request.movementId());
            } catch (IllegalArgumentException | NullPointerException e) {
                movementUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
            }

            // 4. Create entity
            CheckIn checkIn = new CheckIn(
                personnelUuid,
                location,
                personnelUuid
            );

            checkIn.setMovementId(movementUuid);
            checkIn.setNotes(request.notes());

            try {
                checkIn.setStatus(request.status() == null
                    ? CheckInStatus.SAFE
                    : CheckInStatus.valueOf(request.status().toUpperCase(java.util.Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                checkIn.setStatus(CheckInStatus.SAFE);
            }

            checkIn.setSource(TelemetrySource.MANUAL);

            // 5. Save
            CheckIn saved = checkInRepository.save(checkIn);

            CheckInResponse response = new CheckInResponse(
                saved.getId(),
                saved.getPersonnelId(),
                saved.getMovementId(),
                saved.getLocation().getY(),
                saved.getLocation().getX(),
                saved.getStatus().name(),
                saved.getNotes(),
                saved.getRecordedAt()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.err.println("Failed to save check-in: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process check-in.");
        }
    }
}