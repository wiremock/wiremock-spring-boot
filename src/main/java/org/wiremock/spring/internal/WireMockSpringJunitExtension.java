package org.wiremock.spring.internal;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

/**
 * JUnit 5 extension that sets {@link com.github.tomakehurst.wiremock.WireMockServer} instances
 * previously registered with {@link org.wiremock.spring.ConfigureWireMock} on test class fields.
 *
 * @author Maciej Walkowiak
 */
public class WireMockSpringJunitExtension
    implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final Logger LOGGER = LoggerFactory.getLogger(WireMockSpringJunitExtension.class);

  @Override
  public void beforeEach(final ExtensionContext extensionContext) throws Exception {
    this.resetWireMockServersIfConfigured(extensionContext);

    // inject properties into test class fields
    injectWireMockInstances(extensionContext, InjectWireMock.class, InjectWireMock::value);

    this.configureWireMockForDefaultInstance(extensionContext);
  }

  private void resetWireMockServersIfConfigured(final ExtensionContext extensionContext) {
    final List<Object> instances = extensionContext.getRequiredTestInstances().getAllInstances();
    for (final Object instance : instances) {
      final EnableWireMock enableWireMockAnnotation =
          AnnotationUtils.findAnnotation(instance.getClass(), EnableWireMock.class);
      if (enableWireMockAnnotation == null) {
        continue;
      }
      final ConfigureWireMock[] wireMockServers =
          WireMockContextCustomizerFactory.getConfigureWireMocksOrDefault(
              enableWireMockAnnotation.value());
      List.of(wireMockServers).stream()
          .filter(it -> it.resetWireMockServer())
          .map(it -> Store.INSTANCE.findRequiredWireMockInstance(extensionContext, it.name()))
          .forEach(WireMockServer::resetAll);
    }
  }

  private void configureWireMockForDefaultInstance(final ExtensionContext extensionContext) {
    final List<Object> instances = extensionContext.getRequiredTestInstances().getAllInstances();
    WireMockServer wiremock = null;
    String wireMockName = null;
    for (final Object instance : instances) {
      final EnableWireMock enableWireMockAnnotation =
          AnnotationUtils.findAnnotation(instance.getClass(), EnableWireMock.class);
      if (enableWireMockAnnotation == null) {
        continue;
      }
      final ConfigureWireMock[] wireMockServers =
          WireMockContextCustomizerFactory.getConfigureWireMocksOrDefault(
              enableWireMockAnnotation.value());
      if (wireMockServers.length > 1) {
        LOGGER.info(
            "Not configuring WireMock for default instance when several ConfigureWireMock ("
                + wireMockServers.length
                + ")");
      }
      if (wiremock != null) {
        LOGGER.info("Not configuring WireMock for default instance when several candidates found");
        return;
      }
      wireMockName = wireMockServers[0].name();
      wiremock = Store.INSTANCE.findRequiredWireMockInstance(extensionContext, wireMockName);
    }
    if (wiremock != null) {
      LOGGER.info(
          "Configuring WireMock for default instance, '"
              + wireMockName
              + "' on '"
              + wiremock.port()
              + "'.");
      final String host = "localhost";
      if (wiremock.isHttpsEnabled()) {
        WireMock.configureFor(
            WireMock.create().https().host(host).port(wiremock.httpsPort()).build());
      } else {
        WireMock.configureFor(WireMock.create().http().host(host).port(wiremock.port()).build());
      }
    }
  }

  @Override
  public void afterEach(final ExtensionContext context) throws Exception {
    WireMock.configureFor(-1);
  }

  private static <T extends Annotation> void injectWireMockInstances(
      final ExtensionContext extensionContext,
      final Class<T> annotation,
      final Function<T, String> fn)
      throws IllegalAccessException {
    // getRequiredTestInstances() return multiple instances for nested tests
    for (final Object testInstance :
        extensionContext.getRequiredTestInstances().getAllInstances()) {
      final List<Field> annotatedFields =
          AnnotationSupport.findAnnotatedFields(testInstance.getClass(), annotation);
      for (final Field annotatedField : annotatedFields) {
        final T annotationValue = annotatedField.getAnnotation(annotation);
        annotatedField.setAccessible(true); // NOPMD

        final WireMockServer wiremock =
            Store.INSTANCE.findRequiredWireMockInstance(
                extensionContext, fn.apply(annotationValue));
        annotatedField.set(testInstance, wiremock);
      }
    }
  }

  @Override
  public boolean supportsParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType() == WireMockServer.class
        && (parameterContext.isAnnotated(InjectWireMock.class));
  }

  @Override
  public Object resolveParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext) {
    final String wireMockServerName =
        parameterContext.findAnnotation(InjectWireMock.class).get().value();
    return Store.INSTANCE.findRequiredWireMockInstance(extensionContext, wireMockServerName);
  }
}
