package com.medic.clinical_record.presentation.dtos;

import com.medic.clinical_record.domain.model.Prescription;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AddPrescriptionRequest(
        @NotBlank(message = "medicationName is required") String medicationName,
        @NotBlank(message = "dosage is required")         String dosage,
        String instructions,
        @NotNull(message = "prescribedDate is required")  LocalDate prescribedDate
) {

    public Prescription toPrescription() {
        return new Prescription(medicationName, dosage, instructions, prescribedDate);
    }
}
