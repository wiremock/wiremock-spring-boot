package org.wiremock.spring.internal;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.wiremock.spring.InjectWireMock;

/**
 * JUnit 5 extension that resolves {@link InjectWireMock}-annotated constructor and method
 * parameters to the {@link WireMockServer} instances registered with {@link
 * org.wiremock.spring.ConfigureWireMock}.
 *
 * <p>Injecting {@code @InjectWireMock} fields, resetting servers between tests and configuring the
 * default WireMock client happen in {@link WireMockTestExecutionListener} instead, so that they
 * also work with test engines other than JUnit Jupiter (e.g. Cucumber, TestNG) that drive Spring's
 * {@link org.springframework.test.context.TestContextManager} directly. Parameter resolution has no
 * equivalent in the Spring TestContext framework, so it remains here.
 *
 * @author Maciej Walkowiak
 */
public class WireMockSpringJunitExtension implements ParameterResolver {

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
