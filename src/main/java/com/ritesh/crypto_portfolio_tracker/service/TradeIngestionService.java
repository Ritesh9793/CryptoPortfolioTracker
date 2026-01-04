package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritesh.crypto_portfolio_tracker.entity.ApiKey;
import com.ritesh.crypto_portfolio_tracker.entity.Trade;
import com.ritesh.crypto_portfolio_tracker.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TradeIngestionService {

    private final BinanceService binanceService;
    private final ApiKeyService apiKeyService;
    private final TradeRepository tradeRepo;

    public TradeIngestionService(BinanceService binanceService,
                                 ApiKeyService apiKeyService,
                                 TradeRepository tradeRepo) {
        this.binanceService = binanceService;
        this.apiKeyService = apiKeyService;
        this.tradeRepo = tradeRepo;
    }

    public void syncUserTrades(Long userId, ApiKey encryptedKey, String symbol) throws Exception {

        ApiKey key = apiKeyService.decrypt(encryptedKey);

        JsonNode arr = binanceService.getTrades(key, symbol);

        if (!arr.isArray()) return;

        for (JsonNode t : arr) {

            String tradeId = t.path("id").asText();
            if (tradeRepo.findByUserIdAndExchangeTradeId(userId, tradeId).isPresent())
                continue; // skip duplicates

            Trade trade = Trade.builder()
                    .userId(userId)
                    .symbol(t.path("symbol").asText())
                    .side(t.path("isBuyer").asBoolean() ? "BUY" : "SELL")
                    .price(t.path("price").asDouble())
                    .quantity(t.path("qty").asDouble())
                    .amount(t.path("quoteQty").asDouble())
                    .exchangeTradeId(tradeId)
                    .time(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(t.path("time").asLong()),
                            ZoneId.systemDefault()))
                    .build();

            tradeRepo.save(trade);
        }
    }
}
