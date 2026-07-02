package com.medic.clinical_record.infrastructure.adapters.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.infrastructure.config.RabbitMQConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageBrokerEventPublisherAdapter")
class MessageBrokerEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Nested
    @DisplayName("publishRecordCreatedEvent")
    class PublishRecordCreatedEvent {

        @Test
        @DisplayName("sends a JSON message to the configured exchange and routing key")
        void shouldPublishToConfiguredExchangeAndRoutingKey() {
            MessageBrokerEventPublisherAdapter adapter =
                    new MessageBrokerEventPublisherAdapter(rabbitTemplate, objectMapper);
            ClinicalRecord record = new ClinicalRecord("rec-1", "patient-1");

            adapter.publishRecordCreatedEvent(record);

            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(rabbitTemplate).send(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.ROUTING_KEY),
                    messageCaptor.capture());

            Message sent = messageCaptor.getValue();
            assertThat(sent.getMessageProperties().getContentType())
                    .isEqualTo(MessageProperties.CONTENT_TYPE_JSON);
            assertThat(new String(sent.getBody()))
                    .contains("\"clinicalRecordId\":\"rec-1\"")
                    .contains("\"patientId\":\"patient-1\"");
        }

        @Test
        @DisplayName("propagates the MDC correlationId as a message header when present")
        void shouldPropagateCorrelationIdHeaderWhenPresent() {
            MessageBrokerEventPublisherAdapter adapter =
                    new MessageBrokerEventPublisherAdapter(rabbitTemplate, objectMapper);
            MDC.put("correlationId", "corr-123");
            ClinicalRecord record = new ClinicalRecord("rec-2", "patient-2");

            adapter.publishRecordCreatedEvent(record);

            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(rabbitTemplate).send(any(), any(), messageCaptor.capture());
            String correlationIdHeader = messageCaptor.getValue().getMessageProperties().getHeader("correlationId");
            assertThat(correlationIdHeader).isEqualTo("corr-123");
        }

        @Test
        @DisplayName("omits the correlationId header when absent from MDC")
        void shouldOmitCorrelationIdHeaderWhenAbsent() {
            MessageBrokerEventPublisherAdapter adapter =
                    new MessageBrokerEventPublisherAdapter(rabbitTemplate, objectMapper);
            ClinicalRecord record = new ClinicalRecord("rec-3", "patient-3");

            adapter.publishRecordCreatedEvent(record);

            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(rabbitTemplate).send(any(), any(), messageCaptor.capture());
            String correlationIdHeader = messageCaptor.getValue().getMessageProperties().getHeader("correlationId");
            assertThat(correlationIdHeader).isNull();
        }

        @Test
        @DisplayName("wraps serialization failures in an IllegalStateException")
        void shouldWrapSerializationFailureInIllegalStateException() throws JsonProcessingException {
            ObjectMapper failingMapper = org.mockito.Mockito.mock(ObjectMapper.class);
            given(failingMapper.writeValueAsBytes(any()))
                    .willThrow(new com.fasterxml.jackson.databind.JsonMappingException(null, "boom") {});
            MessageBrokerEventPublisherAdapter adapter =
                    new MessageBrokerEventPublisherAdapter(rabbitTemplate, failingMapper);
            ClinicalRecord record = new ClinicalRecord("rec-4", "patient-4");

            assertThatIllegalStateException()
                    .isThrownBy(() -> adapter.publishRecordCreatedEvent(record))
                    .withMessage("Failed to serialize ClinicalRecordCreatedEventMsg");
        }
    }
}
