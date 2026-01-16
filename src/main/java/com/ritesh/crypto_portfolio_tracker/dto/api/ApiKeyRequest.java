package com.ritesh.crypto_portfolio_tracker.dto.api;


import lombok.Data;

@Data
public class ApiKeyRequest {
    public Long UserId;
    public Long ExchangeId;
    public String ApiKey;
    public String ApiSecret;
    public String Label;
}
