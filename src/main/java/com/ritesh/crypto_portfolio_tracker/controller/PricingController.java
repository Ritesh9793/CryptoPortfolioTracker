package com.ritesh.crypto_portfolio_tracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritesh.crypto_portfolio_tracker.entity.PriceSnapshot;
import com.ritesh.crypto_portfolio_tracker.repository.PriceSnapshotRepository;
import com.ritesh.crypto_portfolio_tracker.service.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService pricingService;
    private final PriceSnapshotRepository snapshotRepo;

    public PricingController(PricingService pricingService, PriceSnapshotRepository snapshotRepo) {
        this.pricingService = pricingService;
        this.snapshotRepo = snapshotRepo;
    }

    // Live price by coin id (coingecko)
    @GetMapping("/live/{coinId}")
    public ResponseEntity<JsonNode> livePrice(@PathVariable String coinId) throws Exception {
        return ResponseEntity.ok(pricingService.getPriceByCoinId(coinId));
    }

    // Recent snapshots for an asset
    @GetMapping("/snapshots/{asset}")
    public ResponseEntity<List<PriceSnapshot>> snapshots(@PathVariable String asset) {
        return ResponseEntity.ok(snapshotRepo.findByAssetSymbolOrderByCapturedAtDesc(asset));
    }
}
