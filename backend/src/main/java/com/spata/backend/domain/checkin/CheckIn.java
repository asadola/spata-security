package com.spata.backend.domain.checkin;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_check_ins")
public class CheckIn {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "personnel_id", nullable = false)
    private UUID personnelId;

    @Column(name = "movement_id")
    private UUID movementId;

    @Column(name = "location", nullable = false, columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "check_in_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CheckInStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, columnDefinition = "telemetry_source")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TelemetrySource source;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    protected CheckIn() {}

    public CheckIn(UUID personnelId, Point location, UUID createdBy) {
        this.id = UUID.randomUUID();
        this.personnelId = personnelId;
        this.location = location;
        this.recordedAt = OffsetDateTime.now();
        this.createdBy = createdBy;
        this.status = CheckInStatus.SAFE;
        this.source = TelemetrySource.MANUAL;
        this.createdAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPersonnelId() { return personnelId; }
    public void setPersonnelId(UUID personnelId) { this.personnelId = personnelId; }
    public UUID getMovementId() { return movementId; }
    public void setMovementId(UUID movementId) { this.movementId = movementId; }
    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }
    public OffsetDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(OffsetDateTime recordedAt) { this.recordedAt = recordedAt; }
    public CheckInStatus getStatus() { return status; }
    public void setStatus(CheckInStatus status) { this.status = status; }
    public TelemetrySource getSource() { return source; }
    public void setSource(TelemetrySource source) { this.source = source; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}