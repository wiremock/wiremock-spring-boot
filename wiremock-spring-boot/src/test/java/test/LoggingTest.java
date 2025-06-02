package test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.util.TestSocketUtils;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import org.wiremock.spring.WireMockConfigurationCustomizer;

@SpringBootTest(classes = LoggingTest.AppConfiguration.class)
@EnableWireMock({
  @ConfigureWireMock(
      name = "user-service",
      baseUrlProperties = "user-service.url",
      configurationCustomizers = LoggingTest.SampleConfigurationCustomizer.class),
  @ConfigureWireMock(
      name = "todo-service",
      baseUrlProperties = "todo-service.url",
      configurationCustomizers = LoggingTest.SampleConfigurationCustomizer.class),
})
@ExtendWith(OutputCaptureExtension.class)
class LoggingTest {
  private static final int USER_SERVICE_PORT = TestSocketUtils.findAvailableTcpPort();
  private static final int TODO_SERVICE_PORT = TestSocketUtils.findAvailableTcpPort();

  static class SampleConfigurationCustomizer implements WireMockConfigurationCustomizer {

    @Override
    public void customize(
        final WireMockConfiguration configuration, final ConfigureWireMock options) {
      if (options.name().equals("user-service")) {
        configuration.port(USER_SERVICE_PORT);
      } else {
        configuration.port(TODO_SERVICE_PORT);
      }
    }
  }

  @SpringBootApplication
  static class AppConfiguration {}

  @InjectWireMock("user-service")
  private WireMockServer userService;

  @InjectWireMock("todo-service")
  private WireMockServer todoService;

  @Test
  void appliesConfigurationCustomizer() {
    assertThat(this.userService.port()).isEqualTo(USER_SERVICE_PORT);
    assertThat(this.todoService.port()).isEqualTo(TODO_SERVICE_PORT);
  }

  @Test
  void outputsWireMockLogs(final CapturedOutput capturedOutput)
      throws IOException, InterruptedException {
    this.userService.stubFor(
        get(urlEqualTo("/test"))
            .willReturn(
                aResponse().withHeader("Content-Type", "text/plain").withBody("Hello World!")));

    final HttpClient httpClient = HttpClient.newHttpClient();
    final HttpResponse<String> response =
        httpClient.send(
            HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:" + this.userService.port() + "/test"))
                .build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(response.body()).isEqualTo("Hello World!");
    assertThat(capturedOutput.getAll())
        .as("Must contain correct logger name")
        .contains("WireMock.todo-service", "WireMock.user-service");
    assertThat(capturedOutput.getAll())
        .as("Must contain debug logging for WireMock")
        .contains("Matched response definition:");
  }
}
