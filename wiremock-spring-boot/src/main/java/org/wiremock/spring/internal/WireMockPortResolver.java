package org.wiremock.spring.internal;

import java.util.Arrays;
import java.util.List;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.wiremock.spring.ConfigureWireMock;

public class WireMockPortResolver {
  private final ConfigurableEnvironment environment;
  private final Logger logger;

  WireMockPortResolver(ConfigurableEnvironment environment) {
    this.environment = environment;
    this.logger = LoggerFactory.getLogger(WireMockPortResolver.class);
  }

  int getServerHttpPortProperty(ConfigureWireMock options) {
    if (!options.usePortFromPredefinedPropertyIfFound()) {
      return options.port();
    }
    return Arrays.stream(options.portProperties())
        .filter(StringUtils::isNotBlank)
        .filter(propertyName -> environment.containsProperty(propertyName))
        .map(
            propertyName -> {
              final int predefinedPropertyValue =
                  Integer.parseInt(environment.getProperty(propertyName));
              this.logger.info(
                  "Found predefined port in property with name '{}' on port: {}",
                  propertyName,
                  predefinedPropertyValue);
              return predefinedPropertyValue;
            })
        .findFirst()
        .orElse(options.port());
  }

  int getServerHttpsPortProperty(ConfigureWireMock options) {
    if (!options.usePortFromPredefinedPropertyIfFound()) {
      return options.httpsPort();
    }
    return Arrays.stream(options.httpsPortProperties())
        .filter(StringUtils::isNotBlank)
        .filter(propertyName -> environment.containsProperty(propertyName))
        .map(
            propertyName -> {
              final int predefinedPropertyValue =
                  Integer.parseInt(environment.getProperty(propertyName));
              this.logger.info(
                  "Found predefined https port in property with name '{}' on port: {}",
                  propertyName,
                  predefinedPropertyValue);
              return predefinedPropertyValue;
            })
        .findFirst()
        .orElse(options.httpsPort());
  }

  public boolean anyStaticPortWithDirtySpringContext(List<ConfigureWireMock> configureWireMocks) {
    return configureWireMocks.stream()
        .anyMatch(
            it ->
                it.staticPortDirtySpringContext()
                    && (this.getServerHttpPortProperty(it) > 0
                        || this.getServerHttpsPortProperty(it) > 0));
  }
}
