package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.config.CryptoUtil;
import com.ritesh.crypto_portfolio_tracker.entity.ApiKey;
import com.ritesh.crypto_portfolio_tracker.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyService {

    private final ApiKeyRepository repo;

    @Value("${crypto.secret.key}")
    private String encKey;

    public ApiKeyService(ApiKeyRepository repo) {
        this.repo = repo;
    }

    // ---------------------------------------
    // SAVE (encrypted)
    // ---------------------------------------
    public ApiKey saveKeys(Long userId, Long exchangeId,
                           String apiKey, String apiSecret, String label) {

        CryptoUtil crypto = new CryptoUtil(encKey);

        ApiKey k = ApiKey.builder()
                .userId(userId)
                .exchangeId(exchangeId)
                .label(label)
                .apiKeyEnc(crypto.encrypt(apiKey))
                .apiSecretEnc(crypto.encrypt(apiSecret))
                .createdAt(java.time.LocalDateTime.now())
                .build();

        return repo.save(k);
    }

    // ---------------------------------------
    // DECRYPT (DO NOT SAVE RESULT)
    // ---------------------------------------
    public ApiKey decrypt(ApiKey encrypted) {

        CryptoUtil crypto = new CryptoUtil(encKey);

        return ApiKey.builder()
                .id(encrypted.getId())
                .userId(encrypted.getUserId())
                .exchangeId(encrypted.getExchangeId())
                .label(encrypted.getLabel())
                .createdAt(encrypted.getCreatedAt())
                .apiKeyEnc(crypto.decrypt(encrypted.getApiKeyEnc()))
                .apiSecretEnc(crypto.decrypt(encrypted.getApiSecretEnc()))
                .build();
    }
}
