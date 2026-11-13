package com.spata.backend.domain.movement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FieldMovementCreateRequest(
    @NotBlank(message = "Mission name is required")
    String missionName,

    @NotNull(message = "Transport mode is required")
    TransportMode transportMode,

    // Planned route as list of [lat, lng] pairs
    @NotEmpty(message = "Planned route must have at least 2 points")
    List<double[]> plannedRoute,

    @NotNull(message = "Planned departure is required")
    OffsetDateTime plannedDeparture,

    @NotNull(message = "Planned arrival is required")
    OffsetDateTime plannedArrival,

    @NotEmpty(message = "At least one personnel ID is required")
    List<UUID> personnelIds,

    @NotNull(message = "Region ID is required")
    UUID regionId,

    @NotNull(message = "Created by is required")
    UUID createdBy
) {}