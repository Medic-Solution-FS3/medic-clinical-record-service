package com.medic.clinical_record.infrastructure.adapters.out.messaging.dto;

import java.time.Instant;

public record ClinicalRecordCreatedEventMsg(
        String clinicalRecordId,
        String patientId,
        Instant occurredAt
) {}
