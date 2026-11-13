package com.spata.backend.domain.checkin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckInResponse(
    UUID id,
    UUID personnelId,
    UUID movementId,
    Double latitude,
    Double longitude,
    OffsetDateTime recordedAt,
    CheckInStatus status,
    TelemetrySource source,
    String notes,
    OffsetDateTime createdAt
) {
    public static CheckInResponse from(CheckIn checkIn) {
        Double lat = null;
        Double lng = null;
        
        if (checkIn.getLocation() != null) {
            lng = checkIn.getLocation().getX();
            lat = checkIn.getLocation().getY();
        }

        return new CheckInResponse(
            checkIn.getId(),
            checkIn.getPersonnelId(),
            checkIn.getMovementId(),
            lat,
            lng,
            checkIn.getRecordedAt(),
            checkIn.getStatus(),
            checkIn.getSource(),
            checkIn.getNotes(),
            checkIn.getCreatedAt()
        );
    }
}