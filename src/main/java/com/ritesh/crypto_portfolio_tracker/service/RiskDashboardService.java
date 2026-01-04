package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.dto.dashboard.PortfolioRiskReport;
import com.ritesh.crypto_portfolio_tracker.entity.Holding;
import com.ritesh.crypto_portfolio_tracker.repository.HoldingRepository;
import com.ritesh.crypto_portfolio_tracker.repository.PriceSnapshotRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskDashboardService {

    private final HoldingRepository holdingRepo;
    private final TokenService tokenService;
    private final AdvancedRiskService advRisk;
    private final PriceSnapshotRepository priceRepo;

    public RiskDashboardService(HoldingRepository holdingRepo,
                                TokenService tokenService,
                                AdvancedRiskService advRisk,
                                PriceSnapshotRepository priceRepo) {

        this.holdingRepo = holdingRepo;
        this.tokenService = tokenService;
        this.advRisk = advRisk;
        this.priceRepo = priceRepo;
    }

    public PortfolioRiskReport build(Long userId) {

        List<Holding> holdings = holdingRepo.findByUserId(userId);
        List<PortfolioRiskReport.TokenRiskItem> tokenItems = new ArrayList<>();

        double totalExposure = 0.0;
        double weightedRisk = 0.0;

        for (Holding h : holdings) {
            String chain = "ethereum";
            String contract = tokenService.getContract(h.getAssetSymbol(), chain);

            double price = priceRepo.findLatestPrice(h.getAssetSymbol()).orElse(0.0);
            double value = h.getQuantity() * price;

            var risk = advRisk.analyze(contract, chain);

            totalExposure += value;
            weightedRisk += value * risk.getRiskScore();

            tokenItems.add(
                    PortfolioRiskReport.TokenRiskItem.builder()
                            .symbol(h.getAssetSymbol())
                            .chain(chain)
                            .contract(contract)
                            .valueUsd(value)
                            .riskLevel(risk.getRiskLevel())
                            .riskScore(risk.getRiskScore())
                            .reasons(risk.getReasons())
                            .build()
            );
        }

        String overallRisk = weightedRisk == 0 ? "low" :
                (weightedRisk / totalExposure >= 70 ? "high" :
                        weightedRisk / totalExposure >= 40 ? "medium" : "low");

        return PortfolioRiskReport.builder()
                .userId(userId)
                .overallRiskLevel(overallRisk)
                .totalExposureUsd(totalExposure)
                .tokens(tokenItems)
                .build();
    }
}
