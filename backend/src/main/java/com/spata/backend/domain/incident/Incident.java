package com.spata.backend.domain.incident;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "incident_type", nullable = false, length = 100)
    private String incidentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, columnDefinition = "incident_severity")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "incident_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private IncidentStatus status;

    @Column(name = "location", columnDefinition = "geography(Point, 4326)")
    private Point location;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "reported_by", nullable = false)
    private UUID reportedBy;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "region_id")
    private UUID regionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // JPA requires a default no-args constructor
    protected Incident() {}

    // Custom constructor for easy creation
    public Incident(String title, Point location, UUID reportedBy) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.location = location;
        this.reportedBy = reportedBy;
        this.severity = IncidentSeverity.MEDIUM;
        this.status = IncidentStatus.REPORTED;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        this.createdBy = reportedBy;
    }

    // --- EXPLICIT GETTERS AND SETTERS ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIncidentType() { return incidentType; }
    public void setIncidentType(String incidentType) { this.incidentType = incidentType; }
    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public UUID getReportedBy() { return reportedBy; }
    public void setReportedBy(UUID reportedBy) { this.reportedBy = reportedBy; }
    public UUID getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UUID assignedTo) { this.assignedTo = assignedTo; }
    public UUID getRegionId() { return regionId; }
    public void setRegionId(UUID regionId) { this.regionId = regionId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}