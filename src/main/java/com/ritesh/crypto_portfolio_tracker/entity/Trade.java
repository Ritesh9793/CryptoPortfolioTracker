package com.ritesh.crypto_portfolio_tracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long exchangeId;

    private String exchangeTradeId;
    private String symbol;
    private String side;

    private double quantity;
    private double price;
    private double amount;

    private double fee;
    private String feeAsset;

    private LocalDateTime time;
}

