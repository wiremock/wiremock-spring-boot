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
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.WireMockConfigurationCustomizer;

public class WireMockServerCreator {
  private final Logger logger;

  public WireMockServerCreator(final String name) {
    this.logger = LoggerFactory.getLogger(WireMockServerCreator.class + " " + name);
  }

  public WireMockServer createWireMockServer(
      final ConfigurableApplicationContext context, final ConfigureWireMock options) {
    final WireMockConfiguration serverOptions =
        options().port(options.port()).notifier(new Slf4jNotifier(options.name()));
    this.configureFilesUnderClasspath(options.filesUnderClasspath(), "/" + options.name())
        .ifPresentOrElse(
            present -> this.usingFilesUnderClasspath(serverOptions, present),
            () -> {
              this.configureFilesUnderClasspath(options.filesUnderClasspath(), "")
                  .ifPresentOrElse(
                      present -> this.usingFilesUnderClasspath(serverOptions, present),
                      () -> {
                        this.configureFilesUnderDirectory(
                                options.filesUnderDirectory(), "/" + options.name())
                            .ifPresentOrElse(
                                present -> this.usingFilesUnderDirectory(serverOptions, present),
                                () -> {
                                  this.configureFilesUnderDirectory(
                                          options.filesUnderDirectory(), "")
                                      .ifPresent(
                                          present ->
                                              this.usingFilesUnderDirectory(
                                                  serverOptions, present));
                                });
                      });
            });

    if (options.extensions().length > 0) {
      serverOptions.extensions(options.extensions());
    }

    this.applyCustomizers(options, serverOptions);

    this.logger.info(
        "Configuring WireMockServer with name '{}' on port: {}",
        options.name(),
        serverOptions.portNumber());

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

    Arrays.stream(options.baseUrlProperties())
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toList())
        .forEach(
            propertyName -> {
              final String property = propertyName + "=" + newServer.baseUrl();
              this.logger.info("Adding property '{}' to Spring application context", property);
              TestPropertyValues.of(property).applyTo(context.getEnvironment());
            });

    Arrays.stream(options.portProperties())
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toList())
        .forEach(
            propertyName -> {
              final String property = propertyName + "=" + newServer.port();
              this.logger.info("Adding property '{}' to Spring application context", property);
              TestPropertyValues.of(property).applyTo(context.getEnvironment());
            });

    return newServer;
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
}
