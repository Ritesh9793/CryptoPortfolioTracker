package com.ritesh.crypto_portfolio_tracker.dto.pnl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnrealizedPnlEntry {
    private String symbol;
    private double quantity;
    private double avgCost;
    private double currentPrice;
    private double value;
    private double unrealizedPnl;
}
