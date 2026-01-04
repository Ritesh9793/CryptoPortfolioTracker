package com.ritesh.crypto_portfolio_tracker.controller;

import com.ritesh.crypto_portfolio_tracker.config.JwtService;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import com.ritesh.crypto_portfolio_tracker.service.PortfolioSummaryService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final PortfolioSummaryService portfolioSummaryService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ReportController(PortfolioSummaryService portfolioSummaryService,
                            JwtService jwtService,
                            UserRepository userRepository) {
        this.portfolioSummaryService = portfolioSummaryService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // -----------------------------------------
    // Helper: Resolved userId from JWT
    // -----------------------------------------
    private Long userIdFromAuth(String auth) {
        String token = auth.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    // -----------------------------------------
    // Export Portfolio Summary (CSV)
    // -----------------------------------------
    @GetMapping("/portfolio/csv")
    public ResponseEntity<byte[]> exportPortfolioCsv(
            @RequestHeader("Authorization") String auth
    ) {

        Long userId = userIdFromAuth(auth);
        String csv = portfolioSummaryService.generateCsv(userId);

        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "portfolio_summary.csv");
        headers.setContentType(MediaType.TEXT_PLAIN);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
