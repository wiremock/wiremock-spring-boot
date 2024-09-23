package app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock
class DefaultPropertiesTest {

  @Value("${wiremock.server.baseUrl}")
  private String wiremockUrl;

  @Value("${wiremock.server.port}")
  private String wiremockPort;

  @BeforeEach
  public void before() {
    WireMock.stubFor(get("/the_default_prop_mock").willReturn(aResponse().withStatus(202)));
  }

  @Test
  void test() {
    RestAssured.baseURI = this.wiremockUrl;
    RestAssured.when().get("/the_default_prop_mock").then().statusCode(202);
  }
}
