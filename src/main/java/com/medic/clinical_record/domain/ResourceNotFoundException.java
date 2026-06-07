package com.medic.clinical_record.domain;

/**
 * Thrown when a requested domain resource does not exist.
 * Maps to HTTP 404 Not Found in the presentation layer.
 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " not found with identifier: " + identifier);
    }
}
