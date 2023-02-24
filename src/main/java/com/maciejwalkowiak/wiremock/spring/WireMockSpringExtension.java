package com.maciejwalkowiak.wiremock.spring;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WireMockSpringExtension implements ParameterResolver, BeforeAllCallback, BeforeEachCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(WireMockSpringExtension.class);
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create("wiremock-spring-boot");

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        var annotation = AnnotationSupport.findAnnotation(extensionContext.getRequiredTestClass(), EnableWiremock.class);

        annotation.ifPresent(enableWiremock -> {
            List<ConfigureWiremock> annotations = Arrays.asList(enableWiremock.value());

            LOGGER.info("Found {} @ConfigureWiremock annotations on the test class {}", annotations.size(), extensionContext.getRequiredTestClass());

            for (ConfigureWiremock configureWiremock : annotations) {
                resolveOrCreateWireMockServer(extensionContext, configureWiremock);
            }
        });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == WireMockServer.class && (parameterContext.isAnnotated(Wiremock.class) || parameterContext.isAnnotated(ConfigureWiremock.class));
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.isAnnotated(Wiremock.class)) {
            String name = parameterContext.findAnnotation(Wiremock.class).get().value();
            var wireMockServer = getStore(extensionContext).get(name);
            if (wireMockServer == null) {
                throw new IllegalStateException("WireMockServer with name '" + name + "' not registered.");
            }
            return wireMockServer;
        } else if (parameterContext.isAnnotated(ConfigureWiremock.class)) {
            return resolveOrCreateWireMockServer(extensionContext, parameterContext.findAnnotation(ConfigureWiremock.class).get());
        }
        throw new IllegalStateException("Attempted to resolve parameter from field that is not annotated with @Wiremock or @ConfigureWiremock");
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        // reset
        getStore(extensionContext).values().forEach(WireMockServer::resetAll);

        // inject properties
        List<Field> annotatedFields = AnnotationSupport.findAnnotatedFields(extensionContext.getRequiredTestClass(), Wiremock.class);
        for (Field annotatedField : annotatedFields) {
            Wiremock annotation = annotatedField.getAnnotation(Wiremock.class);
            annotatedField.setAccessible(true);
            annotatedField.set(extensionContext.getRequiredTestInstance(), getStore(extensionContext).get(annotation.value()));
        }

        List<Field> annotatedFields2 = AnnotationSupport.findAnnotatedFields(extensionContext.getRequiredTestClass(), ConfigureWiremock.class);
        for (Field annotatedField : annotatedFields2) {
            ConfigureWiremock annotation = annotatedField.getAnnotation(ConfigureWiremock.class);

            WireMockServer wireMockServer = resolveOrCreateWireMockServer(extensionContext, annotation);

            annotatedField.setAccessible(true);
            annotatedField.set(extensionContext.getRequiredTestInstance(), wireMockServer);
        }
    }

    private WireMockServer resolveOrCreateWireMockServer(ExtensionContext extensionContext, ConfigureWiremock options) {
        LOGGER.info("Configuring WireMockServer with name {} on port: {}", options.name(), options.port());

        Map<String, WireMockServer> wiremockStore = getStore(extensionContext);

        WireMockServer wireMockServer = wiremockStore.get(options.name());
        if (wireMockServer == null) {
            // create & start wiremock server
            WireMockServer newServer = new WireMockServer(options().port(options.port()));
            newServer.start();

            // save server to JUnit store
            wiremockStore.put(options.name(), newServer);

            // add shutdown hook
            ConfigurableApplicationContext applicationContext = resolveApplicationContext(extensionContext);
            applicationContext.addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> {
                LOGGER.info("Stopping WireMockServer with name {}", options.name());
                newServer.stop();
            });

            // configure Spring environment property
            if (StringUtils.isNotBlank(options.property())) {
                String property = options.property() + "=" + newServer.baseUrl();
                LOGGER.debug("Adding property {} to Spring application context", property);
                TestPropertyValues.of(property).applyTo(applicationContext.getEnvironment());
            }

            return newServer;
        } else {
            LOGGER.info("WiremockServer with name {} is already configured", options.name());
            return wireMockServer;
        }
    }

    private static ConfigurableApplicationContext resolveApplicationContext(ExtensionContext extensionContext) {
        return (ConfigurableApplicationContext) SpringExtension.getApplicationContext(extensionContext);
    }

    @SuppressWarnings("unchecked")
    private Map<String, WireMockServer> getStore(ExtensionContext extensionContext) {
        Store store = extensionContext.getRoot().getStore(NAMESPACE.append(resolveApplicationContext(extensionContext).hashCode()));
        return (Map<String, WireMockServer>) store.getOrComputeIfAbsent("servers", s -> new ConcurrentHashMap<String, WireMockServer>());
    }
}