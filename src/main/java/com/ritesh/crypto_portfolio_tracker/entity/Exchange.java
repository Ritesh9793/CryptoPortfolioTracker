package com.ritesh.crypto_portfolio_tracker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "exchanges")
public class Exchange {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exchangeId;
    private String name;
    private String baseUrl;
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
}

