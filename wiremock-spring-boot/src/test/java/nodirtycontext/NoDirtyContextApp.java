package nodirtycontext;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Dedicated {@code @SpringBootApplication}, in its own top-level package so that no other
 * {@code @SpringBootApplication}'s component scan (e.g. {@code usecases.App}) can sweep up {@link
 * ContextCreationCounter} and inflate its count from unrelated tests.
 *
 * @see NoDirtyContextTestConfig
 */
@SpringBootApplication
class NoDirtyContextApp {}
