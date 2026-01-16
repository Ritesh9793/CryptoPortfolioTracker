package com.ritesh.crypto_portfolio_tracker.repository;

import com.ritesh.crypto_portfolio_tracker.entity.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {

    // 1) FULL HISTORY (already present)
    List<PriceSnapshot> findByAssetSymbolOrderByCapturedAtDesc(String assetSymbol);


    // 2) LATEST PRICE (Required for: RiskDashboardService, Scheduler)
    @Query("""
        SELECT p.priceUsd 
        FROM PriceSnapshot p 
        WHERE p.assetSymbol = :symbol 
        ORDER BY p.capturedAt DESC LIMIT 1
    """)
    Optional<Double> findLatestPrice(String symbol);


    // 3) HISTORY BY DATE RANGE (Required for charts)
    List<PriceSnapshot> findByAssetSymbolAndCapturedAtBetweenOrderByCapturedAtAsc(
            String symbol,
            LocalDateTime start,
            LocalDateTime end
    );


    // 4) LIGHTWEIGHT LIST WITH LIMIT (Better performance)
    @Query("""
        SELECT p 
        FROM PriceSnapshot p 
        WHERE p.assetSymbol = :symbol 
        ORDER BY p.capturedAt DESC
    """)
    List<PriceSnapshot> findRecentPrices(String symbol, org.springframework.data.domain.Pageable pageable);


    // 5) OHLC Aggregation (Milestone-5 Charts)
    @Query(value = """
        SELECT
            MIN(price_usd) AS low,
            MAX(price_usd) AS high,
            FIRST_VALUE(price_usd) OVER (ORDER BY captured_at ASC) AS open,
            FIRST_VALUE(price_usd) OVER (ORDER BY captured_at DESC) AS close
        FROM price_snapshot
        WHERE asset_symbol = :symbol
          AND captured_at BETWEEN :start AND :end
        """, nativeQuery = true)
    Object[] getOhlc(
            @Param("symbol") String symbol,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
