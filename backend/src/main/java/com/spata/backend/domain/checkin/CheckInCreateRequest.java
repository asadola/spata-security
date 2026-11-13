package com.spata.backend.domain.checkin;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckInCreateRequest(
    @NotNull(message = "Personnel ID is required")
    UUID personnelId,

    UUID movementId,

    @NotNull(message = "Latitude is required")
    Double latitude,

    @NotNull(message = "Longitude is required")
    Double longitude,

    CheckInStatus status,

    TelemetrySource source,

    String notes,

    @NotNull(message = "Created by is required")
    UUID createdBy
) {}