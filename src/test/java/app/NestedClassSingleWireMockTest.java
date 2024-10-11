package app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
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

@SpringBootTest(classes = NestedClassSingleWireMockTest.AppConfiguration.class)
@EnableWireMock({
  @ConfigureWireMock(
      name = "todo-service",
      baseUrlProperties = "todo-service.url",
      portProperties = "todo-service.port")
})
public class NestedClassSingleWireMockTest {

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
    void injectsWiremockServerToNestedClassField() {
      this.assertWireMockServer(
          this.nestedClassTodoService, "todo-service.url", "todo-service.port");
    }

    @Test
    void injectsWiremockServerToTopLevelClassField() {
      this.assertWireMockServer(
          NestedClassSingleWireMockTest.this.topLevelClassTodoService,
          "todo-service.url",
          "todo-service.port");
    }

    private void assertWireMockServer(
        final WireMockServer wireMockServer, final String property, final String portProperty) {
      assertThat(wireMockServer).as("creates WireMock instance").isNotNull();
      assertThat(wireMockServer.baseUrl()).as("WireMock baseUrl is set").isNotNull();
      assertThat(wireMockServer.port()).as("sets random port").isNotZero();
      assertThat(
              Integer.valueOf(
                  NestedClassSingleWireMockTest.this.environment.getProperty(portProperty)))
          .as("sets Spring port property")
          .isEqualTo(wireMockServer.port());
      assertThat(NestedClassSingleWireMockTest.this.environment.getProperty(property))
          .as("sets Spring property")
          .isEqualTo(wireMockServer.baseUrl());

      // Test that WireMock is configured for the correct WireMock instance
      // Suffixed with port to make it differ for different test runs and servers
      final String mockedPath = "/the_default_prop_mock-" + wireMockServer.port();
      WireMock.stubFor(get(mockedPath).willReturn(aResponse().withStatus(202)));
      RestAssured.baseURI = wireMockServer.baseUrl();
      RestAssured.when().get(mockedPath).then().statusCode(202);
    }
  }
}
