package com.maciejwalkowiak.wiremock.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * JUnit 5 extension that sets {@link WireMockServer} instances previously registered with {@link ConfigureWireMock} on test class fields.
 *
 * @author Maciej Walkowiak
 */
public class WireMockSpringExtension implements BeforeEachCallback, ParameterResolver {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        // reset all wiremock servers associated with application context
        Store.INSTANCE.findAllInstances(extensionContext).forEach(WireMockServer::resetAll);

        // inject properties into test class fields
        injectWireMockInstances(extensionContext, WireMock.class, WireMock::value);
        injectWireMockInstances(extensionContext, InjectWireMock.class, InjectWireMock::value);
    }

    private static <T extends Annotation> void injectWireMockInstances(ExtensionContext extensionContext, Class<T> annotation, Function<T, String> fn) throws IllegalAccessException {
        // getRequiredTestInstances() return multiple instances for nested tests
        for (Object testInstance : extensionContext.getRequiredTestInstances().getAllInstances()) {
            List<Field> annotatedFields = AnnotationSupport.findAnnotatedFields(testInstance.getClass(), annotation);
            for (Field annotatedField : annotatedFields) {
                T annotationValue = annotatedField.getAnnotation(annotation);
                annotatedField.setAccessible(true);

                WireMockServer wiremock = Store.INSTANCE.findRequiredWireMockInstance(extensionContext, fn.apply(annotationValue));
                annotatedField.set(testInstance, wiremock);
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == WireMockServer.class && (parameterContext.isAnnotated(WireMock.class) || parameterContext.isAnnotated(InjectWireMock.class));
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        String wireMockServerName = parameterContext.findAnnotation(WireMock.class)
                .map(WireMock::value)
                .orElseGet(() -> parameterContext.findAnnotation(InjectWireMock.class).get().value());
        return Store.INSTANCE.findRequiredWireMockInstance(extensionContext, wireMockServerName);
    }
}
