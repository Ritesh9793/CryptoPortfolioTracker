package com.ritesh.crypto_portfolio_tracker.repository;

import com.ritesh.crypto_portfolio_tracker.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByUserIdAndExchangeIdAndAssetSymbol(Long userId, Long exchangeId, String asset);
    List<Holding> findByUserId(Long userId);

}
