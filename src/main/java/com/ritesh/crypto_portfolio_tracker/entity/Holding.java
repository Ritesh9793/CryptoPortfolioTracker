package com.ritesh.crypto_portfolio_tracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "holdings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "asset_symbol", nullable = false)
    private String assetSymbol;

    private Double quantity;
    private Double avgCost;

    private String address;

    @Column(name = "wallet_type")
    private String walletType;

    @Column(name = "exchange_id")
    private Long exchangeId;

    @Column(name = "contract_address")
    private String contractAddress;

    @Column(name = "chain")
    private String chain;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
