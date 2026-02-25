package com.ledgerlock.config;

import com.ledgerlock.model.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auditable Annotation - Marks methods for automatic audit logging.
 * 
 * When applied to a controller method, the AuditAspect will automatically
 * log the action with captured context (user, IP, request details).
 * 
 * USAGE:
 * 
 * @Auditable(action = AuditAction.ACCOUNT_TRANSFER_SUCCESS,
 *                   resourceType = "TRANSACTION")
 *                   public ResponseEntity<TransferResponse> transfer(...) { ...
 *                   }
 * 
 *                   FEATURES:
 *                   - Automatic context capture (user, IP, correlation ID)
 *                   - Success/failure detection based on response status or
 *                   exception
 *                   - SpEL expressions for dynamic resource ID extraction
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * The action to log on success.
     */
    AuditAction action();

    /**
     * The action to log on failure (if different from success action).
     * Defaults to the same action with FAILURE result.
     */
    AuditAction failureAction() default AuditAction.AUTH_LOGIN_FAILURE;

    /**
     * Whether to use the failureAction on failure, or just mark the
     * primary action as FAILURE.
     */
    boolean useFailureAction() default false;

    /**
     * The type of resource being acted upon (e.g., "ACCOUNT", "USER").
     */
    String resourceType() default "";

    /**
     * SpEL expression to extract the resource ID from method parameters.
     * Example: "#request.targetEmail" or "#result.id"
     */
    String resourceIdExpression() default "";

    /**
     * Whether to include method parameters in the audit details.
     */
    boolean includeParameters() default false;

    /**
     * Parameter names to exclude from audit details (e.g., "password").
     */
    String[] excludeParameters() default { "password", "passwordHash", "secret", "token" };

    /**
     * Whether to log the response in audit details.
     */
    boolean includeResponse() default false;
}
