package com.ritesh.crypto_portfolio_tracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritesh.crypto_portfolio_tracker.dto.api.ConnectRequest;
import com.ritesh.crypto_portfolio_tracker.entity.ApiKey;
import com.ritesh.crypto_portfolio_tracker.repository.ApiKeyRepository;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import com.ritesh.crypto_portfolio_tracker.service.ApiKeyService;
import com.ritesh.crypto_portfolio_tracker.service.BinanceService;
import com.ritesh.crypto_portfolio_tracker.service.HoldingService;
import com.ritesh.crypto_portfolio_tracker.config.JwtService;

import com.ritesh.crypto_portfolio_tracker.service.TradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final ApiKeyService apiKeyService;
    private final ApiKeyRepository apiKeyRepo;
    private final BinanceService binanceService;
    private final UserRepository userRepository;
    private final HoldingService holdingService;
    private final JwtService jwtService;
    private final TradeService tradeService;

    public ExchangeController(ApiKeyService apiKeyService,
                              ApiKeyRepository apiKeyRepo,
                              BinanceService binanceService,
                              UserRepository userRepository,
                              HoldingService holdingService,
                              JwtService jwtService, TradeService tradeService) {
        this.apiKeyService = apiKeyService;
        this.apiKeyRepo = apiKeyRepo;
        this.binanceService = binanceService;
        this.userRepository = userRepository;
        this.holdingService = holdingService;
        this.jwtService = jwtService;
        this.tradeService = tradeService;
    }


    // -----------------------------------------
    //  CONNECT EXCHANGE
    // -----------------------------------------
    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody ConnectRequest req,
                                     @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        ApiKey saved = apiKeyService.saveKeys(
                userId,
                req.getExchangeId(),
                req.getApiKey(),
                req.getApiSecret(),
                req.getLabel()
        );

        return ResponseEntity.ok("Saved API key id=" + saved.getId());
    }


    // -----------------------------------------
    //  FETCH BINANCE LIVE BALANCE
    // -----------------------------------------
    @GetMapping("/binance/balance")
    public ResponseEntity<?> binanceBalance(@RequestHeader("Authorization") String authHeader) throws Exception {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        ApiKey enc = apiKeyRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No Binance API key saved"));

        ApiKey dec = apiKeyService.decrypt(enc);

        return ResponseEntity.ok(binanceService.getAccountInfo(dec));
    }


    // -----------------------------------------
    //  SYNC HOLDINGS FROM BINANCE → DATABASE
    // -----------------------------------------
    @GetMapping("/binance/sync")
    public ResponseEntity<?> syncHoldings(@RequestHeader("Authorization") String authHeader) throws Exception {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        ApiKey encrypted = apiKeyRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No Binance keys saved"));

        ApiKey dec = apiKeyService.decrypt(encrypted);

        JsonNode account = binanceService.getAccountInfo(dec);

        holdingService.updateFromBinance(account, userId);

        return ResponseEntity.ok("Holdings synced successfully.");
    }

    @GetMapping("/binance/trades/sync")
    public ResponseEntity<?> syncTrades(@RequestHeader("Authorization") String authHeader) throws Exception {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        ApiKey enc = apiKeyRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No Binance API key saved"));

        ApiKey dec = apiKeyService.decrypt(enc);

        // Loop through holdings → fetch trades
        var holdings = holdingService.getHoldings(userId);

        for (var h : holdings) {
            String symbol = h.getAssetSymbol() + "USDT";
            JsonNode tradeArr = binanceService.getTrades(dec, symbol);
            tradeService.saveTradesFromBinance(tradeArr, userId, 1L);
        }

        return ResponseEntity.ok("Trades synced successfully.");
    }

}
