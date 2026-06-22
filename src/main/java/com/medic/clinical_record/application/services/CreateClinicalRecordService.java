package com.medic.clinical_record.application.services;

import com.medic.clinical_record.application.ports.in.CreateClinicalRecordUseCase;
import com.medic.clinical_record.application.ports.out.ClinicalRecordEventPublisherPort;
import com.medic.clinical_record.application.ports.out.ClinicalRecordRepositoryPort;
import com.medic.clinical_record.domain.ClinicalRecordAlreadyExistsException;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateClinicalRecordService implements CreateClinicalRecordUseCase {

    private final ClinicalRecordRepositoryPort repositoryPort;
    private final ClinicalRecordEventPublisherPort eventPublisherPort;

    @Override
    public ClinicalRecord execute(String patientId) {
        if (repositoryPort.findByPatientId(patientId).isPresent()) {
            throw new ClinicalRecordAlreadyExistsException(patientId);
        }

        ClinicalRecord record = new ClinicalRecord(null, patientId);
        ClinicalRecord saved = repositoryPort.save(record);
        eventPublisherPort.publishRecordCreatedEvent(saved);

        log.info("Clinical record created for patient: {}", patientId);

        return saved;
    }
}
