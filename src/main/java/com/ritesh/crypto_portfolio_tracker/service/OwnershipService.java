// ============================================================
// OwnershipService.java
// ============================================================
package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class OwnershipService {

    @Value("${etherscan.api.key:}")
    private String etherscanKey;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * @return true if ownership renounced, false if not, null if unknown
     */
    public Boolean isOwnershipRenounced(String contract, String chain) {

        if (!"ethereum".equalsIgnoreCase(chain) || etherscanKey.isBlank()) {
            return null;
        }

        try {
            String url =
                    "https://api.etherscan.io/api"
                            + "?module=contract"
                            + "&action=getsourcecode"
                            + "&address=" + contract
                            + "&apikey=" + etherscanKey;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) return null;

            JsonNode result = mapper.readTree(resp.body())
                    .path("result");

            if (!result.isArray() || result.isEmpty()) return null;

            JsonNode info = result.get(0);
            String owner = info.path("Owner").asText();

            return owner == null
                    || owner.isBlank()
                    || owner.equalsIgnoreCase("0x0000000000000000000000000000000000000000");

        } catch (Exception e) {
            return null;
        }
    }
}
