package org.wiremock.spring;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * JUnit 5 extension that sets {@link WireMockServer} instances previously registered with {@link
 * ConfigureWireMock} on test class fields.
 *
 * @author Maciej Walkowiak
 */
public class WireMockSpringExtension implements BeforeEachCallback, ParameterResolver {

  @Override
  public void beforeEach(final ExtensionContext extensionContext) throws Exception {
    // reset all wiremock servers associated with application context
    Store.INSTANCE.findAllInstances(extensionContext).forEach(WireMockServer::resetAll);

    // inject properties into test class fields
    injectWireMockInstances(extensionContext, WireMock.class, WireMock::value);
    injectWireMockInstances(extensionContext, InjectWireMock.class, InjectWireMock::value);
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
        && (parameterContext.isAnnotated(WireMock.class)
            || parameterContext.isAnnotated(InjectWireMock.class));
  }

  @Override
  public Object resolveParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext) {
    final String wireMockServerName =
        parameterContext
            .findAnnotation(WireMock.class)
            .map(WireMock::value)
            .orElseGet(() -> parameterContext.findAnnotation(InjectWireMock.class).get().value());
    return Store.INSTANCE.findRequiredWireMockInstance(extensionContext, wireMockServerName);
  }
}
