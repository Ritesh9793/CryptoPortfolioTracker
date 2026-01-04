package com.ritesh.crypto_portfolio_tracker.dto.pnl;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaxLot {
    private double quantity;
    private double costPrice;
    private LocalDateTime time;
}
