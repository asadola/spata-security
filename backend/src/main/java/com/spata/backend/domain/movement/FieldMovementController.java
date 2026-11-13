package com.spata.backend.domain.movement;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movements")
public class FieldMovementController {

    private final FieldMovementService movementService;

    public FieldMovementController(FieldMovementService movementService) {
        this.movementService = movementService;
    }

    @PostMapping
    public ResponseEntity<FieldMovementResponse> createMovement(@Valid @RequestBody FieldMovementCreateRequest request) {
        FieldMovement savedMovement = movementService.createMovement(request);
        return new ResponseEntity<>(FieldMovementResponse.from(savedMovement), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<FieldMovementResponse> updateLocation(
            @PathVariable UUID id,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        FieldMovement updatedMovement = movementService.updateCurrentLocation(id, latitude, longitude);
        return ResponseEntity.ok(FieldMovementResponse.from(updatedMovement));
    }

    @GetMapping("/active")
    public ResponseEntity<List<FieldMovementResponse>> getActiveMovements() {
        List<FieldMovement> movements = movementService.getActiveMovements();
        List<FieldMovementResponse> responses = movements.stream()
            .map(FieldMovementResponse::from)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/personnel/{personnelId}")
    public ResponseEntity<List<FieldMovementResponse>> getMovementsByPersonnel(@PathVariable UUID personnelId) {
        List<FieldMovement> movements = movementService.getMovementsByPersonnel(personnelId);
        List<FieldMovementResponse> responses = movements.stream()
            .map(FieldMovementResponse::from)
            .toList();
        return ResponseEntity.ok(responses);

    
    }
        @GetMapping("/near-incident")
    public ResponseEntity<List<FieldMovementResponse>> getMovementsNearIncident(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "50000") Double radiusInMeters
    ) {
        List<FieldMovement> movements = movementService.getMovementsNearIncident(latitude, longitude, radiusInMeters);
        List<FieldMovementResponse> responses = movements.stream()
            .map(FieldMovementResponse::from)
            .toList();
        return ResponseEntity.ok(responses);
    }
        /**
     * GET /api/v1/movements/deviated?thresholdMeters=5000
     * Returns only movements that have deviated MORE than the threshold.
     */
    @GetMapping("/deviated")
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    public ResponseEntity<List<MovementWithDeviationResponse>> getDeviatedMovements(
            @RequestParam(defaultValue = "5000") Double thresholdMeters
    ) {
        List<MovementWithDeviationResponse> deviated = movementService.getDeviatedMovements(thresholdMeters);
        return ResponseEntity.ok(deviated);
    }


        // ... existing code ...
    /**
     * GET /api/v1/movements/with-deviation
     * Returns ALL active movements with their deviation distances.
     * Perfect for color-coded dashboard maps.
     */
    @GetMapping("/with-deviation")
    public ResponseEntity<List<MovementWithDeviationResponse>> getAllMovementsWithDeviation() {
        List<MovementWithDeviationResponse> all = movementService.getAllMovementsWithDeviation();
        return ResponseEntity.ok(all);
    }
}