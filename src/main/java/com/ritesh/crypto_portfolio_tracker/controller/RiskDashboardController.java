package com.ritesh.crypto_portfolio_tracker.controller;

import com.ritesh.crypto_portfolio_tracker.config.JwtService;
import com.ritesh.crypto_portfolio_tracker.dto.dashboard.PortfolioRiskReport;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import com.ritesh.crypto_portfolio_tracker.service.RiskDashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/risk")
public class RiskDashboardController {

    private final RiskDashboardService dashboardService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public RiskDashboardController(RiskDashboardService dashboardService,
                                   JwtService jwtService,
                                   UserRepository userRepository) {

        this.dashboardService = dashboardService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<PortfolioRiskReport> riskDashboard(
            @RequestHeader("Authorization") String auth) {

        String token = auth.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        Long userId = userRepository.findByEmail(email).get().getId();

        return ResponseEntity.ok(dashboardService.build(userId));
    }
}
