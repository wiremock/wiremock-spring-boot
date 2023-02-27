package com.maciejwalkowiak.wiremock.spring;

import java.util.List;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

/**
 * Creates {@link WireMockContextCustomizer} for test classes annotated with {@link EnableWireMock}.
 *
 * @author Maciej Walkowiak
 */
public class WireMockContextCustomizerFactory implements ContextCustomizerFactory {
    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass,
            List<ContextConfigurationAttributes> configAttributes) {
        EnableWireMock annotation = AnnotationUtils.findAnnotation(testClass, EnableWireMock.class);

        if (annotation != null) {
            return new WireMockContextCustomizer(annotation.value());
        } else {
            return null;
        }
    }
}
