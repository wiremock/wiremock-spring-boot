package org.wiremock.spring.internal;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.ExtensionFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.WireMockConfigurationCustomizer;

public class WireMockServerCreator {
  private static final int PORT_DISABLED = -1;
  private final Logger logger;

  public WireMockServerCreator(final String name) {
    this.logger = LoggerFactory.getLogger(WireMockServerCreator.class + " " + name);
  }

  public WireMockServer createWireMockServer(
      final ConfigurableApplicationContext context, final ConfigureWireMock options) {

    final WireMockConfiguration serverOptions = options();

    final int serverHttpsPort = this.getServerHttpsPortProperty(context.getEnvironment(), options);
    final boolean httpsEnabled = serverHttpsPort != PORT_DISABLED;
    if (httpsEnabled) {
      serverOptions.httpsPort(serverHttpsPort);
    }

    final int serverHttpPort = this.getServerHttpPortProperty(context.getEnvironment(), options);
    final boolean httpEnabled = serverHttpPort != PORT_DISABLED;
    if (httpEnabled) {
      serverOptions.port(serverHttpPort);
    }
    serverOptions.notifier(new Slf4jNotifier(options.name()));

    this.configureFilesUnderDirectory(options.filesUnderDirectory(), "/" + options.name())
        .ifPresentOrElse(
            present -> this.usingFilesUnderDirectory(serverOptions, present),
            () -> {
              this.configureFilesUnderDirectory(options.filesUnderDirectory(), "")
                  .ifPresentOrElse(
                      present -> this.usingFilesUnderDirectory(serverOptions, present),
                      () -> {
                        this.logger.info("No mocks found under directory");
                        this.configureFilesUnderClasspath(
                                options.filesUnderClasspath(), "/" + options.name())
                            .ifPresentOrElse(
                                present -> this.usingFilesUnderClasspath(serverOptions, present),
                                () -> {
                                  this.configureFilesUnderClasspath(
                                          options.filesUnderClasspath(), "")
                                      .ifPresentOrElse(
                                          present ->
                                              this.usingFilesUnderClasspath(serverOptions, present),
                                          () -> {
                                            this.logger.info("No mocks found under classpath");
                                          });
                                });
                      });
            });

    if (options.extensionFactories().length > 0) {
      serverOptions.extensionFactories(options.extensionFactories());
    }

    if (options.extensions().length > 0) {
      serverOptions.extensions(options.extensions());
    }

    this.applyCustomizers(options, serverOptions);

    this.logger.info(
        "Configuring WireMockServer with name '{}' on HTTP port: {} and HTTPS port: {}",
        options.name(),
        serverOptions.portNumber(),
        serverOptions.httpsSettings().port());

    final WireMockServer newServer = new WireMockServer(serverOptions);
    newServer.start();

    this.logger.info(
        "Started WireMockServer with name '{}':{}", options.name(), newServer.baseUrl());

    // save server to store
    Store.INSTANCE.store(context, options.name(), newServer);

    // add shutdown hook
    context.addApplicationListener(
        event -> {
          if (event instanceof ContextClosedEvent) {
            this.logger.info("Stopping WireMockServer with name '{}'", options.name());
            newServer.stop();
          }
        });

    if (httpEnabled) {
      Arrays.stream(options.baseUrlProperties())
          .filter(StringUtils::isNotBlank)
          .collect(Collectors.toList())
          .forEach(
              propertyName -> {
                final String property =
                    propertyName + "=" + String.format("http://localhost:%d", newServer.port());
                this.logger.info(
                    "Adding property '{}' with HTTP base URL to Spring application context",
                    property);
                TestPropertyValues.of(property).applyTo(context.getEnvironment());
              });

      Arrays.stream(options.portProperties())
          .filter(StringUtils::isNotBlank)
          .collect(Collectors.toList())
          .forEach(
              propertyName -> {
                final String property = propertyName + "=" + newServer.port();
                this.logger.info(
                    "Adding property '{}' with HTTP port to Spring application context", property);
                TestPropertyValues.of(property).applyTo(context.getEnvironment());
              });
    }

    if (httpsEnabled) {
      Arrays.stream(options.httpsBaseUrlProperties())
          .filter(StringUtils::isNotBlank)
          .collect(Collectors.toList())
          .forEach(
              propertyName -> {
                final String property =
                    propertyName
                        + "="
                        + String.format("https://localhost:%d", newServer.httpsPort());
                this.logger.info(
                    "Adding property '{}' with HTTPS base URL to Spring application context",
                    property);
                TestPropertyValues.of(property).applyTo(context.getEnvironment());
              });

      Arrays.stream(options.httpsPortProperties())
          .filter(StringUtils::isNotBlank)
          .collect(Collectors.toList())
          .forEach(
              propertyName -> {
                final String property = propertyName + "=" + newServer.httpsPort();
                this.logger.info(
                    "Adding property '{}' with HTTPS port to Spring application context", property);
                TestPropertyValues.of(property).applyTo(context.getEnvironment());
              });
    }

    return newServer;
  }

