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
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.WireMockConfigurationCustomizer;

public class WireMockServerCreator {
  private static final int PORT_DISABLED = -1;
  private final Logger logger;

  public WireMockServerCreator(final String name) {
    this.logger = LoggerFactory.getLogger(WireMockServerCreator.class.getName() + "." + name);
  }

  public WireMockServer createWireMockServer(
      final ConfigurableApplicationContext context, final ConfigureWireMock options) {

    final WireMockConfiguration serverOptions = this.buildServerOptions(context, options);

    this.logger.info(
        "Configuring WireMockServer with name '{}' on HTTP port: {} and HTTPS port: {}",
        options.name(),
        serverOptions.portNumber(),
        serverOptions.httpsSettings().port());

    final WireMockServer newServer = new WireMockServer(serverOptions);
    newServer.start();

    this.logger.info(
        "Started WireMockServer with name '{}':{}", options.name(), newServer.baseUrl());

    Store.INSTANCE.store(context, options.name(), newServer);
    this.registerShutdownHooks(context, options.name(), newServer);
    this.publishServerProperties(context, options, newServer);

    if (options.registerSpringBean()) {
      this.logger.info("Registering WireMockServer '" + options.name() + "' as a Spring Bean.");
      context.getBeanFactory().registerSingleton(options.name(), newServer);
    }

    return newServer;
  }

  private WireMockConfiguration buildServerOptions(
      final ConfigurableApplicationContext context, final ConfigureWireMock options) {
    final WireMockConfiguration serverOptions = options();
    final WireMockPortResolver portResolver = new WireMockPortResolver(context.getEnvironment());

    final int serverHttpsPort = portResolver.getServerHttpsPortProperty(options);
    if (serverHttpsPort != PORT_DISABLED) {
      serverOptions.httpsPort(serverHttpsPort);
      this.configureTls(options, serverOptions);
    }

    final int serverHttpPort = portResolver.getServerHttpPortProperty(options);
    final boolean httpEnabled = serverHttpPort != PORT_DISABLED;
    serverOptions.httpDisabled(!httpEnabled);
    if (httpEnabled) {
      serverOptions.port(serverHttpPort);
    }

    serverOptions.notifier(new Slf4jNotifier(options.name()));
    this.configureMappings(options, serverOptions);

    if (options.extensionFactories().length > 0) {
      serverOptions.extensionFactories(options.extensionFactories());
    }
    if (options.extensions().length > 0) {
      serverOptions.extensions(options.extensions());
    }

    serverOptions.globalTemplating(options.globalTemplating());
    this.applyCustomizers(options, serverOptions);

    return serverOptions;
  }

  private void registerShutdownHooks(
      final ConfigurableApplicationContext context,
      final String name,
      final WireMockServer server) {
    context.addApplicationListener(
        event -> {
          if (event instanceof ContextClosedEvent) {
            this.stopWireMockServer(name, server);
          }
        });

    if (context.getBeanFactory() instanceof DefaultSingletonBeanRegistry singletonBeanRegistry) {
      singletonBeanRegistry.registerDisposableBean(
          name + "-shutdown", () -> this.stopWireMockServer(name, server));
    }
  }

  private void publishServerProperties(
      final ConfigurableApplicationContext context,
      final ConfigureWireMock options,
      final WireMockServer newServer) {
    if (newServer.isHttpEnabled()) {
      this.publishProperties(
          context,
          options.baseUrlProperties(),
          String.format("http://localhost:%d", newServer.port()),
          "HTTP base URL");
      this.publishProperties(
          context, options.portProperties(), String.valueOf(newServer.port()), "HTTP port");
    }

    if (newServer.isHttpsEnabled()) {
      this.publishProperties(
          context,
          options.httpsBaseUrlProperties(),
          String.format("https://localhost:%d", newServer.httpsPort()),
          "HTTPS base URL");
      this.publishProperties(
          context,
          options.httpsPortProperties(),
          String.valueOf(newServer.httpsPort()),
          "HTTPS port");
    }
  }

  private void publishProperties(
      final ConfigurableApplicationContext context,
      final String[] propertyNames,
      final String value,
      final String description) {
    Arrays.stream(propertyNames)
        .filter(StringUtils::isNotBlank)
        .forEach(propertyName -> this.publishProperty(context, propertyName, value, description));
  }

  private void publishProperty(
      final ConfigurableApplicationContext context,
      final String propertyName,
      final String value,
      final String description) {
    final String property = propertyName + "=" + value;
    this.logger.info(
        "Adding property '{}' with {} to Spring application context", property, description);
    TestPropertyValues.of(property).applyTo(context.getEnvironment());
  }

  private void stopWireMockServer(final String name, final WireMockServer server) {
    if (server.isRunning()) {
      this.logger.info("Stopping WireMockServer with name '{}'", name);
      server.stop();
    }
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

  private void configureTls(
      final ConfigureWireMock options, final WireMockConfiguration serverOptions) {
    if (StringUtils.isNotBlank(options.keystorePath())) {
      this.logger.info("Using keystore from '{}' for HTTPS", options.keystorePath());
      serverOptions.keystorePath(options.keystorePath());
      if (StringUtils.isNotBlank(options.keystorePassword())) {
        serverOptions.keystorePassword(options.keystorePassword());
      }
      if (StringUtils.isNotBlank(options.keystoreType())) {
        serverOptions.keystoreType(options.keystoreType());
      }
      if (StringUtils.isNotBlank(options.keyManagerPassword())) {
        serverOptions.keyManagerPassword(options.keyManagerPassword());
      }
    }

    if (StringUtils.isNotBlank(options.trustStorePath())) {
      this.logger.info("Using trust store from '{}' for HTTPS", options.trustStorePath());
      serverOptions.trustStorePath(options.trustStorePath());
      if (StringUtils.isNotBlank(options.trustStorePassword())) {
        serverOptions.trustStorePassword(options.trustStorePassword());
      }
      if (StringUtils.isNotBlank(options.trustStoreType())) {
        serverOptions.trustStoreType(options.trustStoreType());
      }
    }

    if (options.needClientAuth()) {
      serverOptions.needClientAuth(true);
    }
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
