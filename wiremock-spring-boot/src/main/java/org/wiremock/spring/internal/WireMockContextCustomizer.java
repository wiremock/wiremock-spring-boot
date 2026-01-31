package org.wiremock.spring.internal;

import com.github.tomakehurst.wiremock.WireMockServer;
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

  /**
   * Creates an instance of {@link WireMockContextCustomizer}.
   *
   * @param configurations the configurations
   */
  public WireMockContextCustomizer(final List<ConfigureWireMock> configurations) {
    this.configuration = configurations;
  }

  /**
   * Creates an instance of {@link WireMockContextCustomizer}.
   *
   * @param configurations the configurations
   */
  public WireMockContextCustomizer(final ConfigureWireMock... configurations) {
    this(Arrays.asList(configurations));
  }

  @Override
  public void customizeContext(
      final ConfigurableApplicationContext context, final MergedContextConfiguration mergedConfig) {
    for (final ConfigureWireMock configureWiremock : this.configuration) {
      this.resolveOrCreateWireMockServer(context, configureWiremock);
    }

    WireMockPortResolver portResolver = new WireMockPortResolver(context.getEnvironment());
    boolean isDirty = portResolver.anyStaticPortWithDirtySpringContext(this.configuration);
    if (isDirty) {
      LOGGER.info("Will force dirty context because of static port");
      WireMockTestExecutionListener.markContextAsDirty();
    }
  }

  private void resolveOrCreateWireMockServer(
      final ConfigurableApplicationContext context, final ConfigureWireMock options) {
    final WireMockServer wireMockServer =
        Store.INSTANCE.findWireMockInstance(context, options.name());

    if (wireMockServer == null) {
      new WireMockServerCreator(options.name()).createWireMockServer(context, options);
    } else {
      LOGGER.info("WireMockServer with name '{}' is already configured", options.name());
    }
  }

  /**
   * The docs in {@link ContextCustomizer} states that equals and hashcode is being used for caching
   * and needs implementation. The customizeContext method will not be invoked for all tests,
   * because of caching.
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    final WireMockContextCustomizer that = (WireMockContextCustomizer) o;
    return Objects.equals(this.configuration, that.configuration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.configuration);
  }
}
