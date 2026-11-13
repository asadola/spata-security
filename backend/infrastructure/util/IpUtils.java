package com.spata.backend.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {
    
    /**
     * Extracts the real client IP, even if they are behind a proxy or load balancer.
     */
    public static String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For can contain multiple IPs. The first one is the client.
        return xfHeader.split(",")[0].trim();
    }
}