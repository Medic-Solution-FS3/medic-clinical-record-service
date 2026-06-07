package com.medic.clinical_record.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root for a patient's clinical history.
 * Immutable after creation; mutations return a new instance. Framework-agnostic.
 */
public final class ClinicalRecord {

    private final String id;
    private final String patientId;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<Prescription> prescriptions;

    public ClinicalRecord(String id, String patientId) {
        Guard.requireNotBlank(patientId, "patientId");
        this.id = id;
        this.patientId = patientId;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.prescriptions = List.of();
    }

    private ClinicalRecord(String id, String patientId, Instant createdAt, Instant updatedAt,
                           List<Prescription> prescriptions) {
        this.id = id;
        this.patientId = patientId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.prescriptions = List.copyOf(prescriptions);
    }

    public String id() { return id; }
    public String patientId() { return patientId; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public List<Prescription> prescriptions() { return prescriptions; }

    public ClinicalRecord addPrescription(Prescription prescription) {
        Guard.requireNonNull(prescription, "prescription");
        List<Prescription> updatedPrescriptions = new ArrayList<>(this.prescriptions);
        updatedPrescriptions.add(prescription);
        return new ClinicalRecord(this.id, this.patientId, this.createdAt, Instant.now(), updatedPrescriptions);
    }
}
