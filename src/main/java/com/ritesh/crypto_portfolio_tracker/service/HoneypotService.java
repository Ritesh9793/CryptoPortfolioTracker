// ============================================================
// HoneypotService.java
// ============================================================
package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class HoneypotService {

    private static final String HONEYPOT_API =
            "https://api.honeypot.is/v2/IsHoneypot";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * @return true if honeypot detected, false if safe, null if unknown
     */
    public Boolean isHoneypot(String contract, String chain) {

        try {
            String url = HONEYPOT_API
                    + "?address=" + contract
                    + "&chain=" + chain;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return null;

            JsonNode root = mapper.readTree(resp.body());
            return root.path("honeypotResult").path("isHoneypot").asBoolean();

        } catch (Exception e) {
            return null;
        }
    }
}
