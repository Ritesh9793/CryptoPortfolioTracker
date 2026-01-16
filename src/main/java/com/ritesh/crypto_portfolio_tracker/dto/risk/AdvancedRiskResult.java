package com.ritesh.crypto_portfolio_tracker.dto.risk;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdvancedRiskResult {

    private String contract;
    private String chain;

    private double riskScore;        // consolidated 0–100
    private String riskLevel;        // low / medium / high
    private List<String> reasons;    // explanation list

    private Double holderConcentration; // 0.0–1.0
    private Double liquidityUsd;        // pool liquidity

    private Boolean honeypot;
    private Boolean ownershipRenounced;
}
