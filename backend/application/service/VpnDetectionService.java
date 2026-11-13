package com.spata.backend.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class VpnDetectionService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Checks if an IP address is a known VPN, Proxy, or Tor node.
     */
    public boolean isVpnOrProxy(String ip) {
        // Ignore localhost for development
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return false; 
        }

        try {
            // Using ipapi.co (Free tier, no API key required for basic checks)
            String url = "http://ipapi.co/" + ip + "/json/";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                Boolean proxy = (Boolean) response.get("proxy");
                Boolean vpn = (Boolean) response.get("vpn");
                Boolean tor = (Boolean) response.get("tor");
                
                // If any of these are true, block it!
                return Boolean.TRUE.equals(proxy) || Boolean.TRUE.equals(vpn) || Boolean.TRUE.equals(tor);
            }
        } catch (Exception e) {
            System.err.println("Failed to check IP for VPN: " + ip + " - " + e.getMessage());
            // In a strict environment, you might want to return true here to block on error.
        }
        return false;
    }
}