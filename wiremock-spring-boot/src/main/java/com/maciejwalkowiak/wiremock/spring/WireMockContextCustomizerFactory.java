package com.maciejwalkowiak.wiremock.spring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

public class WireMockContextCustomizerFactory implements ContextCustomizerFactory {
    @Override public ContextCustomizer createContextCustomizer(Class<?> testClass,
            List<ContextConfigurationAttributes> configAttributes) {
        EnableWiremock annotation = AnnotationUtils.findAnnotation(testClass, EnableWiremock.class);

        return new WireMockContextCustomizer(annotation != null ? Arrays.asList(annotation.value()) : Collections.emptyList());
    }
}
