package corewiremock;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Dedicated {@code @SpringBootApplication}, in its own top-level package so that {@link
 * GetNumberAdapter} is not swept into every other test's application context by {@code
 * usecases.App}'s component scan.
 */
@SpringBootApplication
class CoreWireMockApp {}
