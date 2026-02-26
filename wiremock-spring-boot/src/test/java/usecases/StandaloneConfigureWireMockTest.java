package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@ConfigureWireMock(name = "user-service", baseUrlProperties = "user-service.url")
class StandaloneConfigureWireMockTest {

  @InjectWireMock("user-service")
  private WireMockServer userService;

  @Value("${user-service.url}")
  private String userServiceUrl;

  @Test
  void wireMockIsStarted() {
    assertThat(userService).isNotNull();
    assertThat(userServiceUrl).isNotBlank();

    WireMock.stubFor(get("/test").willReturn(aResponse().withStatus(200)));

    RestAssured.when().get(userServiceUrl + "/test").then().statusCode(200);
  }
}
