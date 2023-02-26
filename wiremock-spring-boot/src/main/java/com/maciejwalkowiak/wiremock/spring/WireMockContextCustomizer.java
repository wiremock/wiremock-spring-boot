package com.maciejwalkowiak.wiremock.spring;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WireMockContextCustomizer implements ContextCustomizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WireMockContextCustomizer.class);

    private final List<ConfigureWiremock> configuration;

    public WireMockContextCustomizer(List<ConfigureWiremock> configuration) {
        this.configuration = configuration;
    }

    @Override public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        for (ConfigureWiremock configureWiremock : configuration) {
            resolveOrCreateWireMockServer(context, configureWiremock);
        }
    }

    private WireMockServer resolveOrCreateWireMockServer(ConfigurableApplicationContext context, ConfigureWiremock options) {
        LOGGER.info("Configuring WireMockServer with name {} on port: {}", options.name(), options.port());

        Map<String, WireMockServer> wiremockStore = Store.INSTANCE.resolve(context);

        WireMockServer wireMockServer = wiremockStore.get(options.name());
        if (wireMockServer == null) {
            // create & start wiremock server
            WireMockServer newServer = new WireMockServer(options().port(options.port()));
            newServer.start();

            LOGGER.info("Started WireMockServer with name {}:{}", options.name(), newServer.baseUrl());

            // save server to JUnit store
            wiremockStore.put(options.name(), newServer);
            // add shutdown hook
            context.addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> {
                LOGGER.info("Stopping WireMockServer with name {}", options.name());
                newServer.stop();
            });

            // configure Spring environment property
            if (StringUtils.isNotBlank(options.property())) {
                String property = options.property() + "=" + newServer.baseUrl();
                LOGGER.debug("Adding property {} to Spring application context", property);
                TestPropertyValues.of(property).applyTo(context.getEnvironment());
            }

            return newServer;
        } else {
            LOGGER.info("WiremockServer with name {} is already configured", options.name());
            return wireMockServer;
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WireMockContextCustomizer that = (WireMockContextCustomizer) o;
        return Objects.equals(configuration, that.configuration);
    }

    @Override public int hashCode() {
        return Objects.hash(configuration);
    }
}
