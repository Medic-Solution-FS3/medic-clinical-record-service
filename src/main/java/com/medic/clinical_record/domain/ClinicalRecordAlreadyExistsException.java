package com.medic.clinical_record.domain;

public final class ClinicalRecordAlreadyExistsException extends DomainException {

    public ClinicalRecordAlreadyExistsException(String patientId) {
        super("A clinical record already exists for patient: " + patientId);
    }
}
