package com.ritesh.crypto_portfolio_tracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_alerts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RiskAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "asset_symbol")
    private String assetSymbol;

    @Column(name = "alert_type")
    private String alertType; // such as -  'rugpull_warning', 'contract_risk', 'news'

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private Boolean isRead = false;
}
