package com.maciejwalkowiak.wiremock.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * Configures WireMock instance.
 *
 * @author Maciej Walkowiak
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigureWireMock {

    /**
     * Port on which WireMock server is going to listen. {@code 0} means WireMock will pick random port.
     *
     * @return WireMock server port
     */
    int port() default 0;

    /**
     * The name of WireMock server.
     *
     * @return the name of WireMock server.
     */
    String name();

    /**
     * The name of Spring property to inject the {@link WireMockServer#baseUrl()}
     *
     * @return the name of Spring property to inject the {@link WireMockServer#baseUrl()}
     */
    String property() default "";

    /**
     * The location of WireMock stub files. By default, stubs are resolved from classpath location <code>wiremock-server-name/mappings/</code>.
     *
     * If provided, stubs are resolved from <code>stub-location/mappings/</code>.
     *
     * @return the stub location
     */
    String stubLocation() default "";
}
