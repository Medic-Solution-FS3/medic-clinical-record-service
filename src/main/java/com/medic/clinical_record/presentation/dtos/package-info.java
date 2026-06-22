/**
 * Data Transfer Objects — request and response models for the HTTP layer.
 *
 * <p>DTOs carry data across the HTTP boundary and are the only place where Jakarta Validation
 * annotations ({@code @NotNull}, {@code @Size}, etc.) are applied. They are mapped to and from
 * domain objects using MapStruct mappers. DTOs must never be passed into the domain layer.
 */
package com.medic.clinical_record.presentation.dtos;
