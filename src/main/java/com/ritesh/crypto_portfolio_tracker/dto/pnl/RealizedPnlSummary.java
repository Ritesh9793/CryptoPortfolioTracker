package com.ritesh.crypto_portfolio_tracker.dto.pnl;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RealizedPnlSummary {

    private Long userId;
    private List<RealizedPnlEntry> entries;
    private double totalRealizedGain;
}
