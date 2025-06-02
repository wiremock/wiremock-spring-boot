package org.wiremock.spring.internal;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    configureMappings(options, serverOptions);

    if (options.extensionFactories().length > 0) {
      serverOptions.extensionFactories(options.extensionFactories());
    }

    if (options.extensions().length > 0) {
      serverOptions.extensions(options.extensions());
    }

    serverOptions.globalTemplating(options.globalTemplating());

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

    if (options.registerSpringBean()) {
      this.logger.info("Registering WireMockServer '" + options.name() + "' as a Spring Bean.");
      context.getBeanFactory().registerSingleton(options.name(), newServer);
    }

    return newServer;
  }

  private void configureMappings(ConfigureWireMock options, WireMockConfiguration serverOptions) {
    boolean isFilesUnderDirectorySupplied = options.filesUnderDirectory().length != 0;
    boolean isFilesUnderClasspathSupplied = !options.filesUnderClasspath().isEmpty();
    if (isFilesUnderDirectorySupplied) {
      Optional<String> foundFilesUnderDirectoryOpt =
          this.findFirstExistingDirectory(options.filesUnderDirectory());
      if (foundFilesUnderDirectoryOpt.isEmpty()) {
        throw new IllegalStateException(
            "Cannot find configured mappings directory " + options.filesUnderDirectory());
      }
      this.usingFilesUnderDirectory(serverOptions, foundFilesUnderDirectoryOpt.get());
    } else if (isFilesUnderClasspathSupplied) {
      this.usingFilesUnderClasspath(serverOptions, options.filesUnderClasspath());
    } else {
      Optional<String> fondFilesUnderDirOpt =
          this.findFirstExistingDirectory(
              ConfigureWireMock.DEFAULT_FILES_UNDER_DIRECTORY.toArray(new String[0]));
      fondFilesUnderDirOpt.ifPresent(s -> this.usingFilesUnderDirectory(serverOptions, s));
      if (fondFilesUnderDirOpt.isEmpty()) {
        this.logger.info("No mocks found under directory");
      }
    }
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

  private void usingFilesUnderClasspath(
      final WireMockConfiguration serverOptions, final String resource) {
    this.logger.info("Serving WireMock mappings from classpath resource: " + resource);
    serverOptions.usingFilesUnderClasspath(resource);
  }

  private void usingFilesUnderDirectory(
      final WireMockConfiguration serverOptions, final String dir) {
    this.logger.info("Serving WireMock mappings from directory: " + dir);
    serverOptions.usingFilesUnderDirectory(dir);
  }

  private Optional<String> findFirstExistingDirectory(final String... filesUnderDirectory) {
    final List<String> alternatives =
        Stream.of(filesUnderDirectory)
            .filter(
                it -> {
                  final File name = Path.of(it).toFile();
                  final boolean exists =
                      Path.of(it, "mappings").toFile().exists()
                          || Path.of(it, "__files").toFile().exists();
                  this.logger.info(
                      "Looking for mocks in directory " + name + "... " + (exists ? "found" : ""));
                  return exists;
                })
            .toList();
    final String alternativesString = alternatives.stream().collect(Collectors.joining(", "));
    this.logger.debug(
        "Found " + alternativesString + " in " + Path.of("").toFile().getAbsolutePath());
    Optional<String> firstMatch = alternatives.stream().findFirst();
    if (firstMatch.isPresent()) {
      this.logger.info("Using mocks from " + firstMatch.get());
    }
    return firstMatch;
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
}
