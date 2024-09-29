package app;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest(classes = WireMockSpringExtensionTest.AppConfiguration.class)
@EnableWireMock({
  @ConfigureWireMock(
      name = "user-service",
      baseUrlProperties = {"user-service.url", "todo-service.url"},
      portProperties = {"user-service.port", "user-service.port"}),
  @ConfigureWireMock(
      name = "todo-service",
      baseUrlProperties = "todo-service.url",
      portProperties = "todo-service.port"),
  @ConfigureWireMock(
      name = "mojo-service",
      baseUrlProperties = {"mojo-service.url"})
})
public class MultiplePropertiesBindingTest {

  @InjectWireMock("user-service")
  private WireMockServer userServiceWireMockServer;

  @InjectWireMock("mojo-service")
  private WireMockServer mojoServiceWireMockServer;

  @InjectWireMock("todo-service")
  private WireMockServer todoWireMockServer;

  @Autowired private Environment environment;

  @Test
  void bindsUrlToMultipleProperties() {
    assertThat(this.environment.getProperty("user-service.url"))
        .isEqualTo(this.userServiceWireMockServer.baseUrl());
    assertThat(Integer.parseInt(this.environment.getProperty("user-service.port")))
        .isEqualTo(this.userServiceWireMockServer.port());

    assertThat(this.environment.getProperty("todo-service.url"))
        .isEqualTo(this.todoWireMockServer.baseUrl());
    assertThat(Integer.parseInt(this.environment.getProperty("todo-service.port")))
        .isEqualTo(this.todoWireMockServer.port());
  }
}
