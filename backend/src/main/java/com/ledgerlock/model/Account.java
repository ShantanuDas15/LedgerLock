package com.ledgerlock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ledgerlock.security.encryption.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Account Entity - Represents a financial wallet/account.
 * 
 * Critical Design Decisions:
 * - balance uses BigDecimal (never double!) for monetary precision
 * - Column precision (19,4) matches SQL DECIMAL for up to $999 trillion with 4
 * decimals
 * - Status enum prevents operations on frozen/closed accounts
 * 
 * SECURITY:
 * - accountNumber is encrypted at rest using AES-256-GCM
 * - balance is NOT encrypted to allow SQL operations (SUM, comparisons)
 * - In production, consider column-level encryption at the database level
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User relationship - excluded from JSON serialization to prevent
     * circular references and lazy loading issues during caching.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    /**
     * Account number - ENCRYPTED AT REST.
     * 
     * Uses AES-256-GCM encryption via JPA converter.
     * The database stores Base64(IV + ciphertext + authTag).
     * 
     * SECURITY NOTE: Even if the database is breached, attackers
     * cannot read account numbers without the encryption key.
     * 
     * Column length increased to accommodate encrypted value.
     */
    @Column(name = "account_number", nullable = false, unique = true, length = 200)
    @Convert(converter = EncryptedStringConverter.class)
    private String accountNumber;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    // CRITICAL: Use BigDecimal for all monetary values!
    // precision=19, scale=4 matches the SQL schema
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    /**
     * Optimistic Locking Version - CRITICAL FOR CONCURRENT UPDATES.
     * 
     * ENTERPRISE PATTERN: Optimistic locking for high-concurrency scenarios.
     * 
     * HOW IT WORKS:
     * 1. Every time an entity is updated, Hibernate increments `version`
     * 2. UPDATE includes WHERE version = ? (the version we read)
     * 3. If another transaction modified the row, version won't match
     * 4. Hibernate throws OptimisticLockException, triggering retry
     * 
     * WHY USE BOTH PESSIMISTIC AND OPTIMISTIC LOCKING:
     * - Pessimistic (SELECT FOR UPDATE): For write-heavy operations like transfers
     * - Optimistic (@Version): For read-heavy operations with occasional writes
     * 
     * EXAMPLE CONFLICT:
     * T1: Read account (version=1), balance=$100
     * T2: Read account (version=1), balance=$100
     * T2: Update balance to $80 → version becomes 2
     * T1: Update balance to $50 → FAILS (version mismatch: expected 1, found 2)
     * T1: Retry from beginning with fresh data
     * 
     * USED BY: Amazon, Stripe, every major e-commerce platform.
     * 
     * INTERVIEW TALKING POINT:
     * "I use optimistic locking for profile updates and read-heavy operations
     * where pessimistic locks would cause unnecessary contention. The @Version
     * column enables automatic conflict detection with minimal overhead."
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Thread-safe balance operations should be done via LedgerService
     * with proper locking. These methods are for convenience only.
     */
    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }
}
