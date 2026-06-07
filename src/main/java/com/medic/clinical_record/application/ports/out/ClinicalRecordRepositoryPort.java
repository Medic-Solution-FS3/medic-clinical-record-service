package com.medic.clinical_record.application.ports.out;

import com.medic.clinical_record.domain.model.ClinicalRecord;

import java.util.Optional;

public interface ClinicalRecordRepositoryPort {

    ClinicalRecord save(ClinicalRecord clinicalRecord);

    Optional<ClinicalRecord> findByPatientId(String patientId);
}
