package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.dto.risk.AdvancedRiskResult;
import com.ritesh.crypto_portfolio_tracker.entity.ScamToken;
import com.ritesh.crypto_portfolio_tracker.repository.ScamTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdvancedRiskService {

    private final DEXLiquidityService dexLiquidityService;
    private final RiskService riskService;
    private final ScamTokenRepository scamRepo;
    private final HolderConcentrationService holderConcentrationService;
    private final HoneypotService honeypotService;
    private final OwnershipService ownershipService;

    public AdvancedRiskService(
            DEXLiquidityService dexLiquidityService,
            RiskService riskService,
            ScamTokenRepository scamRepo,
            HolderConcentrationService holderConcentrationService,
            HoneypotService honeypotService,
            OwnershipService ownershipService
    ) {
        this.dexLiquidityService = dexLiquidityService;
        this.riskService = riskService;
        this.scamRepo = scamRepo;
        this.holderConcentrationService = holderConcentrationService;
        this.honeypotService = honeypotService;
        this.ownershipService = ownershipService;
    }

    // ------------------------------------------------------------
    // FULL ADVANCED RISK ANALYSIS
    // ------------------------------------------------------------
    public AdvancedRiskResult analyze(String contract, String chain) {

        String normalized = contract.toLowerCase();
        List<String> reasons = new ArrayList<>();
        double score = 0;

        // ---------------- BASE CHECK ----------------
        var base = riskService.checkContract(normalized, chain);
        reasons.add("Base check: " + base.details());

        if ("high".equals(base.riskLevel())) score += 70;
        else if ("medium".equals(base.riskLevel())) score += 40;

        // ---------------- HONEYPOT ----------------
        Boolean honeypot = honeypotService.isHoneypot(normalized, chain);
        if (Boolean.TRUE.equals(honeypot)) {
            score += 50;
            reasons.add("Honeypot detected (cannot sell token)");
        }

        // ---------------- OWNERSHIP ----------------
        Boolean ownershipRenounced = ownershipService.isOwnershipRenounced(normalized, chain);
        if (ownershipRenounced != null && !ownershipRenounced) {
            score += 20;
            reasons.add("Ownership not renounced");
        }

        // ---------------- HOLDER CONCENTRATION ----------------
        Double holderConcentration =
                holderConcentrationService.getTopHolderConcentration(normalized);

        if (holderConcentration != null) {
            reasons.add("Top holder concentration: " + (int) (holderConcentration * 100) + "%");

            if (holderConcentration > 0.90) score += 30;
            else if (holderConcentration > 0.70) score += 20;
        }

        // ---------------- LIQUIDITY ----------------
        Double liquidityUsd =
                dexLiquidityService.getLiquidityUsd(normalized);

        if (liquidityUsd != null) {
            reasons.add("DEX liquidity: $" + liquidityUsd);

            if (liquidityUsd < 1_000) score += 40;
            else if (liquidityUsd < 10_000) score += 20;
        }

        // ---------------- FINAL LEVEL ----------------
        String riskLevel =
                score >= 80 ? "high" :
                        score >= 40 ? "medium" : "low";

        // ---------------- PERSIST ----------------
        if (!"low".equals(riskLevel)) {
            scamRepo.save(
                    ScamToken.builder()
                            .contractAddress(normalized)
                            .chain(chain)
                            .riskLevel(riskLevel)
                            .source("advanced-engine")
                            .lastSeen(LocalDateTime.now())
                            .build()
            );
        }

        return AdvancedRiskResult.builder()
                .contract(normalized)
                .chain(chain)
                .riskScore(Math.min(score, 100))
                .riskLevel(riskLevel)
                .reasons(reasons)
                .holderConcentration(holderConcentration)
                .liquidityUsd(liquidityUsd)
                .honeypot(honeypot)
                .ownershipRenounced(ownershipRenounced)
                .build();
    }
}
