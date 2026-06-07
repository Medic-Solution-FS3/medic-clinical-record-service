package com.medic.clinical_record.presentation.controllers;

import com.medic.clinical_record.application.ports.in.AddPrescriptionUseCase;
import com.medic.clinical_record.application.ports.in.CreateClinicalRecordUseCase;
import com.medic.clinical_record.application.ports.in.GetClinicalRecordUseCase;
import com.medic.clinical_record.presentation.dtos.AddPrescriptionRequest;
import com.medic.clinical_record.presentation.dtos.ClinicalRecordResponse;
import com.medic.clinical_record.presentation.dtos.CreateClinicalRecordRequest;
import com.medic.clinical_record.presentation.dtos.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Clinical Records", description = "Manage patient clinical records and prescriptions")
@RestController
@RequestMapping("/api/v1/clinical-records")
@RequiredArgsConstructor
public class ClinicalRecordController {

    private final CreateClinicalRecordUseCase createClinicalRecordUseCase;
    private final GetClinicalRecordUseCase    getClinicalRecordUseCase;
    private final AddPrescriptionUseCase      addPrescriptionUseCase;

    @Operation(summary = "Create a clinical record",
               description = "Creates a new clinical record for a patient. Each patient may only have one record.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Clinical record created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ClinicalRecordResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed — patientId is blank or missing",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "A clinical record already exists for this patient",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ClinicalRecordResponse> create(
            @Valid @RequestBody CreateClinicalRecordRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ClinicalRecordResponse.from(
                        createClinicalRecordUseCase.execute(request.patientId())));
    }

    @Operation(summary = "Get clinical record by patient ID",
               description = "Retrieves the complete clinical record for a patient, including all prescriptions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clinical record found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ClinicalRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "No clinical record found for this patient",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{patientId}")
    public ResponseEntity<ClinicalRecordResponse> getByPatientId(
            @PathVariable String patientId) {
        return ResponseEntity.ok(
                ClinicalRecordResponse.from(
                        getClinicalRecordUseCase.execute(patientId)));
    }

    @Operation(summary = "Add a prescription to an existing clinical record",
               description = "Appends a new prescription to the patient's clinical record. The record must exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prescription added — returns the updated clinical record",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ClinicalRecordResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed — required prescription fields are blank or missing",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No clinical record found for this patient",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{patientId}/prescriptions")
    public ResponseEntity<ClinicalRecordResponse> addPrescription(
            @PathVariable String patientId,
            @Valid @RequestBody AddPrescriptionRequest request) {
        return ResponseEntity.ok(
                ClinicalRecordResponse.from(
                        addPrescriptionUseCase.execute(patientId, request.toPrescription())));
    }
}
