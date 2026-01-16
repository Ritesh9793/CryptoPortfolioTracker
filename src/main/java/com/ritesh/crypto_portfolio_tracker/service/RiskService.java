package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritesh.crypto_portfolio_tracker.entity.ScamToken;
import com.ritesh.crypto_portfolio_tracker.repository.ScamTokenRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Service
public class RiskService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();
    private final ScamTokenRepository scamRepo;

    @Value("${etherscan.api.key:}")
    private String etherscanKey;

    @Value("${cryptoscamdb.base.url:https://api.cryptoscamdb.org/v1}")
    private String cryptoscamdbBase;

    public RiskService(ScamTokenRepository scamRepo) {
        this.scamRepo = scamRepo;
    }

    // ----------------------------------------------------
    //  PUBLIC: Master Risk Check (Chain-Aware)
    // ----------------------------------------------------
    public RiskCheckResult checkContract(String contractAddress, String chain) {

        String normalized = normalize(contractAddress);

        // -------- 1) Check DB Cache --------
        var cached = scamRepo.findByContractAddress(normalized);
        if (cached.isPresent()) {
            ScamToken st = cached.get();
            return new RiskCheckResult(st.getRiskLevel(), "cached:" + st.getSource());
        }

        // -------- 2) CryptoScamDB (Ethereum Only) --------
        RiskCheckResult scamDbResult = checkCryptoScamDb(normalized, chain);
        if (scamDbResult.riskLevel().equals("high")) {
            saveScam(normalized, chain, "high", "cryptoscamdb");
            return scamDbResult;
        }

        // -------- 3) Etherscan Verification (Ethereum Only) --------
        RiskCheckResult ethResult = checkEtherscan(normalized, chain);
        if (ethResult.riskLevel().equals("medium")) {
            saveScam(normalized, chain, "medium", "etherscan-unverified");
            return ethResult;
        }

        // -------- 4) LOW RISK (DEFAULT) --------
        saveScam(normalized, chain, "low", "heuristic");
        return new RiskCheckResult("low", "No flags found");
    }


    // ----------------------------------------------------
    //  CRYPTOSCAMDB CHECK (Chain-Aware)
    // ----------------------------------------------------
    private RiskCheckResult checkCryptoScamDb(String contract, String chain) {

        // Only Ethereum supported by CryptoScamDB
        if (!chain.equalsIgnoreCase("ethereum")) {
            return new RiskCheckResult("low", "CryptoScamDB does not support this chain.");
        }

        try {
            String url = String.format("%s/check/%s", cryptoscamdbBase, contract);

            HttpRequest req =
                    HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(resp.body());

            boolean scam = root.path("result").path("is_scam").asBoolean(false);

            if (scam) {
                String category = root.path("result").path("category").asText("unknown");
                return new RiskCheckResult("high",
                        "Listed on CryptoScamDB (" + category + ")");
            }

        } catch (Exception ignored) {}

        return new RiskCheckResult("low", "Not listed on CryptoScamDB");
    }


    // ----------------------------------------------------
    //  ETHERSCAN CHECK (Verification + Proxy Detection)
    // ----------------------------------------------------
    private RiskCheckResult checkEtherscan(String contract, String chain) {

        // Only Ethereum supported
        if (!chain.equalsIgnoreCase("ethereum")) {
            return new RiskCheckResult("low", "Etherscan scanning skipped (wrong chain)");
        }

        if (etherscanKey == null || etherscanKey.isBlank()) {
            return new RiskCheckResult("low", "Missing Etherscan API key");
        }

        try {
            String url = String.format(
                    "https://api.etherscan.io/api?module=contract&action=getsourcecode&address=%s&apikey=%s",
                    contract, etherscanKey
            );

            HttpRequest req =
                    HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(resp.body());
            if (!root.has("result") || root.path("result").isEmpty()) {
                return new RiskCheckResult("medium",
                        "Etherscan returned no contract info");
            }

            JsonNode info = root.path("result").get(0);

            String sourceCode = info.path("SourceCode").asText();
            String proxy = info.path("Proxy").asText("0");

            boolean isVerified = sourceCode != null && !sourceCode.isEmpty();
            boolean isProxy = proxy.equals("1");

            if (!isVerified) {
                return new RiskCheckResult("medium",
                        "Unverified contract on Etherscan");
            }

            if (isProxy) {
                return new RiskCheckResult("medium",
                        "Proxy contract (Higher upgrade risk)");
            }

        } catch (Exception ignored) {}

        return new RiskCheckResult("low", "Verified contract on Etherscan");
    }


    // ----------------------------------------------------
    //  Utility: Save ScamToken
    // ----------------------------------------------------
    private void saveScam(String address, String chain, String level, String source) {
        ScamToken st = ScamToken.builder()
                .contractAddress(address)
                .chain(chain)
                .riskLevel(level)
                .source(source)
                .lastSeen(LocalDateTime.now())
                .build();

        scamRepo.save(st);
    }


    // ----------------------------------------------------
    //  Utility: Normalize Ethereum Address
    // ----------------------------------------------------
    private String normalize(String address) {
        return address.trim().toLowerCase();
    }


    // ----------------------------------------------------
    //  DTO Result
    // ----------------------------------------------------
    public record RiskCheckResult(String riskLevel, String details) {}
}
