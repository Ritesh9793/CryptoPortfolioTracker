package com.ritesh.crypto_portfolio_tracker.controller;

import com.ritesh.crypto_portfolio_tracker.config.JwtService;
import com.ritesh.crypto_portfolio_tracker.dto.api.ApiKeyRequest;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import com.ritesh.crypto_portfolio_tracker.service.ApiKeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ApiKeyController(ApiKeyService apiKeyService,
                            JwtService jwtService,
                            UserRepository userRepository) {
        this.apiKeyService = apiKeyService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addKey(
            @RequestHeader("Authorization") String auth,
            @RequestBody ApiKeyRequest req
    ) {

        // 🔐 Resolved user from JWT
        String token = auth.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        // 🔐 Saved securely via ApiKeyService
        apiKeyService.saveKeys(
                userId,
                req.getExchangeId(),
                req.getApiKey(),
                req.getApiSecret(),
                req.getLabel()
        );

        return ResponseEntity.ok("API key saved securely");
    }
}
