/**
 * Domain layer — pure business models, rules, and domain exceptions.
 *
 * <p><strong>STRICT RULE:</strong> No imports from external frameworks are allowed in this package.
 * This means absolutely NO {@code org.springframework.*}, NO {@code org.springframework.data.*},
 * and NO MongoDB/JPA annotations. This layer must remain framework-agnostic so that the
 * core business logic can be tested and evolved independently of infrastructure concerns.
 */
package com.medic.clinical_record.domain;
