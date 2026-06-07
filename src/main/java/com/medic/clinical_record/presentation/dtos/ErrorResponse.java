package com.medic.clinical_record.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error payload returned by the API for all failure scenarios.
 *
 * <p>{@code fieldErrors} is omitted from the JSON when null (non-validation errors).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> fieldErrors
) {

    /**
     * Single field-level validation failure detail.
     */
    public record FieldValidationError(String field, Object rejectedValue, String message) {}

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, null);
    }

    public static ErrorResponse ofValidation(
            int status,
            String error,
            String message,
            String path,
            List<FieldValidationError> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, path, fieldErrors);
    }
}
