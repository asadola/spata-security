package com.spata.backend.application.service;

import com.spata.backend.application.dto.RegistrationRequest;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
public class UserRegistrationService {

    // In production, inject these from application.properties
    private final String keycloakUrl = "http://localhost:8082";
    private final String realm = "spata-realm";

    public void registerUser(RegistrationRequest request) {
        // 1. Initialize Keycloak Admin Client (using master admin credentials for dev)
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build();

        RealmResource realmResource = keycloak.realm(realm);

        // 2. Build the User Representation
        UserRepresentation user = new UserRepresentation();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setEnabled(true);
        user.setRequiredActions(Collections.emptyList()); // Critical: clear required actions so the user can log in immediately

        // 3. Set Password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false); // Crucial: so they don't have to change it on first login
        user.setCredentials(Collections.singletonList(credential));

        // 4. Create User in Keycloak
        Response response = realmResource.users().create(user);
        
        if (response.getStatus() != 201) {
            if (response.getStatus() == 409) {
                response.close();
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this username or email already exists. Please sign in or use different details."
                );
            }
            String error = response.readEntity(String.class);
            response.close();
            throw new RuntimeException("Failed to create user in Keycloak: " + error);
        }
        response.close();

        // 5. Assign the Role (AGENT or DISPATCHER)
        String userId = realmResource.users().search(request.username()).get(0).getId();
        String roleName = request.role().toUpperCase(); // Ensure it's uppercase

        // Find the role in Keycloak
        List<RoleRepresentation> roles = realmResource.roles().list();
        RoleRepresentation targetRole = roles.stream()
                .filter(r -> r.getName().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found in Keycloak. Please create it first."));

        // Assign role to user
        realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(targetRole));
    }
}