/**
 * Exception handling — global error handling for the presentation layer.
 *
 * <p>Contains the {@code @ControllerAdvice} class that intercepts exceptions thrown by any
 * layer and maps them to standardized RFC 7807 Problem Detail responses. Stack traces are
 * never exposed to the client.
 */
package com.medic.clinical_record.presentation.exceptions;
