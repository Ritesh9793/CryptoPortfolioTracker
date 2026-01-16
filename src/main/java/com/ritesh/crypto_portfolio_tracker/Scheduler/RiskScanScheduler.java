package com.ritesh.crypto_portfolio_tracker.Scheduler;

import com.ritesh.crypto_portfolio_tracker.entity.Holding;
import com.ritesh.crypto_portfolio_tracker.repository.HoldingRepository;
import com.ritesh.crypto_portfolio_tracker.service.AdvancedRiskService;
import com.ritesh.crypto_portfolio_tracker.service.NotificationService;
import com.ritesh.crypto_portfolio_tracker.service.TokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RiskScanScheduler {

    private final HoldingRepository holdingRepo;
    private final TokenService tokenService;
    private final AdvancedRiskService advancedRiskService;
    private final NotificationService notificationService;

    public RiskScanScheduler(HoldingRepository holdingRepo,
                             TokenService tokenService,
                             AdvancedRiskService advancedRiskService,
                             NotificationService notificationService) {

        this.holdingRepo = holdingRepo;
        this.tokenService = tokenService;
        this.advancedRiskService = advancedRiskService;
        this.notificationService = notificationService;
    }

    /**
     * Scheduled risk scan.
     *
     * Runs every 30 minutes:
     *  - Reads all user holdings
     *  - Resolves contract address via TokenService
     *  - Performs AdvancedRisk analysis (risk scoring + holder concentration + liquidity)
     *  - Automatically creates alerts when medium/high risks are found
     */
    @Scheduled(cron = "0 */30 * * * *")   // Every 30 minutes
    public void scanAllHoldings() {

        List<Holding> holdings = holdingRepo.findAll();
        if (holdings.isEmpty()) return;

        for (Holding h : holdings) {
            try {
                String symbol = h.getAssetSymbol();
                String chain = (h.getExchangeId() != null) ? "ethereum" : "ethereum";
                // You can enhance chain detection later per exchange type.

                // Get contract address
                String contract = tokenService.getContract(symbol, chain);
                if (contract.equals("unknown")) continue;

                // Advanced risk analysis
                var result = advancedRiskService.analyze(contract, chain);

                // Check if medium/high risk → issue alert
                if (result.getRiskLevel().equals("medium") ||
                        result.getRiskLevel().equals("high")) {

                    notificationService.createAlert(
                            h.getUserId(),
                            symbol,
                            result.getRiskLevel(),
                            String.join("; ", result.getReasons()),
                            true  // send email
                    );
                }

            } catch (Exception ignored) {
                // Prevent scheduler from stopping on one error
            }
        }
    }
}
