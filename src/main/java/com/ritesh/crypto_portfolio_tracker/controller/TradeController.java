package com.ritesh.crypto_portfolio_tracker.controller;

import com.ritesh.crypto_portfolio_tracker.config.JwtService;
import com.ritesh.crypto_portfolio_tracker.entity.ApiKey;
import com.ritesh.crypto_portfolio_tracker.entity.Trade;
import com.ritesh.crypto_portfolio_tracker.repository.ApiKeyRepository;
import com.ritesh.crypto_portfolio_tracker.repository.TradeRepository;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import com.ritesh.crypto_portfolio_tracker.service.TradeIngestionService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final ApiKeyRepository apiKeyRepo;
    private final TradeIngestionService ingestionService;
    private final TradeRepository tradeRepo;

    public TradeController(JwtService jwtService,
                           UserRepository userRepo,
                           ApiKeyRepository apiKeyRepo,
                           TradeIngestionService ingestionService,
                           TradeRepository tradeRepo) {

        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.apiKeyRepo = apiKeyRepo;
        this.ingestionService = ingestionService;
        this.tradeRepo = tradeRepo;
    }

    // -----------------------------------------
    // Helper: Resolve userId from JWT
    // -----------------------------------------
    private Long userIdFromAuth(String auth) {
        String token = auth.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    // -----------------------------------------
    // Sync Trades from Binance
    // -----------------------------------------
    @PostMapping("/sync")
    public ResponseEntity<?> syncTrades(
            @RequestHeader("Authorization") String auth,
            @RequestParam String symbol
    ) throws Exception {

        Long userId = userIdFromAuth(auth);

        ApiKey encrypted = apiKeyRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No API key saved"));

        ingestionService.syncUserTrades(userId, encrypted, symbol.toUpperCase());

        return ResponseEntity.ok("Trades synced successfully.");
    }

    // -----------------------------------------
    // Export Trades as CSV
    // -----------------------------------------
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTrades(
            @RequestHeader("Authorization") String auth
    ) {

        Long userId = userIdFromAuth(auth);
        List<Trade> trades = tradeRepo.findByUserIdOrderByTimeAsc(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("id,symbol,side,price,quantity,amount,time\n");

        for (Trade t : trades) {
            sb.append(String.format(
                    "%d,%s,%s,%.8f,%.8f,%.8f,%s\n",
                    t.getId(),
                    t.getSymbol(),
                    t.getSide(),
                    t.getPrice(),
                    t.getQuantity(),
                    t.getAmount(),
                    t.getTime()
            ));
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "trades.csv");
        headers.setContentType(MediaType.TEXT_PLAIN);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
