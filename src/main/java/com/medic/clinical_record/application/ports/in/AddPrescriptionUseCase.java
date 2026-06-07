package com.medic.clinical_record.application.ports.in;

import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.domain.model.Prescription;

public interface AddPrescriptionUseCase {

    ClinicalRecord execute(String patientId, Prescription prescription);
}
