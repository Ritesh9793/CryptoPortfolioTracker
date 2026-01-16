package com.ritesh.crypto_portfolio_tracker.dto.risk;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskCheckResponse {
    private String contract;
    private String chain;
    private String riskLevel;
    private String details;
}
