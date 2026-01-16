package com.ritesh.crypto_portfolio_tracker.repository;

import com.ritesh.crypto_portfolio_tracker.entity.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {
    List<RiskAlert> findByUserIdOrderByCreatedAtDesc(Long userId);
}
