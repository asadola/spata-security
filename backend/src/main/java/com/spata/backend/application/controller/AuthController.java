package com.spata.backend.application.controller;

import com.spata.backend.application.dto.RegistrationRequest;
import com.spata.backend.application.service.UserRegistrationService;
import com.spata.backend.application.service.VpnDetectionService;
import com.spata.backend.infrastructure.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRegistrationService registrationService;
    private final VpnDetectionService vpnDetectionService;

    public AuthController(UserRegistrationService registrationService, VpnDetectionService vpnDetectionService) {
        this.registrationService = registrationService;
        this.vpnDetectionService = vpnDetectionService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
        @RequestBody RegistrationRequest request,
        HttpServletRequest httpRequest
    ) {
        
        // 1. BRUTAL VPN CHECK
        String clientIp = IpUtils.getClientIp(httpRequest);
        if (vpnDetectionService.isVpnOrProxy(clientIp)) {
            System.err.println("🚨 SECURITY ALERT: VPN/Proxy blocked during registration from IP: " + clientIp);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Registration is not allowed from VPN or Proxy networks.");
        }

        // 2. Validate Role
        if (!request.role().equalsIgnoreCase("AGENT") && !request.role().equalsIgnoreCase("DISPATCHER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role selected.");
        }

        // 3. Register User
        try {
            registrationService.registerUser(request);
            return ResponseEntity.ok(Map.of("message", "User registered successfully."));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed: " + e.getMessage());
        }
    }
}