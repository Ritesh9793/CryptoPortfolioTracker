package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritesh.crypto_portfolio_tracker.dto.api.HoldingRequest;
import com.ritesh.crypto_portfolio_tracker.entity.Holding;
import com.ritesh.crypto_portfolio_tracker.repository.HoldingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HoldingService {

    private final HoldingRepository repo;

    public HoldingService(HoldingRepository repo) {
        this.repo = repo;
    }

    // --------------------------
    // From Binance API Sync
    // --------------------------
    public void updateFromBinance(JsonNode accountData, Long userId) {

        JsonNode balances = accountData.get("balances");

        balances.forEach(bal -> {
            String asset = bal.get("asset").asText();
            double free = bal.get("free").asDouble();
            double locked = bal.get("locked").asDouble();

            double quantity = free + locked;
            if (quantity == 0) return;

            repo.findByUserIdAndExchangeIdAndAssetSymbol(userId, 1L, asset)
                    .ifPresentOrElse(existing -> {
                        existing.setQuantity(quantity);
                        repo.save(existing);
                    }, () -> {
                        repo.save(
                                Holding.builder()
                                        .userId(userId)
                                        .exchangeId(1L)
                                        .assetSymbol(asset)
                                        .quantity(quantity)
                                        .avgCost(0.0)
                                        .walletType("exchange")
                                        .build()
                        );
                    });
        });
    }

    // --------------------------
    // Manual Add
    // --------------------------
    public Holding addHolding(Long userId, HoldingRequest request) {

        Holding h = Holding.builder()
                .userId(userId)
                .assetSymbol(request.getAssetSymbol())
                .quantity(request.getQuantity())
                .avgCost(request.getAvgCost())
                .walletType(request.getWalletType())
                .exchangeId(request.getExchangeId())
                .address(request.getAddress())
                .build();

        return repo.save(h);
    }

    // --------------------------
    // Manual Edit
    // --------------------------
    public Holding updateHolding(Long id, Long userId, HoldingRequest req) {

        Holding existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Holding not found"));

        if (!existing.getUserId().equals(userId))
            throw new RuntimeException("Unauthorized edit");

        existing.setQuantity(req.getQuantity());
        existing.setAvgCost(req.getAvgCost());
        existing.setAddress(req.getAddress());
        existing.setExchangeId(req.getExchangeId());
        existing.setWalletType(req.getWalletType());

        return repo.save(existing);
    }

    // --------------------------
    // Fetch All Holdings for user
    // --------------------------
    public List<Holding> getHoldings(Long userId) {
        return repo.findByUserId(userId);
    }
}
