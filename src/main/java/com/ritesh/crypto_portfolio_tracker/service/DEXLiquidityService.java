package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;

@Service
public class DEXLiquidityService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Uniswap (Ethereum)
    private static final String UNISWAP_URL = "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2";

    public Double getLiquidityUsd(String contract) {
        try {
            String query = """
            { pairs(where:{token0:"%s"}) { reserveUSD } }
            """.formatted(contract.toLowerCase());

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(UNISWAP_URL))
                    .POST(HttpRequest.BodyPublishers.ofString("{\"query\":\"" + query + "\"}"))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(resp.body());
            JsonNode pairs = root.path("data").path("pairs");

            if (pairs.isArray() && !pairs.isEmpty()) {
                return pairs.get(0).path("reserveUSD").asDouble(0.0);
            }

        } catch (Exception ignored) {}

        return null;
    }
}
