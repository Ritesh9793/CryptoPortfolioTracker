package com.ritesh.crypto_portfolio_tracker.dto.api;

import lombok.Data;

@Data
public class HoldingRequest {
    private String assetSymbol;
    private Double quantity;
    private Double avgCost;       // manual cost basis
    private String walletType;    // exchange or wallet
    private Long exchangeId;      // nullable for wallets
    private String address;       // null for exchanges
}
