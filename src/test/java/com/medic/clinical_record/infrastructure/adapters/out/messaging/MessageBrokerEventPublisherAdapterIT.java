package com.medic.clinical_record.infrastructure.adapters.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("MessageBrokerEventPublisherAdapter — integration tests")
class MessageBrokerEventPublisherAdapterIT extends AbstractIntegrationTest {

    static final String QUEUE_NAME           = "q.clinical-records.created";
    static final String CORRELATION_ID_KEY   = "correlationId";
    static final String TEST_CORRELATION_ID  = "test-correlation-id-abc-123";

    @Container
    static final RabbitMQContainer RABBITMQ_CONTAINER =
            new RabbitMQContainer("rabbitmq:3.12-management");

    @DynamicPropertySource
    static void overrideRabbitMQProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host",     RABBITMQ_CONTAINER::getHost);
        registry.add("spring.rabbitmq.port",     RABBITMQ_CONTAINER::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ_CONTAINER::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ_CONTAINER::getAdminPassword);
    }

    @Autowired
    private MessageBrokerEventPublisherAdapter adapter;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @AfterEach
    void cleanMdc() {
        MDC.clear();
    }

    private ClinicalRecord buildRecord() {
        return new ClinicalRecord("cr-001", "patient-abc-123");
    }

    // -------------------------------------------------------------------------
    // Message delivery
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("publishRecordCreatedEvent(ClinicalRecord) — message delivery")
    class MessageDelivery {

        @Test
        @DisplayName("delivers exactly one message to the broker queue")
        void shouldDeliverMessageToBroker() {
            adapter.publishRecordCreatedEvent(buildRecord());

            Message message = rabbitTemplate.receive(QUEUE_NAME, 3_000);
            assertThat(message).isNotNull();
        }

        @Test
        @DisplayName("message body is valid JSON")
        void shouldSerializeBodyAsJson() {
            adapter.publishRecordCreatedEvent(buildRecord());

            Message message = rabbitTemplate.receive(QUEUE_NAME, 3_000);
            assertThat(message).isNotNull();
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            assertThatCode(() -> new ObjectMapper().readTree(body)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("message body contains the patientId of the clinical record")
        void shouldIncludePatientIdInBody() {
            adapter.publishRecordCreatedEvent(buildRecord());

            Message message = rabbitTemplate.receive(QUEUE_NAME, 3_000);
            assertThat(message).isNotNull();
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            assertThat(body).contains("patient-abc-123");
        }
    }

    // -------------------------------------------------------------------------
    // Correlation-Id propagation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("publishRecordCreatedEvent(ClinicalRecord) — correlation-id propagation")
    class CorrelationIdPropagation {

        @Test
        @DisplayName("injects the MDC correlationId as a message header")
        void shouldPropagateCorrelationIdFromMdc() {
            MDC.put(CORRELATION_ID_KEY, TEST_CORRELATION_ID);

            adapter.publishRecordCreatedEvent(buildRecord());

            Message message = rabbitTemplate.receive(QUEUE_NAME, 3_000);
            assertThat(message).isNotNull();
            assertThat(message.getMessageProperties().<String>getHeader(CORRELATION_ID_KEY))
                    .isEqualTo(TEST_CORRELATION_ID);
        }

        @Test
        @DisplayName("omits the correlationId header when MDC carries no value")
        void shouldOmitCorrelationIdHeaderWhenMdcIsEmpty() {
            adapter.publishRecordCreatedEvent(buildRecord());

            Message message = rabbitTemplate.receive(QUEUE_NAME, 3_000);
            assertThat(message).isNotNull();
            assertThat(message.getMessageProperties().<String>getHeader(CORRELATION_ID_KEY))
                    .isNull();
        }
    }
}
