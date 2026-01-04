package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritesh.crypto_portfolio_tracker.entity.ApiKey;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
public class BinanceService {

    private static final String BASE = "https://api.binance.com";
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * ApiKey passed here MUST be decrypted via ApiKeyService.decrypt()
     */
    private String sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(raw);
    }

    // -----------------------------
    // ACCOUNT INFO
    // -----------------------------
    public JsonNode getAccountInfo(ApiKey dec) throws Exception {

        long timestamp = System.currentTimeMillis();
        String query = "timestamp=" + timestamp + "&recvWindow=5000";

        String signature = sign(query, dec.getApiSecretEnc());
        String url = BASE + "/api/v3/account?" + query + "&signature=" + signature;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-MBX-APIKEY", dec.getApiKeyEnc())
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new RuntimeException("Binance error: " + resp.body());
        }

        return mapper.readTree(resp.body());
    }

    // -----------------------------
    // TRADE HISTORY
    // -----------------------------
    public JsonNode getTrades(ApiKey dec, String symbol) throws Exception {

        long timestamp = System.currentTimeMillis();
        String query = "symbol=" + symbol + "&timestamp=" + timestamp + "&recvWindow=5000";

        String signature = sign(query, dec.getApiSecretEnc());
        String url = BASE + "/api/v3/myTrades?" + query + "&signature=" + signature;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-MBX-APIKEY", dec.getApiKeyEnc())
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new RuntimeException("Binance error: " + resp.body());
        }

        return mapper.readTree(resp.body());
    }
}
