package test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextCustomizer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.internal.WireMockContextCustomizerFactory;

class ConfigurationValidationTest {

  @EnableWireMock({@ConfigureWireMock, @ConfigureWireMock})
  private static class EnableWireMockSameDefaultName {}

  @EnableWireMock({@ConfigureWireMock(name = "w1"), @ConfigureWireMock(name = "w1")})
  private static class EnableWireMockSameGivenName {}

  @EnableWireMock({@ConfigureWireMock(port = -1, httpsPort = -1)})
  private static class ConfigureWireMockWithDisabledHttpAndDisabledHttps {}

  @EnableWireMock({@ConfigureWireMock(port = 1, httpsPort = 1)})
  private static class ConfigureWireMockWithSamePortForHttpAndHttps {}

  @EnableWireMock({
    @ConfigureWireMock(name = "w1", port = 1),
    @ConfigureWireMock(name = "w2", port = 1)
  })
  private static class ConfigureWireMockWithSameHttpPortOnTwoMocks {}

  @EnableWireMock({
    @ConfigureWireMock(name = "w1", httpsPort = 1),
    @ConfigureWireMock(name = "w2", httpsPort = 1)
  })
  private static class ConfigureWireMockWithSameHttpsPortOnTwoMocks {}

  @Test
  void testDuplicateNames_default() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class, () -> this.tryConfig(EnableWireMockSameDefaultName.class));
    assertThat(thrown.getMessage())
        .isEqualTo("Names of mocks must be unique, found duplicates of: wiremock");
  }

  @Test
  void testDuplicateNames_given() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class, () -> this.tryConfig(EnableWireMockSameGivenName.class));
    assertThat(thrown.getMessage())
        .isEqualTo("Names of mocks must be unique, found duplicates of: w1");
  }

  @Test
  void testDisabledHttpAndDisabledHttps() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> this.tryConfig(ConfigureWireMockWithDisabledHttpAndDisabledHttps.class));
    assertThat(thrown.getMessage())
        .isEqualTo(
            "ConfigureWireMock wiremock has both HTTP and HTTPS disabled. It is an invalid configuration.");
  }

  @Test
  void testSamePortForHttpAndHttps() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> this.tryConfig(ConfigureWireMockWithSamePortForHttpAndHttps.class));
    assertThat(thrown.getMessage())
        .isEqualTo("ConfigureWireMock wiremock uses same port 1 for HTTP and HTTPS.");
  }

  @Test
  void testSamePortForHttp() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> this.tryConfig(ConfigureWireMockWithSameHttpPortOnTwoMocks.class));
    assertThat(thrown.getMessage())
        .isEqualTo("Some statically configured ports are being used mor than once: 1");
  }

  @Test
  void testSamePortForHttps() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> this.tryConfig(ConfigureWireMockWithSameHttpsPortOnTwoMocks.class));
    assertThat(thrown.getMessage())
        .isEqualTo("Some statically configured ports are being used mor than once: 1");
  }

  private ContextCustomizer tryConfig(final Class<?> clazz) {
    return new WireMockContextCustomizerFactory().createContextCustomizer(clazz, null);
  }
}
