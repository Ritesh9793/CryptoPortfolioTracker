package com.ritesh.crypto_portfolio_tracker.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;


@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
}

