package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CoinGeckoService {

    private static final String BASE =
            "https://api.coingecko.com/api/v3/simple/price";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Returns current USD price for a CoinGecko token id
     * Example id: bitcoin, ethereum, solana
     */
    public Double getPrice(String coinGeckoId) {

        try {
            String url = BASE
                    + "?ids=" + coinGeckoId
                    + "&vs_currencies=usd";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                return null;
            }

            JsonNode root = mapper.readTree(resp.body());
            JsonNode priceNode = root.path(coinGeckoId).path("usd");

            if (priceNode.isMissingNode() || priceNode.isNull()) {
                return null;
            }

            return priceNode.asDouble();

        } catch (Exception e) {
            return null;
        }
    }
}
