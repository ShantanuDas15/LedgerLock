package com.ledgerlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LedgerLock Application Entry Point.
 * 
 * ENTERPRISE-GRADE Banking-as-a-Service Platform
 * 
 * ARCHITECTURE HIGHLIGHTS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ CORE CAPABILITIES │
 * │ ───────────────────────────────────────────────────────────────────── │
 * │ • ACID-compliant transactions with SERIALIZABLE isolation │
 * │ • Double-entry accounting (every debit has matching credit) │
 * │ • OAuth 2.0 JWT authentication with refresh token rotation │
 * │ • Real-time fraud detection with configurable risk thresholds │
 * │ • Transaction limits (per-transaction, daily, monthly) │
 * │ • Account security state machine (active/frozen/suspended/closed) │
 * │ │
 * │ PRODUCTION FEATURES │
 * │ ───────────────────────────────────────────────────────────────────── │
 * │ • Circuit breaker pattern (Resilience4j) for fault tolerance │
 * │ • Two-tier caching (Caffeine L1 + Redis L2) for throughput │
 * │ • Distributed locking (Redis) for multi-pod coordination │
 * │ • Idempotency keys for exactly-once transfer semantics │
 * │ • Prometheus metrics + Grafana dashboards for observability │
 * │ • OpenTelemetry distributed tracing (Jaeger integration) │
 * │ • Structured audit logging for SOX/PCI-DSS compliance │
 * │ │
 * │ SECURITY │
 * │ ───────────────────────────────────────────────────────────────────── │
 * │ • AES-256-GCM field-level encryption for PII │
 * │ • SHA-256 API key hashing (never stored in plain text) │
 * │ • Tiered rate limiting (per-IP, per-user, per-API-key) │
 * │ • Refresh token reuse detection with automatic breach response │
 * │ • HMAC request signing for webhook verification │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * @see com.ledgerlock.config.properties for type-safe configuration
 * @see com.ledgerlock.service.LedgerService for core transfer logic
 * @see com.ledgerlock.service.ResilientTransferService for fault tolerance
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan("com.ledgerlock.config.properties")
public class LedgerLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerLockApplication.class, args);
    }
}
