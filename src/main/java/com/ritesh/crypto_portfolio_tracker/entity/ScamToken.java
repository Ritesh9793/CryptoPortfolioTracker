package com.ritesh.crypto_portfolio_tracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "scam_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScamToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_address", nullable = false, unique = true)
    private String contractAddress;

    @Column(name = "chain")
    private String chain;

    @Column(name = "risk_level")
    private String riskLevel; // as - 'low', 'medium', 'high'

    private String source;

    @UpdateTimestamp
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
}
