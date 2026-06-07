package com.medic.clinical_record.infrastructure.adapters.out.mongodb.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "clinical_records")
public class ClinicalRecordDocument {

    @Id
    private String id;
    private String patientId;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PrescriptionDocument> prescriptions;
}
