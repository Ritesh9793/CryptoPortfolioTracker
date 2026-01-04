package com.ritesh.crypto_portfolio_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritesh.crypto_portfolio_tracker.entity.PriceSnapshot;
import com.ritesh.crypto_portfolio_tracker.repository.PriceSnapshotRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceSnapshotService {

    private final PricingService pricingService;
    private final PriceSnapshotRepository snapshotRepository;

    // list of coin ids you want snapshots for (example)
    private final List<String> trackedCoins = List.of("bitcoin","ethereum","tether","binancecoin"); // extend as needed

    public PriceSnapshotService(PricingService pricingService,
                                PriceSnapshotRepository snapshotRepository) {
        this.pricingService = pricingService;
        this.snapshotRepository = snapshotRepository;
    }

    // run every 5 minutes
    @Scheduled(cron = "${snapshot.cron:0 0/5 * * * *}")
    public void captureSnapshots() {
        trackedCoins.forEach(coinId -> {
            try {
                JsonNode node = pricingService.getPriceByCoinId(coinId);
                if (node.has(coinId)) {
                    JsonNode info = node.get(coinId);
                    double price = info.path("usd").asDouble();
                    double marketCap = info.path("usd_market_cap").asDouble(0.0);

                    PriceSnapshot snap = PriceSnapshot.builder()
                            .assetSymbol(coinId) // use coinId or map to symbol
                            .priceUsd(price)
                            .marketCap(marketCap)
                            .source("coingecko")
                            .capturedAt(LocalDateTime.now())
                            .build();

                    snapshotRepository.save(snap);
                }
            } catch (Exception e) {
                // log and continue -- avoid throwing to prevent scheduler stopping
                // logger.warn("snapshot failed for " + coinId, e);
                // todo: add logger letter - ritesh
            }
        });
    }
}
