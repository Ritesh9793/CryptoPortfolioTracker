package com.ritesh.crypto_portfolio_tracker.controller;

import com.ritesh.crypto_portfolio_tracker.config.JwtService;
import com.ritesh.crypto_portfolio_tracker.dto.pnl.RealizedPnlEntry;
import com.ritesh.crypto_portfolio_tracker.dto.pnl.RealizedPnlSummary;
import com.ritesh.crypto_portfolio_tracker.dto.pnl.UnrealizedPnlEntry;
import com.ritesh.crypto_portfolio_tracker.entity.Trade;
import com.ritesh.crypto_portfolio_tracker.repository.TradeRepository;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import com.ritesh.crypto_portfolio_tracker.service.RealizedPnlService;
import com.ritesh.crypto_portfolio_tracker.service.UnrealizedPnlService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pnl")
public class PnlController {

    private final RealizedPnlService realized;
    private final UnrealizedPnlService unrealized;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TradeRepository tradeRepo;
    private final RealizedPnlService realizedPnlService;

    public PnlController(RealizedPnlService realized,
                         UnrealizedPnlService unrealized,
                         JwtService jwtService,
                         UserRepository userRepository, TradeRepository tradeRepo, RealizedPnlService realizedPnlService) {
        this.realized = realized;
        this.unrealized = unrealized;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.tradeRepo = tradeRepo;
        this.realizedPnlService = realizedPnlService;
    }

    private Long userIdFromAuth(String auth) {
        String token = auth.replace("Bearer ", "");
        String email = jwtService.extractEmail(token);
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @GetMapping("/realized")
    public ResponseEntity<RealizedPnlSummary> getRealized(@RequestHeader("Authorization") String auth) {
        Long uid = userIdFromAuth(auth);
        return ResponseEntity.ok(realized.computeRealizedPnl(uid));
    }

    @GetMapping("/unrealized")
    public ResponseEntity<List<UnrealizedPnlEntry>> getUnrealized(@RequestHeader("Authorization") String auth) {
        Long uid = userIdFromAuth(auth);
        return ResponseEntity.ok(unrealized.computeUnrealizedPnl(uid));
    }

    // CSV export for realized PnL
    @GetMapping("/realized/export")
    public ResponseEntity<byte[]> exportRealizedCsv(@RequestHeader("Authorization") String auth) {
        Long uid = userIdFromAuth(auth);
        RealizedPnlSummary summary = realized.computeRealizedPnl(uid);

        StringBuilder sb = new StringBuilder();
        sb.append("symbol,quantity,buyPrice,buyTime,sellPrice,sellTime,proceeds,costBasis,gain\n");
        for (var e : summary.getEntries()) {
            sb.append(String.format("%s,%.8f,%.8f,%s,%.8f,%s,%.8f,%.8f,%.8f\n",
                    e.getSymbol(),
                    e.getQuantity(),
                    e.getBuyPricePerUnit(),
                    e.getBuyTime(),
                    e.getSellPricePerUnit(),
                    e.getSellTime(),
                    e.getProceeds(),
                    e.getCostBasis(),
                    e.getGain()
            ));
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=realized_pnl.csv");
        headers.setContentType(MediaType.TEXT_PLAIN);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    // CSV export for unrealized
    @GetMapping("/unrealized/export")
    public ResponseEntity<byte[]> exportUnrealizedCsv(@RequestHeader("Authorization") String auth) {
        Long uid = userIdFromAuth(auth);
        List<UnrealizedPnlEntry> list = unrealized.computeUnrealizedPnl(uid);

        StringBuilder sb = new StringBuilder();
        sb.append("symbol,quantity,avgCost,currentPrice,value,unrealizedPnl\n");
        for (var e : list) {
            sb.append(String.format("%s,%.8f,%.8f,%.8f,%.8f,%.8f\n",
                    e.getSymbol(),
                    e.getQuantity(),
                    e.getAvgCost(),
                    e.getCurrentPrice(),
                    e.getValue(),
                    e.getUnrealizedPnl()
            ));
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=unrealized_pnl.csv");
        headers.setContentType(MediaType.TEXT_PLAIN);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/taxlots/export")
    public ResponseEntity<byte[]> exportTaxLots(
            @RequestHeader("Authorization") String auth
    ) {
        Long userId = userIdFromAuth(auth);

        List<Trade> trades = tradeRepo.findByUserIdOrderByTimeAsc(userId);
        List<RealizedPnlEntry> realized = realizedPnlService.computeRealizedPnl(userId).getEntries();

        StringBuilder sb = new StringBuilder();
        sb.append("symbol,quantity,buyPrice,buyTime,sellPrice,sellTime,gain\n");

        for (RealizedPnlEntry e : realized) {
            sb.append(String.format(
                    "%s,%.8f,%.8f,%s,%.8f,%s,%.8f\n",
                    e.getSymbol(),
                    e.getQuantity(),
                    e.getBuyPricePerUnit(),
                    e.getBuyTime(),
                    e.getSellPricePerUnit(),
                    e.getSellTime(),
                    e.getGain()
            ));
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "taxlots.csv");
        headers.setContentType(MediaType.TEXT_PLAIN);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

}
