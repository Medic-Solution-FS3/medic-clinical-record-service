package com.medic.clinical_record.application.services;

import com.medic.clinical_record.application.ports.in.GetClinicalRecordUseCase;
import com.medic.clinical_record.application.ports.out.ClinicalRecordRepositoryPort;
import com.medic.clinical_record.domain.ClinicalRecordNotFoundException;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetClinicalRecordService implements GetClinicalRecordUseCase {

    private final ClinicalRecordRepositoryPort repositoryPort;

    @Override
    public ClinicalRecord execute(String patientId) {
        log.debug("Fetching clinical record for patient: {}", patientId);

        return repositoryPort.findByPatientId(patientId)
                .orElseThrow(() -> new ClinicalRecordNotFoundException(patientId));
    }
}
