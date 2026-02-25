package com.ledgerlock.model;

/**
 * Transaction Status Enum
 * 
 * PENDING: Transaction initiated but not yet committed
 * COMPLETED: Transaction successfully committed to ledger
 * FAILED: Transaction failed (insufficient funds, validation error, etc.)
 * REVERSED: Transaction was reversed by a compensating transaction
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REVERSED
}
