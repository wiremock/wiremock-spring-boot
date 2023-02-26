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

public class WireMockSpringExtension implements BeforeEachCallback, ParameterResolver {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        // reset
        Store.INSTANCE.resolve(extensionContext).values().forEach(WireMockServer::resetAll);

        // inject properties
        List<Field> annotatedFields = AnnotationSupport.findAnnotatedFields(extensionContext.getRequiredTestClass(), Wiremock.class);
        for (Field annotatedField : annotatedFields) {
            Wiremock annotation = annotatedField.getAnnotation(Wiremock.class);
            annotatedField.setAccessible(true);

            WireMockServer wiremock = Store.INSTANCE.findWireMockInstance(extensionContext, annotation.value());
            annotatedField.set(extensionContext.getRequiredTestInstance(), wiremock);
        }
    }

    @Override public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == WireMockServer.class && parameterContext.isAnnotated(Wiremock.class);
    }

    @Override public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Wiremock wiremock = parameterContext.findAnnotation(Wiremock.class).get();
        return Store.INSTANCE.findWireMockInstance(extensionContext, wiremock.value());
    }
}
