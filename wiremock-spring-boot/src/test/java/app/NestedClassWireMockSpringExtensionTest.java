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
      baseUrlProperties = "user-service.url",
      portProperties = "user-service.port"),
  @ConfigureWireMock(
      name = "todo-service",
      baseUrlProperties = "todo-service.url",
      portProperties = "todo-service.port"),
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
        @InjectWireMock("user-service") final WireMockServer wireMockServer) {
      this.assertWireMockServer(wireMockServer, "user-service.url", "user-service.port");
    }

    @Test
    void injectsWiremockServerToNestedClassField() {
      this.assertWireMockServer(
          this.nestedClassTodoService, "todo-service.url", "todo-service.port");
    }

    @Test
    void injectsWiremockServerToTopLevelClassField() {
      this.assertWireMockServer(
          NestedClassWireMockSpringExtensionTest.this.topLevelClassTodoService,
          "todo-service.url",
          "todo-service.port");
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
      assertThat(
              Integer.valueOf(
                  NestedClassWireMockSpringExtensionTest.this.environment.getProperty(
                      portProperty)))
          .as("sets Spring port property")
          .isEqualTo(wireMockServer.port());
      assertThat(NestedClassWireMockSpringExtensionTest.this.environment.getProperty(property))
          .as("sets Spring property")
          .isEqualTo(wireMockServer.baseUrl());
    }
  }
}
