package com.ledgerlock.model;

/**
 * Account Status Enum - Enterprise Account Lifecycle States.
 * 
 * STATE MACHINE:
 * ┌──────────────────────────────────────────────────────────────┐
 * │ ACCOUNT STATE MACHINE │
 * └──────────────────────────────────────────────────────────────┘
 * 
 * ┌────────────────┐
 * │ PENDING_VERIFY │ ──────────────────────────────┐
 * └───────┬────────┘ │
 * │ KYC Approved │ KYC Rejected
 * ▼ ▼
 * ┌────────────────┐ Fraud Alert ┌────────────────┐
 * │ ACTIVE │ ───────────────► │ SUSPENDED │
 * └───────┬────────┘ └───────┬────────┘
 * │ │
 * │ User Request │ Investigation
 * │ or Inactivity │ Complete
 * ▼ ▼
 * ┌────────────────┐ Cleared ┌────────────────┐
 * │ FROZEN │ ◄───────────────│ SUSPENDED │
 * └───────┬────────┘ └───────┬────────┘
 * │ │
 * │ User Request │ Confirmed Fraud
 * ▼ ▼
 * ┌────────────────┐ ┌────────────────┐
 * │ ACTIVE │ │ CLOSED │
 * └────────────────┘ └────────────────┘
 * 
 * REGULATORY COMPLIANCE:
 * - SUSPENDED: Required for AML/KYC investigations (FCA, OCC regulations)
 * - FROZEN: User-initiated or court-ordered (common in dispute resolution)
 * - CLOSED: Permanent, cannot be reopened (audit trail preserved)
 */
public enum AccountStatus {

    /**
     * Normal operation - all transactions allowed.
     */
    ACTIVE("Account is fully operational"),

    /**
     * Pending KYC/AML verification - limited functionality.
     * New accounts start here until identity is verified.
     */
    PENDING_VERIFICATION("Awaiting identity verification"),

    /**
     * User-initiated freeze or temporary hold.
     * Can receive funds but cannot send.
     * User can unfreeze via customer service.
     */
    FROZEN("Temporarily frozen - can receive but not send"),

    /**
     * Suspended due to suspicious activity.
     * CRITICAL: Only compliance team can modify.
     * All transactions blocked pending investigation.
     */
    SUSPENDED("Suspended pending investigation - all transactions blocked"),

    /**
     * Permanently closed.
     * Balance must be zero before closing.
     * Cannot be reopened - create new account instead.
     */
    CLOSED("Permanently closed - no transactions allowed");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if account can SEND funds (debit).
     */
    public boolean canSend() {
        return this == ACTIVE;
    }

    /**
     * Check if account can RECEIVE funds (credit).
     */
    public boolean canReceive() {
        return this == ACTIVE || this == FROZEN || this == PENDING_VERIFICATION;
    }

    /**
     * Check if status can transition to target status.
     * Enforces valid state machine transitions.
     */
    public boolean canTransitionTo(AccountStatus target) {
        return switch (this) {
            case PENDING_VERIFICATION -> target == ACTIVE || target == SUSPENDED || target == CLOSED;
            case ACTIVE -> target == FROZEN || target == SUSPENDED || target == CLOSED;
            case FROZEN -> target == ACTIVE || target == SUSPENDED || target == CLOSED;
            case SUSPENDED -> target == ACTIVE || target == FROZEN || target == CLOSED;
            case CLOSED -> false; // Terminal state
        };
    }
}
