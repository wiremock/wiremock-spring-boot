package com.maciejwalkowiak.wiremock.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

enum Store {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(Store.class);

    private Map<ApplicationContext, Map<String, WireMockServer>> store = new ConcurrentHashMap<>();

    WireMockServer findWireMockInstance(ExtensionContext extensionContext, String name) {
        WireMockServer wiremock = resolve(extensionContext).get(name);

        if (wiremock == null) {
            throw new IllegalStateException("WireMockServer with name '" + name + "' not registered. Perhaps you forgot to configure it first with @ConfigureWireMock?");
        }

        return wiremock;
    }

    Map<String, WireMockServer> resolve(ExtensionContext extensionContext) {
        return resolve(SpringExtension.getApplicationContext(extensionContext));
    }

    Map<String, WireMockServer> resolve(ApplicationContext applicationContext) {
        LOGGER.info("Resolving store from context: {}", applicationContext);
        return store.computeIfAbsent(applicationContext, ctx -> new ConcurrentHashMap<>());
    }
}
