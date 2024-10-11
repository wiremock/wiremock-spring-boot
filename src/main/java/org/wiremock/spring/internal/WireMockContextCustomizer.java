package org.wiremock.spring.internal;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.wiremock.spring.ConfigureWireMock;

/**
 * Attaches properties with urls pointing to {@link WireMockServer} instances to the Spring {@link
 * org.springframework.core.env.Environment}.
 *
 * @author Maciej Walkowiak
 */
public class WireMockContextCustomizer implements ContextCustomizer {
  private static final Logger LOGGER = LoggerFactory.getLogger(WireMockContextCustomizer.class);

  private final List<ConfigureWireMock> configuration;
  private final Class<?> testClass;

  /**
   * Creates an instance of {@link WireMockContextCustomizer}.
   *
   * @param configurations the configurations
   */
  public WireMockContextCustomizer(
      final Class<?> testClass, final List<ConfigureWireMock> configurations) {
    this.configuration = configurations;
    this.testClass = testClass;
  }

  /**
   * Creates an instance of {@link WireMockContextCustomizer}.
   *
   * @param testClass
   * @param configurations the configurations
   */
  public WireMockContextCustomizer(
      final Class<?> testClass, final ConfigureWireMock... configurations) {
    this(testClass, Arrays.asList(configurations));
  }

  @Override
  public void customizeContext(
      final ConfigurableApplicationContext context, final MergedContextConfiguration mergedConfig) {
    for (final ConfigureWireMock configureWiremock : this.configuration) {
      final WireMockServer wireMockServer =
          this.resolveOrCreateWireMockServer(context, configureWiremock);
      if (this.configuration.size() == 1) {
        WireMock.configureFor(wireMockServer.port());
      }
    }
  }

  private WireMockServer resolveOrCreateWireMockServer(
      final ConfigurableApplicationContext context, final ConfigureWireMock options) {
    final WireMockServer wireMockServer =
        Store.INSTANCE.findWireMockInstance(context, options.name());

    if (wireMockServer == null) {
      return new WireMockServerCreator(options.name()).createWireMockServer(context, options);
    } else {
      LOGGER.info("WireMockServer with name '{}' is already configured", options.name());
    }

    return wireMockServer;
  }

  /**
   * The docs in {@link ContextCustomizer} states that equals and hashcode is being used for caching
   * and needs implementation. If test class is not included it will not be unique and
   * customizeContext will not be invoked for all tests.
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final WireMockContextCustomizer other = (WireMockContextCustomizer) obj;
    return Objects.equals(this.configuration, other.configuration)
        && Objects.equals(this.testClass, other.testClass);
  }

  /**
   * @see #equals
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.configuration, this.testClass);
  }
}
