package com.medic.clinical_record.application.services;

import com.medic.clinical_record.application.ports.in.AddPrescriptionUseCase;
import com.medic.clinical_record.application.ports.out.ClinicalRecordRepositoryPort;
import com.medic.clinical_record.domain.ClinicalRecordNotFoundException;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.domain.model.Prescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddPrescriptionService implements AddPrescriptionUseCase {

    private final ClinicalRecordRepositoryPort repositoryPort;

    @Override
    public ClinicalRecord execute(String patientId, Prescription prescription) {
        ClinicalRecord existing = repositoryPort.findByPatientId(patientId)
                .orElseThrow(() -> new ClinicalRecordNotFoundException(patientId));

        ClinicalRecord updated = existing.addPrescription(prescription);
        ClinicalRecord saved = repositoryPort.save(updated);

        log.info("Added new prescription to clinical record for patient: {}", patientId);

        return saved;
    }
}
