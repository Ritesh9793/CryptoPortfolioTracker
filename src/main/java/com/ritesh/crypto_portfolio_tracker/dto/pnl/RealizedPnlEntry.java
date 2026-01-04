package com.ritesh.crypto_portfolio_tracker.dto.pnl;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RealizedPnlEntry {
    private Long tradeSellId;      //  reference id for sell trade
    private String symbol;
    private double quantity;
    private double buyPricePerUnit;
    private LocalDateTime buyTime;
    private double sellPricePerUnit;
    private LocalDateTime sellTime;
    private double proceeds;       // sellPrice * qty
    private double costBasis;      // buyPrice * qty
    private double gain;           // proceeds - costBasis
}
