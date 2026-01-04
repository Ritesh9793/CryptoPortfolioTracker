package com.ritesh.crypto_portfolio_tracker.controller;

import com.ritesh.crypto_portfolio_tracker.config.JwtService;
import com.ritesh.crypto_portfolio_tracker.dto.risk.RiskCheckResponse;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import com.ritesh.crypto_portfolio_tracker.service.NotificationService;
import com.ritesh.crypto_portfolio_tracker.service.RiskService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk")
public class RiskController {

    private final RiskService riskService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public RiskController(RiskService riskService,
                          JwtService jwtService,
                          UserRepository userRepository,
                          NotificationService notificationService) {

        this.riskService = riskService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private Long getUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    // ---------------------------------------
    // CONTRACT RISK ANALYSIS ENDPOINT
    // ---------------------------------------
    @GetMapping("/check")
    public ResponseEntity<RiskCheckResponse> checkContract(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String contract,
            @RequestParam(defaultValue = "ethereum") String chain
    ) {

        Long userId = getUserId(authHeader);

        var result = riskService.checkContract(contract, chain);

        // Auto-alert: required in Milestone-3
        if (result.riskLevel().equals("high") || result.riskLevel().equals("medium")) {
            notificationService.createAlert(
                    userId,
                    contract,
                    result.riskLevel(),
                    result.details(),
                    true
            );
        }

        return ResponseEntity.ok(
                RiskCheckResponse.builder()
                        .contract(contract)
                        .chain(chain)
                        .riskLevel(result.riskLevel())
                        .details(result.details())
                        .build()
        );
    }
}
