package com.medic.clinical_record.presentation.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateClinicalRecordRequest(@NotBlank(message = "patientId is required") String patientId) {}
