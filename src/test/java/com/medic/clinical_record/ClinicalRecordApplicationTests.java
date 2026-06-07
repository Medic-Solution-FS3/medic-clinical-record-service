package com.medic.clinical_record;

import com.medic.clinical_record.application.ports.out.ClinicalRecordEventPublisherPort;
import com.medic.clinical_record.application.ports.out.ClinicalRecordRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        properties = "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration"
)
class ClinicalRecordApplicationTests {

    @MockitoBean
    private ClinicalRecordRepositoryPort repositoryPort;

    @MockitoBean
    private ClinicalRecordEventPublisherPort eventPublisherPort;

    @Test
    void contextLoads() {
    }

}
