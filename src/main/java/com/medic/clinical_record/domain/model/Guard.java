package com.medic.clinical_record.domain.model;

/**
 * Package-private precondition utilities for domain invariant enforcement.
 * Keeps validation logic in one place so domain classes stay focused on their state.
 */
final class Guard {

    private Guard() {}

    static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
    }

    static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }
}
