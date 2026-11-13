package com.spata.backend.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckInResponse(
    UUID id,
    UUID personnelId,
    UUID movementId,
    Double latitude,
    Double longitude,
    String status,
    String notes,
    OffsetDateTime recordedAt
) {}
