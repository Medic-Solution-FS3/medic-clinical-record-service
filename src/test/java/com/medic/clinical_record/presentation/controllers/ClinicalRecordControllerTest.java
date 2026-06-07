package com.medic.clinical_record.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medic.clinical_record.application.ports.in.CreateClinicalRecordUseCase;
import com.medic.clinical_record.domain.ClinicalRecordAlreadyExistsException;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.presentation.dtos.CreateClinicalRecordRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClinicalRecordController.class)
@DisplayName("ClinicalRecordController — POST /api/v1/clinical-records")
class ClinicalRecordControllerTest {

    private static final String POST_URL   = "/api/v1/clinical-records";
    private static final String PATIENT_ID = "patient-abc-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateClinicalRecordUseCase createClinicalRecordUseCase;

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("successful creation")
    class SuccessfulCreation {

        @Test
        @DisplayName("returns 201 Created with patientId in response body")
        void shouldReturn201WithPatientId() throws Exception {
            ClinicalRecord record = new ClinicalRecord("cr-001", PATIENT_ID);
            when(createClinicalRecordUseCase.execute(PATIENT_ID)).thenReturn(record);

            mockMvc.perform(post(POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.patientId").value(PATIENT_ID));
        }

        @Test
        @DisplayName("returns 201 Created with id in response body")
        void shouldReturn201WithId() throws Exception {
            ClinicalRecord record = new ClinicalRecord("cr-001", PATIENT_ID);
            when(createClinicalRecordUseCase.execute(PATIENT_ID)).thenReturn(record);

            mockMvc.perform(post(POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("cr-001"));
        }

        @Test
        @DisplayName("delegates to CreateClinicalRecordUseCase with the patientId from the request")
        void shouldDelegateToUseCaseWithCorrectPatientId() throws Exception {
            ClinicalRecord record = new ClinicalRecord("cr-001", PATIENT_ID);
            when(createClinicalRecordUseCase.execute(PATIENT_ID)).thenReturn(record);

            mockMvc.perform(post(POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isCreated());

            verify(createClinicalRecordUseCase, times(1)).execute(PATIENT_ID);
        }
    }

    // -------------------------------------------------------------------------
    // Validation failures → 400
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("request validation failures")
    class RequestValidationFailures {

        @Test
        @DisplayName("returns 400 Bad Request when patientId is blank")
        void shouldReturn400WhenPatientIdIsBlank() throws Exception {
            mockMvc.perform(post(POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(""))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 Bad Request when body is empty JSON object")
        void shouldReturn400WhenBodyHasNoPatientId() throws Exception {
            mockMvc.perform(post(POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("response body contains a fieldErrors array pointing to the patientId field")
        void shouldIncludeFieldErrorForPatientId() throws Exception {
            mockMvc.perform(post(POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(""))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors").isArray())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("patientId"));
        }

        @Test
        @DisplayName("never calls the use case when request validation fails")
        void shouldNotInvokeUseCaseOnValidationError() throws Exception {
            mockMvc.perform(post(POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(""))))
                    .andExpect(status().isBadRequest());

            verify(createClinicalRecordUseCase, never()).execute(PATIENT_ID);
        }
    }

    // -------------------------------------------------------------------------
    // Business rule violation → 422
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("business rule violations")
    class BusinessRuleViolations {

        @Test
        @DisplayName("returns 422 Unprocessable Entity when a clinical record already exists for the patient")
        void shouldReturn422WhenRecordAlreadyExists() throws Exception {
            when(createClinicalRecordUseCase.execute(PATIENT_ID))
                    .thenThrow(new ClinicalRecordAlreadyExistsException(PATIENT_ID));

            mockMvc.perform(post(POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("response body contains the domain error message on duplicate patient")
        void shouldIncludeDomainMessageOnDuplicate() throws Exception {
            when(createClinicalRecordUseCase.execute(PATIENT_ID))
                    .thenThrow(new ClinicalRecordAlreadyExistsException(PATIENT_ID));

            mockMvc.perform(post(POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    // -------------------------------------------------------------------------
    // Unexpected errors → 500
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("unexpected errors")
    class UnexpectedErrors {

        @Test
        @DisplayName("returns 500 Internal Server Error on unhandled runtime exception")
        void shouldReturn500OnUnexpectedError() throws Exception {
            when(createClinicalRecordUseCase.execute(PATIENT_ID))
                    .thenThrow(new RuntimeException("Unexpected infrastructure failure"));

            mockMvc.perform(post(POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isInternalServerError());
        }
    }
}
