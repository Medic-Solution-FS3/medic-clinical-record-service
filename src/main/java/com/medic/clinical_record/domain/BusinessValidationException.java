package com.medic.clinical_record.domain;

/**
 * Thrown when an operation violates a domain business rule.
 * Maps to HTTP 422 Unprocessable Entity in the presentation layer.
 */
public class BusinessValidationException extends DomainException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
