package org.wiremock.spring.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  /**
   * All {@link ConfigureWireMock} entries that apply to {@code testClass}: those declared directly
   * or via {@link EnableWireMock} (falling back to the default when none are given), plus any
   * standalone ones. Unlike {@link #parseDefinitions}, this does not validate or de-duplicate by
   * name - it is for callers that just need the flat list of entries, such as resolving which
   * {@link com.github.tomakehurst.wiremock.WireMockServer} instances a test method should reset.
   */
  static List<ConfigureWireMock> resolveConfigureWireMocks(final Class<?> testClass) {
    final List<ConfigureWireMock> result = new ArrayList<>();
    for (final EnableWireMock enableWireMockAnnotation : getEnableWireMockAnnotations(testClass)) {
      result.addAll(List.of(getConfigureWireMocksOrDefault(enableWireMockAnnotation.value())));
    }
    result.addAll(getStandaloneConfigureWireMockAnnotations(testClass));
    return result;
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
      final Set<String> duplicateNames =
          check.stream()
              .map(ConfigureWireMock::name)
              .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
              .entrySet()
              .stream()
              .filter(entry -> entry.getValue() > 1)
              .map(Map.Entry::getKey)
              .collect(Collectors.toSet());
      if (!duplicateNames.isEmpty()) {
        throw new IllegalStateException(
            "Names of mocks must be unique, found duplicates of: "
                + duplicateNames.stream().sorted().collect(Collectors.joining(",")));
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
      final Set<Integer> duplicatePorts =
          check.stream()
              .flatMap(it -> Stream.of(it.port(), it.httpsPort()))
              .filter(it -> it > 0)
              .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
              .entrySet()
              .stream()
              .filter(entry -> entry.getValue() > 1)
              .map(Map.Entry::getKey)
              .collect(Collectors.toSet());
      if (!duplicatePorts.isEmpty()) {
        throw new IllegalStateException(
            "Some statically configured ports are being used mor than once: "
                + duplicatePorts.stream()
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
