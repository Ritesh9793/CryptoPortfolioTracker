package com.ritesh.crypto_portfolio_tracker.dto.chart;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CandlePoint {
    private LocalDateTime time;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
}
