package com.spata.backend.domain.movement;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "field_movements")
public class FieldMovement {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "mission_name", nullable = false, length = 255)
    private String missionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "movement_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private MovementStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", nullable = false, columnDefinition = "transport_mode")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TransportMode transportMode;

    // The planned route (LineString)
    @Column(name = "planned_route", columnDefinition = "geography(LineString, 4326)")
    private LineString plannedRoute;

    // Current location (Point)
    @Column(name = "current_location", columnDefinition = "geography(Point, 4326)")
    private Point currentLocation;

    @Column(name = "planned_departure", nullable = false)
    private OffsetDateTime plannedDeparture;

    @Column(name = "planned_arrival", nullable = false)
    private OffsetDateTime plannedArrival;

    @Column(name = "actual_departure")
    private OffsetDateTime actualDeparture;

    @Column(name = "actual_arrival")
    private OffsetDateTime actualArrival;

    // Array of personnel UUIDs
    @Column(name = "personnel_ids", nullable = false, columnDefinition = "uuid[]")
    private UUID[] personnelIds;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected FieldMovement() {}

    public FieldMovement(String missionName, UUID createdBy, UUID regionId) {
        this.id = UUID.randomUUID();
        this.missionName = missionName;
        this.createdBy = createdBy;
        this.regionId = regionId;
        this.status = MovementStatus.PLANNED;
        this.transportMode = TransportMode.VEHICLE;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getMissionName() { return missionName; }
    public void setMissionName(String missionName) { this.missionName = missionName; }
    public MovementStatus getStatus() { return status; }
    public void setStatus(MovementStatus status) { this.status = status; }
    public TransportMode getTransportMode() { return transportMode; }
    public void setTransportMode(TransportMode transportMode) { this.transportMode = transportMode; }
    public LineString getPlannedRoute() { return plannedRoute; }
    public void setPlannedRoute(LineString plannedRoute) { this.plannedRoute = plannedRoute; }
    public Point getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(Point currentLocation) { this.currentLocation = currentLocation; }
    public OffsetDateTime getPlannedDeparture() { return plannedDeparture; }
    public void setPlannedDeparture(OffsetDateTime plannedDeparture) { this.plannedDeparture = plannedDeparture; }
    public OffsetDateTime getPlannedArrival() { return plannedArrival; }
    public void setPlannedArrival(OffsetDateTime plannedArrival) { this.plannedArrival = plannedArrival; }
    public OffsetDateTime getActualDeparture() { return actualDeparture; }
    public void setActualDeparture(OffsetDateTime actualDeparture) { this.actualDeparture = actualDeparture; }
    public OffsetDateTime getActualArrival() { return actualArrival; }
    public void setActualArrival(OffsetDateTime actualArrival) { this.actualArrival = actualArrival; }
    public UUID[] getPersonnelIds() { return personnelIds; }
    public void setPersonnelIds(UUID[] personnelIds) { this.personnelIds = personnelIds; }
    public UUID getRegionId() { return regionId; }
    public void setRegionId(UUID regionId) { this.regionId = regionId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}