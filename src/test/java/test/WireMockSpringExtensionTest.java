package test;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest(classes = WireMockSpringExtensionTest.AppConfiguration.class)
@EnableWireMock({
  @ConfigureWireMock(
      name = "user-service",
      baseUrlProperties = "user-service.url",
      portProperties = "user-service.port"),
  @ConfigureWireMock(
      name = "todo-service",
      baseUrlProperties = "todo-service.url",
      portProperties = "todo-service.port"),
  @ConfigureWireMock(name = "noproperty-service")
})
public class WireMockSpringExtensionTest {

  @SpringBootApplication
  static class AppConfiguration {}

  @InjectWireMock("todo-service")
  private WireMockServer todoWireMockServer;

  @Autowired private Environment environment;

  @Test
  void createsWiremockWithClassLevelConfigureWiremock(
      @InjectWireMock("user-service") final WireMockServer wireMockServer) {
    this.assertWireMockServer(wireMockServer, "user-service.url", "user-service.port");
  }

  @Test
  void createsWiremockWithFieldLevelConfigureWiremock() {
    this.assertWireMockServer(this.todoWireMockServer, "todo-service.url", "todo-service.port");
  }

  @Test
  void doesNotSetPropertyWhenNotProvided(
      @InjectWireMock("noproperty-service") final WireMockServer wireMockServer) {
    assertThat(wireMockServer).as("creates WireMock instance").isNotNull();
  }

  private void assertWireMockServer(
      final WireMockServer wireMockServer, final String property, final String portProperty) {
    assertThat(wireMockServer).as("creates WireMock instance").isNotNull();
    assertThat(wireMockServer.baseUrl()).as("WireMock baseUrl is set").isNotNull();
    assertThat(wireMockServer.port()).as("sets random port").isNotZero();
    final String portPropertyValue = this.environment.getProperty(portProperty);
    assertThat(portPropertyValue).isNotNull();
    assertThat(Integer.valueOf(portPropertyValue))
        .as("sets Spring port property")
        .isEqualTo(wireMockServer.port());
    assertThat(this.environment.getProperty(property))
        .as("sets Spring property")
        .isEqualTo(wireMockServer.baseUrl());
  }
}
