/**
 * REST controllers — inbound HTTP adapters that translate HTTP requests into use case calls.
 *
 * <p>Controllers depend only on input port interfaces ({@code application.ports.in}) and the
 * DTOs defined in {@code presentation.dtos}. They must not contain business logic.
 * All endpoints must be documented with SpringDoc/OpenAPI annotations.
 */
package com.medic.clinical_record.presentation.controllers;
