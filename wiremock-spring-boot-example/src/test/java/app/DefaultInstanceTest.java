package app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock
class DefaultInstanceTest {

  @Value("${wiremock.server.baseUrl}")
  private String wiremockUrl;

  @Test
  void returnsTodos() {
    WireMock.stubFor(get("/ping").willReturn(aResponse().withStatus(200)));

    RestAssured.when().get(this.wiremockUrl + "/ping").then().statusCode(200);
  }
}
