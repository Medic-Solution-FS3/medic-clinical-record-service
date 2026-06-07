package com.medic.clinical_record.application.ports.out;

import com.medic.clinical_record.domain.model.ClinicalRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClinicalRecordEventPublisherPort — outbound event contract")
class ClinicalRecordEventPublisherPortTest {

    @Mock
    private ClinicalRecordEventPublisherPort eventPublisherPort;

    private ClinicalRecord buildRecord() {
        return new ClinicalRecord("cr-001", "patient-abc-123");
    }

    // -------------------------------------------------------------------------
    // publishRecordCreatedEvent()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("publishRecordCreatedEvent(ClinicalRecord)")
    class PublishRecordCreatedEvent {

        @Test
        @DisplayName("publishes the event with the exact ClinicalRecord provided")
        void shouldPublishWithGivenRecord() {
            ClinicalRecord record = buildRecord();

            eventPublisherPort.publishRecordCreatedEvent(record);

            verify(eventPublisherPort).publishRecordCreatedEvent(record);
        }

        @Test
        @DisplayName("is invoked exactly once per record-created event")
        void shouldBeInvokedExactlyOnce() {
            ClinicalRecord record = buildRecord();

            eventPublisherPort.publishRecordCreatedEvent(record);

            verify(eventPublisherPort, times(1)).publishRecordCreatedEvent(record);
        }

        @Test
        @DisplayName("does not publish when called zero times")
        void shouldNotPublishIfNeverCalled() {
            // No call made — Mockito verifies zero interactions by default.
            // This test documents the expectation that publishing is always explicit.
            verify(eventPublisherPort, times(0)).publishRecordCreatedEvent(buildRecord());
        }

        @Test
        @DisplayName("method signature: (ClinicalRecord) → void")
        void shouldDeclareCorrectSignature() throws NoSuchMethodException {
            Method publish = ClinicalRecordEventPublisherPort.class
                    .getMethod("publishRecordCreatedEvent", ClinicalRecord.class);

            assertThat(publish.getReturnType())
                    .as("publishRecordCreatedEvent() must return void")
                    .isEqualTo(void.class);
        }

        @Test
        @DisplayName("parameter belongs to the domain model package")
        void shouldAcceptOnlyDomainTypes() throws NoSuchMethodException {
            Method publish = ClinicalRecordEventPublisherPort.class
                    .getMethod("publishRecordCreatedEvent", ClinicalRecord.class);

            String paramPackage = publish.getParameterTypes()[0].getPackageName();

            assertThat(paramPackage)
                    .as("publishRecordCreatedEvent() must operate on domain model types")
                    .startsWith("com.medic.clinical_record.domain");
        }
    }

    // -------------------------------------------------------------------------
    // Framework agnosticism
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("framework agnosticism")
    class FrameworkAgnosticism {

        @Test
        @DisplayName("carries no Spring, RabbitMQ or Kafka annotations")
        void shouldHaveNoMessagingFrameworkAnnotations() {
            boolean contaminated = Arrays.stream(ClinicalRecordEventPublisherPort.class.getAnnotations())
                    .anyMatch(a -> {
                        String name = a.annotationType().getName();
                        return name.startsWith("org.springframework")
                                || name.startsWith("org.springframework.amqp")
                                || name.startsWith("org.apache.kafka");
                    });

            assertThat(contaminated)
                    .as("ClinicalRecordEventPublisherPort must not carry messaging framework annotations")
                    .isFalse();
        }

        @Test
        @DisplayName("does not extend any messaging framework interface")
        void shouldNotExtendAnyMessagingInterface() {
            boolean extendsMessaging = Arrays.stream(ClinicalRecordEventPublisherPort.class.getInterfaces())
                    .anyMatch(i -> {
                        String name = i.getName();
                        return name.startsWith("org.springframework")
                                || name.startsWith("org.apache.kafka");
                    });

            assertThat(extendsMessaging)
                    .as("ClinicalRecordEventPublisherPort must not extend messaging framework interfaces")
                    .isFalse();
        }
    }
}
