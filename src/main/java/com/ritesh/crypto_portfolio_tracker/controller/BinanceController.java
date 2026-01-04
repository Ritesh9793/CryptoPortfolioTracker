package com.ritesh.crypto_portfolio_tracker.controller;

import com.ritesh.crypto_portfolio_tracker.config.JwtService;
import com.ritesh.crypto_portfolio_tracker.entity.ApiKey;
import com.ritesh.crypto_portfolio_tracker.repository.ApiKeyRepository;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import com.ritesh.crypto_portfolio_tracker.service.ApiKeyService;
import com.ritesh.crypto_portfolio_tracker.service.BinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/binance")
public class BinanceController {

    private final BinanceService binanceService;
    private final ApiKeyRepository apiKeyRepo;
    private final ApiKeyService apiKeyService;
    private final JwtService jwtService;
    private final UserRepository userRepo;

    public BinanceController(BinanceService binanceService,
                             ApiKeyRepository apiKeyRepo,
                             ApiKeyService apiKeyService,
                             JwtService jwtService,
                             UserRepository userRepo) {
        this.binanceService = binanceService;
        this.apiKeyRepo = apiKeyRepo;
        this.apiKeyService = apiKeyService;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    @GetMapping("/balances")
    public ResponseEntity<?> getBalances(
            @RequestHeader("Authorization") String auth
    ) throws Exception {

        String token = auth.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        Long userId = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        ApiKey encrypted = apiKeyRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No Binance API key saved"));

        ApiKey decrypted = apiKeyService.decrypt(encrypted);

        return ResponseEntity.ok(
                binanceService.getAccountInfo(decrypted)
        );
    }
}
