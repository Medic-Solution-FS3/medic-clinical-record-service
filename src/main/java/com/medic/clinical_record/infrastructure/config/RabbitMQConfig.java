package com.medic.clinical_record.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE    = "x.clinical-records";
    public static final String QUEUE       = "q.clinical-records.created";
    public static final String ROUTING_KEY = "clinical-record.created";

    @Bean
    TopicExchange clinicalRecordsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue clinicalRecordCreatedQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    Binding clinicalRecordCreatedBinding(Queue clinicalRecordCreatedQueue,
                                                TopicExchange clinicalRecordsExchange) {
        return BindingBuilder.bind(clinicalRecordCreatedQueue)
                .to(clinicalRecordsExchange)
                .with(ROUTING_KEY);
    }
}
