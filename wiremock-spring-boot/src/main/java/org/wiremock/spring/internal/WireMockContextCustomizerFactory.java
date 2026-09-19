package org.wiremock.spring.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

/**
 * Creates {@link WireMockContextCustomizer} for test classes annotated with {@link EnableWireMock}.
 *
 * @author Maciej Walkowiak
 */
public class WireMockContextCustomizerFactory implements ContextCustomizerFactory {
  static final ConfigureWireMock DEFAULT_CONFIGURE_WIREMOCK =
      DefaultConfigureWireMock.class.getAnnotation(ConfigureWireMock.class);

  @ConfigureWireMock(name = "wiremock")
  private static class DefaultConfigureWireMock {}

  static ConfigureWireMock[] getConfigureWireMocksOrDefault(
      final ConfigureWireMock... configureWireMock) {
    if (configureWireMock == null || configureWireMock.length == 0) {
      return new ConfigureWireMock[] {WireMockContextCustomizerFactory.DEFAULT_CONFIGURE_WIREMOCK};
    }
    return configureWireMock;
  }

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
    for (EnableWireMock enableWireMockAnnotation : getEnableWireMockAnnotations(testClass)) {
      parser.add(getConfigureWireMocksOrDefault(enableWireMockAnnotation.value()));
    }
    for (ConfigureWireMock configureWireMock :
        getStandaloneConfigureWireMockAnnotations(testClass)) {
      parser.addIfAbsent(configureWireMock);
    }
  }

  // Test classes don't change at runtime, and this is invoked on every test method (not just
  // once per test class) via WireMockTestExecutionListener, so the reflection-based scan below
  // is memoized per Class.
  private static final Map<Class<?>, List<ConfigureWireMock>>
      STANDALONE_CONFIGURE_WIRE_MOCK_ANNOTATIONS_CACHE = new ConcurrentHashMap<>();
  private static final Map<Class<?>, List<EnableWireMock>> ENABLE_WIRE_MOCK_ANNOTATIONS_CACHE =
      new ConcurrentHashMap<>();

  static List<ConfigureWireMock> getStandaloneConfigureWireMockAnnotations(
      final Class<?> testClass) {
    return STANDALONE_CONFIGURE_WIRE_MOCK_ANNOTATIONS_CACHE.computeIfAbsent(
        testClass, WireMockContextCustomizerFactory::scanStandaloneConfigureWireMockAnnotations);
  }

  private static List<ConfigureWireMock> scanStandaloneConfigureWireMockAnnotations(
      final Class<?> testClass) {
    final List<ConfigureWireMock> annotations = new ArrayList<>();
    Optional.ofNullable(
            AnnotationSupport.findRepeatableAnnotations(testClass, ConfigureWireMock.class))
        .ifPresent(annotations::addAll);

    // Recurses into the uncached scan, not the cached entry point: ConcurrentHashMap forbids a
    // computeIfAbsent mapping function from making another computeIfAbsent call on the same map.
    Arrays.asList(testClass.getEnclosingClass(), testClass.getSuperclass()).stream()
        .filter(clazz -> clazz != null)
        .forEach(
            clazz ->
                annotations.addAll(
                    scanStandaloneConfigureWireMockAnnotations(clazz).stream()
                        .filter(it -> !annotations.contains(it))
                        .toList()));

    return List.copyOf(annotations);
  }

  static List<EnableWireMock> getEnableWireMockAnnotations(final Class<?> testClass) {
    return ENABLE_WIRE_MOCK_ANNOTATIONS_CACHE.computeIfAbsent(
        testClass, WireMockContextCustomizerFactory::scanEnableWireMockAnnotations);
  }

  private static List<EnableWireMock> scanEnableWireMockAnnotations(final Class<?> testClass) {
    final List<EnableWireMock> annotations = new ArrayList<>();
    Optional.ofNullable(
            AnnotationSupport.findRepeatableAnnotations(testClass, EnableWireMock.class))
        .ifPresent(annotations::addAll);

    // Recurses into the uncached scan, not the cached entry point: ConcurrentHashMap forbids a
    // computeIfAbsent mapping function from making another computeIfAbsent call on the same map.
    Arrays.asList(testClass.getEnclosingClass(), testClass.getSuperclass()).stream()
        .filter(clazz -> clazz != null)
        .forEach(
            clazz ->
                annotations.addAll(
                    scanEnableWireMockAnnotations(clazz).stream()
                        .filter(it -> !annotations.contains(it))
                        .toList()));

    return List.copyOf(annotations);
  }

  private static class ConfigureWiremockHolder {
    private final List<ConfigureWireMock> annotations = new ArrayList<>();

    void add(final ConfigureWireMock... annotations) {
      this.annotations.addAll(Arrays.asList(annotations));
      this.sanityCheckDuplicateNames(this.annotations);
      this.sanityCheckHttpOrHttpsMustBeEnabled(this.annotations);
      this.sanityCheckHttpAndHttpsMustUseDifferentPorts(this.annotations);
      this.sanityCheckUniquePorts(this.annotations);
    }

    void addIfAbsent(final ConfigureWireMock annotation) {
      final boolean nameAlreadyExists =
          this.annotations.stream().anyMatch(it -> it.name().equals(annotation.name()));
      if (!nameAlreadyExists) {
        this.add(annotation);
      }
    }

    private void sanityCheckDuplicateNames(final List<ConfigureWireMock> check) {
      final List<String> names = check.stream().map(it -> it.name()).toList();
      final Set<String> dublicateNames =
          names.stream()
              .filter(it -> Collections.frequency(names, it) > 1)
              .collect(Collectors.toSet());
      if (!dublicateNames.isEmpty()) {
        throw new IllegalStateException(
            "Names of mocks must be unique, found duplicates of: "
                + dublicateNames.stream().sorted().collect(Collectors.joining(",")));
      }
    }

    private void sanityCheckHttpOrHttpsMustBeEnabled(final List<ConfigureWireMock> check) {
      for (final ConfigureWireMock configureWireMock : check) {
        if (configureWireMock.port() == -1 && configureWireMock.httpsPort() == -1) {
          throw new IllegalStateException(
              "ConfigureWireMock "
                  + configureWireMock.name()
                  + " has both HTTP and HTTPS disabled. It is an invalid configuration.");
        }
      }
    }

    private void sanityCheckUniquePorts(final List<ConfigureWireMock> check) {
      final List<Integer> ports =
          check.stream().map(it -> List.of(it.port(), it.httpsPort())).toList().stream()
              .collect(ArrayList::new, List::addAll, List::addAll);
      final Set<Integer> dublicatePors =
          ports.stream()
              .filter(it -> it > 0)
              .filter(it -> Collections.frequency(ports, it) > 1)
              .collect(Collectors.toSet());
      if (!dublicatePors.isEmpty()) {
        throw new IllegalStateException(
            "Some statically configured ports are being used mor than once: "
                + dublicatePors.stream()
                    .sorted()
                    .map(it -> it.toString())
                    .collect(Collectors.joining(",")));
      }
    }

    private void sanityCheckHttpAndHttpsMustUseDifferentPorts(final List<ConfigureWireMock> check) {
      for (final ConfigureWireMock configureWireMock : check) {
        if (configureWireMock.port() > 0
            && configureWireMock.port() == configureWireMock.httpsPort()) {
          throw new IllegalStateException(
              "ConfigureWireMock "
                  + configureWireMock.name()
                  + " uses same port "
                  + configureWireMock.port()
                  + " for HTTP and HTTPS.");
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
