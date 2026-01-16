package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.dto.pnl.RealizedPnlSummary;
import com.ritesh.crypto_portfolio_tracker.dto.pnl.UnrealizedPnlEntry;
import com.ritesh.crypto_portfolio_tracker.repository.HoldingRepository;
import com.ritesh.crypto_portfolio_tracker.repository.PriceSnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioSummaryService {

    private final HoldingRepository holdingRepo;
    private final PriceSnapshotRepository priceRepo;
    private final UnrealizedPnlService unrealizedService;
    private final RealizedPnlService realizedService;

    public PortfolioSummaryService(HoldingRepository holdingRepo,
                                   PriceSnapshotRepository priceRepo,
                                   UnrealizedPnlService unrealizedService,
                                   RealizedPnlService realizedService) {
        this.holdingRepo = holdingRepo;
        this.priceRepo = priceRepo;
        this.unrealizedService = unrealizedService;
        this.realizedService = realizedService;
    }

    public String generateCsv(Long userId) {
        List<UnrealizedPnlEntry> unreal = unrealizedService.computeUnrealizedPnl(userId);
        RealizedPnlSummary realized = realizedService.computeRealizedPnl(userId);

        double totalValue = unreal.stream().mapToDouble(UnrealizedPnlEntry::getValue).sum();
        double totalUnrealized = unreal.stream().mapToDouble(UnrealizedPnlEntry::getUnrealizedPnl).sum();
        double totalRealized = realized.getTotalRealizedGain();

        StringBuilder sb = new StringBuilder();
        sb.append("Portfolio Summary\n\n");
        sb.append(String.format("Total Value: %.2f\n", totalValue));
        sb.append(String.format("Unrealized PnL: %.2f\n", totalUnrealized));
        sb.append(String.format("Realized PnL: %.2f\n\n", totalRealized));

        sb.append("symbol,quantity,avgCost,currentPrice,value,unrealized\n");
        for (var u : unreal) {
            sb.append(String.format(
                    "%s,%.8f,%.8f,%.8f,%.8f,%.8f\n",
                    u.getSymbol(),
                    u.getQuantity(),
                    u.getAvgCost(),
                    u.getCurrentPrice(),
                    u.getValue(),
                    u.getUnrealizedPnl()
            ));
        }

        return sb.toString();
    }
}
