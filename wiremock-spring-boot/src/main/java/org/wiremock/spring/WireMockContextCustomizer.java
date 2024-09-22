package org.wiremock.spring;

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
