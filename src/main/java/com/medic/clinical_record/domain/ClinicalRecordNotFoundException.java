package com.medic.clinical_record.domain;

public final class ClinicalRecordNotFoundException extends ResourceNotFoundException {

    public ClinicalRecordNotFoundException(String patientId) {
        super("Clinical record not found for patient: " + patientId);
    }
}
