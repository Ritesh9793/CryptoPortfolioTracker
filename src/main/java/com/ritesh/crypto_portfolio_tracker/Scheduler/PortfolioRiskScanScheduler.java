// ============================================================
// PortfolioRiskScanScheduler.java
// ============================================================
package com.ritesh.crypto_portfolio_tracker.Scheduler;

import com.ritesh.crypto_portfolio_tracker.entity.Holding;
import com.ritesh.crypto_portfolio_tracker.repository.HoldingRepository;
import com.ritesh.crypto_portfolio_tracker.service.AdvancedRiskService;
import com.ritesh.crypto_portfolio_tracker.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PortfolioRiskScanScheduler {

    private final HoldingRepository holdingRepository;
    private final AdvancedRiskService advancedRiskService;
    private final NotificationService notificationService;

    public PortfolioRiskScanScheduler(HoldingRepository holdingRepository,
                                      AdvancedRiskService advancedRiskService,
                                      NotificationService notificationService) {
        this.holdingRepository = holdingRepository;
        this.advancedRiskService = advancedRiskService;
        this.notificationService = notificationService;
    }

    // ------------------------------------------------------------
    // Scan all user portfolios every 6 hours
    // ------------------------------------------------------------
    @Scheduled(cron = "0 0 */6 * * *")
    public void scanAllPortfolios() {

        List<Holding> holdings = holdingRepository.findAll();

        for (Holding h : holdings) {

            if (h.getContractAddress() == null) continue;

            var result = advancedRiskService.analyze(
                    h.getContractAddress(),
                    h.getChain() == null ? "ethereum" : h.getChain()
            );

            if (!"low".equals(result.getRiskLevel())) {
                notificationService.createAlert(
                        h.getUserId(),
                        h.getAssetSymbol(),
                        "PORTFOLIO_RISK_" + result.getRiskLevel().toUpperCase(),
                        String.join(" | ", result.getReasons()),
                        true
                );
            }
        }
    }
}
