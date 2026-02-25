package com.ledgerlock.model;

/**
 * Transaction Type Enum
 * 
 * TRANSFER: Money moves between two accounts within the system
 * DEPOSIT: Money enters the system from external source
 * WITHDRAWAL: Money leaves the system to external destination
 */
public enum TransactionType {
    TRANSFER,
    DEPOSIT,
    WITHDRAWAL
}
