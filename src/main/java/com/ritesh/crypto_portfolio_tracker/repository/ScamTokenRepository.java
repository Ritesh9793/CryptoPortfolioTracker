package com.ritesh.crypto_portfolio_tracker.repository;

import com.ritesh.crypto_portfolio_tracker.entity.ScamToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScamTokenRepository extends JpaRepository<ScamToken, Long> {
    Optional<ScamToken> findByContractAddress(String contractAddress);
}
