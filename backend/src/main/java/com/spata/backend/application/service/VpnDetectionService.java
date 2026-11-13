package com.spata.backend.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class VpnDetectionService {

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isVpnOrProxy(String ip) {
        if (ip == null || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return false;
        }

        try {
            String url = "http://ipapi.co/" + ip + "/json/";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                Boolean proxy = (Boolean) response.get("proxy");
                Boolean vpn = (Boolean) response.get("vpn");
                Boolean tor = (Boolean) response.get("tor");

                return Boolean.TRUE.equals(proxy) || Boolean.TRUE.equals(vpn) || Boolean.TRUE.equals(tor);
            }
        } catch (Exception e) {
            System.err.println("Failed to check IP for VPN: " + ip + " - " + e.getMessage());
        }
        return false;
    }
}
