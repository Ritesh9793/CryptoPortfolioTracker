package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.dto.chart.CandlePoint;
import com.ritesh.crypto_portfolio_tracker.dto.chart.ChartResponse;
import com.ritesh.crypto_portfolio_tracker.dto.chart.PricePoint;
import com.ritesh.crypto_portfolio_tracker.entity.PriceSnapshot;
import com.ritesh.crypto_portfolio_tracker.repository.PriceSnapshotRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChartService {

    private final PriceSnapshotRepository snapshots;

    public ChartService(PriceSnapshotRepository snapshots) {
        this.snapshots = snapshots;
    }

    // -----------------------------
    // Get Date Range
    // -----------------------------
    public LocalDateTime resolveStart(String range) {
        return switch (range.toLowerCase()) {
            case "1d" -> LocalDateTime.now().minusDays(1);
            case "7d" -> LocalDateTime.now().minusDays(7);
            case "1m" -> LocalDateTime.now().minusMonths(1);
            case "3m" -> LocalDateTime.now().minusMonths(3);
            case "1y" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusDays(7);
        };
    }

    // -----------------------------
    // Build Line Chart
    // -----------------------------
    public List<PricePoint> buildLineChart(List<PriceSnapshot> list) {
        List<PricePoint> points = new ArrayList<>();
        for (PriceSnapshot s : list) {
            points.add(PricePoint.builder()
                    .time(s.getCapturedAt())
                    .price(s.getPriceUsd())
                    .build());
        }
        return points;
    }

    // -----------------------------
    // Build OHLC Candle Chart
    // -----------------------------
    public List<CandlePoint> buildCandleChart(String symbol, LocalDateTime start, LocalDateTime end) {

        List<CandlePoint> candles = new ArrayList<>();

        // Partition by hour (can be changed to day-level)
        LocalDateTime cursor = start;
        while (cursor.isBefore(end)) {

            LocalDateTime next = cursor.plusHours(1);

            Object[] row = snapshots.getOhlc(symbol, cursor, next);

            if (row != null && row.length == 4) {

                Double low   = row[0] != null ? ((Number) row[0]).doubleValue() : null;
                Double high  = row[1] != null ? ((Number) row[1]).doubleValue() : null;
                Double open  = row[2] != null ? ((Number) row[2]).doubleValue() : null;
                Double close = row[3] != null ? ((Number) row[3]).doubleValue() : null;

                if (open != null && close != null) {
                    candles.add(CandlePoint.builder()
                            .time(cursor)
                            .open(open)
                            .high(high)
                            .low(low)
                            .close(close)
                            .build());
                }
            }


            cursor = next;
        }

        return candles;
    }

    // -----------------------------
    // Main Chart Response
    // -----------------------------
    public ChartResponse buildChart(String symbol, String range) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = resolveStart(range);

        List<PriceSnapshot> history =
                snapshots.findByAssetSymbolAndCapturedAtBetweenOrderByCapturedAtAsc(
                        symbol, start, end);

        List<PricePoint> line = buildLineChart(history);
        List<CandlePoint> candles = buildCandleChart(symbol, start, end);

        return ChartResponse.builder()
                .symbol(symbol)
                .range(range)
                .line(line)
                .candles(candles)
                .build();
    }
}
