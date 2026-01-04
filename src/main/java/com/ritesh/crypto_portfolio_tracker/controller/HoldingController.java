package com.ritesh.crypto_portfolio_tracker.controller;

import com.ritesh.crypto_portfolio_tracker.config.JwtService;
import com.ritesh.crypto_portfolio_tracker.dto.api.HoldingRequest;
import com.ritesh.crypto_portfolio_tracker.entity.Holding;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import com.ritesh.crypto_portfolio_tracker.service.HoldingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class HoldingController {

    private final HoldingService holdingService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public HoldingController(HoldingService holdingService,
                             JwtService jwtService,
                             UserRepository userRepository) {
        this.holdingService = holdingService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @GetMapping("/holdings")
    public ResponseEntity<List<Holding>> getHoldings(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(holdingService.getHoldings(extractUserId(authHeader)));
    }

    @PostMapping("/holdings")
    public ResponseEntity<Holding> addHolding(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody HoldingRequest req
    ) {
        return ResponseEntity.ok(
                holdingService.addHolding(extractUserId(authHeader), req)
        );
    }

    @PutMapping("/holdings/{id}")
    public ResponseEntity<Holding> updateHolding(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody HoldingRequest req
    ) {
        return ResponseEntity.ok(
                holdingService.updateHolding(id, extractUserId(authHeader), req)
        );
    }
}
