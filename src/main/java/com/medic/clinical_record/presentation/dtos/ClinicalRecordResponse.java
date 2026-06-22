package com.medic.clinical_record.presentation.dtos;

import com.medic.clinical_record.domain.model.ClinicalRecord;

public record ClinicalRecordResponse(String id, String patientId) {

    public static ClinicalRecordResponse from(ClinicalRecord record) {
        return new ClinicalRecordResponse(record.id(), record.patientId());
    }
}
