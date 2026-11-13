package com.spata.backend.domain.incident;

import com.spata.backend.application.service.VpnDetectionService;
import com.spata.backend.infrastructure.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final VpnDetectionService vpnDetectionService;

    public IncidentController(IncidentService incidentService, VpnDetectionService vpnDetectionService) {
        this.incidentService = incidentService;
        this.vpnDetectionService = vpnDetectionService;
    }

    @PostMapping
    public ResponseEntity<?> createIncident(@Valid @RequestBody IncidentCreateRequest request, HttpServletRequest httpRequest) {
        String clientIp = IpUtils.getClientIp(httpRequest);

        if (vpnDetectionService.isVpnOrProxy(clientIp)) {
            System.err.println("🚨 SECURITY ALERT: VPN/Proxy detected from IP: " + clientIp);
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Incidents cannot be reported from VPNs or Proxy networks for security verification."
            );
        }

        Incident savedIncident = incidentService.createIncident(request);
        return new ResponseEntity<>(IncidentResponse.from(savedIncident), HttpStatus.CREATED);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<IncidentResponse>> getNearbyIncidents(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10000") Double radiusInMeters
    ) {
        List<Incident> nearbyIncidents = incidentService.findIncidentsNearby(latitude, longitude, radiusInMeters);
        
        // THIS IS THE CRITICAL LINE: Map Entity to DTO
        List<IncidentResponse> responseList = nearbyIncidents.stream()
                .map(IncidentResponse::from)
                .toList();
                
        return ResponseEntity.ok(responseList);
    }
}