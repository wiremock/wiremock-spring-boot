package org.wiremock.spring.internal;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

/**
 * Drives the same per-test WireMock behavior as {@link WireMockSpringJunitExtension} - injecting
 * {@link InjectWireMock} fields, resetting servers between tests and configuring the default {@link
 * WireMock} client - through the Spring TestContext framework instead of JUnit Jupiter extension
 * callbacks.
 *
 * <p>Spring invokes {@link org.springframework.test.context.TestExecutionListener} regardless of
 * which test engine drives {@link org.springframework.test.context.TestContextManager} (JUnit
 * Jupiter's {@code SpringExtension}, JUnit 4's {@code SpringJUnit4ClassRunner}, TestNG's {@code
 * AbstractTestNGSpringContextTests}, or Cucumber's {@code cucumber-spring}), so this listener,
 * registered in {@code META-INF/spring.factories}, is what makes {@code @InjectWireMock} and
 * automatic resets work outside of JUnit Jupiter tests too.
 *
 * <p>{@code @InjectWireMock} on a constructor or method <em>parameter</em> has no Spring
 * TestContext equivalent and remains handled by {@link WireMockSpringJunitExtension}, which is a
 * JUnit Jupiter {@code ParameterResolver}.
 */
public class WireMockTestExecutionListener extends AbstractTestExecutionListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(WireMockTestExecutionListener.class);

  // A test class's @InjectWireMock fields never change at runtime, and this is scanned on every
  // test method (not just once per test class), so the reflection-based lookup is memoized.
  private static final Map<Class<?>, List<Field>> INJECT_WIRE_MOCK_FIELDS_CACHE =
      new ConcurrentHashMap<>();

  private static boolean isDirty = false;

  public static void markContextAsDirty() {
    isDirty = true;
  }

  @Override
  public void prepareTestInstance(final TestContext testContext) throws Exception {
    this.injectWireMockFields(testContext);
  }

  @Override
  public void beforeTestMethod(final TestContext testContext) {
    this.resetWireMockServersIfConfigured(testContext);
    this.configureWireMockForDefaultInstance(testContext);
  }

  @Override
  public void afterTestMethod(final TestContext testContext) {
    WireMock.configureFor(-1);
  }

  @Override
  @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
  public void afterTestClass(final TestContext testContext) {
    if (isDirty) {
      isDirty = false;
      testContext.markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
    }
  }

  private void injectWireMockFields(final TestContext testContext) {
    final Object testInstance = testContext.getTestInstance();
    final List<Field> annotatedFields =
        INJECT_WIRE_MOCK_FIELDS_CACHE.computeIfAbsent(
            testInstance.getClass(),
            clazz -> AnnotationSupport.findAnnotatedFields(clazz, InjectWireMock.class));

    for (final Field field : annotatedFields) {
      final InjectWireMock annotation = field.getAnnotation(InjectWireMock.class);
      final WireMockServer wiremock =
          Store.INSTANCE.findRequiredWireMockInstance(
              testContext.getApplicationContext(), annotation.value());

      field.setAccessible(true); // NOPMD
      try {
        field.set(testInstance, wiremock);
      } catch (final IllegalAccessException e) {
        throw new IllegalStateException("Failed to inject WireMockServer into field " + field, e);
      }
    }
  }

  private void resetWireMockServersIfConfigured(final TestContext testContext) {
    WireMockContextCustomizerFactory.resolveConfigureWireMocks(testContext.getTestClass()).stream()
        .filter(ConfigureWireMock::resetWireMockServer)
        .map(
            it ->
                Store.INSTANCE.findRequiredWireMockInstance(
                    testContext.getApplicationContext(), it.name()))
        .forEach(WireMockServer::resetAll);
  }

  private void configureWireMockForDefaultInstance(final TestContext testContext) {
    final Class<?> testClass = testContext.getTestClass();
    WireMockServer wiremock = null;
    String wireMockName = null;

    for (final EnableWireMock enableWireMockAnnotation :
        WireMockContextCustomizerFactory.getEnableWireMockAnnotations(testClass)) {
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
      wiremock =
          Store.INSTANCE.findRequiredWireMockInstance(
              testContext.getApplicationContext(), wireMockName);
    }

    if (wiremock == null) {
      final List<ConfigureWireMock> standaloneAnnotations =
          WireMockContextCustomizerFactory.getStandaloneConfigureWireMockAnnotations(testClass);
      if (standaloneAnnotations.size() == 1) {
        wireMockName = standaloneAnnotations.get(0).name();
        wiremock =
            Store.INSTANCE.findRequiredWireMockInstance(
                testContext.getApplicationContext(), wireMockName);
      } else if (standaloneAnnotations.size() > 1) {
        LOGGER.info("Not configuring WireMock for default instance when several candidates found");
        return;
      }
    }

    if (wiremock != null) {
      final boolean isHttps = wiremock.isHttpsEnabled();
      final int port = isHttps ? wiremock.httpsPort() : wiremock.port();

      LOGGER.info(
          "Configuring WireMock for default instance, '" + wireMockName + "' on '" + port + "'.");
      final String host = "localhost";
      if (isHttps) {
        WireMock.configureFor(WireMock.create().https().host(host).port(port).build());
      } else {
        WireMock.configureFor(WireMock.create().http().host(host).port(port).build());
      }
    }
  }
}
