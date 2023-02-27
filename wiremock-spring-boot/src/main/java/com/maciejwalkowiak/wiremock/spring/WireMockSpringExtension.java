package com.maciejwalkowiak.wiremock.spring;

import java.lang.reflect.Field;
import java.util.List;

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
        List<Field> annotatedFields = AnnotationSupport.findAnnotatedFields(extensionContext.getRequiredTestClass(), WireMock.class);
        for (Field annotatedField : annotatedFields) {
            WireMock annotation = annotatedField.getAnnotation(WireMock.class);
            annotatedField.setAccessible(true);

            WireMockServer wiremock = Store.INSTANCE.findRequiredWireMockInstance(extensionContext, annotation.value());
            annotatedField.set(extensionContext.getRequiredTestInstance(), wiremock);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == WireMockServer.class && parameterContext.isAnnotated(WireMock.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        WireMock wiremock = parameterContext.findAnnotation(WireMock.class).get();
        return Store.INSTANCE.findRequiredWireMockInstance(extensionContext, wiremock.value());
    }
}
