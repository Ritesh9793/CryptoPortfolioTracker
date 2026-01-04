package com.ritesh.crypto_portfolio_tracker.dto.chart;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PricePoint {
    private LocalDateTime time;
    private Double price;
}
