package com.medic.clinical_record.domain.model;

import java.time.LocalDate;

/**
 * Value object representing a medication prescription within a clinical record.
 * Immutable by nature of the record construct. Framework-agnostic: no Spring or Jakarta imports.
 */
public record Prescription(
        String medicationName,
        String dosage,
        String instructions,
        LocalDate prescribedDate
) {

    public Prescription {
        Guard.requireNotBlank(medicationName, "medicationName");
        Guard.requireNotBlank(dosage, "dosage");
        Guard.requireNonNull(prescribedDate, "prescribedDate");
    }
}
