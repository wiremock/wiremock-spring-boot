package com.maciejwalkowiak.wiremock.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects WireMock instance previously configured on the class or field level with {@link ConfigureWiremock}.
 *
 * @author Maciej Walkowiak
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Wiremock {

    /**
     * The name of WireMock instance to inject.
     *
     * @return the name of WireMock instance to inject.
     */
    String value();
}
