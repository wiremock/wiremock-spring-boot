package nodirtycontext;

import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

/**
 * Configuration used by {@link StaticPortNoDirtyContextTest} to drive two independent {@link
 * org.springframework.test.context.TestContextManager} instances directly. It declares no
 * {@code @Test} methods, so JUnit Jupiter never discovers or runs it as a test class on its own.
 */
@SpringBootTest(classes = NoDirtyContextApp.class)
@EnableWireMock({
  @ConfigureWireMock(
      port = NoDirtyContextTestConfig.STATIC_HTTP_PORT,
      staticPortDirtySpringContext = false)
})
class NoDirtyContextTestConfig {
  static final int STATIC_HTTP_PORT = 8144;
}
