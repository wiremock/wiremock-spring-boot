package org.wiremock.spring;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Customizes {@link WireMockConfiguration} programmatically. Can be registered with {@link
 * ConfigureWireMock#configurationCustomizers()}. Customizer must have public no-arg constructor.
 */
public interface WireMockConfigurationCustomizer {

  void customize(WireMockConfiguration configuration, ConfigureWireMock options);
}
