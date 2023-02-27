package com.maciejwalkowiak.wiremock.spring;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Attaches properties with urls pointing to {@link WireMockServer} instances to the Spring {@link Environment}.
 *
 * @author Maciej Walkowiak
 */
public class WireMockContextCustomizer implements ContextCustomizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WireMockContextCustomizer.class);

    private final List<ConfigureWireMock> configuration;

    /**
     * Creates an instance of {@link WireMockContextCustomizer}.
     *
     * @param configurations the configurations
     */
    public WireMockContextCustomizer(List<ConfigureWireMock> configurations) {
        this.configuration = configurations;
    }

    /**
     * Creates an instance of {@link WireMockContextCustomizer}.
     *
     * @param configurations the configurations
     */
    public WireMockContextCustomizer(ConfigureWireMock[] configurations) {
        this(Arrays.asList(configurations));
    }

    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        for (ConfigureWireMock configureWiremock : configuration) {
            resolveOrCreateWireMockServer(context, configureWiremock);
        }
    }

    private void resolveOrCreateWireMockServer(ConfigurableApplicationContext context, ConfigureWireMock options) {
        LOGGER.info("Configuring WireMockServer with name '{}' on port: {}", options.name(), options.port());

        WireMockServer wireMockServer = Store.INSTANCE.findWireMockInstance(context, options.name());

        if (wireMockServer == null) {
            // create & start wiremock server
            WireMockConfiguration serverOptions = options()
                    .usingFilesUnderClasspath(resolveStubLocation(options))
                    .port(options.port());

            if (options.extensions().length > 0) {
                serverOptions.extensions(options.extensions());
            }

            WireMockServer newServer = new WireMockServer(serverOptions);
            newServer.start();

            LOGGER.info("Started WireMockServer with name '{}':{}", options.name(), newServer.baseUrl());

            // save server to store
            Store.INSTANCE.store(context, options.name(), newServer);

            // add shutdown hook
            context.addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> {
                LOGGER.info("Stopping WireMockServer with name '{}'", options.name());
                newServer.stop();
            });

            // configure Spring environment property
            if (StringUtils.isNotBlank(options.property())) {
                String property = options.property() + "=" + newServer.baseUrl();
                LOGGER.debug("Adding property '{}' to Spring application context", property);
                TestPropertyValues.of(property).applyTo(context.getEnvironment());
            }
        } else {
            LOGGER.info("WireMockServer with name '{}' is already configured", options.name());
        }
    }

    private String resolveStubLocation(ConfigureWireMock options) {
        return StringUtils.isBlank(options.stubLocation()) ? "wiremock/" + options.name() : options.stubLocation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WireMockContextCustomizer that = (WireMockContextCustomizer) o;
        return Objects.equals(configuration, that.configuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration);
    }
}
