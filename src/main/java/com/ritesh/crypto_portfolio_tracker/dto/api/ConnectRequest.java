package com.ritesh.crypto_portfolio_tracker.dto.api;

import jakarta.persistence.Id;
import lombok.Data;

@Data
public class ConnectRequest {
    @Id
    private Long exchangeId;

    private String apiKey;

    private String apiSecret;

    private String label;


}
