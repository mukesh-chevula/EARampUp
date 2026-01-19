package com.example.project06e2etesting.integration;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base class for integration tests that provides embedded MongoDB setup.
 * Uses Flapdoodle embedded MongoDB which doesn't require Docker.
 * All integration tests should extend this class to use a real MongoDB instance.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {
    // Embedded MongoDB is automatically configured by Spring Boot with de.flapdoodle.embed.mongo dependency
}

