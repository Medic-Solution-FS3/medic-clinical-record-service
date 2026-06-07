package com.medic.clinical_record.infrastructure.adapters.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medic.clinical_record.application.ports.out.ClinicalRecordEventPublisherPort;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.infrastructure.adapters.out.messaging.dto.ClinicalRecordCreatedEventMsg;
import com.medic.clinical_record.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBrokerEventPublisherAdapter implements ClinicalRecordEventPublisherPort {

    private static final String CORRELATION_ID_HEADER = "correlationId";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishRecordCreatedEvent(ClinicalRecord clinicalRecord) {
        ClinicalRecordCreatedEventMsg eventMsg = new ClinicalRecordCreatedEventMsg(
                clinicalRecord.id(),
                clinicalRecord.patientId(),
                Instant.now()
        );

        Message message = buildMessage(serialize(eventMsg));
        rabbitTemplate.send(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
        log.info("Published clinical-record.created event for patient: {}", clinicalRecord.patientId());
    }

    private byte[] serialize(ClinicalRecordCreatedEventMsg eventMsg) {
        try {
            return objectMapper.writeValueAsBytes(eventMsg);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize ClinicalRecordCreatedEventMsg", e);
        }
    }

    private Message buildMessage(byte[] body) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        String correlationId = MDC.get(CORRELATION_ID_HEADER);
        if (correlationId != null) {
            properties.setHeader(CORRELATION_ID_HEADER, correlationId);
        }
        return new Message(body, properties);
    }
}
