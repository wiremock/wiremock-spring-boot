package com.maciejwalkowiak.wiremock.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.TestContextAnnotationUtils;

/**
 * Creates {@link WireMockContextCustomizer} for test classes annotated with {@link EnableWireMock}.
 *
 * @author Maciej Walkowiak
 */
public class WireMockContextCustomizerFactory implements ContextCustomizerFactory {
    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass,
            List<ContextConfigurationAttributes> configAttributes) {
        // scan class and all enclosing classes if the test class is @Nested
        ConfigureWiremockHolder holder = new ConfigureWiremockHolder();
        parseDefinitions(testClass, holder);

        if (holder.isEmpty()) {
            return null;
        } else {
            return new WireMockContextCustomizer(holder.asArray());
        }
    }

    private void parseDefinitions(Class<?> testClass, ConfigureWiremockHolder parser) {
        parser.parse(testClass);
        if (TestContextAnnotationUtils.searchEnclosingClass(testClass)) {
            parseDefinitions(testClass.getEnclosingClass(), parser);
        }
    }

    private static class ConfigureWiremockHolder {
        private final List<ConfigureWireMock> annotations = new ArrayList<>();

        void add(ConfigureWireMock[] annotations) {
            this.annotations.addAll(Arrays.asList(annotations));
        }

        void parse(Class<?> clazz) {
            EnableWireMock annotation = AnnotationUtils.findAnnotation(clazz, EnableWireMock.class);
            if (annotation != null) {
                add(annotation.value());
            }
        }

        boolean isEmpty() {
            return annotations.isEmpty();
        }

        ConfigureWireMock[] asArray() {
            return annotations.toArray(new ConfigureWireMock[]{});
        }
    }
}
