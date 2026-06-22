package com.medic.clinical_record.ci;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates the structural contract of the GitHub Actions CI workflow file.
 * Tests fail in the Red phase because .github/workflows/ci.yml does not exist yet.
 */
@DisplayName("CI pipeline — .github/workflows/ci.yml structural contract")
class CiPipelineConfigTest {

    private static final Path CI_WORKFLOW = Paths.get(".github", "workflows", "ci.yml");

    private static String content;

    @BeforeAll
    static void readWorkflowFile() throws IOException {
        assertThat(CI_WORKFLOW)
                .as("CI workflow file must exist at .github/workflows/ci.yml")
                .exists();
        content = Files.readString(CI_WORKFLOW);
    }

    // -------------------------------------------------------------------------
    // Triggers
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("pipeline triggers on push events to main")
    void shouldTriggerOnPushToMain() {
        assertThat(content).contains("push");
        assertThat(content).containsPattern("branches.*main|main.*branches");
    }

    @Test
    @DisplayName("pipeline triggers on pull_request events targeting main")
    void shouldTriggerOnPullRequestToMain() {
        assertThat(content).contains("pull_request");
    }

    // -------------------------------------------------------------------------
    // Runner
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("job runs on ubuntu-latest to ensure Docker daemon availability for Testcontainers")
    void shouldRunOnUbuntuLatest() {
        assertThat(content).contains("ubuntu-latest");
    }

    // -------------------------------------------------------------------------
    // Java setup
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("uses Java 21 as defined in pom.xml")
    void shouldUseJava21() {
        assertThat(content).contains("java-version: '21'");
    }

    @Test
    @DisplayName("uses Temurin distribution for reproducible builds")
    void shouldUseTemurinDistribution() {
        assertThat(content).contains("temurin");
    }

    // -------------------------------------------------------------------------
    // Dependency cache
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("caches Maven dependencies to speed up subsequent runs")
    void shouldCacheMavenDependencies() {
        assertThat(content).contains("cache: 'maven'");
    }

    // -------------------------------------------------------------------------
    // Build step
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("runs mvn clean verify to compile, test and package the application")
    void shouldRunMavenCleanVerify() {
        assertThat(content).contains("mvn");
        assertThat(content).contains("clean verify");
    }

    @Test
    @DisplayName("passes -B flag to Maven for non-interactive (batch) mode in CI")
    void shouldUseMavenBatchMode() {
        assertThat(content).contains("mvn -B");
    }

    // -------------------------------------------------------------------------
    // JaCoCo artifact upload
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("uploads JaCoCo report so it is accessible from the GitHub Actions run page")
    void shouldUploadJacocoReportAsArtifact() {
        assertThat(content).contains("upload-artifact");
    }

    @Test
    @DisplayName("JaCoCo artifact path points to target/site/jacoco")
    void shouldPointToJacocoReportDirectory() {
        assertThat(content).contains("target/site/jacoco");
    }
}
