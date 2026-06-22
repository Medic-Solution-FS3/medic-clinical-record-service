package com.medic.clinical_record.application.ports.out;

import com.medic.clinical_record.domain.model.ClinicalRecord;

public interface ClinicalRecordEventPublisherPort {

    void publishRecordCreatedEvent(ClinicalRecord clinicalRecord);
}
