package app;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.wiremock.wiremock.spring.ConfigureWireMock;
import org.wiremock.wiremock.spring.EnableWireMock;
import org.wiremock.wiremock.spring.WireMock;

@SpringBootTest(classes = LegacyWireMockSpringExtensionTest.AppConfiguration.class)
@EnableWireMock({
  @ConfigureWireMock(
      name = "user-service",
      property = "user-service.url",
      portProperty = "user-service.port"),
  @ConfigureWireMock(
      name = "todo-service",
      property = "todo-service.url",
      portProperty = "todo-service.port"),
  @ConfigureWireMock(name = "noproperty-service")
})
public class LegacyWireMockSpringExtensionTest {

  @SpringBootApplication
  static class AppConfiguration {}

  @WireMock("todo-service")
  private WireMockServer todoWireMockServer;

  @Autowired private Environment environment;

  @Test
  void createsWiremockWithClassLevelConfigureWiremock(
      @WireMock("user-service") WireMockServer wireMockServer) {
    assertWireMockServer(wireMockServer, "user-service.url", "user-service.port");
  }

  @Test
  void createsWiremockWithFieldLevelConfigureWiremock() {
    assertWireMockServer(todoWireMockServer, "todo-service.url", "todo-service.port");
  }

  @Test
  void doesNotSetPropertyWhenNotProvided(
      @WireMock("noproperty-service") WireMockServer wireMockServer) {
    assertThat(wireMockServer).as("creates WireMock instance").isNotNull();
  }

  private void assertWireMockServer(
      WireMockServer wireMockServer, String property, String portProperty) {
    assertThat(wireMockServer).as("creates WireMock instance").isNotNull();
    assertThat(wireMockServer.baseUrl()).as("WireMock baseUrl is set").isNotNull();
    assertThat(wireMockServer.port()).as("sets random port").isNotZero();
    assertThat(Integer.valueOf(environment.getProperty(portProperty)))
        .as("sets Spring port property")
        .isEqualTo(wireMockServer.port());
    assertThat(environment.getProperty(property))
        .as("sets Spring property")
        .isEqualTo(wireMockServer.baseUrl());
  }
}
