package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritesh.crypto_portfolio_tracker.entity.Trade;
import com.ritesh.crypto_portfolio_tracker.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TradeService {

    private final TradeRepository repo;

    public TradeService(TradeRepository repo) {
        this.repo = repo;
    }

    /**
     * Save Binance trades safely (idempotent)
     */
    public void saveTradesFromBinance(JsonNode trades,
                                      Long userId,
                                      Long exchangeId) {

        for (JsonNode t : trades) {

            String exchangeTradeId = t.get("id").asText();

            // 🔒 Deduplication
            boolean exists = repo
                    .findByUserIdAndExchangeTradeId(userId, exchangeTradeId)
                    .isPresent();

            if (exists) continue;

            Trade trade = Trade.builder()
                    .userId(userId)
                    .exchangeId(exchangeId)
                    .exchangeTradeId(exchangeTradeId)
                    .symbol(t.get("symbol").asText())
                    .side(t.get("isBuyer").asBoolean() ? "BUY" : "SELL")
                    .quantity(t.get("qty").asDouble())
                    .price(t.get("price").asDouble())
                    .fee(t.get("commission").asDouble())
                    .feeAsset(t.get("commissionAsset").asText())
                    .time(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(t.get("time").asLong()),
                            ZoneId.systemDefault()
                    ))
                    .build();

            repo.save(trade);
        }
    }
}
