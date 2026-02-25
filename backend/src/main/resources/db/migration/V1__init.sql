-- =============================================================================
-- V1__init.sql - LedgerLock Initial Schema
-- =============================================================================
-- This migration creates the foundational tables for the banking simulation:
-- - users: Authentication and identity
-- - accounts: Wallet/balance tracking
-- - transactions: The immutable ledger of all financial movements
-- =============================================================================

-- Enable UUID extension (required for gen_random_uuid())
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- USERS TABLE
-- =============================================================================
-- Stores authentication credentials and user metadata.
-- The email serves as the unique identifier for transfers.
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Index for email lookups during authentication
CREATE INDEX idx_users_email ON users(email);

-- =============================================================================
-- ACCOUNTS TABLE
-- =============================================================================
-- Each user has one or more accounts (wallets).
-- Balance uses DECIMAL(19,4) to avoid floating-point errors.
-- The CHECK constraint prevents negative balances at the database level.
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_accounts_number UNIQUE (account_number),
    CONSTRAINT ck_accounts_positive_balance CHECK (balance >= 0),
    CONSTRAINT ck_accounts_valid_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED'))
);

-- Index for user account lookups
CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- =============================================================================
-- TRANSACTIONS TABLE (THE LEDGER)
-- =============================================================================
-- Immutable record of all financial movements.
-- source_account_id is NULL for deposits (money comes from outside).
-- target_account_id is NULL for withdrawals (money leaves the system).
-- idempotency_key prevents duplicate transactions from double-clicks.
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_account_id UUID,
    target_account_id UUID,
    amount DECIMAL(19, 4) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reference_note TEXT,
    idempotency_key VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_source FOREIGN KEY (source_account_id) 
        REFERENCES accounts(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_target FOREIGN KEY (target_account_id) 
        REFERENCES accounts(id) ON DELETE SET NULL,
    CONSTRAINT uk_transactions_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_transactions_positive_amount CHECK (amount > 0),
    CONSTRAINT ck_transactions_valid_type CHECK (type IN ('TRANSFER', 'DEPOSIT', 'WITHDRAWAL')),
    CONSTRAINT ck_transactions_valid_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED')),
    CONSTRAINT ck_transactions_has_account CHECK (
        source_account_id IS NOT NULL OR target_account_id IS NOT NULL
    )
);

-- Indexes for transaction history queries
CREATE INDEX idx_transactions_source ON transactions(source_account_id);
CREATE INDEX idx_transactions_target ON transactions(target_account_id);
CREATE INDEX idx_transactions_created ON transactions(created_at DESC);
CREATE INDEX idx_transactions_type ON transactions(type);

-- =============================================================================
-- COMMENTS FOR DOCUMENTATION
-- =============================================================================
COMMENT ON TABLE users IS 'User authentication and identity information';
COMMENT ON TABLE accounts IS 'Financial accounts/wallets with balance tracking';
COMMENT ON TABLE transactions IS 'Immutable ledger of all financial transactions';

COMMENT ON COLUMN accounts.balance IS 'Current balance in smallest currency unit, 4 decimal precision';
COMMENT ON COLUMN transactions.idempotency_key IS 'Client-provided key to prevent duplicate transactions';
