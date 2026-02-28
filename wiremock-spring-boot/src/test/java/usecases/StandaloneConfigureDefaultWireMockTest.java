package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@ConfigureWireMock
class StandaloneConfigureDefaultWireMockTest {

  @InjectWireMock private WireMockServer service;

  @Value("${wiremock.server.baseUrl}")
  private String serviceUrl;

  @Test
  void wireMockIsStarted() {
    assertThat(service).isNotNull();
    assertThat(serviceUrl).isNotBlank();

    service.stubFor(get("/test").willReturn(aResponse().withStatus(200)));

    RestAssured.when().get(serviceUrl + "/test").then().statusCode(200);
  }
}
