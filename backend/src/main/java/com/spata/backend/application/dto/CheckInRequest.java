package com.spata.backend.application.dto;

import java.util.UUID;

public record CheckInRequest(
    String personnelId,
    String movementId,
    Double latitude,
    Double longitude,
    String status,
    String notes
) {}