package com.medic.clinical_record.infrastructure.adapters.out.mongodb;

import com.medic.clinical_record.infrastructure.adapters.out.mongodb.entity.ClinicalRecordDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataMongoClinicalRecordRepository extends MongoRepository<ClinicalRecordDocument, String> {

    Optional<ClinicalRecordDocument> findByPatientId(String patientId);
}
