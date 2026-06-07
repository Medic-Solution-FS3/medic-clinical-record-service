package com.medic.clinical_record.application.ports.in;

import com.medic.clinical_record.domain.model.ClinicalRecord;

public interface CreateClinicalRecordUseCase {

    ClinicalRecord execute(String patientId);
}
