package com.ritesh.crypto_portfolio_tracker.repository;

import com.ritesh.crypto_portfolio_tracker.entity.ApiKey;
import com.ritesh.crypto_portfolio_tracker.entity.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByUserId(Long userId);
}
