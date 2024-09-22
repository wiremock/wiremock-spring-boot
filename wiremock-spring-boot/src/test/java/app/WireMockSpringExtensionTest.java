package app;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.wiremock.wiremock.spring.ConfigureWireMock;
import org.wiremock.wiremock.spring.EnableWireMock;
import org.wiremock.wiremock.spring.InjectWireMock;

@SpringBootTest(classes = WireMockSpringExtensionTest.AppConfiguration.class)
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
public class WireMockSpringExtensionTest {

  @SpringBootTest(classes = WireMockSpringExtensionTest.AppConfiguration.class)
  @EnableWireMock({
    @ConfigureWireMock(name = "user-service", property = "user-service.url"),
    @ConfigureWireMock(name = "todo-service", property = "todo-service.url"),
    @ConfigureWireMock(name = "noproperty-service")
  })
  @Nested
  class SinglePropertyBindingTest {

    @InjectWireMock("todo-service")
    private WireMockServer todoWireMockServer;

    @Autowired private Environment environment;

    @Test
    void createsWiremockWithClassLevelConfigureWiremock(
        @InjectWireMock("user-service") WireMockServer wireMockServer) {
      assertWireMockServer(wireMockServer, "user-service.url");
    }

    @Test
    void createsWiremockWithFieldLevelConfigureWiremock() {
      assertWireMockServer(todoWireMockServer, "todo-service.url");
    }

    @Test
    void doesNotSetPropertyWhenNotProvided(
        @InjectWireMock("noproperty-service") WireMockServer wireMockServer) {
      assertThat(wireMockServer).as("inject wiremock sets null when not configured").isNotNull();
    }

    private void assertWireMockServer(WireMockServer wireMockServer, String property) {
      assertThat(wireMockServer).as("creates WireMock instance").isNotNull();
      assertThat(wireMockServer.baseUrl()).as("WireMock baseUrl is set").isNotNull();
      assertThat(wireMockServer.port()).as("sets random port").isNotZero();
      assertThat(environment.getProperty(property))
          .as("sets Spring property")
          .isEqualTo(wireMockServer.baseUrl());
    }
  }

  @SpringBootTest(classes = WireMockSpringExtensionTest.AppConfiguration.class)
  @EnableWireMock({
    @ConfigureWireMock(
        name = "user-service",
        properties = {"user-service.url", "todo-service.url"}),
    @ConfigureWireMock(
        name = "mojo-service",
        property = "mojo-service.url",
        properties = {"other-service.url"})
  })
  @Nested
  class MultiplePropertiesBindingTest {

    @InjectWireMock("user-service")
    private WireMockServer userServiceWireMockServer;

    @InjectWireMock("mojo-service")
    private WireMockServer mojoServiceWireMockServer;

    @Autowired private Environment environment;

    @Test
    void bindsUrlToMultipleProperties() {
      assertThat(environment.getProperty("user-service.url"))
          .isEqualTo(userServiceWireMockServer.baseUrl());
      assertThat(environment.getProperty("todo-service.url"))
          .isEqualTo(todoWireMockServer.baseUrl());
      // single property binding takes precedence over multiple properties binding
      assertThat(environment.getProperty("mojo-service.url"))
          .isEqualTo(mojoServiceWireMockServer.baseUrl());
      assertThat(environment.getProperty("other-service.url")).isNull();
    }
  }

  @SpringBootApplication
  static class AppConfiguration {}

  @InjectWireMock("todo-service")
  private WireMockServer todoWireMockServer;

  @Autowired private Environment environment;

  @Test
  void createsWiremockWithClassLevelConfigureWiremock(
      @InjectWireMock("user-service") WireMockServer wireMockServer) {
    assertWireMockServer(wireMockServer, "user-service.url", "user-service.port");
  }

  @Test
  void createsWiremockWithFieldLevelConfigureWiremock() {
    assertWireMockServer(todoWireMockServer, "todo-service.url", "todo-service.port");
  }

  @Test
  void doesNotSetPropertyWhenNotProvided(
      @InjectWireMock("noproperty-service") WireMockServer wireMockServer) {
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
