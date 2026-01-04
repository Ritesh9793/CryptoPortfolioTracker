package com.ritesh.crypto_portfolio_tracker.repository;

import com.ritesh.crypto_portfolio_tracker.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    // To avoid duplicate trades when ingesting from Binance
    Optional<Trade> findByUserIdAndExchangeTradeId(Long userId, String tradeId);

    // Useful for P&L Engine (per-symbol history)
    List<Trade> findByUserIdAndSymbolOrderByTimeAsc(Long userId, String symbol);

    // Useful to fetch entire user trade history
    List<Trade> findByUserIdOrderByTimeAsc(Long userId);
}
