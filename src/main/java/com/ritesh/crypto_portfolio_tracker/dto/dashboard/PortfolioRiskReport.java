package com.ritesh.crypto_portfolio_tracker.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PortfolioRiskReport {

    private Long userId;
    private Double totalExposureUsd;
    private String overallRiskLevel;

    private List<TokenRiskItem> tokens;

    @Data @Builder
    public static class TokenRiskItem {
        private String symbol;
        private String chain;
        private String contract;
        private Double valueUsd;
        private String riskLevel;
        private Double riskScore;
        private List<String> reasons;
    }
}
