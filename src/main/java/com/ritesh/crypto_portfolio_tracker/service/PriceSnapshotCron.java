package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.entity.PriceSnapshot;
import com.ritesh.crypto_portfolio_tracker.repository.PriceSnapshotRepository;
import com.ritesh.crypto_portfolio_tracker.repository.TokenRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PriceSnapshotCron {

    private final TokenRepository tokenRepo;
    private final CoinGeckoService gecko;
    private final PriceSnapshotRepository snapshotRepo;

    public PriceSnapshotCron(TokenRepository tokenRepo,
                             CoinGeckoService gecko,
                             PriceSnapshotRepository snapshotRepo) {
        this.tokenRepo = tokenRepo;
        this.gecko = gecko;
        this.snapshotRepo = snapshotRepo;
    }

    // Every 5 minutes
    @Scheduled(cron = "0 */5 * * * *")
    public void capturePrices() {

        tokenRepo.findAll().forEach(t -> {
            if (t.getCoingeckoId() == null || t.getCoingeckoId().equals("unknown")) return;

            Double price = gecko.getPrice(t.getCoingeckoId());
            if (price == null) return;

            Double lastPrice = snapshotRepo.findLatestPrice(t.getSymbol()).orElse(null);
            if (lastPrice != null && Math.abs(lastPrice - price) < 0.5) return; // skip small changes

            PriceSnapshot snap = PriceSnapshot.builder()
                    .assetSymbol(t.getSymbol())
                    .priceUsd(price)
                    .capturedAt(LocalDateTime.now())
                    .build();

            snapshotRepo.save(snap);
        });
    }
}
