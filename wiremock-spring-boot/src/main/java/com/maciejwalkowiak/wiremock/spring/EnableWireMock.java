package com.maciejwalkowiak.wiremock.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Enables creating WireMock servers through {@link WireMockContextCustomizer}.
 *
 * @author Maciej Walkowiak
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(WireMockSpringExtension.class)
public @interface EnableWireMock {
    /**
     * A list of {@link WireMockServer} configurations. For each configuration a separate instance of {@link WireMockServer} is created.
     *
     * @return an array of configurations
     */
    ConfigureWireMock[] value() default {};
}
