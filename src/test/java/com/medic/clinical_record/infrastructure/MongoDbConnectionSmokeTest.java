package com.medic.clinical_record.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test that verifies the Testcontainers + MongoDB + Spring Data wiring is correct.
 *
 * <p>Performs a round-trip insert/read against the containerized MongoDB instance to confirm
 * that connection, serialization, and query resolution all work end-to-end.
 */
class MongoDbConnectionSmokeTest extends AbstractIntegrationTest {

    private static final String COLLECTION = "smoke_test";

    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    void cleanUp() {
        mongoTemplate.dropCollection(COLLECTION);
    }

    @Test
    void shouldSaveAndRetrieveDocument() {
        SmokeDocument document = new SmokeDocument("smoke-1", "ping");

        mongoTemplate.save(document);

        SmokeDocument found = mongoTemplate.findOne(
                Query.query(Criteria.where("_id").is("smoke-1")),
                SmokeDocument.class
        );

        assertThat(found).isNotNull();
        assertThat(found.value()).isEqualTo("ping");
    }

    @Test
    void shouldConfirmContainerIsRunning() {
        assertThat(MONGO_DB_CONTAINER.isRunning()).isTrue();
    }

    @Document(collection = COLLECTION)
    record SmokeDocument(@Id String id, String value) {}
}
