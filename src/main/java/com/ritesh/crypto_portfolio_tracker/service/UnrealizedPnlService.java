package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.dto.pnl.UnrealizedPnlEntry;
import com.ritesh.crypto_portfolio_tracker.entity.Holding;
import com.ritesh.crypto_portfolio_tracker.repository.HoldingRepository;
import com.ritesh.crypto_portfolio_tracker.repository.PriceSnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnrealizedPnlService {

    private final HoldingRepository holdingRepo;
    private final PriceSnapshotRepository snapshotRepo;

    public UnrealizedPnlService(HoldingRepository holdingRepo, PriceSnapshotRepository snapshotRepo) {
        this.holdingRepo = holdingRepo;
        this.snapshotRepo = snapshotRepo;
    }

    public List<UnrealizedPnlEntry> computeUnrealizedPnl(Long userId) {
        List<Holding> holdings = holdingRepo.findByUserId(userId);
        List<UnrealizedPnlEntry> out = new ArrayList<>();

        for (Holding h : holdings) {
            double qty = h.getQuantity() == null ? 0.0 : h.getQuantity();
            double avgCost = h.getAvgCost() == null ? 0.0 : h.getAvgCost();
            double currentPrice = snapshotRepo.findLatestPrice(h.getAssetSymbol()).orElse(0.0);
            double value = qty * currentPrice;
            double unrealized = qty * (currentPrice - avgCost);

            out.add(UnrealizedPnlEntry.builder()
                    .symbol(h.getAssetSymbol())
                    .quantity(qty)
                    .avgCost(avgCost)
                    .currentPrice(currentPrice)
                    .value(value)
                    .unrealizedPnl(unrealized)
                    .build());
        }

        return out;
    }

    public double totalUnrealized(Long userId) {
        return computeUnrealizedPnl(userId).stream().mapToDouble(UnrealizedPnlEntry::getUnrealizedPnl).sum();
    }
}
