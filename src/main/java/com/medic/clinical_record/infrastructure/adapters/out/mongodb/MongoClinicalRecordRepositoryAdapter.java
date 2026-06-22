package com.medic.clinical_record.infrastructure.adapters.out.mongodb;

import com.medic.clinical_record.application.ports.out.ClinicalRecordRepositoryPort;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.infrastructure.adapters.out.mongodb.entity.ClinicalRecordDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MongoClinicalRecordRepositoryAdapter implements ClinicalRecordRepositoryPort {

    private final SpringDataMongoClinicalRecordRepository mongoRepository;
    private final ClinicalRecordMapper mapper;

    @Override
    public ClinicalRecord save(ClinicalRecord clinicalRecord) {
        ClinicalRecordDocument document = mapper.toDocument(clinicalRecord);
        ClinicalRecordDocument saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ClinicalRecord> findByPatientId(String patientId) {
        return mongoRepository.findByPatientId(patientId)
                .map(mapper::toDomain);
    }
}
