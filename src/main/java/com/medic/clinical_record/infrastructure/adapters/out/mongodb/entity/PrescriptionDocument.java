package com.medic.clinical_record.infrastructure.adapters.out.mongodb.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PrescriptionDocument {

    private String medicationName;
    private String dosage;
    private String instructions;
    private LocalDate prescribedDate;
}
