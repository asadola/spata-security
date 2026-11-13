package com.spata.backend.application.dto;

public record RegistrationRequest(
    String firstName,
    String lastName,
    String username,
    String email,
    String password,
    String role
) {}