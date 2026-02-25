package com.ledgerlock.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Transaction Entity - The immutable ledger record.
 * 
 * This is the core of the double-entry bookkeeping system:
 * - Every transfer creates ONE transaction record
 * - source_account is debited (money leaves)
 * - target_account is credited (money arrives)
 * - For deposits: source is NULL (external money in)
 * - For withdrawals: target is NULL (money leaves system)
 * 
 * IMMUTABILITY: Transactions should NEVER be modified after creation.
 * Failed transactions get a new "reversal" transaction, not an update.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id")
    private Account targetAccount;

    // CRITICAL: BigDecimal for monetary precision
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "reference_note", columnDefinition = "TEXT")
    private String referenceNote;

    // Idempotency key prevents duplicate transactions from network retries
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Marks the transaction as completed.
     * Once completed, status should not change (immutability principle).
     */
    public void complete() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException(
                    "Can only complete PENDING transactions. Current status: " + this.status);
        }
        this.status = TransactionStatus.COMPLETED;
    }

    /**
     * Marks the transaction as failed.
     */
    public void fail() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException(
                    "Can only fail PENDING transactions. Current status: " + this.status);
        }
        this.status = TransactionStatus.FAILED;
    }
}
