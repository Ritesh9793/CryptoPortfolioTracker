-- ============================
-- USERS
-- ============================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- EXCHANGES
-- ============================
CREATE TABLE exchanges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    base_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- API KEYS
-- ============================
CREATE TABLE api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    api_secret VARCHAR(255) NOT NULL,
    label VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_api_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_exchange FOREIGN KEY (exchange_id) REFERENCES exchanges(id) ON DELETE CASCADE
);

-- ============================
-- HOLDINGS (FIXED)
-- ============================
CREATE TABLE holdings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_symbol VARCHAR(25) NOT NULL,
    quantity DECIMAL(36,18) NOT NULL,
    avg_cost DECIMAL(36,18),
    wallet_type ENUM('exchange','wallet') NOT NULL,
    exchange_id BIGINT NULL,

    contract_address VARCHAR(255),
    chain VARCHAR(50) DEFAULT 'ethereum',

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_hold_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_hold_exchange FOREIGN KEY (exchange_id) REFERENCES exchanges(id) ON DELETE SET NULL
);

-- ============================
-- TRADES (FIXED)
-- ============================
CREATE TABLE trades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,

    exchange_trade_id VARCHAR(100) NOT NULL,

    asset_symbol VARCHAR(25) NOT NULL,
    side ENUM('BUY', 'SELL') NOT NULL,
    quantity DECIMAL(36,18) NOT NULL,
    price DECIMAL(36,18) NOT NULL,

    fee DECIMAL(36,18),
    fee_asset VARCHAR(25),

    time TIMESTAMP NOT NULL,

    CONSTRAINT fk_trade_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_trade_exchange FOREIGN KEY (exchange_id) REFERENCES exchanges(id) ON DELETE CASCADE,

    UNIQUE KEY uk_trade_dedupe (user_id, exchange_trade_id)
);

-- ============================
-- PRICE SNAPSHOTS
-- ============================
CREATE TABLE price_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_symbol VARCHAR(25) NOT NULL,
    price_usd DECIMAL(36,18) NOT NULL,
    market_cap DECIMAL(36,18),
    source VARCHAR(255),
    captured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- RISK ALERTS (FIXED)
-- ============================
CREATE TABLE risk_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_symbol VARCHAR(25) NOT NULL,

    alert_type VARCHAR(100) NOT NULL,

    details TEXT,
    read_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_alert_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================
-- SCAM TOKENS
-- ============================
CREATE TABLE scam_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_address VARCHAR(120) NOT NULL,
    chain VARCHAR(50) NOT NULL,
    risk_level ENUM('low', 'medium', 'high') NOT NULL,
    source VARCHAR(255),
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_contract_chain (contract_address, chain)
);
