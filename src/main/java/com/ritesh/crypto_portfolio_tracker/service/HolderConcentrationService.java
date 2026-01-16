package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;

@Service
public class HolderConcentrationService {

    @Value("${etherscan.api.key:}")
    private String etherscanKey;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public Double getTopHolderConcentration(String contract) {
        try {
            String url =
                    "https://api.etherscan.io/api?module=token&action=tokenholderlist&address=%s&page=1&offset=10&apikey=%s"
                            .formatted(contract, etherscanKey);

            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(resp.body());
            JsonNode holders = root.path("result");

            if (!holders.isArray() || holders.isEmpty()) return null;

            double totalSupply = 0;
            double top10 = 0;

            for (JsonNode h : holders) {
                double balance = h.path("Balance").asDouble(0);
                top10 += balance;
            }

            totalSupply = fetchTotalSupply(contract);

            if (totalSupply == 0) return null;

            return top10 / totalSupply;

        } catch (Exception ignored) {}

        return null;
    }

    private double fetchTotalSupply(String contract) {
        try {
            String url =
                    "https://api.etherscan.io/api?module=stats&action=tokensupply&contractaddress=%s&apikey=%s"
                            .formatted(contract, etherscanKey);

            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(resp.body());

            return root.path("result").asDouble(0);

        } catch (Exception ignored) {}

        return 0;
    }
}
