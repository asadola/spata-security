package com.spata.backend.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract roles and create the authentication token
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalName = jwt.getClaimAsString("preferred_username");
        
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Keycloak stores realm roles in the "realm_access" -> "roles" claim
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        
        if (realmAccess == null || realmAccess.isEmpty()) {
            return Collections.emptyList();
        }

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) {
            return Collections.emptyList();
        }

        // Convert each role to a Spring GrantedAuthority, prefixed with "ROLE_"
        return roles.stream()
                .filter(role -> role instanceof String)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + ((String) role).toUpperCase()))
                .collect(Collectors.toList());
    }
}