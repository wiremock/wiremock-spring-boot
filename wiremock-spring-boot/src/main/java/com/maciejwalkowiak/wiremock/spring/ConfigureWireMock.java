package com.maciejwalkowiak.wiremock.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Extension;

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
     * The name of Spring property to inject the {@link WireMockServer#port()}
     *
     * @return the name of Spring property to inject the {@link WireMockServer#port()}
     */
    String portProperty() default "wiremock.server.port";

    /**
     * The location of WireMock stub files. By default, stubs are resolved from classpath location <code>wiremock-server-name/mappings/</code>.
     *
     * If provided, stubs are resolved from <code>stub-location/mappings/</code>.
     *
     * @return the stub location
     */
    String stubLocation() default "";

    /**
     * Allows user to specify if the mappings should be loaded from classpath or a directory. The location is specified with {@link #stubLocation()}.
     *
     * @return true if stubLocation points to classpath directory, else it is an ordinary directory
     */
    boolean stubLocationOnClasspath() default true;

    /**
     * WireMock extensions to register in {@link WireMockServer}.
     *
     * @return the extensions
     */
    Class<? extends Extension>[] extensions() default {};

    /**
     * Customizes {@link WireMockConfiguration} used by {@link WireMockServer} instance. Customizers are ordered by their natural order in this array. Each customizer must have no-arg constructor.
     *
     * @return the configuration customizers classes
     */
    Class<? extends WireMockConfigurationCustomizer>[] configurationCustomizers() default {};
}
