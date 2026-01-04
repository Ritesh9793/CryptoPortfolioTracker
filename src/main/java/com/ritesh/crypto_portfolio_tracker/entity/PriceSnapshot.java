package com.ritesh.crypto_portfolio_tracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "price_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol;

    @Column(name = "price_usd", nullable = false)
    private Double priceUsd;

    @Column(name = "market_cap")
    private Double marketCap;

    @Column(name = "source")
    private String source; // e.g., "coingecko"

    @CreationTimestamp
    @Column(name = "captured_at")
    private LocalDateTime capturedAt;
}
