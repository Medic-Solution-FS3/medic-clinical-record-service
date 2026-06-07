package com.medic.clinical_record.infrastructure;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests that require a real MongoDB instance.
 *
 * <p>Manages a single {@link MongoDBContainer} shared across all subclasses via a static field,
 * avoiding the cost of starting a new container per test class. Spring's
 * {@code @DynamicPropertySource} wires the container's connection string into the application
 * context before any bean is created.
 *
 * <p>Usage: extend this class and add {@code @Test} methods. No additional container setup needed.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void overrideMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getConnectionString);
    }
}
