package com.medic.clinical_record.infrastructure.adapters.out.mongodb;

import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.domain.model.Prescription;
import com.medic.clinical_record.infrastructure.adapters.out.mongodb.entity.ClinicalRecordDocument;
import com.medic.clinical_record.infrastructure.adapters.out.mongodb.entity.PrescriptionDocument;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClinicalRecordMapper {

    PrescriptionDocument toDocument(Prescription prescription);

    Prescription toDomain(PrescriptionDocument document);

    default ClinicalRecordDocument toDocument(ClinicalRecord record) {
        if (record == null) return null;
        ClinicalRecordDocument document = new ClinicalRecordDocument();
        document.setId(record.id());
        document.setPatientId(record.patientId());
        document.setCreatedAt(record.createdAt());
        document.setUpdatedAt(record.updatedAt());
        document.setPrescriptions(
                record.prescriptions().stream()
                        .map(this::toDocument)
                        .toList()
        );
        return document;
    }

    default ClinicalRecord toDomain(ClinicalRecordDocument document) {
        if (document == null) return null;
        List<Prescription> prescriptions = document.getPrescriptions() == null
                ? List.of()
                : document.getPrescriptions().stream()
                        .map(this::toDomain)
                        .toList();
        return ClinicalRecord.reconstitute(
                document.getId(),
                document.getPatientId(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                prescriptions
        );
    }
}
