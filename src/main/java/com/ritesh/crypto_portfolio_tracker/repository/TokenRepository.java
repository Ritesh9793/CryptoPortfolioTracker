package com.ritesh.crypto_portfolio_tracker.repository;

import com.ritesh.crypto_portfolio_tracker.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findBySymbolIgnoreCaseAndChainIgnoreCase(String symbol, String chain);

    Optional<Token> findByContractAddressIgnoreCase(String address);

    Optional<Token> findByCoingeckoId(String coingeckoId);
}
