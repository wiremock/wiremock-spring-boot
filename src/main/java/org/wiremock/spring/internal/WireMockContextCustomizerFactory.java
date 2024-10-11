package org.wiremock.spring.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

/**
 * Creates {@link WireMockContextCustomizer} for test classes annotated with {@link EnableWireMock}.
 *
 * @author Maciej Walkowiak
 */
public class WireMockContextCustomizerFactory implements ContextCustomizerFactory {
  private static final ConfigureWireMock DEFAULT_CONFIGURE_WIREMOCK =
      DefaultConfigureWireMock.class.getAnnotation(ConfigureWireMock.class);

  @ConfigureWireMock(name = "wiremock")
  private static class DefaultConfigureWireMock {}

  @Override
  public ContextCustomizer createContextCustomizer(
      final Class<?> testClass, final List<ContextConfigurationAttributes> configAttributes) {
    // scan class and all enclosing classes if the test class is @Nested
    final ConfigureWiremockHolder holder = new ConfigureWiremockHolder();
    this.parseDefinitions(testClass, holder);

    if (holder.isEmpty()) {
      return null;
    } else {
      return new WireMockContextCustomizer(holder.asArray());
    }
  }

  private void parseDefinitions(final Class<?> testClass, final ConfigureWiremockHolder parser) {
    parser.parse(testClass);
    if (TestContextAnnotationUtils.searchEnclosingClass(testClass)) {
      this.parseDefinitions(testClass.getEnclosingClass(), parser);
    }
  }

  private static class ConfigureWiremockHolder {
    private final List<ConfigureWireMock> annotations = new ArrayList<>();

    void add(final ConfigureWireMock... annotations) {
      this.annotations.addAll(Arrays.asList(annotations));
    }

    void parse(final Class<?> clazz) {
      final EnableWireMock annotation = AnnotationUtils.findAnnotation(clazz, EnableWireMock.class);
      if (annotation != null) {
        final ConfigureWireMock[] value = annotation.value();
        if (value.length == 0) {
          this.add(WireMockContextCustomizerFactory.DEFAULT_CONFIGURE_WIREMOCK);
        } else {
          this.add(value);
        }
      }
    }

    boolean isEmpty() {
      return this.annotations.isEmpty();
    }

    ConfigureWireMock[] asArray() {
      return this.annotations.toArray(new ConfigureWireMock[] {});
    }
  }
}