  private int getServerHttpPortProperty(
      final ConfigurableEnvironment environment, final ConfigureWireMock options) {
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

  private int getServerHttpsPortProperty(
      final ConfigurableEnvironment environment, final ConfigureWireMock options) {
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

  private WireMockConfiguration usingFilesUnderClasspath(
      final WireMockConfiguration serverOptions, final String resource) {
    this.logger.info("Serving WireMock mappings from classpath resource: " + resource);
    return serverOptions.usingFilesUnderClasspath(resource);
  }

  private WireMockConfiguration usingFilesUnderDirectory(
      final WireMockConfiguration serverOptions, final String dir) {
    this.logger.info("Serving WireMock mappings from directory: " + dir);
    return serverOptions.usingFilesUnderDirectory(dir);
  }

  private Optional<String> configureFilesUnderClasspath(
      final String[] filesUnderClasspath, final String suffix) {
    final List<String> alternatives =
        List.of(filesUnderClasspath).stream()
            .map(it -> it + suffix)
            .filter(
                it -> {
                  final String name = "/" + it;
                  final boolean exists = WireMockContextCustomizer.class.getResource(name) != null;
                  this.logger.info(
                      "Looking for mocks in classpath " + name + "... " + (exists ? "found" : ""));
                  return exists;
                })
            .toList();
    if (alternatives.size() > 1) {
      throw new IllegalStateException(
          "Found several filesUnderClasspath: "
              + alternatives.stream().collect(Collectors.joining(", ")));
    }
    return alternatives.stream().findFirst();
  }

  private Optional<String> configureFilesUnderDirectory(
      final String[] filesUnderDirectory, final String suffix) {
    final List<String> alternatives =
        List.of(filesUnderDirectory).stream()
            .map(it -> it + suffix)
            .filter(
                it -> {
                  final File name = Path.of(it).toFile();
                  final boolean exists = name.exists();
                  this.logger.info(
                      "Looking for mocks in directory " + name + "... " + (exists ? "found" : ""));
                  return exists;
                })
            .toList();
    final String alternativesString = alternatives.stream().collect(Collectors.joining(", "));
    if (alternatives.size() > 1) {
      throw new IllegalStateException("Found several filesUnderDirectory: " + alternativesString);
    }
    this.logger.debug(
        "Found " + alternativesString + " in " + Path.of("").toFile().getAbsolutePath());
    return alternatives.stream().findFirst();
  }

  @SuppressFBWarnings
  private void applyCustomizers(
      final ConfigureWireMock options, final WireMockConfiguration serverOptions) {
    for (final Class<? extends WireMockConfigurationCustomizer> customizer :
        options.configurationCustomizers()) {
      try {
        ReflectionUtils.newInstance(customizer).customize(serverOptions, options);
      } catch (final Exception e) {
        if (e instanceof NoSuchMethodException) { // NOPMD
          this.logger.error("Customizer {} must have a no-arg constructor", customizer, e);
        }
        throw e;
      }
    }
  }

  private static ExtensionFactory instantiateExtensionFactory(
      Class<? extends ExtensionFactory> factoryClass) {
    return Exceptions.uncheck(
        () -> factoryClass.getDeclaredConstructor().newInstance(), ExtensionFactory.class);
  }
}
