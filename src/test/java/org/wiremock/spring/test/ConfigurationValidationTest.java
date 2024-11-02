package org.wiremock.spring.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.internal.WireMockContextCustomizerFactory;

class ConfigurationValidationTest {

  @EnableWireMock({@ConfigureWireMock, @ConfigureWireMock})
  private static class EnableWireMockSameDefaultName {}

  @EnableWireMock({@ConfigureWireMock(name = "w1"), @ConfigureWireMock(name = "w1")})
  private static class EnableWireMockSameGivenName {}

  @Test
  void testDuplicateNames_default() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                new WireMockContextCustomizerFactory()
                    .createContextCustomizer(EnableWireMockSameDefaultName.class, null));
    assertThat(thrown.getMessage())
        .isEqualTo("Names of mocks must be unique, found duplicates of: wiremock");
  }

  @Test
  void testDuplicateNames_given() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                new WireMockContextCustomizerFactory()
                    .createContextCustomizer(EnableWireMockSameGivenName.class, null));
    assertThat(thrown.getMessage())
        .isEqualTo("Names of mocks must be unique, found duplicates of: w1");
  }
}
