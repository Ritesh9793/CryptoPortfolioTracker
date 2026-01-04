package com.ritesh.crypto_portfolio_tracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long exchangeId;
    private String label;

    @Column(name = "api_key_enc", nullable = false)
    private String apiKeyEnc;

    @Column(name = "api_secret_enc", nullable = false)
    private String apiSecretEnc;

    private LocalDateTime createdAt;
}
