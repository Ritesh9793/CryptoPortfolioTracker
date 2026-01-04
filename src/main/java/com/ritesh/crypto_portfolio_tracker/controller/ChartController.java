package com.ritesh.crypto_portfolio_tracker.controller;

import com.ritesh.crypto_portfolio_tracker.dto.chart.ChartResponse;
import com.ritesh.crypto_portfolio_tracker.service.ChartService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charts")
public class ChartController {

    private final ChartService chartService;

    public ChartController(ChartService chartService) {
        this.chartService = chartService;
    }

    /**
     * GET /api/charts/{symbol}?range=7d
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<ChartResponse> chart(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "7d") String range) {

        return ResponseEntity.ok(
                chartService.buildChart(symbol.toUpperCase(), range)
        );
    }
}
