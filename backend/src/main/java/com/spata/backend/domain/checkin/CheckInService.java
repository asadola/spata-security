package com.spata.backend.domain.checkin;

import com.spata.backend.domain.movement.FieldMovement;
import com.spata.backend.domain.movement.FieldMovementRepository;
import com.spata.backend.domain.movement.MovementStatus;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final FieldMovementRepository movementRepository;
    private final KafkaTemplate<String, CheckInRecordedEvent> kafkaTemplate; // NEW
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private static final String TOPIC = "spata.checkins";

    public CheckInService(CheckInRepository checkInRepository, 
                          FieldMovementRepository movementRepository,
                          KafkaTemplate<String, CheckInRecordedEvent> kafkaTemplate) {
        this.checkInRepository = checkInRepository;
        this.movementRepository = movementRepository;
        this.kafkaTemplate = kafkaTemplate; // NEW
    }

    @Transactional
    public CheckIn recordCheckIn(CheckInCreateRequest request) {
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));

        CheckIn checkIn = new CheckIn(
            request.personnelId(),
            location,
            request.createdBy()
        );

        checkIn.setMovementId(request.movementId());
        checkIn.setStatus(request.status() != null ? request.status() : CheckInStatus.SAFE);
        checkIn.setSource(request.source() != null ? request.source() : TelemetrySource.MANUAL);
        checkIn.setNotes(request.notes());

        // 1. Update Movement if linked
        if (request.movementId() != null) {
            FieldMovement movement = movementRepository.findById(request.movementId())
                .orElseThrow(() -> new RuntimeException("Movement not found"));
            
            movement.setCurrentLocation(location);
            movement.setStatus(MovementStatus.IN_TRANSIT);
            movement.setUpdatedAt(java.time.OffsetDateTime.now());
            movementRepository.save(movement);
        }

        // 2. Save CheckIn
        CheckIn savedCheckIn = checkInRepository.save(checkIn);

        // 3. PUBLISH EVENT TO KAFKA
        CheckInRecordedEvent event = new CheckInRecordedEvent(
            savedCheckIn.getId(),
            savedCheckIn.getPersonnelId(),
            savedCheckIn.getMovementId(),
            savedCheckIn.getStatus(),
            savedCheckIn.getLocation().getY(), // Latitude
            savedCheckIn.getLocation().getX(), // Longitude
            savedCheckIn.getRecordedAt()
        );

        kafkaTemplate.send(TOPIC, savedCheckIn.getId().toString(), event);
        System.out.println(">>> Event published to Kafka topic: " + TOPIC);

        return savedCheckIn;
    }

    // ... keep existing query methods ...
    @Transactional(readOnly = true)
    public List<CheckIn> getCheckInsByPersonnel(UUID personnelId) {
        return checkInRepository.findByPersonnelIdOrderByRecordedAtDesc(personnelId);
    }

    @Transactional(readOnly = true)
    public List<CheckIn> getCheckInsByMovement(UUID movementId) {
        return checkInRepository.findByMovementIdOrderByRecordedAtAsc(movementId);
    }
}