package com.medic.clinical_record.domain;

/**
 * Base exception for all domain rule violations.
 *
 * <p>Subclasses signal specific business scenarios (not-found, rule breach, etc.).
 * The presentation layer maps each subtype to an appropriate HTTP status code.
 * No framework imports are allowed in this package.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
