package com.ritesh.crypto_portfolio_tracker.dto.chart;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ChartResponse {

    private String symbol;
    private String range;

    private List<PricePoint> line;
    private List<CandlePoint> candles;
}
