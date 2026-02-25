package com.ledgerlock.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * User Entity - Represents an authenticated user in the system.
 * 
 * SECURITY FEATURES:
 * - password_hash is never exposed in API responses
 * - Roles are stored in junction table for flexibility
 * - Email serves as unique identifier for P2P transfers
 * 
 * RBAC IMPLEMENTATION:
 * - Users can have multiple roles (e.g., TELLER + MANAGER)
 * - Roles determine permissions via Permission.getPermissionsForRole()
 * - Default role is CUSTOMER for new registrations
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    /**
     * User roles - determines access permissions.
     * Stored in user_roles junction table.
     * 
     * Most users have single role (CUSTOMER), but staff may have multiple
     * (e.g., TELLER who is also backup COMPLIANCE officer).
     */
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Whether user account is enabled (can login).
     * Disabled = soft-delete or pending verification.
     */
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /**
     * Whether account is locked (too many failed logins).
     */
    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    /**
     * Failed login attempts counter.
     * Reset on successful login, increment on failure.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    /**
     * Timestamp of last successful login.
     */
    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    /**
     * Optimistic Locking Version for concurrent update detection.
     */
    @Version
    @Column(name = "version")
    private Long version;

    // One user can have multiple accounts (e.g., USD, EUR wallets)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Account> accounts = new ArrayList<>();

    /**
     * Helper method to add an account with bidirectional relationship.
     */
    public void addAccount(Account account) {
        accounts.add(account);
        account.setUser(this);
    }

    /**
     * Helper method to remove an account with bidirectional relationship.
     */
    public void removeAccount(Account account) {
        accounts.remove(account);
        account.setUser(null);
    }

    // ==================== ROLE MANAGEMENT HELPERS ====================

    /**
     * Add a role to this user.
     */
    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    /**
     * Remove a role from this user.
     */
    public void removeRole(Role role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    /**
     * Check if user has a specific role.
     */
    public boolean hasRole(Role role) {
        return this.roles != null && this.roles.contains(role);
    }

    /**
     * Check if user has ANY of the specified roles.
     */
    public boolean hasAnyRole(Role... checkRoles) {
        if (this.roles == null)
            return false;
        for (Role role : checkRoles) {
            if (this.roles.contains(role))
                return true;
        }
        return false;
    }

    /**
     * Check if user has a specific permission (derived from roles).
     */
    public boolean hasPermission(Permission permission) {
        if (this.roles == null)
            return false;
        return this.roles.stream()
                .flatMap(role -> Permission.getPermissionsForRole(role).stream())
                .anyMatch(p -> p == permission);
    }

    /**
     * Get all permissions for this user (aggregated from all roles).
     */
    public Set<Permission> getAllPermissions() {
        Set<Permission> permissions = new HashSet<>();
        if (this.roles != null) {
            for (Role role : this.roles) {
                permissions.addAll(Permission.getPermissionsForRole(role));
            }
        }
        return permissions;
    }

    /**
     * Get highest privilege role for this user.
     */
    public Role getHighestRole() {
        if (this.roles == null || this.roles.isEmpty()) {
            return Role.CUSTOMER;
        }
        return this.roles.stream()
                .max((r1, r2) -> Integer.compare(r1.getHierarchyLevel(), r2.getHierarchyLevel()))
                .orElse(Role.CUSTOMER);
    }

    /**
     * Record successful login.
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = OffsetDateTime.now();
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
    }

    /**
     * Record failed login attempt.
     * Locks account after 5 failed attempts.
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
        }
    }
}
