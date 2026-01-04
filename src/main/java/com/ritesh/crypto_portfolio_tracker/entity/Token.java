package com.ritesh.crypto_portfolio_tracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Symbol (e.g., ETH, USDT, SHIB)
    @Column(nullable = false)
    private String symbol;

    // EVM compatible chain (ethereum, bsc, polygon…)
    @Column(nullable = false)
    private String chain;

    // Contract address (for native tokens like ETH, store "native")
    @Column(nullable = false)
    private String contractAddress;

    // OPTIONAL: CoinGecko ID (for price mapping)
    private String coingeckoId;

    // OPTIONAL metadata
    private String name;
    private Integer decimals;

    private LocalDateTime lastUpdated;
}
