package com.medic.clinical_record.presentation.controllers;

import com.medic.clinical_record.application.ports.in.CreateClinicalRecordUseCase;
import com.medic.clinical_record.presentation.dtos.ClinicalRecordResponse;
import com.medic.clinical_record.presentation.dtos.CreateClinicalRecordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clinical-records")
@RequiredArgsConstructor
public class ClinicalRecordController {

    private final CreateClinicalRecordUseCase createClinicalRecordUseCase;

    @PostMapping
    public ResponseEntity<ClinicalRecordResponse> create(
            @Valid @RequestBody CreateClinicalRecordRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ClinicalRecordResponse.from(
                        createClinicalRecordUseCase.execute(request.patientId())));
    }
}
