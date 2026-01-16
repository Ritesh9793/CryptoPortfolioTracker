package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HexFormat;
import java.nio.charset.StandardCharsets;

@Service
public class PricingService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    @Value("${coingecko.base.url:https://api.coingecko.com/api/v3}")
    private String coingeckoBase;


    public JsonNode getPriceByCoinId(String coinId) throws Exception {
        String url = String.format("%s/simple/price?ids=%s&vs_currencies=usd&include_market_cap=true", coingeckoBase, coinId);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(resp.body());
    }
}
