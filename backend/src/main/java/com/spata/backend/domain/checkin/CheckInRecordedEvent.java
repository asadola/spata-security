package com.spata.backend.domain.checkin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckInRecordedEvent(
    UUID checkInId,
    UUID personnelId,
    UUID movementId,
    CheckInStatus status,
    Double latitude,
    Double longitude,
    OffsetDateTime recordedAt
) {}