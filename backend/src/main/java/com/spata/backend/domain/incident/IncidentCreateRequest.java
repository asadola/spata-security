package com.spata.backend.domain.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IncidentCreateRequest(
    @NotBlank(message = "Title is required")
    String title,

    String description,

    @NotBlank(message = "Incident type is required")
    String incidentType,

    @NotNull(message = "Latitude is required")
    Double latitude,

    @NotNull(message = "Longitude is required")
    Double longitude,

    @NotNull(message = "Reported by user ID is required")
    java.util.UUID reportedBy
) {}