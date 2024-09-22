package app;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest(classes = NestedClassWireMockSpringExtensionTest.AppConfiguration.class)
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
public class NestedClassWireMockSpringExtensionTest {

  @SpringBootApplication
  static class AppConfiguration {}

  @Autowired private Environment environment;

  @InjectWireMock("todo-service")
  private WireMockServer topLevelClassTodoService;

  @Nested
  @DisplayName("Test Something")
  class NestedTest {

    @InjectWireMock("todo-service")
    private WireMockServer nestedClassTodoService;

    @Test
    void injectsWiremockServerToMethodParameter(
        @InjectWireMock("user-service") WireMockServer wireMockServer) {
      assertWireMockServer(wireMockServer, "user-service.url", "user-service.port");
    }

    @Test
    void injectsWiremockServerToNestedClassField() {
      assertWireMockServer(nestedClassTodoService, "todo-service.url", "todo-service.port");
    }

    @Test
    void injectsWiremockServerToTopLevelClassField() {
      assertWireMockServer(topLevelClassTodoService, "todo-service.url", "todo-service.port");
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
}
